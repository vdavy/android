/**
 *
 */
package com.stationmillenium.android.activities.songsearchhistory;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.libutils.AppUtils;
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
public class SongSearchHistoryImageDisplayActivity extends AppCompatActivity implements OnGlobalLayoutListener {

    //static parts
    private final static String TAG = "SongImageActivity";
    private final static String IMAGE_URL_BUNDLE = "ImageURLBundle";
    private final static String ACTIVITY_TITLE_BUNDLE = "ActivityTitleBundle";

    //instance vars
    private String imageURL;
    private String activityTitle;

    //widgets
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
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

        //image URL part
        if (imageURL == null) {
            imageURL = getResources().getString(R.string.player_image_url_root) + getIntent().getStringExtra(LocalIntentsData.IMAGE_FILE_PATH.toString());
        }

        if (BuildConfig.DEBUG)
            Log.d(TAG, "Image URL : " + imageURL);

        PiwikTracker.trackScreenView(SONG_HISTORY_DISPLAY_IMAGE);
    }

    @Override
    public void onPause() {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Pause song search image display activity");

        //recycle image when pausing
        AppUtils.recycleBitmapFromImageView(imageView);

        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(IMAGE_URL_BUNDLE, imageURL);
        outState.putString(ACTIVITY_TITLE_BUNDLE, String.valueOf(getTitle()));
        super.onSaveInstanceState(outState);
    }

    /**
     * Display image when layout is loaded
     */
    @Override
    public void onGlobalLayout() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Layout loaded - load image");
        }
        Glide.with(this)
            .load(imageURL)
            .fitCenter()
            .listener(new RequestListener<String, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    findViewById(R.id.image_display_progressbar).setVisibility(View.GONE);
                    return false;
                }
            })
            .into(imageView);
    }

}
