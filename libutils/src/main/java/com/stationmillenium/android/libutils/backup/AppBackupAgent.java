/**
 *
 */
package com.stationmillenium.android.libutils.backup;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

import timber.log.Timber;

/**
 * Class to backup preferences automatically
 *
 * @author vincent
 */
public class AppBackupAgent extends BackupAgentHelper {

    private static final String SHARED_PREFERECES_KEY = "com.stationmillenium.android_preferences";

    @Override
    public void onCreate() {
        Timber.d("Backup app data...");
        SharedPreferencesBackupHelper spbm = new SharedPreferencesBackupHelper(this, SHARED_PREFERECES_KEY);
        addHelper(SHARED_PREFERECES_KEY, spbm);
    }

}
