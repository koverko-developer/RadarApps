package com.google.android.gms.samples.vision.ocrreader.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NFBroadcast extends BroadcastReceiver {

    static final String TAG = NFBroadcast.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "----------   start broadcast ----------");
        context.startService(new Intent(context, NotificationServices.class));

    }


}
