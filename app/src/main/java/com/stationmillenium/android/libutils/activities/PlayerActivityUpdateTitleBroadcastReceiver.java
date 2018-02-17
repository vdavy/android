package com.stationmillenium.android.libutils.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.stationmillenium.android.activities.PlayerActivity;
import com.stationmillenium.android.libutils.AppUtils;
import com.stationmillenium.android.libutils.dtos.CurrentTitleDTO;
import com.stationmillenium.android.libutils.intents.LocalIntentsData;

import timber.log.Timber;

/**
 * Receiver for the update title broadcast in {@link PlayerActivity}
 *
 * @author vincent
 */
public class PlayerActivityUpdateTitleBroadcastReceiver extends BroadcastReceiver {

    private PlayerActivity playerActivity;

    public PlayerActivityUpdateTitleBroadcastReceiver(PlayerActivity playerActivity) {
        this.playerActivity = playerActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("Update title intent received - process...");

        if (AppUtils.isMediaPlayerServiceRunning(playerActivity.getApplicationContext())) {
            Timber.d("Media player service running - applying received data...");

            CurrentTitleDTO songData = null;
            if ((intent != null) && (intent.getExtras() != null)) {
                songData = (CurrentTitleDTO) intent.getExtras().get(LocalIntentsData.CURRENT_TITLE.toString());
            }

            if (songData != null && songData.getCurrentSong() != null && songData.getHistory() != null) { //if data found
                //update the data
                playerActivity.setSongData(songData);

            } else { //no data available - use default
                Timber.w("No data available !");
                playerActivity.setSongData(null);
            }

        } else {
            Timber.d("Media player service not running - reseting widgets...");
            playerActivity.forceStopState();
        }
    }

}
