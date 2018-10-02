package com.stationmillenium.android.libutils.cast;

import android.content.Context;
import android.support.v7.app.MediaRouteActionProvider;

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
