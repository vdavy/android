package com.stationmillenium.android.libutils.cast;

import android.content.Context;

import androidx.mediarouter.app.MediaRouteActionProvider;

public class CustomMediaRouteActionProvider extends MediaRouteActionProvider {

    /**
     * Creates the action provider.
     *
     * @param context The context.
     */
    public CustomMediaRouteActionProvider(Context context) {
        super(context);
        setDialogFactory(new CustomMediaRouteDialogFactory());
    }

}
