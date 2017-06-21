package com.stationmillenium.android.cast;

import android.content.Context;

import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;
import com.stationmillenium.android.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Cast provider
 * https://developers.google.com/cast/docs/android_sender_integrate#initialize_the_cast_context
 * Created by vincent on 21/06/17.
 */

public class CastOptionsProvider implements OptionsProvider {

    public static final String CUSTOM_NAMESPACE = "urn:x-cast:millenium_cast_namespace";

    @Override
    public CastOptions getCastOptions(Context appContext) {
        List<String> supportedNamespaces = new ArrayList<>();
        supportedNamespaces.add(CUSTOM_NAMESPACE);
        CastOptions castOptions = new CastOptions.Builder()
                .setReceiverApplicationId(appContext.getString(R.string.app_id))
                .setSupportedNamespaces(supportedNamespaces)
                .build();
        return castOptions;
    }

    @Override
    public List<SessionProvider> getAdditionalSessionProviders(Context context) {
        return null;
    }

}
