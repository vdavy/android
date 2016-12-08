package com.stationmillenium.android.replay.activities;

import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stationmillenium.android.replay.R;
import com.stationmillenium.android.replay.databinding.ReplayItemFragmentBinding;
import com.stationmillenium.android.replay.dto.TrackDTO;
import com.stationmillenium.android.replay.utils.view.FlowLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Replay item fragment
 * Created by vincent on 28/11/16.
 */
public class ReplayItemFragment extends Fragment {

    private ReplayItemFragmentBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.replay_item_fragment, container, false);
        return binding.getRoot();
    }

    /**
     * Bind the replay data
     * @param replay replay to display
     */
    public void setReplay(TrackDTO replay) {
        binding.setReplayItem(replay);
        displayTags(replay);
    }

    private void displayTags(TrackDTO replay) {
        //set the layout params
        FlowLayout.LayoutParams layoutParams = new FlowLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.replay_item_tag_space),
                getResources().getDimensionPixelSize(R.dimen.replay_item_tag_space));

        for (final String tag : parseTagList(replay)) {
            TextView tagTextView = new TextView(getContext());
            tagTextView.setLayoutParams(layoutParams);
            tagTextView.setText(getString(R.string.replay_genre, tag));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                tagTextView.setBackground(getResources().getDrawable(R.drawable.replay_genre_background));
            }
            tagTextView.setTextAppearance(getContext(), R.style.ReplayItemTag);
            tagTextView.setPadding(getResources().getDimensionPixelSize(R.dimen.replay_item_textview_padding), 0,
                    getResources().getDimensionPixelSize(R.dimen.replay_item_textview_padding), 0);
            tagTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ReplayItemActivity) getActivity()).searchTag(tag);
                }
            });
            binding.replayItemLinearLayout.addView(tagTextView);
        }
    }

    private List<String> parseTagList(TrackDTO replay) {
        if (replay.getTagList() != null && replay.getTagList().length() > 0) {
            List<String> tagList = new ArrayList<>();
            boolean multiwordTag = false;
            for (String tag : replay.getTagList().split(" ")) { // tags are space separated
                if (tag != null && tag.length() > 0) {
                    if (tag.startsWith("\"")) { // the tag is a start of a multiword tag
                        tagList.add(tag.substring(1));
                        multiwordTag = true;
                    } else if (multiwordTag) {
                        String word;
                        if (tag.endsWith("\"")) { //we found the end of the multiword
                            word = tag.substring(0, tag.length() - 2);
                            multiwordTag = false;
                        } else { // add word to the last tag
                            word = tag;
                        }
                        word = tagList.get(tagList.size() - 1) + " " + word;
                        tagList.remove(tagList.size() - 1);
                        tagList.add(word);
                    } else {
                        tagList.add(tag);
                    }
                }
            }
            return tagList;
        } else {
            return Collections.EMPTY_LIST;
        }
    }
}
