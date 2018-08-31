/**
 *
 */
package com.stationmillenium.android.activities.preferences;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.preferences.AlarmSharedPreferencesActivity.AlarmSharedPreferencesConstants;
import com.stationmillenium.android.libutils.preferences.SeekBarDialogPreference;
import com.stationmillenium.android.libutils.preferences.TimePreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

/**
 * Fragment to manage alarm preferences
 *
 * @author vincent
 */
public class AlarmSharedPreferencesFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

    //preference fields
    private CheckBoxPreference alarmEnabled;
    private TimePreference alarmTime;
    private MultiSelectListPreference alarmDaysList;
    private SeekBarDialogPreference alarmVolumeSeekBar;

    private AlarmSharedPreferencesActivity activity;
    
    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        //init view
        super.onCreate(savedInstanceState);
        activity = (AlarmSharedPreferencesActivity) getActivity();    

        addPreferencesFromResource(R.xml.alarm_preferences);
        initializePreferenceFields(); //load fields
        initAlarmDaysList(); //init alarm days field

        //init fields
        initAlarmTime();
        initAlarmEnabled();
        initAlarmVolume();

        //register callback for preference changes
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Init the alarm enabled field
     */
    private void initAlarmEnabled() {
        alarmEnabled.setOnPreferenceChangeListener((preference, newValue) -> {
            long alarmTime = PreferenceManager.getDefaultSharedPreferences(getActivity()).getLong(AlarmSharedPreferencesConstants.ALARM_TIME, 0);
            if (alarmTime != 0) { //alarm time is set
                activity.sendUpdateAlarmTimeIntent(); //send intent to update alarm
                return true;
            } else { //alarm time is not set
                Timber.d("Alarm time not set - can't program alarm");
                Toast.makeText(getActivity(), R.string.alarm_no_time_set, Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    /**
     * Init the alarm time field
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initAlarmTime() {
        alarmTime.setOnPreferenceChangeListener((preference, newValue) -> {
            activity.sendUpdateAlarmTimeIntent(); //send intent to update alarm
            return true;
        });
    }

    /**
     * Initialize preference fields
     *
     */
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initializePreferenceFields() {
        alarmEnabled = (CheckBoxPreference) findPreference(AlarmSharedPreferencesConstants.ALARM_ENABLED);
        alarmTime = (TimePreference) findPreference(AlarmSharedPreferencesConstants.ALARM_TIME);
        alarmVolumeSeekBar = (SeekBarDialogPreference) findPreference(AlarmSharedPreferencesConstants.ALARM_VOLUME);
        alarmDaysList = (MultiSelectListPreference) findPreference(AlarmSharedPreferencesConstants.ALARM_DAYS);
    }

    /**
     * Init the alarm volume field
     */
    private void initAlarmVolume() {
        Timber.d("Init the alarm volume field");

        //set min and current value
        AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        alarmVolumeSeekBar.setMaxProgress(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        if (!PreferenceManager.getDefaultSharedPreferences(getActivity()).contains(AlarmSharedPreferencesConstants.ALARM_VOLUME)) { //add default volume value if no value
            int volumeValue = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .edit()
                    .putInt(AlarmSharedPreferencesConstants.ALARM_VOLUME, volumeValue)
                    .apply();
            alarmVolumeSeekBar.setProgress(volumeValue);
        }

        //set summary and progress text suffix
        updateAlarmVolumeSummary(alarmVolumeSeekBar.getProgress());
        alarmVolumeSeekBar.setProgressTextSuffix(" " + getString(R.string.preferences_alarm_volume_progress_text_suffix, alarmVolumeSeekBar.getMaxProgress()));

        //set the on change handler for summary update
        alarmVolumeSeekBar.setOnPreferenceChangeListener((preference, newValue) -> {
            Timber.d("Alarm volume field value change");
            updateAlarmVolumeSummary((Integer) newValue);
            return true;
        });
    }

    /**
     * Set the summary for alarm volume field
     *
     * @param value the new value for volume
     */
    private void updateAlarmVolumeSummary(int value) {
        String summaryText = getString(R.string.preferences_alarm_volume_summary, value, alarmVolumeSeekBar.getMaxProgress());
        alarmVolumeSeekBar.setSummary(summaryText);
    }

    /**
     * Initilize the alarm days list
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initAlarmDaysList() {
        Timber.d("Init the alarm days list summary");
        //get values
        Set<String> selectedDays = alarmDaysList.getValues();

        //manage init summary
        manageAlarmDaysSummary(alarmDaysList, selectedDays);

        //add update summary listener
        alarmDaysList.setOnPreferenceChangeListener((preference, newValue) -> {
            Timber.d("Update the alarm days list summary");
            manageAlarmDaysSummary(preference, (Set<String>) newValue);
            activity.sendUpdateAlarmTimeIntent(); //update alarm
            return true;
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (AlarmSharedPreferencesConstants.ALARM_ENABLED.equals(key)) { //if the changed preference is the alarm enabled
            Timber.d("Alarm enabled shared preference changed");
            boolean alarmEnabledValue = sharedPreferences.getBoolean(AlarmSharedPreferencesConstants.ALARM_ENABLED, false); //get new value
            alarmEnabled.setChecked(alarmEnabledValue); //set checked
            new BackupManager(getActivity()).dataChanged();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.d("Unregister OnSharedPreferenceChangeListener");
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Set the correct summary on alarm days {@link MultiSelectListPreference}
     *
     * @param alarmDaysList the {@link MultiSelectListPreference}
     * @param selectedDays  the {@link Set} of selected days
     */
    private void manageAlarmDaysSummary(Preference alarmDaysList, Set<String> selectedDays) {
        String[] dayNames = getResources().getStringArray(R.array.preferences_alarm_days);
        if (selectedDays != null) { //if some data available
            List<String> selectedDaysList = new ArrayList<>(selectedDays);
            Collections.sort(selectedDaysList);

            //manage summary
            if (!selectedDaysList.isEmpty()) { //if some data - set up summary
                try {
                    StringBuilder summary = new StringBuilder();
                    int i = 1;
                    for (String day : selectedDaysList) {
                        summary.append(dayNames[Integer.parseInt(day)]);
                        if (i < selectedDaysList.size()) {
                            summary.append(getString(R.string.preferences_alarm_activation_days_separator)).append(" ");
                            i++;
                        }
                    }
                    alarmDaysList.setSummary(summary.toString());
                } catch (NumberFormatException e) {
                    Timber.w(e, "Format exception");
                    alarmDaysList.setSummary("");
                }

            } else { //display no data summary
                alarmDaysList.setSummary(getString(R.string.preferences_alarm_activation_days_no_selection));
            }

        } else { //display no data summary
            alarmDaysList.setSummary(getString(R.string.preferences_alarm_activation_days_no_selection));
        }
    }

}
