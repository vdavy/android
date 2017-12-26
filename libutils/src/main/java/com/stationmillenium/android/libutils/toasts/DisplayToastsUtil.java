package com.stationmillenium.android.libutils.toasts;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.widget.Toast;

/**
 * Display toasts in a correct thread
 * Created by vincent on 24/10/17.
 */
public class DisplayToastsUtil {

    private Handler handler;
    private Context context;

    public DisplayToastsUtil(@NonNull Context context) {
        this.context = context;
        handler = new Handler(Looper.getMainLooper()); //create handler to display toasts
    }

    /**
     * Display a toast using the handler
     *
     * @param toastText the text
     */
    public void displayToast(@NonNull final String toastText) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context.getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Display a toast using the handler
     *
     * @param toastRes the text res
     */
    public void displayToast(@StringRes final int toastRes) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context.getApplicationContext(), toastRes, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
