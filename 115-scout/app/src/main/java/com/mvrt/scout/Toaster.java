package com.mvrt.scout;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Lee Mracek on 10/20/14.
 * Utility for making Toasts
 */
public class Toaster {
    public static final int TOAST_LONG = Toast.LENGTH_LONG;
    public static final int TOAST_SHORT = Toast.LENGTH_SHORT;

    public static void makeToast(String text, int length) {
        Toast.makeText(ScoutBase.getAppContext(), text, length).show();
        Log.i(Constants.Logging.TOAST_LOGCAT.getPath(), "Toasted: " + text);
    }

    public static void burnToast(String text, int length) {
        Toast toast = Toast.makeText(ScoutBase.getAppContext(), text, length);
        TextView textVew = (TextView) toast.getView().findViewById(android.R.id.message);
        textVew.setTextColor(Color.RED);
        toast.show();
        Log.i(Constants.Logging.TOAST_LOGCAT.getPath(), "Burnt: " + text);
    }

    public static void burnToastUI(final Activity context, final String text, final int length) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                burnToast(text, length);
            }
        });
    }

    @SuppressWarnings("unused")
    public static void makeToastUI(final Activity context, final String text, final int length) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                makeToast(text, length);
            }
        });
    }
}
