package com.stationmillenium.android.replay.utils;

import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
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
    public static void setReplayImage(final ImageView replayArtwork, TrackDTO replay) {
        // set round image : http://stackoverflow.com/questions/25278821/how-do-rounded-image-with-glide-library
        Glide.with(replayArtwork.getContext())
                .load(replay.getArtworkURL())
                .asBitmap()
                .placeholder(R.drawable.default_replay)
                .centerCrop().into(new BitmapImageViewTarget(replayArtwork) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(replayArtwork.getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                view.setImageDrawable(circularBitmapDrawable);
            }
        });
    }
}
