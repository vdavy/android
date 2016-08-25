package com.stationmillenium.android.utils.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.utils.network.NetworkUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

/**
 * Image loader based on {@link AsyncTask}
 *
 * @author vincent
 */
public class ImageLoader extends AsyncTask<String, Void, Bitmap> {

    private static final String TAG = "ImageLoader";

    //references
    private WeakReference<ImageView> imageViewRef;
    private WeakReference<ProgressBar> progressBarRef;
    private String imageFileName;
    private WeakReference<Context> contextRef;
    private boolean loadImageFromDiskIfExists;
    private int imageWidth;
    private int imageHeight;

    /**
     * Create a new {@link ImageLoader}
     *
     * @param imageView                 the {@link ImageView} to display final image
     * @param progressBar               the {@link ProgressBar} to wait during image download
     * @param imageFileName             the file name of the local image file
     * @param context                   the {@link Context}
     * @param loadImageFromDiskIfExists <code>true</code> if prefer loading the image from disk if exists, <code>false</code> if reload required
     */
    public ImageLoader(ImageView imageView, ProgressBar progressBar, String imageFileName, Context context, boolean loadImageFromDiskIfExists) {
        imageViewRef = new WeakReference<>(imageView);
        progressBarRef = new WeakReference<>(progressBar);
        this.imageFileName = imageFileName;
        contextRef = new WeakReference<>(context);
        this.loadImageFromDiskIfExists = loadImageFromDiskIfExists;
    }

    @Override
    protected void onPreExecute() {
        imageWidth = imageViewRef.get().getWidth();
        imageHeight = imageViewRef.get().getHeight();
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Load image...");

        //cache image on disk
        if ((contextRef.get() != null) && (!cancelRequested())) { //if we have a context
            File imageFile = new File(contextRef.get().getCacheDir(), imageFileName);

            if ((!loadImageFromDiskIfExists) || ((loadImageFromDiskIfExists) && (!imageFile.exists()))) { //should we load the image from the internet ?
                if (BuildConfig.DEBUG)
                    Log.d(TAG, "Image load required");
                //connect to image source URL
                InputStream imageIs = NetworkUtils.connectToURL(params[0], null,
                        contextRef.get().getResources().getString(R.string.player_connection_request_method),
                        null,
                        Integer.parseInt(contextRef.get().getResources().getString(R.string.player_connection_connect_timeout)),
                        Integer.parseInt(contextRef.get().getResources().getString(R.string.player_connection_read_timeout)));

                if ((imageIs != null) && (!cancelRequested())) { //we got input stream for image
                    if (BuildConfig.DEBUG)
                        Log.d(TAG, "Write image to disk");
                    writeImageToDisk(imageFile, imageIs);
                } else {
                    Log.e(TAG, "Image input stream is null !");
                    return null;
                }

            } else if (BuildConfig.DEBUG) //image file already exists
                Log.d(TAG, "Image file already exists - no need to load it");


            //load the image for display
            if ((imageFile.exists()) && (!cancelRequested())) {
                if ((imageViewRef.get() != null) && (!cancelRequested())) {
                    //load image size
                    Options bfo = new Options();
                    bfo.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(imageFile.getAbsolutePath(), bfo);
                    int inSampleSize = calculateInSampleSize(bfo, imageWidth, imageHeight);

                    //process new image
                    bfo = new Options();
                    bfo.inSampleSize = inSampleSize;
                    return BitmapFactory.decodeFile(imageFile.getAbsolutePath(), bfo);

                } else {
                    Log.w(TAG, "Image view ref was null !");
                    return null;
                }

            } else {
                Log.e(TAG, "Image file was not found !");
                return null;
            }

        } else {
            Log.e(TAG, "Context is null !");
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        if ((result != null) && (imageViewRef.get() != null) && (progressBarRef.get() != null)) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "Set image to view");
            progressBarRef.get().setVisibility(View.GONE);
            imageViewRef.get().setImageBitmap(result);

        } else {
            Log.w(TAG, "Error while setting image to view - image : " + result + " - image view : " + imageViewRef.get());
        }
    }

    /**
     * Write the image to disk
     *
     * @param imageFile the image output {@link File}
     * @param imageIs   the image {@link InputStream}
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
     * Compute image sample size
     *
     * @param options   the image {@link Options} for image size
     * @param reqWidth  the requested width
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

    /**
     * Check if cancellation has been requested
     *
     * @return <code>true</code> if requested, <code>false</code> otherwise
     */
    private boolean cancelRequested() {
        boolean cancelled = isCancelled();
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Cancel requested : " + cancelled);

        return cancelled;
    }
}
