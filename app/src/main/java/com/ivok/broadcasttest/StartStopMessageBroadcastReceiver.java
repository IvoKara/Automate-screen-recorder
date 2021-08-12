package com.ivok.broadcasttest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartStopMessageBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent serviceIntent = new Intent(context, RecordingService.class);

        //received from 'Automate' app that is used to watch if the target app (Facebook)
        //in in foreground (i.e. if it is launched)
        if("com.ivok.START_SERVICE".equals(intent.getAction())) {

            //start hidden Activity in order to use startActivity for result
            Intent i = new Intent(context, RequestRecordingActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }

        //received from 'Automate' when target app is closed
        if("com.ivok.STOP_SERVICE".equals(intent.getAction())) {
            //stop the service
            context.stopService(serviceIntent);
        }
    }
}
