package com.stationmillenium.android.replay.utils;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.NonNull;
import android.widget.ImageView;
import android.widget.TextView;

import com.stationmillenium.android.replay.R;

import java.text.DateFormat;
import java.util.Date;

/**
 * Utils to manage replay duration
 * Created by vincent on 06/11/16.
 */
public class ReplayBindingUtil {

    private static final int DRAWABLE_INDEX = 1;

    /**
     * Compute the replay duration
     * @param context to get strings
     * @param duration the duration in ms
     * @return the formatted text
     */
    @NonNull
    private static String computeReplayDuration(@NonNull Context context, int duration) {
        int durationSeconds = duration / 1000; //original duration is in ms
        int durationMinutes = durationSeconds / 60;
        durationSeconds = durationSeconds % 60;
        if (durationMinutes >= 60) {
            int durationHours = durationMinutes / 60;
            durationMinutes = durationMinutes % 60;
            return context.getString(R.string.replay_duration_hours, String.valueOf(durationHours),
                    (durationMinutes < 10 ? "0" + durationMinutes : durationMinutes),
                    (durationSeconds < 10 ? "0" + durationSeconds : durationSeconds));
        } else {
            return context.getString(R.string.replay_duration, String.valueOf(durationMinutes), (durationSeconds < 10 ? "0" + durationSeconds : durationSeconds));
        }
    }

    @BindingAdapter("android:text")
    public static void bindReplayDuration(@NonNull TextView textView, int duration) {
        textView.setText(computeReplayDuration(textView.getContext(), duration));
    }

    @BindingAdapter("android:text")
    public static void bindReplayDate(@NonNull TextView textView, Date lastModified) {
        textView.setText(DateFormat.getDateInstance().format(lastModified));
    }

    @BindingAdapter("percentPlayed")
    public static void setReplayPlayedDuration(@NonNull ImageView imageView, Integer playedPercent) {
        Drawable background = imageView.getBackground();
        if (background instanceof LayerDrawable) {
            ((LayerDrawable) background).getDrawable(DRAWABLE_INDEX).setLevel(playedPercent);
        }
    }
}
