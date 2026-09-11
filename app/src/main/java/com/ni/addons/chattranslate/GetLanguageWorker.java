package com.ni.addons.chattranslate;

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
import com.google.gson.Gson;
import com.ni.addons.LanguageData;
import com.ni.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GetLanguageWorker extends ListenableWorker {
    private static final String TAG = GetLanguageWorker.class.getSimpleName();
    private Context mContext;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler mainHandler = HandlerCompat.createAsync(Looper.getMainLooper());

    public GetLanguageWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mContext = context;
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        return CallbackToFutureAdapter.getFuture(completer -> {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    List<LanguageData> languageList = getTranslateLanguages();
                    if (languageList.size() > 0) {
                        String languageString = new Gson().toJson(languageList);
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                completer.set(Result.success(createOutputData(languageString)));
                            }
                        });
                    } else {
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

    private List<LanguageData> getTranslateLanguages() {
        List<LanguageData> languageList = new ArrayList<>();
        HttpURLConnection conn = null;
        StringBuilder sb = new StringBuilder("https://translation.googleapis.com/language/translate/v2/languages");
        sb.append("?key=").append(/*"AIzaSyAcLN3iArCxK--jhLojAFNRtK6G56sO6Bo"*/"AIzaSyAaqMnp73p7x4IHP9cXwQiaa-Aq9l3Gl2U");
        try {
            URL url = new URL(sb.toString());
            Log.d(TAG, "doInBackground: " + url);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setUseCaches(false);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Content-Type", "application/json");

            //Create JSONObject here
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("target", Constants.TAG_DEFAULT_LANGUAGE_CODE);

            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(jsonParam.toString());
            out.close();
            int HttpResult = conn.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                StringBuilder resultBuilder = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        conn.getInputStream(), StandardCharsets.UTF_8));
                String line = null;
                while ((line = br.readLine()) != null) {
                    resultBuilder.append(line).append("\n");
                }
                br.close();
                Log.i(TAG, "getTranslateLanguages: " + resultBuilder);
                JSONObject resultObject = new JSONObject(resultBuilder.toString());
                JSONObject dataObject = resultObject.getJSONObject("data");
                JSONArray languageArray = dataObject.getJSONArray("languages");
                for (int i = 0; i < languageArray.length(); i++) {
                    LanguageData data = new LanguageData();
                    data.setLanguage(languageArray.getJSONObject(i).getString("name"));
                    data.setLanguageCode(languageArray.getJSONObject(i).getString("language"));
                    languageList.add(data);
                }
            } else {
                Log.e(TAG, "getTranslateLanguages: " + conn.getResponseMessage());
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return languageList;
    }

    private Data createOutputData(String languageJson) {
        Data.Builder builder = new Data.Builder();
        builder.putString(Constants.TAG_DATA, languageJson);
        return builder.build();
    }
}
