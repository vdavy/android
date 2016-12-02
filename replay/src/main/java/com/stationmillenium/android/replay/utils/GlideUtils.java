package com.stationmillenium.android.replay.utils;

import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.stationmillenium.android.replay.R;
import com.stationmillenium.android.replay.dto.TrackDTO;

/**
 * Glide utils
 * Created by vincent on 29/11/16.
 */
public class GlideUtils {

    /**
     * Set replay image for replay item
     * @param replayArtwork the image view destination
     * @param replay the replay item
     */
    @BindingAdapter({"bind:replayImage"})
    public static void setReplayImage(final ImageView replayArtwork, TrackDTO replay) {
        // set round image : http://stackoverflow.com/questions/25278821/how-do-rounded-image-with-glide-library
        Glide.with(replayArtwork.getContext())
                .load(replay.getArtworkURL())
                .asBitmap()
                .placeholder(R.drawable.default_replay)
                .centerCrop()
                .into(new BitmapImageViewTarget(replayArtwork) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(replayArtwork.getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                view.setImageDrawable(circularBitmapDrawable);
            }
        });
    }

    /**
     * Set replay image for replay item
     * @param linearLayout the linear layout to put background sound image in
     * @param replay the replay item
     */
    @BindingAdapter({"bind:replayBackground"})
    public static void setReplaySoundImageAsBackground(final LinearLayout linearLayout, TrackDTO replay) {
        // set image as background : http://stackoverflow.com/questions/33971626/set-background-image-to-relative-layout-using-glide-in-android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Glide.with(linearLayout.getContext())
                    .load(replay.getWaveformURL())
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            BitmapDrawable bitmapDrawable = new BitmapDrawable(linearLayout.getResources(), resource);
                            bitmapDrawable.mutate().setColorFilter(0xffff0000, PorterDuff.Mode.MULTIPLY);
                            linearLayout.setBackground(bitmapDrawable);
                        }
                    });
        }
    }
}

