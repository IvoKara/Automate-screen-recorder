package com.ivok.broadcasttest;

import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FFmpegUsage {

    private String folderPath;

    public FFmpegUsage(String videoName) {
        segmentateVideo(videoName);
    }

    public String getFolderPath() {
        return folderPath;
    }

    private void segmentateVideo(String videoName) {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath() + "/.broadcasttest/";

        //folder to store the segments of the source recording
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd_HH-mm-ss", Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());
        String time = formatter.format(curDate);

        String segmentsPath = path + "segments_" + time + "/";
        folderPath = segmentsPath;
        //creating the folder
        createSegmentsFolder(segmentsPath);

        //get the source video duration in miliseconds
        long duration = getVideoDurationInMs(path + videoName);

        Log.d("DURATION", String.valueOf(duration));

        int secondsForSegment = 10;
        for (int segmentSec = 0; segmentSec < duration/1000.0; segmentSec+=secondsForSegment) {

            //interface command for segmentation
            String command = "-i {sourceVideoPath} -ss {startPoint} -t {cutDuration} -c copy {segmentsPath}";
            String segmentName = "segment-from_{mark}s.mp4";
            if ((duration/1000.0) - segmentSec < secondsForSegment) {
                command = "-i {sourceVideoPath} -ss {startPoint} -c copy {segmentsPath}";
                segmentName = "segment-from_{mark}s_last.mp4";
            }
            //edit command for the corresponding segment
            command = command.replace("{sourceVideoPath}", path+videoName);
            command = command.replace("{startPoint}", String.valueOf(segmentSec));
            command = command.replace("{cutDuration}", String.valueOf(secondsForSegment));
            command = command.replace("{segmentsPath}", segmentsPath);

            segmentName = segmentName.replace("{mark}", String.valueOf(segmentSec).replace('.', '-'));
            command += segmentName;

            //execute the output command
            executeFFmpegCommand(command);

            try {
                addFileTolist(segmentsPath, segmentName);
            } catch (Exception e) {
                Log.e("FILE ERROR", e.getMessage());
            }
        }
    }

    private void addFileTolist(String path, String name) throws Exception {
        FileOutputStream fileList = new FileOutputStream(path+"/filelist.txt", true);
        String line = "file " + '\'' + path + name + "\'\n";
        fileList.write(line.getBytes());
        fileList.close();
    }

    private void executeFFmpegCommand(String command) {
        FFmpegSession session = FFmpegKit.execute(command);
        if (ReturnCode.isSuccess(session.getReturnCode())) {
            // SUCCESS
            Log.d("FFMPEG", "successful segmentation");
        } else if (ReturnCode.isCancel(session.getReturnCode())) {
            // CANCEL
            Log.w("FFMPEG", "Canceled segmentation");
        } else {
            // FAILURE
            Log.e("FFMPEG ERROR", String.format("Command failed with state %s and rc %s.%s",
                    session.getState(), session.getReturnCode(), session.getFailStackTrace()));
        }
    }

    private long getVideoDurationInMs(String pathToFile) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(pathToFile);
        long duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        retriever.release();

        return duration;
    }

    private void createSegmentsFolder(String path) {
        File folder = new File(path);
        if (!folder.exists()) {
            if (folder.mkdirs()) {
                Log.d("FOLDER 'segments'", "created");
            }
        }
    }
}
