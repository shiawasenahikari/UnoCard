////////////////////////////////////////////////////////////////////////////////
//
// Uno Card Game
// Author: Hikari Toyama
// Compile Environment: Android Studio 3.4, with Android SDK 28
//
////////////////////////////////////////////////////////////////////////////////

package com.github.hikari_toyama.unocard;

import android.app.Application;

import org.opencv.android.OpenCVLoader;

/**
 * Custom application context.
 * Initialize OpenCV library when application launched.
 */
public class App extends Application {
    /**
     * Triggered when application launched.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        if (!OpenCVLoader.initDebug()) {
            throw new RuntimeException("OpenCV Initialization Failed");
        } // if (!OpenCVLoader.initDebug())
    } // onCreate()
} // App Class

// E.O.F