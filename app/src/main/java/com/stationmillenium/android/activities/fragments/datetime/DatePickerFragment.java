package com.stationmillenium.android.activities.fragments.datetime;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.DatePicker;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.songsearchhistory.SongSearchHistoryActivity;
import com.stationmillenium.android.libutils.intents.LocalIntents;
import com.stationmillenium.android.libutils.intents.LocalIntentsData;

import java.util.Calendar;

/**
 * Implementation of {@link DialogFragment} to display a {@link DatePickerDialog}
 *
 * @author vincent
 * http://developer.android.com/guide/topics/ui/controls/pickers.html
 */
public class DatePickerFragment extends DialogFragment implements OnDateSetListener {

    private static final String TAG = "DatePickerFragment";

    //flag due to twice calls of callback : http://stackoverflow.com/questions/12436073/datepicker-ondatechangedlistener-called-twice
    private boolean alreadySet;
    private boolean isFragmentPaused; //to not send intent on screen rotation

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
        datePickerDialog.setTitle(getString(R.string.song_search_history_menu_time_search_select_date));
        return datePickerDialog;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        if ((!alreadySet) && (!isFragmentPaused)) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "Selected date : " + year + "-" + monthOfYear + "-" + dayOfMonth);

            //send intent to transfer chosen date
            Intent dateIntent = new Intent(getActivity(), SongSearchHistoryActivity.class);
            dateIntent.setAction(LocalIntents.ON_DATE_PICKED_UP.toString());
            dateIntent.putExtra(LocalIntentsData.SONG_SEARCH_YEAR.toString(), year);
            dateIntent.putExtra(LocalIntentsData.SONG_SEARCH_MONTH.toString(), monthOfYear);
            dateIntent.putExtra(LocalIntentsData.SONG_SEARCH_DATE.toString(), dayOfMonth);
            startActivity(dateIntent);

            //flag we have send data
            alreadySet = true;
        } else
            Log.w(TAG, "Date already set or fragment paused");
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
