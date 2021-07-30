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
import android.os.CountDownTimer;
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

    public static int resultCode;
    public static Intent data;

    private boolean isFinished;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent serviceIntent = new Intent(context, ExampleService.class);

        if("com.ivok.START_SERVICE".equals(intent.getAction())) {

            isFinished = false;

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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(serviceIntent);
            else
                context.startService(serviceIntent);

            Log.d("BroadcastReceiver", "Start Command sent");
            //Toast.makeText(context, "Autostart started", Toast.LENGTH_SHORT).show();

            int minutes = 10;
            new CountDownTimer(minutes*60*1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    isFinished = true;
                    context.stopService(serviceIntent);
                }
            }.start();
        }
        if("com.ivok.STOP_SERVICE".equals(intent.getAction())) {
            if(!isFinished) {
                context.stopService(serviceIntent);
            }
        }
    }
}
