package com.stationmillenium.android.libutils.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.PlayerActivity;
import com.stationmillenium.android.libutils.AppUtils;
import com.stationmillenium.android.libutils.dtos.CurrentTitleDTO;
import com.stationmillenium.android.libutils.intents.LocalIntentsData;

import java.util.ArrayList;
import java.util.List;

/**
 * Receiver for the update title broadcast in {@link PlayerActivity}
 *
 * @author vincent
 */
public class PlayerActivityUpdateTitleBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "UpdateTitleBR";

    private PlayerActivity playerActivity;

    public PlayerActivityUpdateTitleBroadcastReceiver(PlayerActivity playerActivity) {
        this.playerActivity = playerActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Update title intent received - process...");

        if (AppUtils.isMediaPlayerServiceRunning(playerActivity.getApplicationContext())) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Media player service running - applying received data...");
            }

            CurrentTitleDTO songData = null;
            if ((intent != null) && (intent.getExtras() != null)) {
                songData = (CurrentTitleDTO) intent.getExtras().get(LocalIntentsData.CURRENT_TITLE.toString());
            }

            if (songData != null) { //if data found
                //update the image
                playerActivity.setImageFromURL(songData.getCurrentSong().getImageURL());

                //update current title
                if ((songData.getCurrentSong().getArtist() != null) && (songData.getCurrentSong().getTitle() != null)) {
                    String titleText = playerActivity.getResources().getString(R.string.player_current_title, songData.getCurrentSong().getArtist(), songData.getCurrentSong().getTitle());
                    playerActivity.setCurrentTitleTextView(titleText);
                } else {
                    playerActivity.setCurrentTitleTextView(playerActivity.getResources().getString(R.string.player_no_title));
                }

                //update the history view
                List<String> historyTextList = new ArrayList<>();
                for (CurrentTitleDTO.Song historySong : songData.getHistory()) {
                    String historyText = playerActivity.getResources().getString(R.string.player_current_title, historySong.getArtist(), historySong.getTitle());
                    historyTextList.add(historyText);
                }
                playerActivity.setHistoryListValues(historyTextList);

            } else { //no data available - use default
                Log.w(TAG, "No data available !");
                playerActivity.setImageSwitcherResource(R.drawable.player_default_image);
                playerActivity.setCurrentTitleTextView(playerActivity.getResources().getString(R.string.player_no_title));
            }

        } else {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Media player service not running - reseting widgets...");
            }

            playerActivity.playerStopped(); //reset all widgets
        }
    }

}
