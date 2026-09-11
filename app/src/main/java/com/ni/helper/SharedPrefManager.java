package com.ni.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.ni.utils.Constants;

/**
 * Created by Belal on 03/11/16.
 */

public class SharedPrefManager {
    private static final String SHARED_PREF_NAME = "TokenPref";
    private static final String TAG_TOKEN = "device_token";
    public static final String TAG_ACCESS_TOKEN = "access_token";
    public static final String TAG_ACCESS_TOKEN_PREF = "access_token_pref";

    private static SharedPrefManager mInstance;
    private static Context mCtx;

    //addons
    public static final String CHAT_LANGUAGE = "chat_language";
    public static final String DEFAULT_CHAT_LANGUAGE = "";
    public static SharedPreferences mPref;
    public static SharedPreferences.Editor mEditor;

    private SharedPrefManager(Context context) {
        mCtx = context;
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPrefManager(context);
        }
        return mInstance;
    }

    //this method will save the device token to shared preferences
    public boolean saveDeviceToken(String token){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TAG_TOKEN, token);
        editor.apply();
        return true;
    }
    public boolean saveAccessToken(String token){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(TAG_ACCESS_TOKEN_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TAG_ACCESS_TOKEN, token);
        editor.apply();
        return true;
    }



    //this method will fetch the device token from shared preferences
    public String getDeviceToken(){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return  sharedPreferences.getString(TAG_TOKEN, null);
    }

    public String getAccessToken(){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(TAG_ACCESS_TOKEN_PREF, Context.MODE_PRIVATE);
        return  sharedPreferences.getString(TAG_ACCESS_TOKEN, null);
    }

    //addons
    public static void putString(String key, String val) {
        mEditor.putString(key, val).commit();
    }


    public static void initPref(Context mContext) {
        mPref = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mEditor = mPref.edit();
        SharedPrefManager.mCtx = mContext;
    }

    public static SharedPreferences.Editor getEditor(Context mContext) {
        mPref = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mEditor = mPref.edit();
        return mEditor;
    }

    public static String getString(String key, String defaultValue) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, defaultValue);
    }

    public static boolean putStringIntoStore(String key, String value) {
        SharedPreferences prefs = mCtx.getSharedPreferences("store", mCtx.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        return editor.putString(key, value).commit();
    }
    public static String getStringFromStore(String key) {
        SharedPreferences prefs = mCtx.getSharedPreferences("store", mCtx.MODE_PRIVATE);
        if (("" + key).equals(Constants.TAG_ACCESS_TOKENS)) {
            return prefs.getString(key, Constants.TAG_ACCESS_TOKENS_PREF);
        }
        return prefs.getString(key, "");

    }
}
