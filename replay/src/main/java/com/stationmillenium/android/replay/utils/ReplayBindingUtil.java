package com.stationmillenium.android.replay.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;

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
     * @param duration the duration in s
     * @return the formatted text
     */
    @NonNull
    private static String computeReplayDuration(@NonNull Context context, long duration) {
        long durationMinutes = duration / 60;
        duration = duration % 60;
        if (durationMinutes >= 60) {
            long durationHours = durationMinutes / 60;
            durationMinutes = durationMinutes % 60;
            return context.getString(R.string.replay_duration_hours, String.valueOf(durationHours),
                    (durationMinutes < 10 ? "0" + durationMinutes : durationMinutes),
                    (duration < 10 ? "0" + duration : duration));
        } else {
            return context.getString(R.string.replay_duration, String.valueOf(durationMinutes), (duration < 10 ? "0" + duration : duration));
        }
    }

    @BindingAdapter("android:text")
    public static void bindReplayDuration(@NonNull TextView textView, long duration) {
        textView.setText(computeReplayDuration(textView.getContext(), duration / 1_000_000_000));
    }

    @BindingAdapter("android:text")
    public static void bindReplayDate(@NonNull TextView textView, Date lastModified) {
        if (lastModified != null) {
            textView.setText(DateFormat.getDateInstance().format(lastModified));
        }
    }

    @BindingAdapter("fileSize")
    public static void bindReplayFileSize(@NonNull TextView textView, long fileSize) {
        float fileSizeMB = (float) fileSize / 1024 / 1024;
        textView.setText(textView.getContext().getString(R.string.replay_size, fileSizeMB));
    }

    @BindingAdapter("percentPlayed")
    public static void setReplayPlayedDuration(@NonNull TextView textView, Integer playedPercent) {
        Drawable background = textView.getBackground();
        if (background instanceof LayerDrawable) {
            ((LayerDrawable) background).getDrawable(DRAWABLE_INDEX).setLevel(playedPercent);
        }
    }

    @BindingAdapter({"replayPlayedTime", "replayDuration"})
    public static void setReplayPlayedDurationAsText(@NonNull TextView textView, Integer replayPlayedTime, Integer replayDuration) {
        int playedPercentIn10000;
        if (replayPlayedTime != null && replayDuration != null && replayDuration > 0) {
            textView.setText(textView.getContext().getString(R.string.replay_duration_played,
                    computeReplayDuration(textView.getContext(), replayPlayedTime / 1000),
                    computeReplayDuration(textView.getContext(), replayDuration / 1000)));

            float duration = (float) replayDuration;
            float playedPercent;
            if (duration > 0) {
                playedPercent = replayPlayedTime / duration;
            } else {
                playedPercent = 0;
            }
            playedPercentIn10000 = (int) (playedPercent * 10000);

            Drawable background = textView.getBackground();
            if (background instanceof LayerDrawable) {
                ((LayerDrawable) background).getDrawable(DRAWABLE_INDEX).setLevel(playedPercentIn10000);
            }
        } else {
            textView.setText("");
            playedPercentIn10000 = replayPlayedTime > 0 ? 10000 : 0;
        }

        Drawable background = textView.getBackground();
        if (background instanceof LayerDrawable) {
            ((LayerDrawable) background).getDrawable(DRAWABLE_INDEX).setLevel(playedPercentIn10000);
        }
        int padding = (int) textView.getContext().getResources().getDimension(R.dimen.replay_played_time_padding);
        textView.setPadding(0, padding, 0, padding);
    }
}
