/**
 *
 */
package com.stationmillenium.android.services;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.content.LocalBroadcastManager;

import com.stationmillenium.android.R;
import com.stationmillenium.android.libutils.AppUtils;
import com.stationmillenium.android.libutils.dtos.CurrentTitleDTO;
import com.stationmillenium.android.libutils.exceptions.XMLParserException;
import com.stationmillenium.android.libutils.intents.LocalIntents;
import com.stationmillenium.android.libutils.intents.LocalIntentsData;
import com.stationmillenium.android.libutils.network.NetworkUtils;
import com.stationmillenium.android.libutils.toasts.DisplayToastsUtil;
import com.stationmillenium.android.libutils.xml.XMLCurrentTitleParser;

import java.io.InputStream;

import timber.log.Timber;

/**
 * Service to manage current title grabbering
 *
 * @author vincent
 */
public class CurrentTitlePlayerService extends JobIntentService {

    private static final String TAG = "CurrentTitleService";
    private static final int JOB_ID = 1001;

    private DisplayToastsUtil displayToast;

    @Override
    public void onCreate() {
        super.onCreate();
        displayToast = new DisplayToastsUtil(getApplicationContext());
    }

    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, CurrentTitlePlayerService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        if (AppUtils.isMediaPlayerServiceRunning(getApplicationContext())) { //is the media player service running,
            if (AppUtils.isNetworkAvailable(this)) { //is network ok ?
                Timber.d("Network is available - get XML data...");
                InputStream is = NetworkUtils.connectToURL(getResources().getString(R.string.player_current_song_url),
                        null,
                        getResources().getString(R.string.player_connection_request_method),
                        getResources().getString(R.string.player_connection_content_type),
                        Integer.parseInt(getResources().getString(R.string.player_connection_connect_timeout)),
                        Integer.parseInt(getResources().getString(R.string.player_connection_read_timeout)));

                if (is != null) { //input stream ok
                    Timber.d("Input stream is OK - process it...");
                    try {
                        //get and parse XML data
                        XMLCurrentTitleParser currentTitleParser = new XMLCurrentTitleParser(is);
                        CurrentTitleDTO songDataDTO = currentTitleParser.parseXML();
                        Timber.d("Gathered song data : %s", songDataDTO);

                        //process image if needed
                        if ((songDataDTO != null)
                                && (songDataDTO.getCurrentSong() != null)
                                && (songDataDTO.getCurrentSong().getMetadata() != null)) {
                            songDataDTO.getCurrentSong().setImageURL(getResources().getString(R.string.player_image_url_root) + songDataDTO.getCurrentSong().getMetadata().getPath());
                        }

                        //send intent
                        Intent intentToSend = new Intent(LocalIntents.CURRENT_TITLE_UPDATED.toString());
                        intentToSend.putExtra(LocalIntentsData.CURRENT_TITLE.toString(), songDataDTO);
                        Timber.d("Send intent to update current title : %s", intentToSend);

                        LocalBroadcastManager.getInstance(this).sendBroadcast(intentToSend);
                        sendBroadcast(intentToSend); //for the widget

                    } catch (XMLParserException e) {
                        Timber.w(e, "Error while parsing XML data");
                    }

                } else { //error while getting input stream
                    Timber.d("No input stream - stopping service...");
                }

            } else {
                Timber.d("Network is unavailable - stopping service...");

                displayToast.displayToast(R.string.player_network_unavailable);
            }

        } else { //media player service is not running, no need to update title
            Timber.d("Media player is not running - stopping service...");
        }
    }

}
