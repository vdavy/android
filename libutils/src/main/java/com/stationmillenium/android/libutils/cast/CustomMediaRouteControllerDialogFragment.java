package com.stationmillenium.android.libutils.cast;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.MediaRouteControllerDialog;
import android.support.v7.app.MediaRouteControllerDialogFragment;

public class CustomMediaRouteControllerDialogFragment extends MediaRouteControllerDialogFragment {

    /**
     * Called when the controller dialog is being created.
     * <p>
     * Subclasses may override this method to customize the dialog.
     * </p>
     */
    @Override
    public MediaRouteControllerDialog onCreateControllerDialog(
            Context context, Bundle savedInstanceState) {
        return new CustomMediaRouteControllerDialog(context);
    }
}
