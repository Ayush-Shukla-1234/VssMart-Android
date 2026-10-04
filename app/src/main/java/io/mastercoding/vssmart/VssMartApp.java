package io.mastercoding.vssmart;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * Main Application class enforcing Light Mode globally across all activities and fragments.
 */
public class VssMartApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Enforce pure Light Mode globally across all device configurations
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
}
