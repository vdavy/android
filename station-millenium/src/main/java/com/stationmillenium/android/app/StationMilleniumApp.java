package com.stationmillenium.android.app;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.stationmillenium.android.R;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.acra.config.ACRAConfiguration;
import org.acra.config.ACRAConfigurationException;
import org.acra.config.ConfigurationBuilder;
import org.acra.security.KeyStoreFactory;
import org.acra.sender.HttpSender;
import org.piwik.sdk.Piwik;
import org.piwik.sdk.Tracker;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * Main app class - to init ACRA
 * <p/>
 * https://github.com/ACRA/acra/wiki/BasicSetup
 * Created by vincent on 21/09/14.
 */
@ReportsCrashes(formUri = "https://www.station-millenium.com:6984/acra-millenium/_design/acra-storage/_update/report",
        reportType = HttpSender.Type.JSON,
        httpMethod = HttpSender.Method.PUT,
        formUriBasicAuthLogin = "millenium",
        formUriBasicAuthPassword = "rdserqsdsds"
)
public class StationMilleniumApp extends Application {

    private static final String TAG = "StationMilleniumApp";
    private static final String KEYSTORE_NAME = "millenium.store";
    private static final String KEYSTORE_PASS = "MilleniumAcralyzerSSL";

    private Tracker piwikAppTracker;
    private Tracker piwikStreamTracker;

    public static class AppKeySoreFactory implements KeyStoreFactory {

        @Nullable
        @Override
        public KeyStore create(@NonNull Context context) {
            KeyStore keyStore = null;
            try {
                InputStream keystoreInputStream = context.getAssets().open(KEYSTORE_NAME);
                keyStore = KeyStore.getInstance("BKS");
                keyStore.load(keystoreInputStream, KEYSTORE_PASS.toCharArray());
                keystoreInputStream.close();
            } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
                Log.w(TAG, "Error opening keystore", e);
            }
            return keyStore;
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        initACRA();

        piwikAppTracker = initPiwikAppTracker();
        piwikStreamTracker = initPiwikStreamTracker();
    }

    private void initACRA() {
        try {
            // The following line triggers the initialization of ACRA
            ConfigurationBuilder configurationBuilder = new ConfigurationBuilder(this);
            configurationBuilder.setKeyStoreFactoryClass(AppKeySoreFactory.class);
            ACRAConfiguration acraConfiguration = configurationBuilder.build();
            ACRA.init(this, acraConfiguration);
            ACRA.getErrorReporter().setEnabled(getResources().getBoolean(R.bool.enable_acra));
        } catch (ACRAConfigurationException e) {
            Log.e(TAG, "Error with ACRA initialization", e);
        }
    }

    private Tracker initPiwikAppTracker() {
        try {
            return Piwik.getInstance(this).newTracker(getString(R.string.piwik_url), getResources().getInteger(R.integer.piwik_app_site_id));
        } catch (MalformedURLException e) {
            Log.w(TAG, "Error while piwik app tracker init", e);
            return null;
        }
    }

    private Tracker initPiwikStreamTracker() {
        try {
            return Piwik.getInstance(this).newTracker(getString(R.string.piwik_url), getResources().getInteger(R.integer.piwik_stream_site_id));
        } catch (MalformedURLException e) {
            Log.w(TAG, "Error while piwik stream tracker init", e);
            return null;
        }
    }

    public Tracker getPiwikAppTracker() {
        return piwikAppTracker;
    }

    public Tracker getPiwikStreamTracker() {
        return piwikStreamTracker;
    }
}

