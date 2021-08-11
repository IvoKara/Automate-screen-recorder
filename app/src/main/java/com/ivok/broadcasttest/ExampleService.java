package com.ivok.broadcasttest;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.hardware.camera2.*;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileDescriptor;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.AbstractQueue;
import java.util.Locale;
import java.util.Objects;

public class ExampleService extends Service {

    private static final String TAG = "ExampleService";

    private final int videoEncoderAsInt = MediaRecorder.VideoEncoder.H264;    // H264
    private final int outputFormatAsInt = MediaRecorder.OutputFormat.MPEG_4;    // MPEG_4
    private int audioSourceAsInt;     // MIC or VOICE_RECOGNITION(Android 10)
    private final int videoFrameRate = 30;      //30FPS
    private final int videoBitrate = 4000000;  //4Mbps
    private final int audioBitrate = 128000;
    private final int audioSamplingRate = 44100;//44.1kHz

    private final boolean isVideoHD = false;
    private final boolean isAudioEnabled = true;
    private final boolean isCustomSettingsEnabled = true;
    private boolean hasMaxFileBeenReached = false;

    private int orientationHint = 400;
    private int mScreenDensity;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mResultCode; //RESULT_OK
    private Intent mResultData; /* caution */
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

    private Uri returnedUri = null;

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
//        DeleteFile(filePath);
//        SendSyncBroadcast(ffmpeg.getFolderName());
    }

    private void SendSyncBroadcast(String folderName) {
        Intent i = new Intent("com.ivok.SYNC_FOLDER");
        i.putExtra("folderName", folderName);
        sendBroadcast(i);
    }

    private void DeleteFile(String path) {
        File video = new File(path);
        if(video.delete()) {
            Log.d(TAG, "Deleted recording file");
        }
    }

    private void createFolder() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            ContentResolver resolver = getContentResolver();
//            ContentValues contentValues = new ContentValues();
//            contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/" + folderName);
//            contentValues.put(MediaStore.Video.Media.TITLE, fileName);
//            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
//            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
//            returnedUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
//            Log.i("URI", returnedUri.toString());
//        } else {
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

        if (orientationHint != 400){
            mMediaRecorder.setOrientationHint(orientationHint);
        }

        if (isAudioEnabled) {
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.setAudioEncodingBitRate(audioBitrate);
            mMediaRecorder.setAudioSamplingRate(audioSamplingRate);
        }

        mMediaRecorder.setVideoEncoder(videoEncoderAsInt);


//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            try {
//                ContentResolver contentResolver = getContentResolver();
//                FileDescriptor inputPFD = Objects.requireNonNull(contentResolver.openFileDescriptor(returnedUri, "rw")).getFileDescriptor();
//                mMediaRecorder.setOutputFile(inputPFD);
//            } catch (Exception e) {
//                Log.e(TAG, "Error on MediaRecorder");
//                Toast.makeText(this, "Error on MediaRecorder", Toast.LENGTH_SHORT).show();
//            }
//        } else {
            mMediaRecorder.setOutputFile(filePath);
            //Log.e("filepath", filePath);
