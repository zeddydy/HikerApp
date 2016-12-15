package com.hiker.app.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hiker.app.activities.MainActivity;
import com.hiker.app.utils.Constants;

public class MyBroadcastReceiver extends BroadcastReceiver {
    private MainActivity mainActivity;

    public MyBroadcastReceiver() {}

    public MyBroadcastReceiver(MainActivity ma) {
        mainActivity = ma;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getBooleanExtra(Constants.EXTRA_POINTS_UPDATED, false)) {
            //Update des fragments
            if (mainActivity  != null) mainActivity.updateMap();
        } else if (intent.getBooleanExtra(Constants.EXTRA_TRACK_UPDATED, false)) {
            //Update des fragments
            if (mainActivity  != null) mainActivity.updateFragments();
        } else if (intent.getBooleanExtra(Constants.EXTRA_SESSION_START, false)) {
            //Update des fragments
            if (mainActivity  != null) mainActivity.onSessionStart();
        } else if (intent.getBooleanExtra(Constants.EXTRA_SESSION_STOP, false)) {
            //Update des fragments
            if (mainActivity  != null) mainActivity.onSessionStop();
        }
    }
}
