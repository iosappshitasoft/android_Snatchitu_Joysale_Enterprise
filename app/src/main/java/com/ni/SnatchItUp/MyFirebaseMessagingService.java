package com.ni.SnatchItUp;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.ni.addons.call.CallActivity;
import com.ni.addons.call.appRTC.util.AppRTCUtils;
import com.ni.helper.NetworkReceiver;
import com.ni.helper.SharedPrefManager;
import com.ni.model.CallStatus;
import com.ni.utils.ApiClient;
import com.ni.utils.ApiInterface;
import com.ni.utils.CommonFunctions;
import com.ni.utils.Constants;
import com.ni.utils.DefensiveClass;
import com.ni.utils.GetSet;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by hitasoft on 03/11/16.
 * <p>
 * This class is to Get a FCM Messages From FCM Server and Display a Push Notification.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    static final String TAG = "MyFirebaseMsgService";
    ApiInterface apiInterface;
    private String deviceName, deviceModel;
    String message = "";
    public static String chatUserID = "";
    SharedPreferences pref;
    SharedPreferences.Editor editor;


    @Override
    public void onNewToken(@NonNull String refreshedToken) {
        super.onNewToken(refreshedToken);
        pref = getApplicationContext().getSharedPreferences("ChatPref",
                MODE_PRIVATE);
        editor = pref.edit();
        Log.i(TAG, "onNewToken: " + refreshedToken);
        storeToken(refreshedToken);
    }

  /*  @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Data Payload=" + remoteMessage.getData());
                String strData = remoteMessage.getData().toString().replace("\"data=\"", "\"data\" : ");
                Log.i(TAG, "onMessageReceived: " + strData);
                long sendTime = remoteMessage.getSentTime();

            JSONObject dataJson = null;
            JSONObject jsonData1 = null;
            String message = null;
            String type = null;
            try {
                dataJson = new JSONObject(strData);
                jsonData1 = dataJson.getJSONObject("data");
                message = stripHtml(jsonData1.getString(Constants.TAG_MESSAGE));
                type = jsonData1.getString(Constants.TAG_TYPE);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (type != null && type.equalsIgnoreCase("callstatus")){
                 EventBus.getDefault().post(new CallStatus("1"));
         }
                if (type != null && (type.equals("normal") || type.equals("notification") || type.equals("exchange") || type.equals("admin"))) {
                    generateNotification(message, type);
                } else {
                    sendPushNotificationNew(jsonData1, sendTime);
                }

                Log.v(TAG, " Notification JSON Notification JSON" + dataJson.toString());

        }
    }*/
 /* @Override
  public void onMessageReceived(RemoteMessage remoteMessage) {
      super.onMessageReceived(remoteMessage);
      Log.d(TAG, "onMessageReceived: "+new Gson().toJson(remoteMessage));
      if (remoteMessage.getData().size() > 0) {
          Log.d(TAG, "Data Payload=" + remoteMessage.getData());

          Map<String, String> data = remoteMessage.getData();
          String message = data.get("message");
          String type = data.get("type");
          long sendTime = remoteMessage.getSentTime();

          JSONObject dataJson = new JSONObject(data);
          Log.i(TAG, "onMessageReceived: " + dataJson.toString());

          if (type != null && type.equalsIgnoreCase("callstatus")) {
              EventBus.getDefault().post(new CallStatus("1"));
          } else if (type != null && (type.equals("normal") || type.equals("notification") || type.equals("exchange") || type.equals("admin"))) {
              generateNotification(message, type);
          } else {
              sendPushNotificationNew(dataJson, sendTime);
          }

          Log.v(TAG, "Notification JSON: " + dataJson.toString());
      }
  }*/

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "onMessageReceived: " + new Gson().toJson(remoteMessage));
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Data Payload=" + remoteMessage.getData());

            Map<String, String> data = remoteMessage.getData();
            String message = data.get("message");
            String type = data.get("type");
            long sendTime = remoteMessage.getSentTime();

            JSONObject dataJson = new JSONObject(data);
            Log.i(TAG, "onMessageReceived: " + dataJson.toString());

            if (type != null && type.equalsIgnoreCase("callstatus")) {
                EventBus.getDefault().post(new CallStatus("1"));
            } else if (type != null && (type.equals("normal") || type.equals("notification") || type.equals("exchange") || type.equals("admin"))) {
                generateNotification(message, type);
            } else {
                sendPushNotificationNew(dataJson, sendTime);
            }

            Log.v(TAG, "Notification JSON: " + dataJson.toString());
        }
    }



    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getApplicationContext().getAssets().open("call.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }


    /**
     * For removing the html tags from the given text
     **/
    public String stripHtml(String html) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return "" + Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return "" + Html.fromHtml(html);
        }
    }

    private void generateNotification(String message, String type) {
        Random random = new Random();
        int m = random.nextInt(9999 - 1000) + 1000;
        long when = System.currentTimeMillis();
        String title = getApplicationContext().getString(R.string.app_name);
        Bitmap bitmap = getAppLargeIcon();
        int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);
        Intent notificationIntent = null;
        boolean stopNotification = false;

        if (type == null || type.equals("notification") || type.equals("admin")) {
            notificationIntent = new Intent(getApplicationContext(), com.ni.SnatchItUp.Notification.class);
        } else if (type.equalsIgnoreCase("exchange")) {

            String[] msg = message.split(":");
            String fullname = msg[0].trim();
            if (fullname.contains("\"")) {
                fullname = fullname.replace("\"", "");
            }
            if (ChatActivity.fullName.equals(fullname)) {
                stopNotification = true;
            } else if (ExchangeView.fullName.equals(fullname)) {
                stopNotification = true;
            } else {
                notificationIntent = new Intent(getApplicationContext(), ExchangeActivity.class);
            }
        } else {
            String[] msg = message.split(":");
            String fullname = msg[0].trim();
            if (fullname.contains("\"")) {
                fullname = fullname.replace("\"", "");
            }
            if (ChatActivity.fullName.equals(fullname)) {
                stopNotification = true;
            } else if (ExchangeView.fullName.equals(fullname)) {
                stopNotification = true;
            } else {
                notificationIntent = new Intent(getApplicationContext(), MessageActivity.class);
            }
        }
        if (!stopNotification) {
            String CHANNEL_ID = "my_channel_01";// The id of the channel.
            CharSequence name = "my channel";// The user-visible name of the channel.
            int importance = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                importance = NotificationManager.IMPORTANCE_HIGH;
            }
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // set intent so it does not start a new activity
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent resultPendingIntent;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                resultPendingIntent = PendingIntent.getActivity(this, uniqueInt, notificationIntent,
                        PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_ONE_SHOT);
            } else {
                resultPendingIntent = PendingIntent.getActivity(this, uniqueInt, notificationIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
            }

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
            Notification notification;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                notification = mBuilder.setContentIntent(resultPendingIntent)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setLargeIcon(bitmap)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setWhen(when)
                        .setChannelId(CHANNEL_ID)
                        .build();


                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(mChannel);
                }
            } else {
                notification = mBuilder.setLargeIcon(bitmap)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setWhen(when)
                        .setContentText(message)
                        .setContentIntent(resultPendingIntent)
                        .build();
            }

            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            // Play default notification sound
            notification.defaults |= Notification.DEFAULT_SOUND;

            // Vibrate if vibrate is enabled
            notification.defaults |= Notification.DEFAULT_VIBRATE;

            if (notificationManager != null) {
                notificationManager.notify(m, notification);
            }
        }
    }

    private void storeToken(String token) {
        //saving the token on shared preferences
        SharedPrefManager.getInstance(getApplicationContext()).saveDeviceToken(token);

        //get the logined user details from preference
        Log.d(TAG, "storeToken: " + token);

        Constants.pref = getApplicationContext().getSharedPreferences("JoysalePref",
                MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();
        Constants.REGISTER_ID = token;
        deviceName = JoysaleApplication.getDeviceName();
        deviceModel = JoysaleApplication.getDeviceModel();
        Log.d(TAG, "storeToken: " + true);
        if (Constants.pref.getBoolean(Constants.PREF_ISLOGGED, false)) {
            GetSet.setLogged(true);
            GetSet.setUserId(
                    Constants.pref.getString(Constants.TAG_USER_ID, null));
            addDeviceId();
            Log.d(TAG, "storeToken: " + true);
        }
    }

    private void addDeviceId() {
        Log.d(TAG, "storeToken: " + true);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        if (NetworkReceiver.isConnected()) {
            final String token = SharedPrefManager.getInstance(this).getDeviceToken();
            Map<String, String> map = new HashMap<String, String>();
            String[] languages = getResources().getStringArray(R.array.languages);
            String[] langCode = getResources().getStringArray(R.array.languageCode);
            String selectedLang = Constants.pref.getString(Constants.PREF_LANGUAGE, Constants.LANGUAGE);
            int index = Arrays.asList(languages).indexOf(selectedLang);
            final String languageCode = Arrays.asList(langCode).get(index);

            Log.v(TAG, "languageCode=" + languageCode);

            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_DEVICE_ID, Constants.ANDROID_ID);
            map.put(Constants.TAG_FCM_USERID, GetSet.getUserId());
            map.put(Constants.TAG_DEVICE_TYPE, "1");
            map.put(Constants.TAG_DEVICE_MODE, "1");
            map.put(Constants.TAG_LANG_TYPE, languageCode);
            map.put(Constants.TAG_DEVICE_TOKEN, token);
            map.put(Constants.TAG_DEVICE_NAME, deviceName != null ? deviceName : "");
            map.put(Constants.TAG_DEVICE_MODEL, deviceModel != null ? deviceModel : "");
            map.put(Constants.TAG_DEVICE_OS, "" + Build.VERSION.SDK_INT);
            map.put(Constants.TAG_VOIP_TOKEN, "");
            Call<ResponseBody> call = apiInterface.addDeviceId(map);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    call.cancel();
                }
            });

        }
    }


    //addon
   /* private void sendPushNotificationNew(JSONObject jsonData, long sendTime) {
        try {
            //parsing json data
            String title = DefensiveClass.optString(jsonData, "title");
            JSONObject msg = jsonData.optJSONObject("message");
            String userId = DefensiveClass.optInt(msg, Constants.TAG_USERID);
            String type = DefensiveClass.optString(msg, Constants.TAG_TYPE);
            String time = DefensiveClass.optString(msg, Constants.TAG_TIME_STAMP);


            *//*   String messagest = stripHtml(jsonData.getString(Constants.TAG_MESSAGE));
            Log.i(TAG, "sendPushNotificationNew: "+messagest);*//*

            if (type != null && !type.equals("audio") || type != null && !type.equals("video")) {
                message = "Hello World";
            }

//            String message = DefensiveClass.optString(msg, Constants.TAG_MESSAGE);
            String chatId = DefensiveClass.optString(msg, Constants.TAG_CHAT_ID);
            String userImage = DefensiveClass.optString(msg, Constants.TAG_USERIMAGE);
            String userName = DefensiveClass.optString(msg, Constants.TAG_USERNAME);
            String roomId = DefensiveClass.optString(msg, Constants.TAG_ROOM_ID);
            String platform = DefensiveClass.optString(msg, Constants.TAG_PLATFORM);
            String callUTC = DefensiveClass.optString(msg, Constants.TAG_TIME_STAMP);

            String img = Constants.RESIZE_URL + CommonFunctions.getImageName(userImage) + Constants.IMAGE_RES;
            //get the logined user details from preference
            Constants.pref = getApplicationContext().getSharedPreferences("ChatPref",
                    MODE_PRIVATE);
            Constants.editor = Constants.pref.edit();
            //Addon Call
            if (type.equals("audio") || type.equals("video") || !type.equals("notification")) {
                if (type.equalsIgnoreCase("bye")) {
                    if (CallActivity.isInCall) {
                        CallActivity.isInCall = false;
                    }
                    if (JoysaleApplication.getCurrentActivity() instanceof CallActivity) {
                        CallActivity activity = (CallActivity) JoysaleApplication.getCurrentActivity();
                        if (activity.roomId.equalsIgnoreCase(roomId)) {
                            if (CallActivity.isInCall) {
                                CallActivity.isInCall = false;
                            }
                            activity.finishAndRemoveTask();
                        }
                    }
                } else {
                    if (!CallActivity.isInCall) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                                checkRoom(roomId, userId, userName, userImage, chatId, platform, type, callUTC);
                            } else {
                                if (JoysaleApplication.getCurrentActivity() == null) {
                                    Looper.prepare();
                                    getApplicationContext().startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getApplicationContext().getPackageName())).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                                    Toast.makeText(getApplicationContext(), R.string.read_phone_state_permission_forandroid12, Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            checkRoom(roomId, userId, userName, userImage, chatId, platform, type, callUTC);
                        }
                    } else {
                        callStatus(userId);
                    }
                }
                Constants.TAG_CHATID_Firebase = chatId;
            }


            if (!chatUserID.equals(userId) && !type.equals("audio") && !type.equals("video") && !type.equals("bye")) {
                showSmallNotification(userId, type, chatId, message, img, userName, userImage);
            }

            //Addon Call
            if (type.equals("bye")) {
                if (JoysaleApplication.getCurrentActivity() instanceof CallActivity) {
                    CallActivity activity = (CallActivity) JoysaleApplication.getCurrentActivity();
                    if (activity.roomId.equals(roomId)) {
                        activity.finishAndRemoveTask();
                    }
                }
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "NullPointerException: " + e.getMessage());
            e.printStackTrace();
        }
    }*/
    private void sendPushNotificationNew(JSONObject jsonData, long sendTime) {
        try {

            // Parsing json data
            String title = DefensiveClass.optString(jsonData, "title");

            // Parse the message string to a JSON object
            JSONObject msg = new JSONObject(DefensiveClass.optString(jsonData, "message"));

            String userId = DefensiveClass.optString(msg, Constants.TAG_USERID);
            String type = DefensiveClass.optString(msg, Constants.TAG_TYPE);
            String time = DefensiveClass.optString(msg, Constants.TAG_TIME_STAMP);

            if (type != null && !type.equals("audio") && !type.equals("video")) {
                message = "Hello World";
            }

            String chatId = DefensiveClass.optString(msg, Constants.TAG_CHAT_ID);
            String userImage = DefensiveClass.optString(msg, Constants.TAG_USERIMAGE);
            String userName = DefensiveClass.optString(msg, Constants.TAG_USERNAME);
            String roomId = DefensiveClass.optString(msg, Constants.TAG_ROOM_ID);
            String platform = DefensiveClass.optString(msg, Constants.TAG_PLATFORM);
            String callUTC = DefensiveClass.optString(msg, Constants.TAG_TIME_STAMP);

            String img = "";
            if (!TextUtils.isEmpty(userImage)) {
                img = Constants.RESIZE_URL + CommonFunctions.getImageName(userImage) + Constants.IMAGE_RES;
            }

            // Get the logged in user details from preference
            Constants.pref = getApplicationContext().getSharedPreferences("ChatPref", MODE_PRIVATE);
            Constants.editor = Constants.pref.edit();

            // Addon Call
            if (type.equals("audio") || type.equals("video") || !type.equals("notification")) {
                if (type.equalsIgnoreCase("bye")) {
                    if (CallActivity.isInCall) {
                        CallActivity.isInCall = false;
                    }
                    if (JoysaleApplication.getCurrentActivity() instanceof CallActivity) {
                        CallActivity activity = (CallActivity) JoysaleApplication.getCurrentActivity();
                        if (activity.roomId.equalsIgnoreCase(roomId)) {
                            if (CallActivity.isInCall) {
                                CallActivity.isInCall = false;
                            }
                            activity.finishAndRemoveTask();
                        }
                    }
                } else {
                    if (!CallActivity.isInCall) {
                        Log.d(TAG, "sendPushNotificationNew11111111111: ");
                        checkRoom(roomId, userId, userName, userImage, chatId, platform, type, callUTC);
                     /*   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
                                    ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                                checkRoom(roomId, userId, userName, userImage, chatId, platform, type, callUTC);
                            } else {
                                if (JoysaleApplication.getCurrentActivity() == null) {
                                    Looper.prepare();
                                    getApplicationContext().startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getApplicationContext().getPackageName())).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                                    Toast.makeText(getApplicationContext(), R.string.read_phone_state_permission_forandroid12, Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            checkRoom(roomId, userId, userName, userImage, chatId, platform, type, callUTC);
                        }*/
                    } else {
                        callStatus(userId);
                    }
                }
                Constants.TAG_CHATID_Firebase = chatId;
            }

            if (!chatUserID.equals(userId) && !type.equals("audio") && !type.equals("video") && !type.equals("bye")) {
                showSmallNotification(userId, type, chatId, message, img, userName, userImage);
            }

            // Addon Call
            if (type.equals("bye")) {
                if (JoysaleApplication.getCurrentActivity() instanceof CallActivity) {
                    CallActivity activity = (CallActivity) JoysaleApplication.getCurrentActivity();
                    if (activity.roomId.equals(roomId)) {
                        activity.finishAndRemoveTask();
                    }
                }
            }
        } catch (JSONException | NullPointerException e) {
            Log.e(TAG, "Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //Addon Call
    public void checkRoom(String roomId, String userId, String userName, String userImage,
                          String chatId, String platform, String type, String callUTC) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_CHECK_ROOM,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        Log.v(TAG, "checkRoom: " + res);
                        try {
                            JSONObject json = new JSONObject(res);
                            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                            if (response.equalsIgnoreCase("true")) {
                                if (JoysaleApplication.getCurrentActivity() instanceof CallActivity) {
                                    CallActivity activity = (CallActivity) JoysaleApplication.getCurrentActivity();
                                    if (activity.roomId.equals(roomId)) {
                                        activity.finish();
                                    }
                                }
                            } else {
                                CommonFunctions functions = new CommonFunctions(getApplicationContext());
                                String currentUTC = functions.getCurrentUTCTime();
                                Date callDate = new Date();
                                Date currentDate = new Date();
                                SimpleDateFormat df = new SimpleDateFormat(CommonFunctions.UTC_DATE_PATTERN, Locale.ENGLISH);
                                df.setTimeZone(TimeZone.getTimeZone("UTC"));
                                callDate = df.parse(callUTC);
                                currentDate = df.parse(currentUTC);

                                Calendar calendar = Calendar.getInstance();
                                calendar.setTimeZone(TimeZone.getDefault());
                                calendar.setTime(callDate);

                                Calendar now = Calendar.getInstance();
                                now.setTimeZone(TimeZone.getDefault());
                                now.setTime(currentDate);

                                long diffSeconds = TimeUnit.MILLISECONDS.toSeconds(now.getTimeInMillis() - calendar.getTimeInMillis());


                                if (JoysaleApplication.getCurrentActivity() instanceof CallActivity) {
                                    CallActivity activity = (CallActivity) JoysaleApplication.getCurrentActivity();
                                    activity.finish();

                                } else {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        if (!CallActivity.isInCall) {
                                            CallActivity.isInCall = true;
                                            AppRTCUtils appRTCUtils = new AppRTCUtils(getApplicationContext());
                                            Intent callIntent = appRTCUtils.connectToRoom(userId, Constants.TAG_RECEIVE, type);
                                            callIntent.putExtra(Constants.TAG_CHAT_ID, chatId);
                                            callIntent.putExtra(Constants.TAG_USERNAME, userName);
                                            callIntent.putExtra(Constants.TAG_USERIMAGE, userImage);
                                            callIntent.putExtra(Constants.TAG_USERID, userId);
                                            callIntent.putExtra(Constants.TAG_ROOM_ID, roomId);
                                            callIntent.putExtra(Constants.TAG_PLATFORM, platform);
                                            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(callIntent);
                                        }
                                    }   else {
                                        TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                                        int isPhoneCallOn = telephony.getCallState();
                                        if (!CallActivity.isInCall && isPhoneCallOn == 0) {
                                            CallActivity.isInCall = true;
                                            AppRTCUtils appRTCUtils = new AppRTCUtils(getApplicationContext());
                                            Intent callIntent = appRTCUtils.connectToRoom(userId, Constants.TAG_RECEIVE, type);
                                            callIntent.putExtra(Constants.TAG_CHAT_ID, chatId);
                                            callIntent.putExtra(Constants.TAG_USERNAME, userName);
                                            callIntent.putExtra(Constants.TAG_USERIMAGE, userImage);
                                            callIntent.putExtra(Constants.TAG_USERID, userId);
                                            callIntent.putExtra(Constants.TAG_ROOM_ID, roomId);
                                            callIntent.putExtra(Constants.TAG_PLATFORM, platform);
                                            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(callIntent);
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }

        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                pref = getApplicationContext().getSharedPreferences("ChatPref",
                        MODE_PRIVATE);
                Map<String, String> map = new HashMap<String, String>();
//                map.put(Constants.TAG_AUTHORIZATION, pref.getString(Constants.TAG_AUTHORIZATION, ""));
                String authorization = !TextUtils.isEmpty(Constants.pref.getString(Constants.TAG_ACCESS_TOKENS, Constants.TAG_ACCESS_TOKENS_PREF))
                        ? Constants.pref.getString(Constants.TAG_ACCESS_TOKENS, Constants.TAG_ACCESS_TOKENS_PREF)
                        : "";
                map.put(Constants.TAG_AUTHORIZATION, Constants.pref.getString(Constants.TAG_ACCESS_TOKENS,Constants.TAG_ACCESS_TOKENS_PREF));
                Log.i(TAG, "getHeaders: " + map);
                return map;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put(Constants.TAG_ROOM_ID, roomId);
                Log.v(TAG, "checkRoomParams: " + map);
                return map;
            }
        };

        JoysaleApplication.getInstance().addToRequestQueue(req, TAG);
    }

    private void showSmallNotification(String userId, String type, String chatId, String message, String img, String userName, String userImage) {

        Intent intent = null;
        String title = getString(R.string.app_name);
        Random random = new Random();
        int m = random.nextInt(9999 - 1000) + 1000;
        switch (type) {
            case "friend_request":
                intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.putExtra("from", "notification");
                intent.putExtra("userId", userId);
                message = userName + " " + message;
                break;
            case "chat_message":
                if (userId.equals("")) {
                    userId = "0";
                }
                if (message.equals("")) {
                    message = "Sent a photo";
                }
                title = userName + " sent message";
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(Constants.TAG_CHAT_ID, chatId);
                map.put(Constants.TAG_USERIMAGE, img);
                map.put(Constants.TAG_USERNAME, userName);
                map.put(Constants.TAG_USERID, userId);
                map.put(Constants.TAG_BLOCKED_BY_ME, "false");
                map.put(Constants.TAG_BLOCK, "false");
                map.put(Constants.TAG_USER_STATUS, "1");
                map.put(Constants.TAG_ONLINE, "1");
                map.put(Constants.TAG_LAST_ONLINE, "0");

                intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra("data", map);
                intent.putExtra("position", 0);
                intent.putExtra("from", "notification");
                break;
//            case "premium_purchase":
//                intent = new Intent(getApplicationContext(), MainScreenActivity.class);
//                break;

            //Addon Call
            case "audio":
            case "video":
                if (!CallActivity.isInCall) {
                    intent = new Intent(getApplicationContext(), CallActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(Constants.TAG_TYPE, type);
                    intent.putExtra("from", "receive");
                    intent.putExtra(Constants.TAG_CHAT_ID, chatId);
                    intent.putExtra(Constants.TAG_USERNAME, userName);
                    intent.putExtra(Constants.TAG_IMAGE_URL, userImage);
                    intent.putExtra(Constants.TAG_USERID, userId);
                    break;
                }

            default:
//                intent = new Intent(getApplicationContext(), MainScreenActivity.class);
                intent = new Intent(getApplicationContext(), FragmentMainActivity.class);
                break;
        }

        int notifyID = 1;
        String CHANNEL_ID = "my_channel_01";// The id of the channel.
        CharSequence name = "my channel";// The user-visible name of the channel.


        int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);
        long when = System.currentTimeMillis();

        // Adds the back stack for the Intent (but not the Intent itself)
        PendingIntent resultPendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            resultPendingIntent = PendingIntent.getActivity(this, uniqueInt, intent,
                    PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_ONE_SHOT);
        } else {
            resultPendingIntent = PendingIntent.getActivity(this, uniqueInt, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        Notification notification;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Bitmap largeIcon = getNotificationLargeIcon(img);

        //Addon Call
        if (type.equals("audio") || type.equals("video")) {
//            intent = new Intent(this, CallActivity.class);
            // Creating a pending intent and wrapping our intent
            PendingIntent pendingIntent;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                pendingIntent = PendingIntent.getActivity(this, uniqueInt, intent,
                        PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_ONE_SHOT);
            } else {
                pendingIntent = PendingIntent.getActivity(this, uniqueInt, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
            }
            try {
                // Perform the operation associated with our pendingIntent
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }


        if (type.equalsIgnoreCase("audio") || type.equalsIgnoreCase("video")) {
            /* setFullScreenIntent(resultPendingIntent,true) automatically opens an activity */
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                notification = mBuilder.setContentIntent(resultPendingIntent)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setContentTitle(title)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(largeIcon)
                        .setContentText(message)
                        .setChannelId(CHANNEL_ID)
                        .setFullScreenIntent(resultPendingIntent, true)
                        .build();

                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(mChannel);
                }
            } else {
                notification = mBuilder.setSmallIcon(R.mipmap.ic_launcher).setTicker(title).setWhen(when)
                        .setContentIntent(resultPendingIntent)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setContentTitle(title)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(largeIcon)
                        .setContentText(message)
                        .setFullScreenIntent(resultPendingIntent, true)
                        .build();
            }
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                notification = mBuilder.setContentIntent(resultPendingIntent)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setContentTitle(title)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(largeIcon)
                        .setContentText(message)
                        .setChannelId(CHANNEL_ID)
                        .build();

                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(mChannel);
                }
            } else {
                notification = mBuilder.setSmallIcon(R.mipmap.ic_launcher).setTicker(title).setWhen(when)
                        .setContentIntent(resultPendingIntent)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setContentTitle(title)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(largeIcon)
                        .setContentText(message)
                        .build();
            }
        }

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        // Play default notification sound
        notification.defaults |= Notification.DEFAULT_SOUND;

        // Vibrate if vibrate is enabled
        notification.defaults |= Notification.DEFAULT_VIBRATE;

        if (type.equals("chat_message")) {
            if (GetSet.isMsgNotificationEnable()) {
                notificationManager.notify(userId, m, notification);
            }
        } else {
            notificationManager.notify(userId, m, notification);
        }


    }

    private Bitmap getAppLargeIcon() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.applogo);
        }
        return bitmap;
    }

    private Bitmap getNotificationLargeIcon(String imageUrl) {
        if (!TextUtils.isEmpty(imageUrl) && (imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))) {
            Bitmap remoteIcon = getBitmapFromURL(imageUrl);
            if (remoteIcon != null) {
                return remoteIcon;
            }
        }
        return getAppLargeIcon();
    }

    private Bitmap getBitmapFromURL(String strURL) {
        HttpURLConnection connection = null;
        InputStream input = null;
        try {
            URL url = new URL(strURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setDoInput(true);
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }
            input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ignored) {
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    private void callStatus(String userid) {
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.TAG_TOID,userid);
            map.put(Constants.SOAP_USERNAME,Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            Log.d(TAG, "callStatus: "+userid);
            Log.d(TAG, "callStatus: "+GetSet.getUserId());
            Call<HashMap<String, String>> call = apiInterface.getCallStatus(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {

                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {

                }

            });

        }
    }


}