/**
 * 
 */
package com.stationmillenium.android.activities.fragments;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.AsyncTask;
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

/**
 * Antenna grid fragment
 * @author vincent
 *
 */
public class AntennaGridFragment extends Fragment {

	/**
	 * Image loader based on {@link AsyncTask}
	 * @author vincent
	 *
	 */
	private class ImageLoader extends AsyncTask<String, Void, Bitmap> {

		private static final String TAG = "ImageLoader";
		private static final String ANTENNA_GRID_IMAGE_FILE_NAME = "Antenna-grid.jpeg";

		//references
		private WeakReference<ImageView> imageViewRef;
		private WeakReference<ProgressBar> progressBarRef;
		
		/**
		 * Create a new {@link ImageLoader} 
		 * @param imageView the {@link ImageView} to display final image
		 * @param progressBar the {@link ProgressBar} to wait during image download
		 */
		public ImageLoader(ImageView imageView, ProgressBar progressBar) {
			imageViewRef = new WeakReference<ImageView>(imageView);
			progressBarRef = new WeakReference<ProgressBar>(progressBar); 
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Load image...");

			//cache image on disk
			File imageFile = new File(getActivity().getCacheDir(), ANTENNA_GRID_IMAGE_FILE_NAME);
			InputStream imageIs = connectToURLSource(params[0]);
			if (imageIs != null) {
				writeImageToDisk(imageFile, imageIs);
	
				if (imageViewRef.get() != null) {
					//load image size
					Options bfo = new Options();
					bfo.inJustDecodeBounds = true;
					BitmapFactory.decodeFile(imageFile.getAbsolutePath(), bfo);
					int inSampleSize = calculateInSampleSize(bfo, imageViewRef.get().getWidth(), imageViewRef.get().getHeight());
					
					//process new image
					bfo = new Options();
					bfo.inSampleSize = inSampleSize;
					return BitmapFactory.decodeFile(imageFile.getAbsolutePath(), bfo);
	
				} else {
					Log.w(TAG, "Image view ref was null !");
					return null;
				}
			
			} else {
				Log.e(TAG, "Image input stream is null !");
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(Bitmap result) {
			if ((result != null) && (imageViewRef.get() != null) && (progressBarRef.get() != null)) {
				if (BuildConfig.DEBUG)
					Log.d(TAG, "Set antenna grid image");
				progressBarRef.get().setVisibility(View.GONE);
				imageViewRef.get().setImageBitmap(result);
				imageToDisplay = result;
			} else {
				Log.w(TAG, "Error while setting antenna image - image : " + result + " - image view : " + imageViewRef.get());
			}
		}

		/**
		 * Write the image to disk
		 * @param imageFile the image output {@link File}
		 * @param imageIs the image {@link InputStream}
		 */
		private void writeImageToDisk(File imageFile, InputStream imageIs) {
			//write input stream data to file	
			OutputStream imageOs = null;
			try {
				imageOs = new BufferedOutputStream(new FileOutputStream(imageFile));
				int bufferSize = 1024;
				byte[] buffer = new byte[bufferSize];
				int len = 0;
				while ((len = imageIs.read(buffer)) != -1) {
					imageOs.write(buffer, 0, len);
				}

			} catch (FileNotFoundException e) { //handle errors
				Log.e(TAG, "Error while creating cache image file", e);
			} catch (IOException e) {
				Log.e(TAG, "IO error with cache image file", e);
			} finally { //close streams
				try {
					if (imageIs != null)
						imageIs.close();
					if (imageOs != null)
						imageOs.close();
				} catch (IOException e) {
					Log.w(TAG, "Errors during closing of image streams", e);
				}
			}
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Image written to " + imageFile);
		}


		/**
		 * Connect to the URL source	
		 * @param urlText the URL to connect as text
		 * @return the {@link InputStream} of the connection
		 */
		private InputStream connectToURLSource(String urlText) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Connect to server to get image");
			try {
				//set up connection
				URL url = new URL(urlText);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setConnectTimeout(Integer.parseInt(getResources().getString(R.string.player_connection_connect_timeout)));
				connection.setReadTimeout(Integer.parseInt(getResources().getString(R.string.player_connection_read_timeout)));
				connection.setRequestMethod(getResources().getString(R.string.player_connection_request_method));
				if (BuildConfig.DEBUG)
					Log.d(TAG, "Connection to use : " + connection);

				//connect
				connection.connect();
				if (BuildConfig.DEBUG)
					Log.d(TAG, "Response code : " + connection.getResponseCode());
				return connection.getInputStream();

			} catch (MalformedURLException e) {
				Log.e(TAG, "Error with URL", e);
				return null;
			} catch (IOException e) {
				Log.e(TAG, "Error while getting image data", e);
				return null;
			}
		}

		/**
		 * Compute image sample size
		 * @param options the image {@link Options} for image size
		 * @param reqWidth the requested width
		 * @param reqHeight the requested height
		 * @return the sample size
		 */
		private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
			// Raw height and width of image
			final int height = options.outHeight;
			final int width = options.outWidth;
			int inSampleSize = 1;

			if (height > reqHeight || width > reqWidth) {

				// Calculate ratios of height and width to requested height and width
				final int heightRatio = Math.round((float) height / (float) reqHeight);
				final int widthRatio = Math.round((float) width / (float) reqWidth);

				// Choose the smallest ratio as inSampleSize value, this will guarantee
				// a final image with both dimensions larger than or equal to the
				// requested height and width.
				inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
			}

			return inSampleSize;
		}

	}

	//static parts
	private final static String TAG = "AntennaGridFragment";
	private final static String IMAGE_TO_DISPLAY_BUNDLE = "IMAGE_TO_DISPLAY_BUNDLE"; 
	
	//instance vars
	private ImageLoader imageLoader;
	private Bitmap imageToDisplay;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if ((savedInstanceState != null) && (savedInstanceState.getParcelable(IMAGE_TO_DISPLAY_BUNDLE) != null)) //get saved image if any available
			imageToDisplay = savedInstanceState.getParcelable(IMAGE_TO_DISPLAY_BUNDLE);
		
		return inflater.inflate(R.layout.antenna_grid_fragment, container, false);
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public void onResume() {
		super.onResume();
		if (BuildConfig.DEBUG)
			Log.d(TAG, "Resuming antenna grid fragment");

		//load image
		ImageView imageView = (ImageView) getView().findViewById(R.id.antenna_grid_imageview);
		ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.antenna_grid_progressbar);
		if (imageToDisplay == null) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Launch antenna grid image loading...");
			imageLoader = new ImageLoader(imageView, progressBar);
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
	
}