//        }
        Log.d("Android version", String.valueOf(Build.VERSION.SDK_INT));
        mMediaRecorder.setVideoSize(mScreenWidth, mScreenHeight);

        if (!isCustomSettingsEnabled) {
            if (!isVideoHD) {
                mMediaRecorder.setVideoEncodingBitRate(12000000);
                mMediaRecorder.setVideoFrameRate(30);
            } else {
                mMediaRecorder.setVideoEncodingBitRate(5 * mScreenWidth * mScreenHeight);
                mMediaRecorder.setVideoFrameRate(60); //after setVideoSource(), setOutFormat()
            }
        } else {
            Log.d("videoBitrate:", String.valueOf(videoBitrate));
            Log.d("videoFramerate:", String.valueOf(videoFrameRate));
            mMediaRecorder.setVideoEncodingBitRate(videoBitrate);
            mMediaRecorder.setVideoFrameRate(videoFrameRate);
        }

        try {
            mMediaRecorder.prepare();
        } catch (Exception e) {
            Log.e("Error prepare", e.getMessage());
        }

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

            Surface s = mMediaRecorder.getSurface();
            Log.e(TAG, String.valueOf(s));

        mVirtualDisplay = mMediaProjection.createVirtualDisplay(TAG, mScreenWidth, mScreenHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, s, null, null);
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

        final String notificationButtonText = "STOP REC";
        final String notificationTitle = "Facebook";
        final String notificationDescription = "Running Facebook in foregroud";

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
                Notification notification;

                Intent myIntent = new Intent(this, NotificationReceiver.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, myIntent, 0);

                Notification.Action action = new Notification.Action.Builder(
                        Icon.createWithResource(this, android.R.drawable.presence_video_online),
                        notificationButtonText,
                        pendingIntent).build();


                    //Modify notification badge
                    notification = new Notification.Builder(getApplicationContext(), channelId).
                            setOngoing(true).setSmallIcon(R.drawable.icon).setContentTitle(notificationTitle).
                            setContentText(notificationDescription).addAction(action).build();
                startForeground(101, notification);
            }
        } else {
            startForeground(101, new Notification());
        }

        /*******************************/

        //Create folder and file info
        try {
            setPathAndName();
            createFolder();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
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

        mMediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
            @Override
            public void onError(MediaRecorder mediaRecorder, int what, int extra) {
                if ( what == 268435556 && hasMaxFileBeenReached) {
                    // Benign error b/c recording is too short and has no frames. See SO: https://stackoverflow.com/questions/40616466/mediarecorder-stop-failed-1007
                    return;
                }
//                ResultReceiver receiver = intent.getParcelableExtra(ScreenRecordService.BUNDLED_LISTENER);
//                Bundle bundle = new Bundle();
//                bundle.putInt(ERROR_KEY, SETTINGS_ERROR);
//                bundle.putString(ERROR_REASON_KEY, String.valueOf(what));
//                if (receiver != null) {
//                    receiver.send(Activity.RESULT_OK, bundle);
//                }
                Log.e(TAG, "Error set MediaRecorder listener");
            }
        });

        mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
                    hasMaxFileBeenReached = true;
                    Log.i(TAG,String.format(Locale.US,"onInfoListen what : %d | extra %d", what, extra));
//                    ResultReceiver receiver = intent.getParcelableExtra(ScreenRecordService.BUNDLED_LISTENER);
//                    Bundle bundle = new Bundle();
//                    bundle.putInt(ERROR_KEY, MAX_FILE_SIZE_REACHED_ERROR);
//                    bundle.putString(ERROR_REASON_KEY, getString(com.hbisoft.hbrecorder.R.string.max_file_reached));
//                    if (receiver != null) {
//                        receiver.send(Activity.RESULT_OK, bundle);
//                    }
                    Log.e(TAG, "Error Info set MediaRecorder listener");

                }
            }
        });

        //Start Recording
        try {
            mMediaRecorder.start();
//            ResultReceiver receiver = intent.getParcelableExtra(ScreenRecordService.BUNDLED_LISTENER);
//            Bundle bundle = new Bundle();
//            bundle.putInt(ON_START_KEY, ON_START);
//            if (receiver != null) {
//                receiver.send(Activity.RESULT_OK, bundle);
//            }
        } catch (Exception e) {
            // From the tests I've done, this can happen if another application is using the mic or if an unsupported video encoder was selected
//            ResultReceiver receiver = intent.getParcelableExtra(ScreenRecordService.BUNDLED_LISTENER);
//            Bundle bundle = new Bundle();
//            bundle.putInt(ERROR_KEY, SETTINGS_ERROR);
//            bundle.putString(ERROR_REASON_KEY, Log.getStackTraceString(e));
//            if (receiver != null) {
//                receiver.send(Activity.RESULT_OK, bundle);
//            }
            Log.e(TAG, "Error on start Recording");
        }

        return Service.START_STICKY;
    }
}