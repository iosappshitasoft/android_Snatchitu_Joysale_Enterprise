package com.ni.helper.worker;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.os.HandlerCompat;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;
import com.ni.helper.ImageStorage;
import com.ni.utils.Constants;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadFilesWorker extends ListenableWorker {
    private static final String TAG = DownloadFilesWorker.class.getSimpleName();
    private Context mContext;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = HandlerCompat.createAsync(Looper.getMainLooper());

    public DownloadFilesWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mContext = context;
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        ImageStorage imageStorage = new ImageStorage(mContext);
        final boolean[] fileDownloaded = {false};
        final String fileUrl = getInputData().getString(Constants.TAG_URL);
        final String fileName = imageStorage.getFileName(fileUrl);
        final String from = getInputData().getString(Constants.TAG_FROM);

        return CallbackToFutureAdapter.getFuture(completer -> {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    File cacheDir = imageStorage.getExtCacheDir();
                    File cacheFile = new File(cacheDir, fileName);
                    try {
                        BufferedInputStream in = new BufferedInputStream(new URL(fileUrl).openStream());
                        FileOutputStream fileOutputStream = new FileOutputStream(cacheFile);
                        byte[] dataBuffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                            fileOutputStream.write(dataBuffer, 0, bytesRead);
                        }
                        if (cacheFile.exists() && cacheFile.length() > 0) {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || from.equals(Constants.TAG_AUDIO_SENT)) {
                                File savedFile = imageStorage.saveToSDCardV9(cacheFile, from, fileName, Constants.TAG_AUDIO);
                                if (savedFile != null && savedFile.exists()) {
                                    fileDownloaded[0] = true;
                                    Log.d(TAG, "Download Completed: " + savedFile.getAbsolutePath());
                                    mainHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            completer.set(Result.success(createOutputData(savedFile.getAbsolutePath())));
                                        }
                                    });
                                }
                            } else {
                                Uri savedUri = imageStorage.saveToSDCardV10(cacheFile, from, fileName, Constants.TAG_AUDIO, imageStorage.getMimeType(cacheFile.getAbsolutePath()));
                                if (savedUri != null) {
                                    Log.d(TAG, "Download Completed: " + savedUri);
                                    fileDownloaded[0] = true;
                                    mainHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            completer.set(Result.success(createOutputData("" + savedUri)));
                                        }
                                    });
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("Error Message", e.getMessage());
                        e.printStackTrace();
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                completer.set(Result.failure(Data.EMPTY));
                            }
                        });
                    }
                }
            });
            return completer;
        });
    }

    private Data createOutputData(String filePath) {
        Data.Builder builder = new Data.Builder();
        builder.putString(Constants.TAG_DATA, filePath);
        return builder.build();
    }
}
