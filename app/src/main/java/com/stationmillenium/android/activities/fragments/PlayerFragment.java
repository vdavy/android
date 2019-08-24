/**
 *
 */
package com.stationmillenium.android.activities.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.PlayerActivity;
import com.stationmillenium.android.databinding.PlayerFragmentBinding;
import com.stationmillenium.android.libutils.activities.PlayerState;
import com.stationmillenium.android.libutils.dtos.CurrentTitleDTO.Song;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Activity to display the player
 *
 * @author vincent
 */
public class PlayerFragment extends Fragment {

    //static part
    private static final String SONG_DATA_SAVE = "SONG_DATA_SAVE";
    private static final String CURRENT_TIME_SAVE_MINUTES = "CURRENT_TIME_SAVE_MINUTES";
    private static final String CURRENT_TIME_SAVE_SECONDS = "CURRENT_TIME_SAVE_SECONDS";
    private static final String PLAYER_STATE_SAVE = "PLAYER_STATE_SAVE";
    private static final String HISTORY_LIST_SAVE = "HISTORY_LIST_SAVE";

    //widgets
    private PlayerFragmentBinding binding;

    private List<String> currentTitleList = null;

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Timber.d("Create the activity");

        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.inflate(inflater, R.layout.player_fragment, container, false);
        binding.setActivity((PlayerActivity) getActivity());
        binding.setPlayerState(PlayerState.STOPPED);

        //image switcher
        binding.playerImageSwitcher.setFactory(() -> {
            ImageView imageView = new ImageView(getActivity());
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            imageView.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.transparent));
            return imageView;
        });

        //restore value
        if (savedInstanceState != null) {
            setSongData((Song) savedInstanceState.getSerializable(SONG_DATA_SAVE));
            setPlayingTime(savedInstanceState.getString(CURRENT_TIME_SAVE_MINUTES), savedInstanceState.getString(CURRENT_TIME_SAVE_SECONDS));
            setPlayerState((PlayerState) savedInstanceState.getSerializable(PLAYER_STATE_SAVE));
            if (savedInstanceState.containsKey(HISTORY_LIST_SAVE)) {
                setHistoryList(savedInstanceState.getStringArrayList(HISTORY_LIST_SAVE));
            }
        }
        return binding.getRoot();
    }

    private void reinitOnStop() {
        binding.setHistoryArray(null);
        binding.setSongData(null);
        currentTitleList = null;
    }

    public PlayerState getPlayerState() {
        return binding.getPlayerState();
    }

    public void setPlayerState(PlayerState playerState) {
        Timber.v("Set player state : %s", playerState);
        binding.setPlayerState(playerState);
        if (playerState == PlayerState.STOPPED) {
            reinitOnStop();
        }
    }

    public void setPlayingTime(String minutes, String seconds) {
        binding.setPlayingMinutes(minutes);
        binding.setPlayingSeconds(seconds);
    }

    public void setSongData(Song songData) {
        binding.setSongData(songData);
    }

    public void setHistoryList(List<String> historyList) {
        if (historyList == null) {
            binding.setHistoryArray(null);
            currentTitleList = null;
        } else if (currentTitleList == null) {
            binding.setHistoryArray(historyList);
            currentTitleList = historyList;
        } else {
            for (int i = 0; i < currentTitleList.size(); i++) {
                if (currentTitleList.get(i) != null && historyList.get(i) != null
                        && !currentTitleList.get(i).equals(historyList.get(i))) {
                    binding.setHistoryArray(historyList);
                    currentTitleList = historyList;
                    break;
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable(SONG_DATA_SAVE, binding.getSongData());
        outState.putString(CURRENT_TIME_SAVE_MINUTES, binding.getPlayingMinutes());
        outState.putString(CURRENT_TIME_SAVE_SECONDS, binding.getPlayingSeconds());
        outState.putSerializable(PLAYER_STATE_SAVE, binding.getPlayerState());
        if (binding.getHistoryArray() != null) {
            outState.putStringArrayList(HISTORY_LIST_SAVE, new ArrayList<>(binding.getHistoryArray()));
        }
        super.onSaveInstanceState(outState);
    }

    public void setPlayingOnChromecast(boolean playingOnChromecast) {
        binding.setPlayingOnChromecast(playingOnChromecast);
    }
}
