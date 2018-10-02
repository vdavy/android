package com.stationmillenium.android.libutils.cast;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.CustomMediaRouteVolumeSlider;
import android.support.v7.app.MediaRouteControllerDialog;

import com.stationmillenium.android.libutils.R;

public class CustomMediaRouteControllerDialog extends MediaRouteControllerDialog {

    public CustomMediaRouteControllerDialog(Context context) {
        super(context);
    }

    public CustomMediaRouteControllerDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CustomMediaRouteVolumeSlider) findViewById(R.id.mr_volume_slider)).setColor(getContext().getResources().getColor(R.color.icons));
    }
}
