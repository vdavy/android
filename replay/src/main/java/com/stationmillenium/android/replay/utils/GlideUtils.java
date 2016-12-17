package com.stationmillenium.android.replay.utils;

import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.stationmillenium.android.replay.R;
import com.stationmillenium.android.replay.dto.TrackDTO;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.ColorFilterTransformation;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

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
    @BindingAdapter({"replayImage"})
    public static void setReplayImage(final ImageView replayArtwork, TrackDTO replay) {
        // set round image : http://stackoverflow.com/questions/25278821/how-do-rounded-image-with-glide-library
        Glide.with(replayArtwork.getContext())
                .load(replay.getArtworkURL())
                .asBitmap()
                .placeholder(R.drawable.default_replay)
                .centerCrop()
                .transform(new CropCircleTransformation(replayArtwork.getContext()))
                .into(replayArtwork);
    }

    /**
     * Set replay image for replay item
     * @param imageView the linear layout to put background sound image in
     * @param replay the replay item
     */
    @BindingAdapter({"replayBackground"})
    public static void setReplaySoundImageAsBackground(final ImageView imageView, TrackDTO replay) {
        Glide.with(imageView.getContext())
                .load(replay.getWaveformURL())
                .asBitmap()
                .centerCrop()
                .transform(
                        new ColorFilterTransformation(imageView.getContext(), ResourcesCompat.getColor(imageView.getContext().getResources(), R.color.accent, null)),
                        new BlurTransformation(imageView.getContext(), 10),
                        new RoundedCornersTransformation(imageView.getContext(), imageView.getResources().getDimensionPixelSize(R.dimen.replay_item_tag_space), 0))
                .listener(new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        imageView.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .into(imageView);
    }


    @BindingAdapter({"searchedSongImage", "progressBar"})
    public static void setSearchedSongImageDisplay(final ImageView imageView, String imageURL, final ProgressBar progressBar) {
        Glide.with(imageView.getContext())
                .load(imageURL)
                .fitCenter()
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(imageView);
    }

}

