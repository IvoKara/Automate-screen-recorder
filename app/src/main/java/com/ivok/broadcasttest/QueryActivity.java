package com.ivok.broadcasttest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

public class QueryActivity extends Activity {

    //code for 'startActivityForResult' and 'onActivityResult'
    private static final int SCREEN_RECORD_REQUEST_CODE = 777;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Destroy", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Do nothing
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);
    }

    @Override
    protected void onStart() {
        //ask the system for screen recording (screencast)
        super.onStart();
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        //The returned result from 'createScreenCaptureIntent' MUST BE passed to 'startActivityForResult'
        Intent permissionIntent = mediaProjectionManager != null ? mediaProjectionManager.createScreenCaptureIntent() : null;
        startActivityForResult(permissionIntent, SCREEN_RECORD_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("ActivityResult", "" + RESULT_OK);
        if (requestCode == SCREEN_RECORD_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                //when everything is OK prepare a Service for recording
                Intent service = new Intent(this, ExampleService.class);

                //get system parameters needed for recording
                DisplayMetrics displayMetrics = new DisplayMetrics();
                WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
                wm.getDefaultDisplay().getRealMetrics(displayMetrics);
                int displayWidth = displayMetrics.widthPixels;
                int displayHeight = displayMetrics.heightPixels;
                int displayDensity = displayMetrics.densityDpi;

                //put them in extra in order to use this parameters in the Service
                service.putExtra("width", displayWidth);
                service.putExtra("height", displayHeight);
                service.putExtra("density", displayDensity);
                //these two are IMPORTANT for starting MediaProjection
                service.putExtra("resultCode", resultCode);
                service.putExtra("data", data);

                //different methods for different verions for starting the Service
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    startForegroundService(service);
                else
                    startService(service);

                Log.d("BroadcastReceiver", "Start Command sent");

                //Destroy the Activity, otherwise it will block the recording
                this.finish();
            }
        }
    }
}