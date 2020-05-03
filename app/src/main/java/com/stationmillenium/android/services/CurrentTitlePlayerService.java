/**
 *
 */
package com.stationmillenium.android.services;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.stationmillenium.android.R;
import com.stationmillenium.android.dtos.CurrentTrack;
import com.stationmillenium.android.libutils.AppUtils;
import com.stationmillenium.android.libutils.DateTime;
import com.stationmillenium.android.libutils.dtos.CurrentTitleDTO;
import com.stationmillenium.android.libutils.intents.LocalIntents;
import com.stationmillenium.android.libutils.intents.LocalIntentsData;
import com.stationmillenium.android.libutils.toasts.DisplayToastsUtil;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

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

                try {
                    RestTemplate restTemplate = new RestTemplate();
                    restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                    CurrentTrack currentTrack = restTemplate.getForObject(getResources().getString(R.string.player_current_song_url), CurrentTrack.class);

                    CurrentTitleDTO currentTitleDTO = new CurrentTitleDTO();

                    if (currentTrack != null && currentTrack.isTrack()) {
                        currentTitleDTO.getCurrentSong().setArtist(currentTrack.getArtist());
                        currentTitleDTO.getCurrentSong().setTitle(currentTrack.getTitle());
                        currentTitleDTO.getCurrentSong().setPlayedDate(new Date(DateTime.parseRfc3339(currentTrack.getTime()).getValue()));
                        if (currentTrack.isImage()) {
                            currentTitleDTO.getCurrentSong().setImageURL(getResources().getString(R.string.player_image_url_root) + currentTrack.getTime());
                        }

                        CurrentTrack[] last5Tracks = restTemplate.getForObject(getResources().getString(R.string.player_last_5_tracks_url), CurrentTrack[].class);
                        for (CurrentTrack historyTrack : last5Tracks) {
                            if (historyTrack.isTrack()) {
                                CurrentTitleDTO.Song song = new CurrentTitleDTO.Song();
                                song.setArtist(historyTrack.getArtist());
                                song.setTitle(historyTrack.getTitle());
                                song.setPlayedDate(new Date(DateTime.parseRfc3339(historyTrack.getTime()).getValue()));
                                song.setImageURL(getResources().getString(R.string.player_image_url_root) + historyTrack.getTime());

                                currentTitleDTO.getHistory().add(song);
                            }
                        }
                    }

                    //send intent
                    Intent intentToSend = new Intent(LocalIntents.CURRENT_TITLE_UPDATED.toString());
                    intentToSend.putExtra(LocalIntentsData.CURRENT_TITLE.toString(), currentTitleDTO);
                    Timber.d("Send intent to update current title : %s", intentToSend);

                    LocalBroadcastManager.getInstance(this).sendBroadcast(intentToSend);

                } catch (Exception e) {
                    Timber.w(e, "Error getting current track");
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
