package com.stationmillenium.android.replay.utils;

import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.stationmillenium.android.replay.R;
import com.stationmillenium.android.replay.dto.PlaylistDTO;
import com.stationmillenium.android.replay.dto.TrackDTO;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.ColorFilterTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Glide utils
 * Created by vincent on 29/11/16.
 */
public class GlideUtils {

    /**
     * Set replay image for replay item
     * @param replayArtwork the image view destination
     * @param trackDTO the replay item
     */
    @BindingAdapter({"replayImage"})
    public static void setReplayTrackImage(final ImageView replayArtwork, TrackDTO trackDTO) {
        setReplayImage(replayArtwork, trackDTO.getArtworkURL());
    }

    /**
     * Set replay image for replay item
     * @param replayArtwork the image view destination
     * @param playlistDTO the replay item
     */
    @BindingAdapter({"replayImage"})
    public static void setReplayPlaylistImage(final ImageView replayArtwork, PlaylistDTO playlistDTO) {
        setReplayImage(replayArtwork, playlistDTO.getArtworkURL());
    }

    private static void setReplayImage(final ImageView replayArtwork, String imageURL) {
        // set round image : http://stackoverflow.com/questions/25278821/how-do-rounded-image-with-glide-library
        Glide.with(replayArtwork.getContext())
                .load(imageURL)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.default_replay)
                        .centerCrop()
                        .apply(RequestOptions.circleCropTransform()))
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
                .apply(new RequestOptions()
                        .centerCrop()
                        .transform(new MultiTransformation<>(
                                new ColorFilterTransformation(ResourcesCompat.getColor(imageView.getContext().getResources(), R.color.accent, null)),
                                new BlurTransformation(10),
                                new RoundedCornersTransformation(imageView.getResources().getDimensionPixelSize(R.dimen.replay_item_tag_space), 0))))
//                .listener(new RequestListener<Drawable>() {
//                    @Override
//                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                        Timber.e(e);
//                        return false;
//                    }
//
//                    @Override
//                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                        imageView.setVisibility(View.VISIBLE);
//                        return false;
//                    }
//
//                })
                .into(imageView);
    }


    @BindingAdapter({"searchedSongImage", "progressBar"})
    public static void setSearchedSongImageDisplay(final ImageView imageView, String imageURL, final ProgressBar progressBar) {
        Glide.with(imageView.getContext())
                .load(imageURL)
                .apply(new RequestOptions().fitCenter())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(imageView);
    }

}

