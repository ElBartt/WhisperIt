package com.alexandra.arnauld.whisperit;

/**
 * Created by arnau on 17/03/2017.
 */

import android.util.Log;

public class AppLog {
    private static final String APP_TAG = "Whisper It.";

    public static int logString(String message){
        return Log.i(APP_TAG,message);
    }
}