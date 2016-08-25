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
import com.stationmillenium.android.utils.AppUtils;

/**
 * Abstract webview fragment
 *
 * @author vincent
 */
public abstract class AbstractWebViewFragment extends Fragment {

    private final static String TAG = "AbstractWebViewFragment";

    protected int title;
    protected int url;

    private boolean resetWebview;
    private WebView webView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); //set fragment has menu : http://developer.android.com/guide/components/fragments.html#ActionBar
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        resetWebview = (savedInstanceState == null);
        return inflater.inflate(R.layout.webview_fragment, container, false);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onResume() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Resuming webview fragment");
        }
        super.onResume();

        if (resetWebview) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Init new webview");
            }

            //setup web view
            webView = (WebView) getView().findViewById(R.id.webview);
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setBuiltInZoomControls(true);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setUseWideViewPort(true);

            //add webview handlers
            final Activity activity = getActivity();
            final ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.webview_progressbar);

            //set the progress bar
            webView.setWebChromeClient(new WebChromeClient() {
                public void onProgressChanged(WebView view, int progress) {
                    if (BuildConfig.DEBUG)
                        Log.d(TAG, "Page load progress : " + progress);
                    progressBar.setProgress(progress);
                    progressBar.setVisibility((progress == 100) ? View.GONE : View.VISIBLE);

                }
            });

            //set up error messages display
            webView.setWebViewClient(new WebViewClient() {
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    Log.e(TAG, "Error while loading webview : #" + errorCode + " : " + description);
                    Toast.makeText(activity, getString(R.string.webview_error, description), Toast.LENGTH_SHORT).show();
                }
            });

            //load the page
            webView.loadUrl(getString(url));

        } else if ((AppUtils.isAPILevel11Available()) && (webView != null)) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Resume webview");
            }
            webView.onResume();
        }

        //set title
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(title);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onPause() {
        //reset activity title
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Pausing webview fragment");
        }
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);

        if ((AppUtils.isAPILevel11Available()) && (webView != null)) {
            webView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.webview_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_reload_page) { //reload web view
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Reload the web view");
            }
            if (webView != null) {
                webView.reload();
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

}
