package com.stationmillenium.android.libutils.activities;

import android.databinding.BindingAdapter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ArrayAdapter;
import android.widget.ImageSwitcher;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.stationmillenium.android.R;

import java.util.List;

/**
 * Binding utils for {@link com.stationmillenium.android.activities.PlayerActivity}}
 * Created by vincent on 26/02/17.
 */

public class PlayerActivityBindingUtils {

    @BindingAdapter("imageURL")
    public static void setImageFromURL(final ImageSwitcher imageSwitcher, String url) {
        Glide.with(imageSwitcher.getContext())
            .load(url)
            .apply(new RequestOptions()
                    .placeholder(R.drawable.player_default_image)
                    .error(R.drawable.player_default_image)
                    .centerCrop())
            .into(new SimpleTarget<Drawable>() {

                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    imageSwitcher.setImageDrawable(resource);
                }

                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    if (errorDrawable instanceof BitmapDrawable) {
                        imageSwitcher.setImageDrawable(errorDrawable);
                    }
                }
            });
    }

    @BindingAdapter("historyList")
    public static void setHistoryListValues(ListView historyList, List<String> values) {
        historyList.setAdapter((values != null) ? new ArrayAdapter<>(historyList.getContext(), R.layout.player_history_list_item, R.id.player_history_item_text, values) : null);
    }

    @BindingAdapter({"artist", "title"})
    public static void setArtistAndTitle(TextView textView, String artist, String title) {
        //update current title
        if ((artist != null) && (title != null)) {
            String titleText = textView.getResources().getString(R.string.player_current_title, artist, title);
            textView.setText(titleText);
        } else {
            textView.setText(R.string.player_no_title);
        }
    }

}
