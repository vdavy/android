package com.stationmillenium.android.libutils.cast;

import android.content.Context;
import android.os.Bundle;

import androidx.mediarouter.app.MediaRouteControllerDialog;
import androidx.mediarouter.app.MediaRouteControllerDialogFragment;

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
        return new MediaRouteControllerDialog(context);
    }
}
