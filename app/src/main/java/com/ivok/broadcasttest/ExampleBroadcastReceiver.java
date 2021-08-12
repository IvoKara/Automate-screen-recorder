package com.ivok.broadcasttest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ExampleBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent serviceIntent = new Intent(context, ExampleService.class);

        if("com.ivok.START_SERVICE".equals(intent.getAction())) {

            Intent i = new Intent(context, QueryActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }

        if("com.ivok.STOP_SERVICE".equals(intent.getAction())) {
            context.stopService(serviceIntent);
        }
    }
}
