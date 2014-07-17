/**
 * 
 */
package com.stationmillenium.android.activities.fragments;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.MainActivity;
import com.stationmillenium.android.utils.Utils;
import com.stationmillenium.android.utils.views.ImageLoader;

/**
 * Antenna grid fragment
 * Implements {@link OnGlobalLayoutListener} to display image when the layout is full loaded (due to using {@link AsyncTask} in {@link ImageLoader})
 * @author vincent
 *
 */
public class AntennaGridFragment extends Fragment implements OnGlobalLayoutListener {

	//static parts
	private final static String TAG = "AntennaGridFragment";
	private static final String ANTENNA_GRID_IMAGE_FILE_NAME = "Antenna-grid.jpeg";
	
	//instance vars
	private ImageLoader imageLoader;
	
	//widgets
	private ImageView imageView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.image_display_layout, container, false);
		view.getViewTreeObserver().addOnGlobalLayoutListener(this);
		return view;
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public void onResume() {
		super.onResume();
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Resuming antenna grid fragment");
		
		//load image
		imageView = (ImageView) getView().findViewById(R.id.image_display_imageview);
		ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.image_display_progressbar);
		
		//start loader
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Launch antenna grid image loading...");
		imageLoader = new ImageLoader(imageView, progressBar, ANTENNA_GRID_IMAGE_FILE_NAME, getActivity(), true);
		
		//set title and activity full screen
		((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.antenna_grid_activity_title);
	}

	@Override
	public void onPause() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Pause antenna grid fragment");
		if (imageLoader != null) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Cancel the async image loader");
			imageLoader.cancel(true); //cancel loading if running
		}
		
		//reset activity title
		((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name); 
		
		//recycle image when pausing
		Utils.recycleBitmapFromImageView(imageView);
		
		super.onPause();
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
				imageLoader.execute(getString(R.string.antenna_grid_image_url));
			} else if (BuildConfig.DEBUG)
				Log.d(TAG, "Image loader already executed");
		} if (BuildConfig.DEBUG)
			Log.d(TAG, "Image loader is null");
	}
		
}
