/**
 * 
 */
package com.stationmillenium.android.activities.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.stationmillenium.android.R;

/**
 * Replay webview fragment
 * @author vincent
 *
 */
public class ReplayWebViewFragment extends Fragment {

	private final static String TAG = "ReplayWebViewFragment";
	
	private boolean resetWebview;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		resetWebview = (savedInstanceState == null);
		return inflater.inflate(R.layout.replay_webview_fragment, container, false);
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onResume() {
		Log.d(TAG, "Resuming replay webview fragment");
		super.onResume();

		if (resetWebview) {
			Log.d(TAG, "Init new webview");
			
			//setup web view
			WebView replayWebView = (WebView) getView().findViewById(R.id.replay_webview);
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
					Log.d(TAG, "Page load progress : " + progress);
					progressBar.setProgress(progress);
					progressBar.setVisibility((progress == 100) ?  View.GONE : View.VISIBLE);
						
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
			
		} else {
			Log.d(TAG, "Resume webview");
			((WebView) getView().findViewById(R.id.replay_webview)).onResume();
		}
			
		//set title
		((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(R.string.replay_activity_title);
	}
	
	@Override
	public void onPause() {
		//reset activity title
		Log.d(TAG, "Pausing replay webview fragment");
		((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);
		((WebView) getView().findViewById(R.id.replay_webview)).onPause();
		super.onPause();
	}
	
}
