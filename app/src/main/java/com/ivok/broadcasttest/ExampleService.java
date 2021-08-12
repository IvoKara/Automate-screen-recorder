package com.ivok.broadcasttest;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class ExampleService extends Service {

    private static final String TAG = "ExampleService";

    private final int videoEncoderAsInt = MediaRecorder.VideoEncoder.H264;
    private final int outputFormatAsInt = MediaRecorder.OutputFormat.MPEG_4;
    private int audioSourceAsInt;     // MIC or VOICE_RECOGNITION(Android 10)
    private final int videoFrameRate = 30;// FPS
    private final int videoBitrate = 4000000;// bps
    private final int audioBitrate = 128000;
    private final int audioSamplingRate = 44100;// Hz

    private final boolean isVideoHD = false;
    private final boolean isAudioEnabled = false;
    private final boolean isCustomSettingsEnabled = true;
    private boolean hasMaxFileBeenReached = false;

    private int orientationHint = 400;
    private int mScreenDensity;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mResultCode;
    private Intent mResultData;
    private MediaProjection mMediaProjection;
    private MediaRecorder mMediaRecorder;
    private VirtualDisplay mVirtualDisplay;
    private Intent mIntent;

    private String folderName = ".broadcasttest";
    private final String path = String.valueOf(Environment.
            getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            + "/" + folderName);
    private static String filePath;
    private static String fileName;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, TAG + " stopped", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onDestroy");
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
        if (mMediaRecorder != null) {
            mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.reset();
        }
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        FFmpegUsage ffmpeg = new FFmpegUsage(fileName);
        DeleteFile(filePath);
        SendSyncBroadcast(ffmpeg.getFolderPath());
    }

    private void SendSyncBroadcast(String folderPath) {
        Intent i = new Intent("com.ivok.SYNC_FOLDER");
        i.putExtra("folderPath", folderPath);
        sendBroadcast(i);
    }

    private void DeleteFile(String path) {
        File video = new File(path);
        if(video.delete()) {
            Log.d(TAG, "Deleted recording file");
        }
    }

    private void createFolder() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            File f1 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), folderName);
            if (!f1.exists()) {
                if (f1.mkdirs()) {
                    Log.d("Folder ", "created");
                }

            }
        }
    }

    private void setPathAndName() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());
        String curTime = formatter.format(curDate).replace(" ", "");
        String videoQuality = "HD";
        if (!isVideoHD) {
            videoQuality = "SD";
        }

        String name = videoQuality + curTime;

        fileName = name + ".mp4";

        filePath = path + "/" + fileName;

        Log.d("Filepath", filePath);
    }

    private void initRecorder() throws Exception {
        mMediaRecorder = new MediaRecorder();

        if (isAudioEnabled) {
            mMediaRecorder.setAudioSource(audioSourceAsInt);
        }

        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(outputFormatAsInt);

        if (isAudioEnabled) {
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.setAudioEncodingBitRate(audioBitrate);
            mMediaRecorder.setAudioSamplingRate(audioSamplingRate);
        }

        mMediaRecorder.setVideoEncoder(videoEncoderAsInt);
        mMediaRecorder.setOutputFile(filePath);
        mMediaRecorder.setVideoSize(mScreenWidth, mScreenHeight);

        Log.d("Android version", String.valueOf(Build.VERSION.SDK_INT));
        Log.d("videoBitrate:", String.valueOf(videoBitrate));
        Log.d("videoFramerate:", String.valueOf(videoFrameRate));

        mMediaRecorder.setVideoEncodingBitRate(videoBitrate);
        mMediaRecorder.setVideoFrameRate(videoFrameRate);

        //THROWS EXCEPTION
        mMediaRecorder.prepare();
    }

    private void initMediaProjection() {

        Log.e("MediaProjection", "data: " + mResultData);

        mMediaProjection = ((MediaProjectionManager) Objects.requireNonNull(
                getSystemService(Context.MEDIA_PROJECTION_SERVICE))
        ).getMediaProjection(mResultCode, mResultData);

        Log.e("MediaProjection", String.valueOf(mMediaProjection)); //check if null
    }

    private void initVirtualDisplay() {
        Log.e("VirtualDisplay", String.valueOf(mMediaRecorder));
        Log.d("width", String.valueOf(mScreenWidth));
        Log.d("height", String.valueOf(mScreenHeight));
        Log.d("density", String.valueOf(mScreenDensity));

        mVirtualDisplay = mMediaProjection.createVirtualDisplay(
                TAG, mScreenWidth, mScreenHeight, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder.getSurface(), null, null);
    }

    private void createNotification() {
        final String notificationTitle = "Facebook";
        final String notificationDescription = "Running Facebook in foreground";

        //Notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "001";
            String channelName = "FacebookChannel";

            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);

                Notification notification = new Notification.Builder(getApplicationContext(), channelId).
                        setOngoing(true).setSmallIcon(R.drawable.icon).setContentTitle(notificationTitle).
                        setContentText(notificationDescription).build();

                startForeground(101, notification);
            }
        } else {
            startForeground(101, new Notification());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(this, TAG + " started", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onStartCommand");

        //on POT.. only works voice recognition
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            audioSourceAsInt = MediaRecorder.AudioSource.VOICE_RECOGNITION;
        } else {
            audioSourceAsInt = MediaRecorder.AudioSource.MIC;
        }

        mIntent = intent;
        mScreenWidth = intent.getIntExtra("width", 0);
        mScreenHeight = intent.getIntExtra("height", 0);
        mScreenDensity = intent.getIntExtra("density", 1);
        mResultCode = intent.getIntExtra("resultCode", -1);
        mResultData = intent.getParcelableExtra("data");

        //create notification
        createNotification();

        //Create folder and file info
        try {
            setPathAndName();
            createFolder();
        } catch (Exception e) {
            Log.e(TAG, "Error on creating folder: " + e.getMessage());
        }

        //Init MediaRecorder
        try {
            initRecorder();
        } catch (Exception e) {
            Log.e(TAG, "Error on init Recorder: " + e.getMessage());
        }

        //Init MediaProjection
        try {
            initMediaProjection();
        } catch (Exception e) {
            Log.e(TAG, "Error on init MediaProjection: " + e.getMessage());
        }

        //Init VirtualDisplay
        try {
            initVirtualDisplay();
        } catch (Exception e) {
            Log.e(TAG, "Error on init VirtualDisplay: " + e.getMessage());
        }

        //Start Recording
        try {
            mMediaRecorder.start();
        } catch (Exception e) {
            Log.e(TAG, "Error on start Recording" + e.getMessage());
        }

        return Service.START_STICKY;
    }
}