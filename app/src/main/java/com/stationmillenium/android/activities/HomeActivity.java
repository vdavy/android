package com.stationmillenium.android.activities;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.fragments.HomeFragment;
import com.stationmillenium.android.databinding.HomeActivityBinding;
import com.stationmillenium.android.libutils.PiwikTracker;
import com.stationmillenium.android.libutils.drawer.DrawerUtils;
import com.stationmillenium.android.libutils.dtos.FacebookPost;
import com.stationmillenium.android.providers.FacebookFeedLoader;

import java.util.List;

import timber.log.Timber;

import static com.stationmillenium.android.libutils.PiwikTracker.PiwikPages.HOME;
import static com.stationmillenium.android.libutils.PiwikTracker.PiwikPages.SHARE_APP_INVITE;

/**
 * Main activity : drawer manager and home
 *
 * @author vincent
 */
public class HomeActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<FacebookPost>> {

    private static final int LOADER_INDEX = 0;
    private static final int APP_INVITE_INTENT = 10;
    private static final int SOCIAL_NETWORKS_INDEX = 0;
    private static final int EMAIL_SMS_INDEX = 1;

    private DrawerUtils drawerUtils;
    private HomeFragment fragment;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            StrictMode.ThreadPolicy.Builder tpBuilder = new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDialog();

            tpBuilder.penaltyFlashScreen();


            StrictMode.setThreadPolicy(tpBuilder.build());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                        .detectLeakedClosableObjects()
                        .detectLeakedRegistrationObjects()
                        .detectLeakedSqlLiteObjects()
                        .penaltyLog()
                        .build());
            }

            Timber.d("Display the main activity");
        }

        super.onCreate(savedInstanceState);
        HomeActivityBinding binding = DataBindingUtil.setContentView(this, R.layout.home_activity);
        setSupportActionBar(binding.mainToolbar);
        fragment = (HomeFragment) getSupportFragmentManager().findFragmentById(R.id.home_fragment);

        drawerUtils = new DrawerUtils(this, binding.mainDrawerLayout, binding.mainToolbar, R.id.nav_drawer_home);
        getSupportLoaderManager().initLoader(LOADER_INDEX, null, this).forceLoad();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerUtils.onOptionsItemSelected(item)) {
            return true;
        } else if (item.getItemId() == R.id.action_invite) {
            showShareMethodDialog();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Select the share method
     */
    private void showShareMethodDialog() {
        new AlertDialog.Builder(this).setTitle(R.string.app_invite_send_by).setItems(R.array.send_by_mode, (dialog, which) -> {
            switch (which) {
                case SOCIAL_NETWORKS_INDEX:
                    Timber.d("Share through social networks");
                    shareAppThroughSocialNetworks();
                    break;
                case EMAIL_SMS_INDEX:
                    Timber.d("Share through Email & SMS");
                    sendAppInvitationIntent();
                    break;
            }
        }).show();
    }

    /**
     * Share through sms or email
     */
    private void sendAppInvitationIntent() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.app_invite_title))
            .setMessage(getString(R.string.app_invite_message))
            .setCustomImage(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                getPackageName() + "/" +
                R.drawable.app_invite_image))
            .setCallToActionText(getString(R.string.app_invite_cta))
            .build();
        startActivityForResult(intent, APP_INVITE_INTENT);
    }

    private void shareAppThroughSocialNetworks() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.google_play_url));
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.app_invite_share_through)));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Timber.d("onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == APP_INVITE_INTENT) {
            if (resultCode == RESULT_OK) {
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                Timber.d("Invitation sent - ids : %s", (ids.length >= 1 ? ids[0] : "no id"));
                PiwikTracker.trackScreenView(SHARE_APP_INVITE);
                Toast.makeText(this, R.string.app_invite_thanks, Toast.LENGTH_SHORT).show();
            } else {
                Timber.d("Invitation was cancelled");
            }
        }
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

    @Override
    public Loader<List<FacebookPost>> onCreateLoader(int id, Bundle args) {
        fragment.setRefreshing(true);
        return new FacebookFeedLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<List<FacebookPost>> loader, List<FacebookPost> data) {
        fragment.setReplayList(data);
        fragment.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<List<FacebookPost>> loader) {
        fragment.setReplayList(null);
        fragment.setRefreshing(false);
    }

    /**
     * Open social network native app if available or browser instead
     * Called by data binding
     * @param internalURL the internal native app URL
     * @param webURL the web browser URL
     */
    public void openSocialNetwork(String internalURL, String webURL) {
        try {
            //launch app
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(internalURL));
            startActivity(intent);

        } catch (ActivityNotFoundException e) {
            Timber.w(e, "Native app not found");

            //lauch web browser instead
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webURL));
            startActivity(browserIntent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PiwikTracker.trackScreenView(HOME);
    }

    /**
     * Open facebook post URL - called from databinding
     * @param item the clicked facebook post
     */
    public void openFacebook(FacebookPost item) {
        Timber.d("List item clicked - open URL");
        openSocialNetwork(getString(R.string.facebook_internal_url_prefix, item.getUrl()), item.getUrl());
    }

    public void onRefresh() {
        Timber.d("Data refresh requested");
        getSupportLoaderManager().restartLoader(LOADER_INDEX, null, this).forceLoad();
    }
}
