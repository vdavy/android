package com.stationmillenium.android.activities;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.fragments.AbstractWebViewFragment;
import com.stationmillenium.android.activities.fragments.AntennaGridWebViewFragement;
import com.stationmillenium.android.activities.fragments.HomeFragment;
import com.stationmillenium.android.activities.fragments.LinksFragment;
import com.stationmillenium.android.activities.fragments.ReplayWebViewFragement;
import com.stationmillenium.android.activities.preferences.AlarmSharedPreferencesActivity;
import com.stationmillenium.android.activities.preferences.SharedPreferencesActivity;
import com.stationmillenium.android.activities.songsearchhistory.SongSearchHistoryActivity;
import com.stationmillenium.android.libutils.AppUtils;
import com.stationmillenium.android.libutils.PiwikTracker;
import com.stationmillenium.android.libutils.intents.LocalIntentsData;

import static com.stationmillenium.android.libutils.PiwikTracker.PiwikPages.SHARE_APP_INVITE;

/**
 * Main activity : drawer manager and home
 *
 * @author vincent
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int APP_INVITE_INTENT = 10;
    private static final int SOCIAL_NETWORKS_INDEX = 0;
    private static final int EMAIL_SMS_INDEX = 1;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            StrictMode.ThreadPolicy.Builder tpBuilder = new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDialog();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
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

            Log.d(TAG, "Display the main activity");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //add home fragment 
        if (savedInstanceState == null) { //if no saved instance - otherwise fragment will be automatically added
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.content_frame, new HomeFragment())
                    .commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //classic menu case
        if (item.getItemId() == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SharedPreferencesActivity.class);
            startActivity(settingsIntent);
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
        new AlertDialog.Builder(this).setTitle(R.string.app_invite_send_by).setItems(R.array.send_by_mode, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case SOCIAL_NETWORKS_INDEX:
                        Log.d(TAG, "Share through social networks");
                        shareAppThroughSocialNetworks();
                        break;
                    case EMAIL_SMS_INDEX:
                        Log.d(TAG, "Share through Email & SMS");
                        sendAppInvitationIntent();
                        break;
                }
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
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == APP_INVITE_INTENT) {
            if (resultCode == RESULT_OK) {
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                Log.d(TAG, "Invitation sent - ids : " + (ids.length >= 1 ? ids[0] : "no id"));
                PiwikTracker.trackScreenView(getApplication(), SHARE_APP_INVITE);
                Toast.makeText(this, R.string.app_invite_thanks, Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "Invitation was cancelled");
            }
        }
    }

    /**
     * Start the {@link PlayerActivity}
     *
     * @param view the {@link View} originating the event
     */
    public void startPlayer(View view) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Launch player");
        Intent playerIntent = new Intent(this, PlayerActivity.class);
        if (!AppUtils.isMediaPlayerServiceRunning(getApplicationContext()))
            playerIntent.putExtra(LocalIntentsData.ALLOW_AUTOSTART.toString(), true);

        startActivity(playerIntent);
    }

    /**
     * Display the {@link AbstractWebViewFragment}
     *
     * @param view the {@link View} originating the event
     */
    public void displayReplayFragment(View view) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Display the replay fragment");
        displayFragment(new ReplayWebViewFragement());
    }

    /**
     * Display the {@link AntennaGridWebViewFragement}
     *
     * @param view the {@link View} originating the event
     */
    public void displayAntennaGridFragment(View view) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Display the antenna grid fragment");
        displayFragment(new AntennaGridWebViewFragement());
    }

    /**
     * Display the {@link LinksFragment}
     *
     * @param view the {@link View} originating the event
     */
    public void displayLinksFragment(View view) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Display the links fragment");
        displayFragment(new LinksFragment());
    }

    /**
     * Display a {@link Fragment} with effects and back stack
     *
     * @param fragmentToDisplay the {@link Fragment} to display
     */
    private void displayFragment(Fragment fragmentToDisplay) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.change_fragment_fadein,
                        R.anim.change_fragment_fadeout,
                        R.anim.change_fragment_fadein,
                        R.anim.change_fragment_fadeout)
                .replace(R.id.content_frame, fragmentToDisplay)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Start the {@link AlarmSharedPreferencesActivity}
     *
     * @param view the originating view
     */
    public void startAlarmPreferencesActivity(View view) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Display the alarm shared preferences activity");
        Intent settingsIntent = new Intent(this, AlarmSharedPreferencesActivity.class);
        startActivity(settingsIntent);
    }

    /**
     * Start the {@link SongSearchHistoryActivity}
     *
     * @param view the originating view
     */
    public void startSongHistorySearchActivity(View view) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "Display the song history search activity");
        Intent settingsIntent = new Intent(this, SongSearchHistoryActivity.class);
        startActivity(settingsIntent);
    }

}
