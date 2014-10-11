/**
 *
 */
package com.stationmillenium.android.activities.songsearchhistory;

import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.utils.AppUtils;
import com.stationmillenium.android.utils.intents.LocalIntentsData;
import com.stationmillenium.android.utils.views.ImageLoader;

import java.util.Date;

/**
 * Song search image display activity
 * Implements {@link OnGlobalLayoutListener} to display image when the layout is full loaded (due to using {@link AsyncTask} in {@link ImageLoader})
 *
 * @author vincent
 */
public class SongSearchHistoryImageDisplayActivity extends ActionBarActivity implements OnGlobalLayoutListener {

    //static parts
    private final static String TAG = "SongSearchImageDisplayActivity";
    private final static String IMAGE_FILE_NAME_BUNDLE = "ImageFileNameBundle";
    private final static String IMAGE_URL_BUNDLE = "ImageURLBundle";
    private final static String ACTIVITY_TITLE_BUNDLE = "ActivityTitleBundle";
    private final static String GENERATED_FILE_NAME_KEY = "generated";

    //instance vars
    private ImageLoader imageLoader;
    private String imageFileName;
    private String imageURL;
    private String activityTitle;

    //widgets
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            imageFileName = savedInstanceState.getString(IMAGE_FILE_NAME_BUNDLE);
            imageURL = savedInstanceState.getString(IMAGE_URL_BUNDLE);
            activityTitle = savedInstanceState.getString(ACTIVITY_TITLE_BUNDLE);
        }

        //set layout
        setContentView(R.layout.image_display_layout);
        View mainView = findViewById(R.id.image_display_layout);
        mainView.getViewTreeObserver().addOnGlobalLayoutListener(this);
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

        //get widgets
        imageView = (ImageView) findViewById(R.id.image_display_imageview);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.image_display_progressbar);

        //try to get data in saved place
        //file name part
        if (imageFileName == null) { //set the file name
            imageFileName = getIntent().getStringExtra(LocalIntentsData.IMAGE_FILE_PATH.toString());
            if (imageFileName == null) {
                imageFileName = GENERATED_FILE_NAME_KEY + new Date().getTime();
                if (BuildConfig.DEBUG)
                    Log.d(TAG, "Name not found - use generated one : " + imageFileName);
            } else
                imageFileName = imageFileName.substring(imageFileName.lastIndexOf("/") + 1);
        }
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Image file name : " + imageFileName);

        //image URL part
        if (imageURL == null) {
            imageURL = getResources().getString(R.string.player_image_url_root) + getIntent().getStringExtra(LocalIntentsData.IMAGE_FILE_PATH.toString());
        }

        if (BuildConfig.DEBUG)
            Log.d(TAG, "Image URL : " + imageURL);

        //start loader
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Launch song search image loading...");
        imageLoader = new ImageLoader(imageView, progressBar, imageFileName, this, true);
    }

    @Override
    public void onPause() {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Pause song search image display activity");
        if (imageLoader != null) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "Cancel the async image loader");
            imageLoader.cancel(true); //cancel loading if running
        }

        //recycle image when pausing
        AppUtils.recycleBitmapFromImageView(imageView);

        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(IMAGE_FILE_NAME_BUNDLE, imageFileName);
        outState.putString(IMAGE_URL_BUNDLE, imageURL);
        outState.putString(ACTIVITY_TITLE_BUNDLE, String.valueOf(getTitle()));
        super.onSaveInstanceState(outState);
    }

    /**
     * Display image when layout is loaded
     */
    @Override
    public void onGlobalLayout() {
        if (imageLoader != null) {
            if (imageLoader.getStatus() == Status.PENDING) {
                if (BuildConfig.DEBUG)
                    Log.d(TAG, "Layout loaded - load image");
                imageLoader.execute(imageURL);
            } else if (BuildConfig.DEBUG)
                Log.d(TAG, "Image loader already executed");
        }
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Image loader is null");
    }

}
