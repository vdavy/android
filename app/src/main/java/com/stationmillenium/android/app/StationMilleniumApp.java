package com.stationmillenium.android.app;

import android.app.Application;

import com.stationmillenium.android.R;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

/**
 * Main app class - to init ACRA
 * <p/>
 * https://github.com/ACRA/acra/wiki/BasicSetup
 * Created by vincent on 21/09/14.
 */
@ReportsCrashes(formKey = "",
        formUri = "https://millenium.iriscouch.com/acra-millenium/_design/acra-storage/_update/report",
        reportType = HttpSender.Type.JSON,
        httpMethod = HttpSender.Method.PUT,
        formUriBasicAuthLogin = "",
        formUriBasicAuthPassword = ""
)
public class StationMilleniumApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
        ACRA.getErrorReporter().setEnabled(getResources().getBoolean(R.bool.enable_acra));
    }

}

