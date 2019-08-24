package com.stationmillenium.android.replay.utils;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.stationmillenium.android.replay.R;
import com.stationmillenium.android.replay.dto.PlaylistDTO;
import com.stationmillenium.android.replay.dto.TrackDTO;

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
        setReplayImage(replayArtwork, trackDTO.getImageURL());
    }

    /**
     * Set replay image for replay item
     * @param replayArtwork the image view destination
     * @param playlistDTO the replay item
     */
    @BindingAdapter({"replayImage"})
    public static void setReplayPlaylistImage(final ImageView replayArtwork, PlaylistDTO playlistDTO) {
        setReplayImage(replayArtwork, playlistDTO.getImageURL());
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

