/**
 * 
 */
package com.stationmillenium.android.activities.songsearchhistory;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.utils.intents.LocalIntentsData;
import com.stationmillenium.android.utils.views.ImageLoader;
import com.stationmillenium.android.utils.views.ImageLoader.ReturnImage;

/**
 * Song search image display activity
 * @author vincent
 *
 */
public class SongSearchHistoryImageDisplayActivity extends Activity implements ReturnImage, OnGlobalLayoutListener {

	//static parts
	private final static String TAG = "SongSearchImageDisplayActivity";
	private final static String IMAGE_TO_DISPLAY_BUNDLE = "SONG_SEARCH_IMAGE_TO_DISPLAY_BUNDLE"; 
	
	//instance vars
	private ImageLoader imageLoader;
	private Bitmap imageToDisplay;
	private String imageURL;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if ((savedInstanceState != null) && (savedInstanceState.getParcelable(IMAGE_TO_DISPLAY_BUNDLE) != null)) //get saved image if any available
			imageToDisplay = savedInstanceState.getParcelable(IMAGE_TO_DISPLAY_BUNDLE);
		
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
		String activityTitle = getIntent().getStringExtra(LocalIntentsData.IMAGE_TITLE.toString());
		setTitle(activityTitle);
		
		//load image
		ImageView imageView = (ImageView) findViewById(R.id.image_display_imageview);
		ProgressBar progressBar = (ProgressBar) findViewById(R.id.image_display_progressbar);
		if (imageToDisplay == null) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Launch song search image loading...");
			String imageFileName = getIntent().getStringExtra(LocalIntentsData.IMAGE_FILE_PATH.toString());
			imageFileName = imageFileName.substring(imageFileName.lastIndexOf("/") + 1);
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Image file name : " + imageFileName);
			imageLoader = new ImageLoader(imageView, progressBar, imageFileName, this, this, true);
			imageURL = getResources().getString(R.string.player_image_url_root) + getIntent().getStringExtra(LocalIntentsData.IMAGE_FILE_PATH.toString());
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Image URL : " + imageURL);
		
		} else {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Image already loaded - display it...");
			imageView.setImageBitmap(imageToDisplay);
			progressBar.setVisibility(View.GONE);
		}
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
		
		super.onPause();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(IMAGE_TO_DISPLAY_BUNDLE, imageToDisplay);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void setReturnedImage(Bitmap image) {
		imageToDisplay = image;
	}
	
	@Override
	public void onGlobalLayout() {
		if (imageLoader != null) {
			if (imageLoader.getStatus() == Status.PENDING) {
				if (BuildConfig.DEBUG)
					Log.d(TAG, "Layout loaded - load image");
				imageLoader.execute(imageURL);
			} else if (BuildConfig.DEBUG)
				Log.d(TAG, "Image loader already executed");
		} if (BuildConfig.DEBUG)
			Log.d(TAG, "Image loader is null");
	}
	
}
