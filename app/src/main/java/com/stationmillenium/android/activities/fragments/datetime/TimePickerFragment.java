package com.stationmillenium.android.activities.fragments.datetime;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.songsearchhistory.SongSearchHistoryActivity;
import com.stationmillenium.android.libutils.intents.LocalIntents;
import com.stationmillenium.android.libutils.intents.LocalIntentsData;

import java.util.Calendar;

/**
 * Implementation of {@link DialogFragment} to display a {@link TimePickerDialog}
 *
 * @author vincent
 * http://developer.android.com/guide/topics/ui/controls/pickers.html
 */
public class TimePickerFragment extends DialogFragment implements OnTimeSetListener {

    private static final String TAG = "TimePickerFragment";

    //flag due to twice calls of callback : http://stackoverflow.com/questions/12436073/datepicker-ondatechangedlistener-called-twice
    private boolean alreadySet;
    private boolean isFragmentPaused; //to not send intent on screen rotation

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
        timePickerDialog.setTitle(getString(R.string.song_search_history_menu_time_search_select_time));
        return timePickerDialog;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if ((!alreadySet) && (!isFragmentPaused)) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "Selected time : " + hourOfDay + ":" + minute);

            //send intent to transfer chosen time
            Intent timeIntent = new Intent(getActivity(), SongSearchHistoryActivity.class);
            timeIntent.setAction(LocalIntents.ON_TIME_PICKED_UP.toString());
            timeIntent.putExtra(LocalIntentsData.SONG_SEARCH_HOURS.toString(), hourOfDay);
            timeIntent.putExtra(LocalIntentsData.SONG_SEARCH_MINUTES.toString(), minute);
            startActivity(timeIntent);

            //flag we have send data
            alreadySet = true;
        } else
            Log.w(TAG, "Date already set");
    }

    @Override
    public void onPause() {
        isFragmentPaused = true;
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        isFragmentPaused = false;
    }

}
