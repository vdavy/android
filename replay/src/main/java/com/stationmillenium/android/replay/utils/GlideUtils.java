package com.stationmillenium.android.replay.utils;

import android.databinding.BindingAdapter;
import android.support.v4.content.res.ResourcesCompat;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
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
                .animate(R.anim.abc_fade_in)
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
                .animate(R.anim.abc_fade_in)
                .transform(
                        new ColorFilterTransformation(imageView.getContext(), ResourcesCompat.getColor(imageView.getContext().getResources(), R.color.accent, null)),
                        new BlurTransformation(imageView.getContext(), 10),
                        new RoundedCornersTransformation(imageView.getContext(), imageView.getResources().getDimensionPixelSize(R.dimen.replay_item_tag_space), 0))
                .into(imageView);
    }

}

