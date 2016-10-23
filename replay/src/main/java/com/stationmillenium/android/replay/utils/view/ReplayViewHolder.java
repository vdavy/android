package com.stationmillenium.android.replay.utils.view;

import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.stationmillenium.android.replay.databinding.ReplayListItemBinding;
import com.stationmillenium.android.replay.dto.TrackDTO;

/**
 * Binding and view holder for replay item in list
 * Created by vincent on 23/10/16.
 */
public class ReplayViewHolder extends ViewHolder {

    private ReplayListItemBinding binding;

    public ReplayViewHolder(View itemView) {
        super(itemView);
        binding = ReplayListItemBinding.bind(itemView);
    }

    public void bindReplay(TrackDTO trackDTO) {
        binding.setReplayItem(trackDTO);
        // set round image : http://stackoverflow.com/questions/25278821/how-do-rounded-image-with-glide-library
        Glide.with(binding.replayArtwork.getContext())
                .load(trackDTO.getArtworkURL()).asBitmap().centerCrop().into(new BitmapImageViewTarget(binding.replayArtwork) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(binding.replayArtwork.getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                view.setImageDrawable(circularBitmapDrawable);
            }
        });
    }
}
