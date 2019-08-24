package com.stationmillenium.android.libutils.cast;

import androidx.annotation.NonNull;
import androidx.mediarouter.app.MediaRouteControllerDialogFragment;
import androidx.mediarouter.app.MediaRouteDialogFactory;

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
