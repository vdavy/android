/**
 *
 */
package com.stationmillenium.android.activities.songsearchhistory;

import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.databinding.ImageDisplayLayoutBinding;
import com.stationmillenium.android.libutils.PiwikTracker;
import com.stationmillenium.android.libutils.intents.LocalIntentsData;
import com.stationmillenium.android.libutils.views.ImageLoader;

import static com.stationmillenium.android.libutils.PiwikTracker.PiwikPages.SONG_HISTORY_DISPLAY_IMAGE;

/**
 * Song search image display activity
 * Implements {@link OnGlobalLayoutListener} to display image when the layout is full loaded (due to using {@link AsyncTask} in {@link ImageLoader})
 *
 * @author vincent
 */
public class SongSearchHistoryImageDisplayActivity extends AppCompatActivity {

    //static parts
    private final static String TAG = "SongImageActivity";
    private final static String IMAGE_URL_BUNDLE = "ImageURLBundle";
    private final static String ACTIVITY_TITLE_BUNDLE = "ActivityTitleBundle";

    //instance vars
    private String imageURL;
    private String activityTitle;

    //binding
    private ImageDisplayLayoutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            imageURL = savedInstanceState.getString(IMAGE_URL_BUNDLE);
            activityTitle = savedInstanceState.getString(ACTIVITY_TITLE_BUNDLE);
        }

        //set layout
        binding = DataBindingUtil.setContentView(this, R.layout.image_display_layout);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Resuming song search image display activity");

        //set title
        if (activityTitle == null) //if title is null, try to get it from intent
            activityTitle = getIntent().getStringExtra(LocalIntentsData.IMAGE_TITLE.toString());
        setTitle(activityTitle);

        //image URL part
        if (imageURL == null) {
            imageURL = getResources().getString(R.string.player_image_url_root) + getIntent().getStringExtra(LocalIntentsData.IMAGE_FILE_PATH.toString());
        }
        binding.setImageURL(imageURL);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Image URL : " + imageURL);
        }

        PiwikTracker.trackScreenView(SONG_HISTORY_DISPLAY_IMAGE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(IMAGE_URL_BUNDLE, imageURL);
        outState.putString(ACTIVITY_TITLE_BUNDLE, String.valueOf(getTitle()));
        super.onSaveInstanceState(outState);
    }

}
