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
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.preferences.AlarmSharedPreferencesActivity.AlarmSharedPreferencesConstants;
import com.stationmillenium.android.libutils.AppUtils;
import com.stationmillenium.android.libutils.preferences.ListPreferenceMultiSelect;
import com.stationmillenium.android.libutils.preferences.SeekBarDialogPreference;
import com.stationmillenium.android.libutils.preferences.TimePreference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Activity to manage alarm preferences
 *
 * @author vincent
 */
public class AlarmSharedPreferencesFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

    //static intialization part
    private static final String TAG = "AlarmPreferenceActivity";
    private static final String ALARM_DAYS_LIST_STRING_SEPARATOR = "\\|";

    //preference fields
    private CheckBoxPreference alarmEnabled;
    private TimePreference alarmTime;
    private MultiSelectListPreference alarmDaysList;
    private ListPreference alarmDaysListString;
    private SeekBarDialogPreference alarmVolumeSeekBar;

    private AlarmSharedPreferencesActivity activity;
    
    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        //init view
        super.onCreate(savedInstanceState);
        activity = (AlarmSharedPreferencesActivity) getActivity();    

        if (AppUtils.isAPILevel11Available()) {
            addPreferencesFromResource(R.xml.alarm_preferences);
            initializePreferenceFields(true); //load fields
            initAlarmDaysList(); //init alarm days field

        } else {
            addPreferencesFromResource(R.xml.alarm_preferences_api_level_10);
            initializePreferenceFields(false); //load fields
            initAlarmDaysListAsString(); //init alarm days string field
        }


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
        alarmEnabled.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                long alarmTime = PreferenceManager.getDefaultSharedPreferences(getActivity()).getLong(AlarmSharedPreferencesConstants.ALARM_TIME, 0);
                if (alarmTime != 0) { //alarm time is set
                    activity.sendUpdateAlarmTimeIntent(); //send intent to update alarm
                    return true;
                } else { //alarm time is not set
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Alarm time not set - can't program alarm");
                    }
                    Toast.makeText(getActivity(), R.string.alarm_no_time_set, Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        });
    }

    /**
     * Init the alarm time field
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initAlarmTime() {
        alarmTime.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                activity.sendUpdateAlarmTimeIntent(); //send intent to update alarm
                return true;
            }
        });
    }

    /**
     * Initialize preference fields
     *
     * @param isAPILevel11Available if the API level 11 is available
     */
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initializePreferenceFields(boolean isAPILevel11Available) {
        alarmEnabled = (CheckBoxPreference) findPreference(AlarmSharedPreferencesConstants.ALARM_ENABLED);
        alarmTime = (TimePreference) findPreference(AlarmSharedPreferencesConstants.ALARM_TIME);
        alarmVolumeSeekBar = (SeekBarDialogPreference) findPreference(AlarmSharedPreferencesConstants.ALARM_VOLUME);
        if (isAPILevel11Available) {
            alarmDaysList = (MultiSelectListPreference) findPreference(AlarmSharedPreferencesConstants.ALARM_DAYS);
        } else {
            alarmDaysListString = (ListPreferenceMultiSelect) findPreference(AlarmSharedPreferencesConstants.ALARM_DAYS_STRING);
        }
    }

    /**
     * Init the alarm volume field
     */
    private void initAlarmVolume() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Init the alarm volume field");
        }
        
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
        alarmVolumeSeekBar.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (BuildConfig.DEBUG)
                    Log.d(TAG, "Alarm volume field value change");
                updateAlarmVolumeSummary((Integer) newValue);
                return true;
            }
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
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Init the alarm days list summary");
        //get values
        Set<String> selectedDays = alarmDaysList.getValues();

        //manage init summary
        manageAlarmDaysSummary(alarmDaysList, selectedDays);

        //add update summary listener
        alarmDaysList.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @SuppressWarnings("unchecked")
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (BuildConfig.DEBUG)
                    Log.d(TAG, "Update the alarm days list summary");
                manageAlarmDaysSummary(preference, (Set<String>) newValue);
                activity.sendUpdateAlarmTimeIntent(); //update alarm
                return true;
            }
        });
    }

    /**
     * Initilize the alarm days list, in string format (for api level 10 only)
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initAlarmDaysListAsString() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Init the alarm days list summary");
        }
        //get values
        String alarmDaysString = alarmDaysListString.getValue();
        Set<String> selectedDays = convertAlarmDaysStringToSet(alarmDaysString);

        //manage init summary
        manageAlarmDaysSummary(alarmDaysListString, selectedDays);

        //add update summary listener
        alarmDaysListString.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (BuildConfig.DEBUG)
                    Log.d(TAG, "Update the alarm days list summary");
                Set<String> selectedDays = convertAlarmDaysStringToSet((String) newValue);
                manageAlarmDaysSummary(preference, selectedDays);
                activity.sendUpdateAlarmTimeIntent(); //update alarm
                return true;
            }
        });
    }

    /**
     * Convert the alarm days list from {@link String} to {@link Set}
     *
     * @param alarmDaysString the original {@link String}
     * @return the converted {@link Set}
     */
    private Set<String> convertAlarmDaysStringToSet(String alarmDaysString) {
        Set<String> selectedDays = null;
        if ((alarmDaysString != null) && (!alarmDaysString.equals(""))) {
            String[] alarmDaysArray = alarmDaysString.split(ALARM_DAYS_LIST_STRING_SEPARATOR);
            selectedDays = new HashSet<>(Arrays.asList(alarmDaysArray));
        }
        return selectedDays;
    }



    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (AlarmSharedPreferencesConstants.ALARM_ENABLED.equals(key)) { //if the changed preference is the alarm enabled
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Alarm enabled shared preference changed");
            }
            boolean alarmEnabledValue = sharedPreferences.getBoolean(AlarmSharedPreferencesConstants.ALARM_ENABLED, false); //get new value
            alarmEnabled.setChecked(alarmEnabledValue); //set checked
            new BackupManager(getActivity()).dataChanged();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Unregister OnSharedPreferenceChangeListener");
        }
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
                    String summary = "";
                    int i = 1;
                    for (String day : selectedDaysList) {
                        summary += dayNames[Integer.parseInt(day)];
                        if (i < selectedDaysList.size()) {
                            summary += getString(R.string.preferences_alarm_activation_days_separator) + " ";
                            i++;
                        }
                    }
                    alarmDaysList.setSummary(summary);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Format exception", e);
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
