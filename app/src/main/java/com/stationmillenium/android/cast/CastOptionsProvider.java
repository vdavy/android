package com.stationmillenium.android.cast;

import android.content.Context;

import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;
import com.google.android.gms.cast.framework.media.CastMediaOptions;
import com.google.android.gms.cast.framework.media.MediaIntentReceiver;
import com.google.android.gms.cast.framework.media.NotificationOptions;
import com.stationmillenium.android.R;
import com.stationmillenium.android.activities.CastExpandedControlsActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Cast provider
 * https://developers.google.com/cast/docs/android_sender_integrate#initialize_the_cast_context
 * Created by vincent on 21/06/17.
 */
public class CastOptionsProvider implements OptionsProvider {

    @Override
    public CastOptions getCastOptions(Context appContext) {
        List<String> buttonActions = new ArrayList<>();
        buttonActions.add(MediaIntentReceiver.ACTION_TOGGLE_PLAYBACK);
        buttonActions.add(MediaIntentReceiver.ACTION_DISCONNECT);
        int[] compatButtonActionsIndices = new int[]{ 0, 1 };
        NotificationOptions notificationOptions = new NotificationOptions.Builder()
                .setActions(buttonActions, compatButtonActionsIndices)
                .setSmallIconDrawableResId(R.drawable.ic_notif_icon)
                .setTargetActivityClassName(CastExpandedControlsActivity.class.getName())
                .build();
        CastMediaOptions mediaOptions = new CastMediaOptions.Builder()
                .setNotificationOptions(notificationOptions)
                .setExpandedControllerActivityClassName(CastExpandedControlsActivity.class.getName())
                .build();
        CastOptions castOptions = new CastOptions.Builder()
                .setReceiverApplicationId(appContext.getString(R.string.app_id))
                .setCastMediaOptions(mediaOptions)
                .build();
        return castOptions;
    }

    @Override
    public List<SessionProvider> getAdditionalSessionProviders(Context context) {
        return null;
    }

}
