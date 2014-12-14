package com.stationmillenium.android.utils.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import com.stationmillenium.android.R;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Time preference class to use a {@link TimePicker} inside a preference screen overriding a {@link DialogPreference}
 *
 * @author vincent
 *         http://stackoverflow.com/questions/5533078/timepicker-in-preferencescreen
 */
public class TimePreference extends DialogPreference {
    private Calendar calendar;
    private TimePicker picker = null;

    public TimePreference(Context ctxt) {
        this(ctxt, null);
    }

    public TimePreference(Context ctxt, AttributeSet attrs) {
        this(ctxt, attrs, 0);
    }

    public TimePreference(Context ctxt, AttributeSet attrs, int defStyle) {
        super(ctxt, attrs, defStyle);

        setPositiveButtonText(R.string.preferences_alarm_set);
        setNegativeButtonText(R.string.preferences_alarm_cancel);
        calendar = new GregorianCalendar();
    }

    @Override
    protected View onCreateDialogView() {
        picker = new TimePicker(getContext());
        picker.setIs24HourView(true);
        return (picker);
    }

    @Override
    protected void onBindDialogView(@NotNull View v) {
        super.onBindDialogView(v);
        picker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        picker.setCurrentMinute(calendar.get(Calendar.MINUTE));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            calendar.set(Calendar.HOUR_OF_DAY, picker.getCurrentHour());
            calendar.set(Calendar.MINUTE, picker.getCurrentMinute());

            setSummary(getSummary());
            if (callChangeListener(calendar.getTimeInMillis())) {
                persistLong(calendar.getTimeInMillis());
                notifyChanged();
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        if (restoreValue) { //if we need to restore the saved value
            if (defaultValue == null) { //if we don't have a default value
                // try to restore saved value as long and use current time for default
                calendar.setTimeInMillis(getPersistedLong(System.currentTimeMillis()));
            } else { //we have default value
                //try to restore saved value (as string) and use default value
                calendar.setTimeInMillis(Long.parseLong(getPersistedString((String) defaultValue)));
            }
        } else { //we don't restore saved value
            if (defaultValue == null) { //we don't have default value, use current time
                calendar.setTimeInMillis(System.currentTimeMillis());
            } else { //we have default value, use it
                calendar.setTimeInMillis(Long.parseLong((String) defaultValue));
            }
        }
        setSummary(getSummary());

    }

    @Override
    public CharSequence getSummary() {
        if ((getPersistedLong(0) != 0) || (!getPersistedString("").equals(""))) {
            //custom summary
            String dateText = DateFormat.getTimeFormat(getContext()).format(new Date(calendar.getTimeInMillis()));
            return getContext().getString(R.string.preferences_alarm_time_set, dateText);
        } else
            return getContext().getString(R.string.preferences_alarm_no_time);
    }
} 