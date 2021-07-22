package com.ivok.broadcasttest;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.hbisoft.hbrecorder.HBRecorder;
import com.hbisoft.hbrecorder.HBRecorderCodecInfo;
import com.hbisoft.hbrecorder.HBRecorderListener;

import java.io.ByteArrayOutputStream;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.WINDOW_SERVICE;
import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;


public class ExampleBroadcastReceiver extends BroadcastReceiver/* implements HBRecorderListener */{

    /*private static final int SCREEN_RECORD_REQUEST_CODE = 777;
    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
    private static final int PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE = PERMISSION_REQ_ID_RECORD_AUDIO + 1;
    private boolean hasPermissions = false;

    private HBRecorder hbRecorder;
    private Context context;*/

    public static int resultCode;
    public static Intent data;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent serviceIntent = new Intent(context, ExampleService.class);
//        Toast.makeText(context, intent.getAction().toString(), Toast.LENGTH_SHORT).show();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        wm.getDefaultDisplay().getRealMetrics(displayMetrics);
        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;
        int displayDensity = displayMetrics.densityDpi;

        Configuration configuration = context.getResources().getConfiguration();
        boolean isLandscape = configuration.orientation == ORIENTATION_LANDSCAPE;

        serviceIntent.putExtra("width", displayWidth);
        serviceIntent.putExtra("height", displayHeight);
        serviceIntent.putExtra("density", displayDensity);
        serviceIntent.putExtra("resultCode", resultCode);
        serviceIntent.putExtra("data", data);

        if("com.ivok.START_SERVICE".equals(intent.getAction())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(serviceIntent);
            else
                context.startService(serviceIntent);

            Log.i("Autostart", "started");
//            Toast.makeText(context, "Autostart started", Toast.LENGTH_SHORT).show();
        }
        if("com.ivok.STOP_SERVICE".equals(intent.getAction())) {
            context.stopService(serviceIntent);
        }
        /*Toast.makeText(context, "EBR triggered", Toast.LENGTH_SHORT).show();

        this.context = context;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            hbRecorder = new HBRecorder(context, this);
//            Intent permissionIntent = MainActivity
            hbRecorder.startScreenRecording(intent, RESULT_OK, null);
        }*/
    }
/*
    @Override
    public void HBRecorderOnStart() {

    }

    @Override
    public void HBRecorderOnComplete() {

    }

    @Override
    public void HBRecorderOnError(int errorCode, String reason) {

    }*/

    /*@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startRecordingScreen() {
        quickSettings();
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent permissionIntent = mediaProjectionManager != null ? mediaProjectionManager.createScreenCaptureIntent() : null;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void quickSettings() {
        hbRecorder.setAudioBitrate(128000);
        hbRecorder.setAudioSamplingRate(44100);
        hbRecorder.recordHDVideo(false);
        hbRecorder.isAudioEnabled(true);
        //Customise Notification
        hbRecorder.setNotificationSmallIcon(drawable2ByteArray(R.drawable.icon));
        hbRecorder.setNotificationTitle(context.getString(R.string.stop_recording_notification_title));
        hbRecorder.setNotificationDescription(context.getString(R.string.stop_recording_notification_message));
    }

    private byte[] drawable2ByteArray(@DrawableRes int drawableId) {
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), drawableId);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        icon.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }*/
}
