/**
 * 
 */
package com.stationmillenium.android.activities.fragments;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.MainActivity;
import com.stationmillenium.android.utils.views.ImageLoader;
import com.stationmillenium.android.utils.views.ImageLoader.ReturnImage;

/**
 * Antenna grid fragment
 * @author vincent
 *
 */
public class AntennaGridFragment extends Fragment implements ReturnImage {

	//static parts
	private final static String TAG = "AntennaGridFragment";
	private final static String IMAGE_TO_DISPLAY_BUNDLE = "ANTENNA_GRID_IMAGE_TO_DISPLAY_BUNDLE"; 
	private static final String ANTENNA_GRID_IMAGE_FILE_NAME = "Antenna-grid.jpeg";
	
	//instance vars
	private ImageLoader imageLoader;
	private Bitmap imageToDisplay;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if ((savedInstanceState != null) && (savedInstanceState.getParcelable(IMAGE_TO_DISPLAY_BUNDLE) != null)) //get saved image if any available
			imageToDisplay = savedInstanceState.getParcelable(IMAGE_TO_DISPLAY_BUNDLE);
		
		return inflater.inflate(R.layout.image_display_layout, container, false);
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public void onResume() {
		super.onResume();
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Resuming antenna grid fragment");

		//load image
		ImageView imageView = (ImageView) getView().findViewById(R.id.image_display_imageview);
		ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.image_display_progressbar);
		if (imageToDisplay == null) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Launch antenna grid image loading...");
			imageLoader = new ImageLoader(imageView, progressBar, ANTENNA_GRID_IMAGE_FILE_NAME, getActivity(), this, false);
			imageLoader.execute(getString(R.string.antenna_grid_image_url));
		
		} else {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Image already loaded - display it...");
			imageView.setImageBitmap(imageToDisplay);
			progressBar.setVisibility(View.GONE);
		}
		
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
		
		((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name); //reset activity title
		
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
	
}
