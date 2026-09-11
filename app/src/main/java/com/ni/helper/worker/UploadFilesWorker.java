package com.ni.helper.worker;

import android.content.Context;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UploadFilesWorker extends ListenableWorker {
    private static final String TAG = UploadFilesWorker.class.getSimpleName();
    private Context mContext;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = HandlerCompat.createAsync(Looper.getMainLooper());

    public UploadFilesWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mContext = context;
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        ImageStorage imageStorage = new ImageStorage(mContext);
        final String filePath = getInputData().getString(Constants.TAG_AUDIO);
        final String fileName = imageStorage.getFileName(filePath);
        final String recordTime = getInputData().getString(Constants.TAG_AUDIO_DURATION);

        return CallbackToFutureAdapter.getFuture(completer -> {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    String response = "";
                    HttpURLConnection conn = null;
                    DataOutputStream dos = null;
                    DataInputStream inStream = null;
                    StringBuilder builder = new StringBuilder();
                    String lineEnd = "\r\n";
                    String twoHyphens = "--";
                    String boundary = "*****";
                    int bytesRead, bytesAvailable, bufferSize;
                    byte[] buffer;
                    int maxBufferSize = 1 * 1024 * 1024;
                    String urlString = Constants.API_UPLOAD_AUDIO;
                    Log.i(TAG, "startWork: " + urlString);
                    try {
                        FileInputStream fileInputStream = new FileInputStream(new File(filePath));
                        URL url = new URL(urlString);
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setDoInput(true);
                        conn.setDoOutput(true);
                        conn.setUseCaches(false);
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Connection", "Keep-Alive");
                        conn.setRequestProperty("Content-Type",
                                "multipart/form-data;boundary=" + boundary);
                        dos = new DataOutputStream(conn.getOutputStream());

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"type\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes("chat");
                        dos.writeBytes(lineEnd);

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"audio\";filename=\""
                                + filePath + "\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        buffer = new byte[bufferSize];

                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                        while (bytesRead > 0) {
                            dos.write(buffer, 0, bufferSize);
                            bytesAvailable = fileInputStream.available();
                            bufferSize = Math.min(bytesAvailable, maxBufferSize);
                            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                        }
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                        BufferedReader in = new BufferedReader(new InputStreamReader(
                                conn.getInputStream()));
                        String inputLine;

                        while ((inputLine = in.readLine()) != null) {
                            builder.append(inputLine);
                            Log.i(TAG, "File is writing");
                        }

                        fileInputStream.close();
                        response = builder.toString();
                        dos.flush();
                        dos.close();

                    } catch (MalformedURLException ex) {
                        Log.e(TAG, "MalformedURLException: " + ex.getMessage(), ex);
                    } catch (IOException ioe) {
                        Log.e(TAG, "IOException: " + ioe.getMessage(), ioe);
                    }

                    try {
                        inStream = new DataInputStream(conn.getInputStream());
                        BufferedReader lines = new BufferedReader(new InputStreamReader(inStream, StandardCharsets.UTF_8));
                        String str;
                        while ((str = lines.readLine()) != null) {
                            Log.e(TAG, "Server Response" + str);
                        }
                        inStream.close();
                    } catch (IOException ioex) {
                        Log.e(TAG, "IOException: " + ioex.getMessage(), ioex);
                    }

                    Log.d(TAG, "uploadFilesWork: " + response);
                    try {
                        JSONObject jsonobject = new JSONObject(response);
                        String status = jsonobject.getString(Constants.TAG_STATUS);
                        if (status.equals("true")) {
                            String finalResponse = response;
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    completer.set(Result.success(createOutputData(finalResponse, recordTime)));
                                }
                            });
                        }
                    } catch (JSONException e) {
                        setError(completer);
                        e.printStackTrace();
                    }
                }
            });
            return completer;
        });
    }

    private void setError(CallbackToFutureAdapter.Completer<Result> completer) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                completer.set(Result.failure(Data.EMPTY));
            }
        });
    }

    private Data createOutputData(String uploadResponse, String recordTime) {
        Data.Builder builder = new Data.Builder();
        builder.putString(Constants.TAG_DATA, uploadResponse);
        builder.putString(Constants.TAG_AUDIO_DURATION, recordTime);
        return builder.build();
    }
}
