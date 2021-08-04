package com.ivok.broadcasttest;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.Iterator;

public class QueryActivity extends Activity {

    private static final int SCREEN_RECORD_REQUEST_CODE = 777;
    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
    private static final int PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE = PERMISSION_REQ_ID_RECORD_AUDIO + 1;
    public static boolean isActive = false;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActive = false;
        Toast.makeText(this, "Destroy", Toast.LENGTH_SHORT).show();
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);
        isActive = true;
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent permissionIntent = mediaProjectionManager != null ? mediaProjectionManager.createScreenCaptureIntent() : null;
//        listExtras(permissionIntent);
        startActivityForResult(permissionIntent, SCREEN_RECORD_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("ActivityResult", "" + RESULT_OK);
        if (requestCode == SCREEN_RECORD_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Log.e("MediaProjection1", "data: " + data);

                ExampleBroadcastReceiver.resultCode = resultCode;
                ExampleBroadcastReceiver.data = data;
//                listExtras(data);
                this.finish();
            }
        }
    }

    private void listExtras(Intent i) {
        Bundle bundle = i.getExtras();
        if(bundle != null) {
            Iterator<String> iterator = bundle.keySet().iterator();
            while(iterator.hasNext()) {
                String key = iterator.next();
                Log.e("Extra", "[" + key + "=" + bundle.get(key) + "]");
                //i.getParcelableExtra(key);
            }

        }
    }
}