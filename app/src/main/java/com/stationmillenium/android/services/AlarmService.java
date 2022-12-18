/**
 *
 */
package com.stationmillenium.android.services;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;

import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.preferences.AlarmSharedPreferencesActivity.AlarmSharedPreferencesConstants;
import com.stationmillenium.android.libutils.AppUtils;
import com.stationmillenium.android.libutils.intents.LocalIntents;
import com.stationmillenium.android.libutils.intents.LocalIntentsData;
import com.stationmillenium.android.libutils.toasts.DisplayToastsUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

/**
 * Service to manage alarm intent : launch player when alarm elapsed and set pending intents
 *
 * @author vincent
 */
public class AlarmService extends JobIntentService {

    /**
     * Unique job ID for this service.
     */
    private static final int JOB_ID = 1000;
    private static int NOTIF_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "channelId";

    private DisplayToastsUtil displayToast;

    @Override
    public void onCreate() {
        super.onCreate();
        displayToast = new DisplayToastsUtil(getApplicationContext());
    }

    @TargetApi(Build.VERSION_CODES.O)
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (AppUtils.isAPILevel26Available() && intent != null && LocalIntents.ON_ALARM_TIME_ELAPSED.toString().equals(intent.getAction())) {
            startForeground(NOTIF_ID, buildNotification());
            requestPlayerStart();
            enqueueWork(this, intent);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification buildNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        if (notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }

        return new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notif_alarm)
                .setContentTitle(getString(R.string.alarm_notification_title))
                .setContentText(getString(R.string.alarm_notification_content))
                .build();
    }

    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, AlarmService.class, JOB_ID, work);
    }

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this); //load preferences first
        if (LocalIntents.SET_ALARM_TIME.toString().equals(intent.getAction())) {
            programAlarm(intent, preferences);
        } else if (AppUtils.isAPILevel26Available()) {
            scheduleNextRepeatAlarm(preferences);
            if (AppUtils.isAPILevel26Available()) {
                stopForeground(true);
            }
        } else {
            Timber.d("Received intent to launch player");
            requestPlayerStart();
            scheduleNextRepeatAlarm(preferences);
        }
    }

    private void scheduleNextRepeatAlarm(SharedPreferences preferences) {
        //next, properly reset alarm preference
        if (getRepeatDays(preferences).length == 0) {
            Timber.d("No repeat days set - disable alarm");

            //no repeat days set, so disable alarm
            preferences.edit()
                    .putBoolean(AlarmSharedPreferencesConstants.ALARM_ENABLED, false)
                    .apply();

        } else {
            Timber.d("Repeat days set - restart service to set alarm");

            //start the service
            Intent alarmIntent = new Intent(this, AlarmService.class);
            alarmIntent.setAction(LocalIntents.SET_ALARM_TIME.toString());
            AlarmService.enqueueWork(this, alarmIntent);
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void requestPlayerStart() {
        //launch player
        if (!AppUtils.isMediaPlayerServiceRunning(this)) { //do not launch media player twice
            if (!AppUtils.isWifiOnlyAndWifiNotConnected(this)) { //if wifi requested, check it is enabled and connected
                //start player service
                Timber.d("Start media player service for alarm triggering");

                Intent playIntent = new Intent(this, MediaPlayerService.class);
                playIntent.putExtra(LocalIntentsData.RESUME_PLAYER_ACTIVITY.toString(), false);
                playIntent.putExtra(LocalIntentsData.GET_VOLUME_FROM_PREFERENCES.toString(), true);
                if (AppUtils.isAPILevel26Available()) {
                    startForegroundService(playIntent);
                } else {
                    startService(playIntent);
                }
                displayToast.displayToast(getString(R.string.alarm_triggered));

            } else {
                Timber.w("Wifi requested but not connected for alarm");
                displayToast.displayToast(getString(R.string.player_no_wifi));
            }

        } else {
            displayToast.displayToast(getString(R.string.alarm_already_triggered));
            Timber.d("Alarm not triggered, media player already running");
        }
    }

    private void programAlarm(@NonNull Intent intent, SharedPreferences preferences) {
        Timber.d("Set alarm time intent received : %s", intent);

        if (preferences.getBoolean(AlarmSharedPreferencesConstants.ALARM_ENABLED, false)) { //check if alarm is enabled
            //get repeat days
            int[] repeatDays = getRepeatDays(preferences);
            Timber.d("Alarm repeat days : %s", repeatDays);

            Calendar alarmTime = getAlarmTimeCalendar(preferences); //get the alarm time set in a calendar
            if (alarmTime != null) { //if alarm time is set
                if (repeatDays.length == 0) { //no repeat days case
                    Timber.d("No repeat days for alarm");

                    //if alarm time elapsed : set it for tomorrow
                    if (Calendar.getInstance().after(alarmTime)) {
                        Timber.d("Alarm time elapsed - set if for next day");
                        alarmTime.add(Calendar.DATE, 1);
                    }

                    //program alarm
                    programAlarm(alarmTime);

                } else { //process alarm with repeat days
                    Timber.d("Alarm with repeat days");

                    int dayIndex = convertCalendarDayIndexToArrayIndex(alarmTime); //convert the day index
                    int nextRepeatDay = -1;
                    for (int day : repeatDays) { //check each day
                        if ((day > dayIndex)
                                || ((day == dayIndex) && (Calendar.getInstance().before(alarmTime)))) { //if we found the next day for repeat
                            nextRepeatDay = day; //save it and exit
                            break;
                        }
                    }

                    //the next repeat day has not been found, so use the first in next week
                    if (nextRepeatDay == -1) {
                        nextRepeatDay = repeatDays[0];
                    }

                    //deal with the case of the repeat day is the same day
                    if ((nextRepeatDay == dayIndex) && (Calendar.getInstance().after(alarmTime))) {
                        Timber.d("Repeat day is the same day as today - repeat in one week");
                        alarmTime.add(Calendar.DATE, 7);

                    } else if (nextRepeatDay != dayIndex) { //as repeat date is not today, we need to add some day delay
                        Timber.d("Repeat day is not today : add some delay");
                        alarmTime.setFirstDayOfWeek(Calendar.SUNDAY); //set start day of week as sunday
                        nextRepeatDay += 2; //shift 2 days forward
                        if (nextRepeatDay > 7) { //at the end of the week, return to the beginning
                            nextRepeatDay -= 7;
                        }
                        if (nextRepeatDay < Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) { //repeat day is next week : add delay of 7 days
                            alarmTime.add(Calendar.DATE, 7);
                        } else if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) { //repeat day is nearly next week, only 1 day delay is needed
                            alarmTime.add(Calendar.DATE, 1);
                        }

                        alarmTime.set(Calendar.DAY_OF_WEEK, nextRepeatDay); //set the next repeat day
                    }

                    //program the alarm
                    programAlarm(alarmTime);
                }

            } else { //alarm time not set
                Timber.d("Alarm time not set - can't program alarm");

                displayToast.displayToast(getString(R.string.alarm_no_time_set));
                preferences.edit() //disable the alarm
                        .putBoolean(AlarmSharedPreferencesConstants.ALARM_ENABLED, false)
                        .apply();
            }

        } else { //alarm disabled, cancel intent
            Timber.d("Alarm disabled - cancel intent");
            cancelAlarm();
        }
    }

    /**
     * Cancel the alarm
     */
    @TargetApi(Build.VERSION_CODES.O)
    private void cancelAlarm() {
        PendingIntent playPendingIntent = createLaunchPlayerPendingIntent();
        getAlarmManager().cancel(playPendingIntent);
    }

    /**
     * Create a launch player {@link PendingIntent}
     */
    @SuppressLint("NewApi")
    private PendingIntent createLaunchPlayerPendingIntent() {
        Intent launchPlayerIntent = new Intent(this, AlarmService.class);
        launchPlayerIntent.setAction(LocalIntents.ON_ALARM_TIME_ELAPSED.toString());
        return AppUtils.isAPILevel26Available()
                ? PendingIntent.getForegroundService(this, 0, launchPlayerIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE)
                : PendingIntent.getService(this, 0, launchPlayerIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    /**
     * Get the {@link AlarmManager}
     *
     * @return the {@link AlarmManager}
     */
    private AlarmManager getAlarmManager() {
        return (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    }

    /**
     * Get the alarm repeat days
     *
     * @param preferences the {@link SharedPreferences}
     * @return the repeat days into a int array
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private int[] getRepeatDays(SharedPreferences preferences) {
        Set<String> repeatDaysSet = preferences.getStringSet(AlarmSharedPreferencesConstants.ALARM_DAYS, null);

        //process set to convert to array
        if ((repeatDaysSet != null) && (!repeatDaysSet.isEmpty())) {
            int[] finalArray = new int[repeatDaysSet.size()];
            List<String> selectedDaysList = new ArrayList<>(repeatDaysSet);
            Collections.sort(selectedDaysList);
            for (int i = 0; i < selectedDaysList.size(); i++)
                finalArray[i] = Integer.parseInt(selectedDaysList.get(i));

            return finalArray;

        } else { //if no data, return empty array
            return new int[0];
        }
    }

    /**
     * Get the {@link Calendar} with alarm time set
     *
     * @param preferences the {@link SharedPreferences}
     * @return the {@link Calendar}
     */
    private Calendar getAlarmTimeCalendar(SharedPreferences preferences) {
        long timePickerTime = preferences.getLong(AlarmSharedPreferencesConstants.ALARM_TIME, 0); //get the time in millis from timepicker
        if (timePickerTime != 0) {
            Timber.d("Alarm time is set - use it");

            Calendar setTimeCalendar = Calendar.getInstance(); //set in into calendar
            setTimeCalendar.setTimeInMillis(timePickerTime);

            Calendar currentTime = Calendar.getInstance(); //set a current day calendar with hour and minute alarm time
            currentTime.set(Calendar.HOUR_OF_DAY, setTimeCalendar.get(Calendar.HOUR_OF_DAY));
            currentTime.set(Calendar.MINUTE, setTimeCalendar.get(Calendar.MINUTE));
            currentTime.set(Calendar.SECOND, 0);
            currentTime.set(Calendar.MILLISECOND, 0);
            return currentTime;

        } else { //alarm time not set, so return null
            Timber.d("Alarm time is not set - use current time with delta");
            return null;
        }
    }

    /**
     * Program the alarm
     *
     * @param programTime the {@link Calendar} for time to program alarm
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void programAlarm(Calendar programTime) {
        Timber.d("Program alarm");

        //program alarm
        PendingIntent playPendingIntent = createLaunchPlayerPendingIntent();
        if (AppUtils.isAPILevel21Available()) {
            getAlarmManager().setAlarmClock(new AlarmManager.AlarmClockInfo(programTime.getTimeInMillis(), playPendingIntent), playPendingIntent);
        } else if (AppUtils.isAPILevel19Available()) {
            getAlarmManager().setExact(AlarmManager.RTC_WAKEUP, programTime.getTimeInMillis(), playPendingIntent);
        } else {
            getAlarmManager().set(AlarmManager.RTC_WAKEUP, programTime.getTimeInMillis(), playPendingIntent);
        }

        //display toast
        String toastText;
        if (programTime.get(Calendar.DATE) == Calendar.getInstance().get(Calendar.DATE)) { //display only hour and minute
            toastText = getString(R.string.alarm_set_same_day,
                    addZeroIfNeeded(programTime.get(Calendar.HOUR_OF_DAY)),
                    addZeroIfNeeded(programTime.get(Calendar.MINUTE)));
        } else { //display hour, minutes and day
            String[] dayNamesArray = getResources().getStringArray(R.array.preferences_alarm_days);
            int dayIndex = convertCalendarDayIndexToArrayIndex(programTime); //convert the day index
            toastText = getString(R.string.alarm_set,
                    dayNamesArray[dayIndex],
                    addZeroIfNeeded(programTime.get(Calendar.HOUR_OF_DAY)),
                    addZeroIfNeeded(programTime.get(Calendar.MINUTE)));
        }

        //show toast
        displayToast.displayToast(toastText);
    }

    /**
     * Add a "0" before value if < 10
     *
     * @param value the value
     * @return the converted {@link String}
     */
    private String addZeroIfNeeded(int value) {
        return (value < 10) ? "0" + value : "" + value;
    }

    /**
     * Convert a {@link Calendar} day index (from 1 to 7 with index 1 is sunday) to internal array index (from 0 to 6 with index 0 is monday)
     *
     * @param calendar the {@link Calendar} input
     * @return the index as int
     */
    private int convertCalendarDayIndexToArrayIndex(Calendar calendar) {
        int dayIndex = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayIndex == Calendar.SUNDAY) //shift day
            dayIndex += 7;
        dayIndex -= 2;
        return dayIndex;
    }


}
