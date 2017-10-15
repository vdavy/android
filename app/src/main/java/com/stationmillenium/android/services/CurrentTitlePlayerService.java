/**
 *
 */
package com.stationmillenium.android.services;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.libutils.AppUtils;
import com.stationmillenium.android.libutils.dtos.CurrentTitleDTO;
import com.stationmillenium.android.libutils.exceptions.XMLParserException;
import com.stationmillenium.android.libutils.intents.LocalIntents;
import com.stationmillenium.android.libutils.intents.LocalIntentsData;
import com.stationmillenium.android.libutils.network.NetworkUtils;
import com.stationmillenium.android.libutils.xml.XMLCurrentTitleParser;

import java.io.InputStream;

/**
 * Service to manage current title grabbering
 *
 * @author vincent
 */
public class CurrentTitlePlayerService extends JobIntentService {

    private static final String TAG = "CurrentTitleService";
    private static final int JOB_ID = 1001;
    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, CurrentTitlePlayerService.class, JOB_ID, work);
    }

    /* (non-Javadoc)
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        if (AppUtils.isMediaPlayerServiceRunning(getApplicationContext())) { //is the media player service running,
            if (AppUtils.isNetworkAvailable(this)) { //is network ok ?
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Network is available - get XML data...");
                }
                InputStream is = NetworkUtils.connectToURL(getResources().getString(R.string.player_current_song_url),
                        null,
                        getResources().getString(R.string.player_connection_request_method),
                        getResources().getString(R.string.player_connection_content_type),
                        Integer.parseInt(getResources().getString(R.string.player_connection_connect_timeout)),
                        Integer.parseInt(getResources().getString(R.string.player_connection_read_timeout)));

                if (is != null) { //input stream ok
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Input stream is OK - process it...");
                    }
                    try {
                        //get and parse XML data
                        XMLCurrentTitleParser currentTitleParser = new XMLCurrentTitleParser(is);
                        CurrentTitleDTO songDataDTO = currentTitleParser.parseXML();
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "Gathered song data : " + songDataDTO);
                        }
                        //process image if needed
                        if ((songDataDTO != null)
                                && (songDataDTO.getCurrentSong() != null)
                                && (songDataDTO.getCurrentSong().getMetadata() != null)) {
                            songDataDTO.getCurrentSong().setImageURL(getResources().getString(R.string.player_image_url_root) + songDataDTO.getCurrentSong().getMetadata().getPath());
                        }

                        //send intent
                        Intent intentToSend = new Intent(LocalIntents.CURRENT_TITLE_UPDATED.toString());
                        intentToSend.putExtra(LocalIntentsData.CURRENT_TITLE.toString(), songDataDTO);
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "Send intent to update current title : " + intentToSend);
                        }
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intentToSend);
                        sendBroadcast(intentToSend); //for the widget

                    } catch (XMLParserException e) {
                        Log.w(TAG, "Error while parsing XML data", e);
                    }

                } else if (BuildConfig.DEBUG) { //error while getting input stream
                    Log.d(TAG, "No input stream - stopping service...");
                }

            } else {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Network is unavailable - stopping service...");
                }
                Toast.makeText(this, R.string.player_network_unavailable, Toast.LENGTH_SHORT).show();
            }

        } else { //media player service is not running, no need to update title
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Media player is not running - stopping service...");
            }
        }
    }

}
