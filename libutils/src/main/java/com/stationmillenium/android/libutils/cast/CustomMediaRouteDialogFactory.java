package com.stationmillenium.android.libutils.cast;

import android.support.annotation.NonNull;
import android.support.v7.app.MediaRouteControllerDialogFragment;
import android.support.v7.app.MediaRouteDialogFactory;

public class CustomMediaRouteDialogFactory extends MediaRouteDialogFactory {

    private static final CustomMediaRouteDialogFactory sDefault = new CustomMediaRouteDialogFactory();

    @NonNull
    public static MediaRouteDialogFactory getDefault() {
        return sDefault;
    }

    @NonNull
    public MediaRouteControllerDialogFragment onCreateControllerDialogFragment() {
        return new CustomMediaRouteControllerDialogFragment();
    }
}
