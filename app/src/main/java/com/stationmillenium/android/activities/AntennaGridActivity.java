/**
 *
 */
package com.stationmillenium.android.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.snackbar.Snackbar;
import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.fragments.AntennaGridWebViewFragement;
import com.stationmillenium.android.databinding.AntennaGridActivityBinding;
import com.stationmillenium.android.libutils.PiwikTracker;
import com.stationmillenium.android.libutils.drawer.DrawerUtils;

import timber.log.Timber;

import static com.stationmillenium.android.libutils.PiwikTracker.PiwikPages.ANTENNA_GRID;

/**
 * Activity to display grid webview
 *
 * @author vincent
 */
public class AntennaGridActivity extends AppCompatActivity {

    private boolean resetWebview;

    private DrawerUtils drawerUtils;
    private AntennaGridWebViewFragement fragment;
    private AntennaGridActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //init view
        super.onCreate(savedInstanceState);
        resetWebview = (savedInstanceState == null);
        Timber.d("Load main preferences");

        binding = AntennaGridActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.antennaGridToolbar);
        fragment = (AntennaGridWebViewFragement) getSupportFragmentManager().findFragmentById(R.id.antenna_grid_fragment);

        drawerUtils = new DrawerUtils(this, binding.antennaGridDrawerLayout, binding.antennaGridToolbar, R.id.nav_drawer_antenna);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return drawerUtils.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerUtils.onPostCreate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerUtils.onConfigurationChanged(newConfig);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onResume() {
        Timber.d("Resuming webview");
        super.onResume();

        if (resetWebview) {
            Timber.d("Init new webview");

            //setup web view
            WebSettings webSettings = fragment.getWebView().getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setBuiltInZoomControls(true);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setUseWideViewPort(true);

            //set the progress bar
            fragment.getWebView().setWebChromeClient(new WebChromeClient() {
                public void onProgressChanged(WebView view, int progress) {
                    Timber.d("Page load progress : %s", progress);
                    fragment.setProgress(progress);
                }
            });

            //set up error messages display
            fragment.getWebView().setWebViewClient(new WebViewClient() {
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    Timber.e("Error while loading webview : #" + errorCode + " : " + description);
                    Snackbar.make(binding.antennaGridCoordinatorLayout, getString(R.string.webview_error, description), Snackbar.LENGTH_SHORT).show();
                }
            });

            //load the page
            fragment.getWebView().loadUrl(getString(R.string.antenna_grid_page_url));
            PiwikTracker.trackScreenView(ANTENNA_GRID);

        } else if (fragment.getWebView() != null) {
            Timber.d("Resume webview");
            fragment.getWebView().onResume();
        }
    }


    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onPause() {
        //reset activity title
        Timber.d("Pausing webview");
        if (fragment.getWebView() != null) {
            fragment.getWebView().onPause();
        }
        super.onPause();
    }

}
