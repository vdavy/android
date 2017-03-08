/**
 *
 */
package com.stationmillenium.android.activities.fragments;

import android.annotation.SuppressLint;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.PlayerActivity;
import com.stationmillenium.android.databinding.PlayerFragmentBinding;
import com.stationmillenium.android.libutils.activities.PlayerState;
import com.stationmillenium.android.libutils.dtos.CurrentTitleDTO.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to display the player
 *
 * @author vincent
 */
public class PlayerFragment extends Fragment {

    //static part
    private static final String TAG = "PlayerFragment";
    private static final String SONG_DATA_SAVE = "SONG_DATA_SAVE";
    private static final String CURRENT_TIME_SAVE_MINUTES = "CURRENT_TIME_SAVE_MINUTES";
    private static final String CURRENT_TIME_SAVE_SECONDS = "CURRENT_TIME_SAVE_SECONDS";
    private static final String PLAYER_STATE_SAVE = "PLAYER_STATE_SAVE";
    private static final String HISTORY_LIST_SAVE = "HISTORY_LIST_SAVE";



    //widgets
    private PlayerFragmentBinding binding;


    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Create the activity");
        }
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.inflate(inflater, R.layout.player_fragment, container, false);
        binding.setActivity((PlayerActivity) getActivity());

        //image switcher
        binding.playerImageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
             @Override
             public View makeView() {
                 ImageView imageView = new ImageView(getActivity());
                 imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                 imageView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                 imageView.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.transparent));
                 return imageView;
             }
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
    }

    public PlayerState getPlayerState() {
        return binding.getPlayerState();
    }

    public void setPlayerState(PlayerState playerState) {
        Log.v(TAG, "Set player state : " + playerState);
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
        binding.setHistoryArray(historyList);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(SONG_DATA_SAVE, binding.getSongData());
        outState.putString(CURRENT_TIME_SAVE_MINUTES, binding.getPlayingMinutes());
        outState.putString(CURRENT_TIME_SAVE_SECONDS, binding.getPlayingSeconds());
        outState.putSerializable(PLAYER_STATE_SAVE, binding.getPlayerState());
        outState.putStringArrayList(HISTORY_LIST_SAVE, new ArrayList<>(binding.getHistoryArray()));
        super.onSaveInstanceState(outState);
    }
}
