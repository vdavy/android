/**
 *
 */
package com.stationmillenium.android.activities.fragments;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.app.StationMilleniumApp;
import com.stationmillenium.android.utils.AppUtils;

/**
 * Replay webview fragment
 *
 * @author vincent
 */
public class ReplayWebViewFragment extends Fragment {

    private final static String TAG = "ReplayWebViewFragment";

    private boolean resetWebview;
    private WebView replayWebView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); //set fragment has menu : http://developer.android.com/guide/components/fragments.html#ActionBar
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        resetWebview = (savedInstanceState == null);
        return inflater.inflate(R.layout.replay_webview_fragment, container, false);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onResume() {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Resuming replay webview fragment");
        super.onResume();

        if (resetWebview) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "Init new webview");

            //setup web view
            replayWebView = (WebView) getView().findViewById(R.id.replay_webview);
            WebSettings webSettings = replayWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setBuiltInZoomControls(true);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setUseWideViewPort(true);

            //add webview handlers
            final Activity activity = getActivity();
            final ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.replay_webview_progressbar);

            //set the progress bar
            replayWebView.setWebChromeClient(new WebChromeClient() {
                public void onProgressChanged(WebView view, int progress) {
                    if (BuildConfig.DEBUG)
                        Log.d(TAG, "Page load progress : " + progress);
                    progressBar.setProgress(progress);
                    progressBar.setVisibility((progress == 100) ? View.GONE : View.VISIBLE);

                }
            });

            //set up error messages display
            replayWebView.setWebViewClient(new WebViewClient() {
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    Log.e(TAG, "Error while loading webview : #" + errorCode + " : " + description);
                    Toast.makeText(activity, getString(R.string.webview_error, description), Toast.LENGTH_SHORT).show();
                }
            });

            //load the page
            replayWebView.loadUrl(getString(R.string.replay_url));

        } else if ((AppUtils.isAPILevel11Available()) && (replayWebView != null)) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "Resume webview");
            replayWebView.onResume();
        }

        //set title
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.replay_activity_title);
        ((StationMilleniumApp) getActivity().getApplication()).getPiwikTracker().trackScreenView("/main/replay");
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onPause() {
        //reset activity title
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Pausing replay webview fragment");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);

        if ((AppUtils.isAPILevel11Available()) && (replayWebView != null)) {
            replayWebView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.replay_webview, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_reload_page) { //reload web view
            if (BuildConfig.DEBUG)
                Log.d(TAG, "Reload the web view");

            if (replayWebView != null)
                replayWebView.reload();
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

}
