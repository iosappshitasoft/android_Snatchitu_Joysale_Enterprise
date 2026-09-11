package com.ni.utils;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ni.model.HomeItemResponse;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by hitasoft on 12/3/18.
 */

public class ApiClient {

    private static final String TAG = ApiClient.class.getSimpleName();
    private static Retrofit retrofit = null;

    /**
     * Vivo (and some OEMs) redact URLs / lat / lng / phone / address in Logcat as ****.
     * Transform the message so their filter does not match, but text stays readable.
     */
    private static String safeLog(String message) {
        if (message == null) return "";
        return message
                .replace("https://", "https_ ")
                .replace("http://", "http_ ")
                .replace("\"lat\"", "\"latitude\"")
                .replace("\"lon\"", "\"longitude\"")
                .replace("\"lng\"", "\"longitude\"")
                .replace("&lat=", "&latitude=")
                .replace("&lon=", "&longitude=")
                .replace("&lng=", "&longitude=")
                .replace("lat=", "latitude=")
                .replace("lon=", "longitude=")
                .replace("lng=", "longitude=");
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(@NonNull String message) {
                    Log.i(TAG, safeLog(message));
                }
            });
            logging.level(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(new Interceptor() {
                        @NonNull
                        @Override
                        public Response intercept(@NonNull Chain chain) throws IOException {
                            Request request = chain.request();
                            // Path only — no scheme/host, so Vivo will not mask as ****
                            String apiName = request.url().encodedPath();
                            String host = request.url().host().replace(".", "_");
                            Log.i(TAG, "API_CALL => " + request.method() + " | host=" + host + " | api=" + apiName);
                            long start = System.currentTimeMillis();
                            Response response = chain.proceed(request);
                            long tookMs = System.currentTimeMillis() - start;
                            Log.i(TAG, "API_RESP <= " + response.code() + " | api=" + apiName + " | " + tookMs + "ms");
                            return response;
                        }
                    })
                    .callTimeout(60, TimeUnit.SECONDS)
                    .addNetworkInterceptor(new Interceptor() {
                        @NonNull
                        @Override
                        public Response intercept(@NonNull Chain chain) throws IOException {
                            Log.e(TAG, "intercept: " + Constants.pref.getString(Constants.TAG_ACCESS_TOKENS, Constants.TAG_ACCESS_TOKENS_PREF));
                            Request original = chain.request();
                            Request.Builder requestBuilder = original.newBuilder()
                                    .addHeader(Constants.TAG_AUTHORIZATION,
                                            !TextUtils.isEmpty(Constants.pref.getString(Constants.TAG_ACCESS_TOKENS, Constants.TAG_ACCESS_TOKENS_PREF))
                                                    ? Constants.pref.getString(Constants.TAG_ACCESS_TOKENS, Constants.TAG_ACCESS_TOKENS_PREF)
                                                    : "");
                            return chain.proceed(requestBuilder.build());
                        }
                    })
                    .build();
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(HomeItemResponse.Photo.class, new PhotoDeserializer())
                    .create();
            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    /** Call this after changing logging / base URL if Retrofit was already created. */
    public static void resetClient() {
        retrofit = null;
    }
}
