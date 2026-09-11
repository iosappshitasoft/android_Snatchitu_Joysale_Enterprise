package com.ni.addons.call;


import static android.os.Build.VERSION.SDK_INT;
import static com.ni.addons.call.appRTC.util.AppRTCUtils.CAPTURE_PERMISSION_REQUEST_CODE;
import static com.ni.addons.call.appRTC.util.AppRTCUtils.EXTRA_AECDUMP_ENABLED;
import static com.ni.addons.call.appRTC.util.AppRTCUtils.EXTRA_AUDIOCODEC;
import static com.ni.addons.call.appRTC.util.AppRTCUtils.EXTRA_AUDIO_BITRATE;
import static com.ni.addons.call.appRTC.util.AppRTCUtils.EXTRA_CAMERA2;
import static com.ni.addons.call.appRTC.util.AppRTCUtils.EXTRA_CAPTURETOTEXTURE_ENABLED;
import static com.ni.addons.call.appRTC.util.AppRTCUtils.EXTRA_CMDLINE;
import static com.ni.addons.call.appRTC.util.AppRTCUtils.EXTRA_DISABLE_BUILT_IN_AEC;
import static com.ni.addons.call.appRTC.util.AppRTCUtils.EXTRA_DISABLE_BUILT_IN_AGC;
import static com.ni.addons.call.appRTC.util.AppRTCUtils.EXTRA_DISABLE_BUILT_IN_NS;
import static com.ni.addons.call.appRTC.util.AppRTCUtils.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF;
import static com.ni.addons.call.appRTC.util.AppRTCUtils.EXTRA_ENABLE_RTCEVENTLOG;
import static com.ni.addons.call.appRTC.util.AppRTCUtils.EXTRA_FLEXFEC_ENABLED;
import static com.ni.addons.call.appRTC.util.AppRTCUtils.EXTRA_HWCODEC_ENABLED;
import static com.ni.addons.call.appRTC.util.AppRTCUtils.EXTRA_NOAUDIOPROCESSING_ENABLED;
import static com.ni.addons.call.appRTC.util.AppRTCUtils.EXTRA_OPENSLES_ENABLED;
import static com.ni.addons.call.appRTC.util.AppRTCUtils.EXTRA_RUNTIME;
import static com.ni.addons.call.appRTC.util.AppRTCUtils.EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED;
import static com.ni.addons.call.appRTC.util.AppRTCUtils.EXTRA_VIDEOCODEC;
import static com.ni.addons.call.appRTC.util.AppRTCUtils.EXTRA_VIDEO_BITRATE;
import static com.ni.addons.call.appRTC.util.AppRTCUtils.EXTRA_VIDEO_CALL;
import static com.ni.addons.call.appRTC.util.AppRTCUtils.EXTRA_VIDEO_FPS;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.ni.addons.call.appRTC.AppRTCAudioManager;
import com.ni.addons.call.appRTC.AppRTCClient;
import com.ni.addons.call.appRTC.DirectRTCClient;
import com.ni.addons.call.appRTC.PeerConnectionClient;
import com.ni.addons.call.appRTC.UnhandledExceptionHandler;
import com.ni.addons.call.appRTC.WebSocketRTCClient;
import com.ni.addons.call.appRTC.util.AppRTCUtils;
import com.ni.helper.NetworkReceiver;
import com.ni.helper.PhoneStateReceiver;
import com.ni.helper.SignallingClient;
import com.ni.SnatchItUp.BaseActivity;
import com.ni.SnatchItUp.FragmentMainActivity;
import com.ni.SnatchItUp.JoysaleApplication;
import com.ni.SnatchItUp.R;
import com.ni.model.CallStatus;
import com.ni.utils.CommonFunctions;
import com.ni.utils.Constants;
import com.ni.utils.DefensiveClass;
import com.ni.utils.GetSet;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFileRenderer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class CallActivity extends BaseActivity implements NetworkReceiver.ConnectivityReceiverListener, PeerConnectionClient.PeerConnectionEvents, SignallingClient.SignalingInterface, AppRTCClient.SignalingEvents,
        PhoneStateReceiver.PhoneState, SensorEventListener, MediaPlayer.OnPreparedListener, CallControlsFragment.OnCallEvents {


    private static final String TAG = CallActivity.class.getSimpleName();
    //Declaration of Widgets
    ImageView btnAccept, btnDecline, userImageView, bgImg;
    TextView txtUserName, txtCallType, txtCallTime, txtSwipeUp, txtSwipeDown;
    RelativeLayout parentLay, callView;
    LinearLayout acceptLay;
    HashMap<String, String> partnerData = new HashMap<String, String>();
    Display display;

    //Declaration of Variables
    public static boolean isInCall = false, callEnd = false, callEndByReceiver = false, isCallPause = false;
    public static String fromID = "";

    List<PeerConnection.IceServer> iceServers = new LinkedList<PeerConnection.IceServer>();

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    SurfaceViewRenderer localVideoView;
    SurfaceViewRenderer remoteVideoView;


    @Nullable
    private VideoFileRenderer videoFileRenderer;
    private final ProxyVideoSink remoteProxyRenderer = new ProxyVideoSink();
    private final ProxyVideoSink localProxyVideoSink = new ProxyVideoSink();
    @Nullable
    private PeerConnectionClient peerConnectionClient;
    @Nullable
    private AppRTCClient appRtcClient;
    @Nullable
    private AppRTCClient.SignalingParameters signalingParameters;
    @Nullable
    private AppRTCAudioManager audioManager;
    private final List<VideoSink> remoteSinks = new ArrayList<>();
    private boolean commandLineRun, activityRunning;
    public GestureDetector gestureDetector;
    private AppRTCClient.RoomConnectionParameters roomConnectionParameters;
    @Nullable
    private PeerConnectionClient.PeerConnectionParameters peerConnectionParameters;
    private static Intent mediaProjectionPermissionResultData;
    private static int mediaProjectionPermissionResultCode;
    Vibrator vibrator;
    Ringtone ringtone;
    CountDownTimer callEmitTimer;
    Timer callTimer = new Timer();
    Animation downAnimation, upAnimation, fadeInAnimation;
    ToneGenerator toneGenerator;


    private boolean isCallAttend = false, isCallConnected = false, speaker = false, isScreenOn,
            isPeerReceived, connected, isError, callControlFragmentVisible,
            screenCaptureEnabled, isSwappedFeeds, isFrontCamera = true;

    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private static final String CPU_WAKELOCK = "MyApp::MyWakelockTag";
    public String chatType = "", name = "",
            toastText = "", userName = "", userImage = "", platform = "", roomId = "";
    public static String from = "", userId = "", partnerId = "", chatId = "";


    Dialog permissionDialog;
    private SensorManager mSensorManager;
    private Sensor mProximity;
    public static int CALL_TIME_OUT = 30000;
    private Intent intent;
    private Uri roomUri;
    private int displayHeight, displayWidth;
    private CallControlsFragment controlsFragment;
    PowerManager.WakeLock screenLock = null;


    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (!isConnected) {
            toastText = getString(R.string.poor_internet_connection);
            disconnect();
        }

    }

    @Override
    public void onLocalDescription(SessionDescription sdp) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appRtcClient != null) {
                    if (signalingParameters.initiator) {
                        appRtcClient.sendOfferSdp(sdp);
                    } else {
                        appRtcClient.sendAnswerSdp(sdp);
                    }
                }
                if (peerConnectionParameters.videoMaxBitrate > 0) {
                    Log.d(TAG, "Set video maximum bitrate: " + peerConnectionParameters.videoMaxBitrate);
                    peerConnectionClient.setVideoMaxBitrate(peerConnectionParameters.videoMaxBitrate);
                }
            }
        });
    }

    @Override
    public void onIceCandidate(IceCandidate candidate) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appRtcClient != null) {
                    appRtcClient.sendLocalIceCandidate(candidate);
                }
            }
        });

    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] candidates) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appRtcClient != null) {
                    appRtcClient.sendLocalIceCandidateRemovals(candidates);
                }
            }
        });

    }

    @Override
    public void onIceConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isCallAttend = true;
            }
        });

    }

    @Override
    public void onIceDisconnected() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connected = false;
                if (!NetworkReceiver.isConnected()) {
                    toastText = getString(R.string.poor_internet_connection);
                } else if (!isCallConnected) {
                    toastText = getString(R.string.call_cancelled);
                }
                disconnect();
            }
        });

    }


    private void updateVideoViews(final boolean remoteVisible) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (localVideoView != null) {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) localVideoView.getLayoutParams();
                    if (remoteVisible && params != null) {
                        params.width = getDisplayMetrics().widthPixels * 30 / 100;
                        params.height = getDisplayMetrics().heightPixels * 20 / 100;
                        params.bottomMargin = getDisplayMetrics().widthPixels * 40 / 100;
                        params.rightMargin = 20;
                        localVideoView.setLayoutParams(params);
                    }
                }
            }

        });

    }

    @Override
    public void onConnected() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtUserName.setVisibility(View.GONE);
                if (from.equals(Constants.TAG_RECEIVE)) {
                    startAudioManager();
                    if (controlsFragment != null) {
                        controlsFragment.setCallUI();
                    }
                } else {
                    stopTone();
                }
                connected = true;
                txtCallType.setVisibility(View.GONE);
                btnAccept.setVisibility(View.GONE);
                txtSwipeUp.setVisibility(View.GONE);
                if (chatType.equals(Constants.TAG_AUDIO)) {
                    txtCallTime.setVisibility(View.VISIBLE);
                    cancelCallEmitTimer();
                    startCountDown("answer");
                } else {
                    updateVideoViews(chatType.equals(Constants.TAG_VIDEO));
                }
                callConnected();
            }

        });

    }

    @Override
    public void onDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connected = false;
                disconnect();
            }
        });

    }

    @Override
    public void onPeerConnectionClosed() {

    }

    @Override
    public void onPeerConnectionStatsReady(StatsReport[] reports) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            }
        });

    }

    @Override
    public void onPeerConnectionError(String description) {

    }

    @Override
    public void onIceClosed() {

    }

    @Override
    public void onClosed() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (platform != null && platform.equals("ios")) {
                    connected = false;
                    disconnect();
                } else {
                    connected = false;
                    disconnect();
                }
            }
        });

    }

    @Override
    public void onAwayStatus(JSONObject online) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (online.has(Constants.TAG_TYPE) && online.get(Constants.TAG_TYPE).equals("away")) {
                        txtCallType.setVisibility(View.VISIBLE);
//                        txtCallType.setText(partnerData.get(Constants.TAG_USERNAME) + " " + getString(R.string.away_from_chat));
                        txtCallType.setText(intent.getExtras().getString(Constants.TAG_USERNAME) + " " + getString(R.string.away_from_chat));
                    } else {
                        txtCallType.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "onAwayStatus: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void onByeStatus(String bye) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                disconnect();
            }
        });

    }

    @Override
    public void onConnectedToRoom(AppRTCClient.SignalingParameters params) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "runca: " + params);
                onConnectedToRoomInternal(params);
            }
        });
    }

    @Override
    public void onRemoteDescription(SessionDescription sdp) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e(TAG, "Received remote SDP for non-initilized peer connection.");
                    return;
                }
                if (toneGenerator != null) {
                    toneGenerator.stopTone();
                    toneGenerator = null;
                }

                isPeerReceived = true;
                isCallConnected = true;
                peerConnectionClient.setRemoteDescription(sdp);
            }
        });

    }

    @Override
    public void onRemoteIceCandidate(IceCandidate candidate) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e(TAG, "Received ICE candidate for a non-initialized peer connection.");
                    return;
                }
                peerConnectionClient.addRemoteIceCandidate(candidate);
            }
        });

    }

    @Override
    public void onRemoteIceCandidatesRemoved(IceCandidate[] candidates) {
        if (peerConnectionClient == null) {
            Log.e(TAG, "Received ICE candidate removals for a non-initialized peer connection.");
            return;
        }
        peerConnectionClient.removeRemoteIceCandidates(candidates);

    }

    @Override
    public void onChannelClose() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                disconnect();
            }
        });

    }

    @Override
    public void onChannelError(String description) {
        reportError(description);
    }

    @Override
    public void onIncomingCall() {

        Log.i(TAG, "onIncomingCall: " + "incom");
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Log.e(TAG, "onSensorChanged: " + Arrays.toString(sensorEvent.values));
        if (sensorEvent.values[0] == 0) {
            //TODO Turn on screen
            if (chatType.equals(Constants.TAG_AUDIO)) {
                turnOnScreen(60000);
            }
        } else {
            //TODO Turn off screen
            if (chatType.equals(Constants.TAG_AUDIO)) {
                turnOffScreen();
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    public void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld())
            wakeLock.release();
    }

    public void releaseScreenWakeLock() {
        if (screenLock != null && screenLock.isHeld())
            screenLock.release();
    }


    public void turnOnScreen(int timeout) {
        releaseWakeLock();
        if (!isScreenOn) {
            if (wakeLock != null && !wakeLock.isHeld()) {
                if (timeout == 0 && wakeLock != null) wakeLock.acquire();
                else if (wakeLock != null) wakeLock.acquire(CALL_TIME_OUT);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true);
                setTurnScreenOn(true);
            } else {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            }
            isScreenOn = true;
        }
    }

    public void turnOffScreen() {
        // turn off screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && wakeLock != null) {
            releaseWakeLock();
            wakeLock.acquire();
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            /*WindowManager.LayoutParams params = getWindow().getAttributes();
            params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
            params.screenBrightness = 0;
            getWindow().setAttributes(params);*/
        }
        isScreenOn = false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }


    private static class ProxyVideoSink implements VideoSink {
        private VideoSink target;

        @Override
        synchronized public void onFrame(VideoFrame frame) {
            if (target == null) {
                Logging.d(TAG, "Dropping frame in proxy because target is null.");
                return;
            }

            target.onFrame(frame);
        }

        synchronized public void setTarget(VideoSink target) {
            this.target = target;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new UnhandledExceptionHandler(this));
        setContentView(R.layout.activity_call);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        isInCall = true;
//        MainScreenActivity.isInVideoCall = true;
        FragmentMainActivity.isInVideoCall = true;
        gestureDetector = new GestureDetector(this, new SingleTapConfirm());
        pref = getApplicationContext().getSharedPreferences("ChatPref",
                MODE_PRIVATE);

        editor = pref.edit();
        isInCall = true;
        connected = false;
        signalingParameters = null;
        intent = getIntent();


        chatType = intent.getExtras().getString(Constants.TAG_CHAT_TYPE);
        from = intent.getExtras().getString(Constants.TAG_FROM);
        userId = intent.getExtras().getString(Constants.TAG_USERID);
        userName = intent.getExtras().getString(Constants.TAG_USERNAME);
        userImage = intent.getExtras().getString(Constants.TAG_USERIMAGE_ONE);
        partnerId = intent.getExtras().getString(Constants.TAG_RECEIVER_ID);
        platform = intent.getExtras().getString(Constants.TAG_PLATFORM);
//        chatId = partnerData.get(Constants.TAG_CHAT_ID);
        chatId = intent.getExtras().getString(Constants.TAG_CHATID);
        fromID = intent.getStringExtra(Constants.TAG_USERID);
        roomUri = intent.getData();
        if (from.equals(Constants.TAG_RECEIVE)) {
            roomId = intent.getStringExtra(Constants.TAG_ROOM_ID);
        }
        initPowerOptions();

        initView();

        if (roomUri == null) {
            Log.e(TAG, "Didn't get any URL in intent!");
            setResult(RESULT_CANCELED);
            finishActivity();
            return;
        }

        if (from == null) {
            finishActivity();
        }

        /*f (chatType.equals(Constants.TAG_AUDIO)) {
            Log.i(TAG, "onCreate permission: " + checkCallPermissions(AppRTCUtils.MANDATORY_AUDIO_PERMISSIONS, this));

            if (!checkCallPermissions(AppRTCUtils.MANDATORY_AUDIO_PERMISSIONS, this)) {
                requestCallPermissions(AppRTCUtils.MANDATORY_AUDIO_PERMISSIONS, 101);
            } else {
                initRender();
            }
        } else {
            if (!checkCallPermissions(AppRTCUtils.MANDATORY_VIDEO_PERMISSIONS, this)) {
                requestCallPermissions(AppRTCUtils.MANDATORY_VIDEO_PERMISSIONS, 101);
            } else {
                initRender();
            }
        }*/
        if (chatType.equals(Constants.TAG_AUDIO)) {

            if (SDK_INT>=Build.VERSION_CODES.S){
                if (!checkCallPermissions(AppRTCUtils.MANDATORY_AUDIO_PERMISSIONS, this)) {
                    requestCallPermissions(AppRTCUtils.MANDATORY_AUDIO_PERMISSIONS, 101);
                } else {
                    initRender();
                }
            }else{
                if (!checkCallPermissions(AppRTCUtils.MANDATORY_AUDIO_PERMISSIONS, this)) {
                    requestCallPermissions(AppRTCUtils.MANDATORY_AUDIO_PERMISSIONS, 101);
                } else {
                    initRender();
                }
            }

        } else {
            if (SDK_INT>=Build.VERSION_CODES.S){
                if (!checkCallPermissions(AppRTCUtils.MANDATORY_VIDEO_PERMISSIONS, this)) {
                    requestCallPermissions(AppRTCUtils.MANDATORY_VIDEO_PERMISSIONS, 101);
                } else {
                    initRender();
                }
            }else{
                if (!checkCallPermissions(AppRTCUtils.MANDATORY_VIDEO_PERMISSIONS, this)) {
                    requestCallPermissions(AppRTCUtils.MANDATORY_VIDEO_PERMISSIONS, 101);
                } else {
                    initRender();
                }
            }

        }


    }


    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }
    }

    private void initPowerOptions() {
        if ((getSystemService(POWER_SERVICE)) != null) {
            screenLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(
                    PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, CPU_WAKELOCK);
            screenLock.acquire(CALL_TIME_OUT);
        }
    }

    private void reportError(final String description) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isError) {
                    isError = true;
                    disconnectWithErrorMessage(description);
                }
            }
        });
    }

    private void setSwappedFeeds(boolean isSwappedFeeds) {
        Logging.d(TAG, "setSwappedFeeds: " + isSwappedFeeds);
        this.isSwappedFeeds = isSwappedFeeds;
        localProxyVideoSink.setTarget(isSwappedFeeds ? remoteVideoView : localVideoView);
        remoteProxyRenderer.setTarget(isSwappedFeeds ? localVideoView : remoteVideoView);
    }

    // Should be called from UI thread
    private void callConnected() {
        if (peerConnectionClient == null || isError) {
            Log.w(TAG, "Call is connected in closed or error state");
            return;
        }
        // Enable statistics callback.
        peerConnectionClient.enableStatsEvents(true, AppRTCUtils.STAT_CALLBACK_PERIOD);
        setSwappedFeeds(false /* isSwappedFeeds */);
        setMirror(false);
    }

    private void setMirror(boolean isSwappedFeeds) {
        if (isSwappedFeeds) {
            remoteVideoView.setMirror(true);
            localVideoView.setMirror(false);
        } else {
            remoteVideoView.setMirror(false);
            localVideoView.setMirror(true);
        }
    }


    @TargetApi(17)
    private DisplayMetrics getDisplayMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager =
                (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
        return displayMetrics;
    }

    private void disconnectWithErrorMessage(final String errorMessage) {
        disconnect();
        /*if (commandLineRun || !activityRunning) {
            Log.e(TAG, "Critical error: " + errorMessage);
            disconnect();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(getText(R.string.channel_error_title))
                    .setMessage(errorMessage)
                    .setCancelable(false)
                    .setNeutralButton(getString(R.string.okay),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    disconnect();
                                }
                            })
                    .create()
                    .show();
        }*/
    }


    private void initRender() {

        final EglBase rootEglBase = EglBase.create();
        localVideoView.init(rootEglBase.getEglBaseContext(), null);
        localVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        String saveRemoteVideoToFile = intent.getStringExtra(AppRTCUtils.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE);
        // When saveRemoteVideoToFile is set we save the video from the remote to a file.
        if (saveRemoteVideoToFile != null) {
            int videoOutWidth = intent.getIntExtra(AppRTCUtils.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH, 0);
            int videoOutHeight = intent.getIntExtra(AppRTCUtils.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT, 0);
            try {
                videoFileRenderer = new VideoFileRenderer(
                        saveRemoteVideoToFile, videoOutWidth, videoOutHeight, rootEglBase.getEglBaseContext());
                remoteSinks.add(videoFileRenderer);
            } catch (IOException e) {
                throw new RuntimeException(
                        "Failed to open video file for output: " + saveRemoteVideoToFile, e);
            }
        }
        remoteVideoView.init(rootEglBase.getEglBaseContext(), null);
        remoteVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        localVideoView.setZOrderMediaOverlay(true);
        localVideoView.setEnableHardwareScaler(true);
        remoteVideoView.setEnableHardwareScaler(false);
        // Start with local feed in fullscreen and swap it to the pip when the call is connected.
        setSwappedFeeds(true /* isSwappedFeeds */);

        // Check for mandatory permissions.
        if (chatType.equals(Constants.TAG_AUDIO)) {
            for (String permission : AppRTCUtils.MANDATORY_AUDIO_PERMISSIONS) {
                if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    setResult(RESULT_CANCELED);
                    finishActivity();
                    return;
                }
            }
        } else {
            for (String permission : AppRTCUtils.MANDATORY_VIDEO_PERMISSIONS) {
                if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    setResult(RESULT_CANCELED);
                    finishActivity();
                    return;
                }
            }
        }

        boolean loopback = intent.getBooleanExtra(AppRTCUtils.EXTRA_LOOPBACK, false);
        boolean tracing = intent.getBooleanExtra(AppRTCUtils.EXTRA_TRACING, false);

        int videoWidth = intent.getIntExtra(AppRTCUtils.EXTRA_VIDEO_WIDTH, 0);
        int videoHeight = intent.getIntExtra(AppRTCUtils.EXTRA_VIDEO_HEIGHT, 0);
        screenCaptureEnabled = intent.getBooleanExtra(AppRTCUtils.EXTRA_SCREENCAPTURE, false);
        // If capturing format is not specified for screencapture, use screen resolution.
        if (screenCaptureEnabled && videoWidth == 0 && videoHeight == 0) {
            DisplayMetrics displayMetrics = getDisplayMetrics();
            videoWidth = displayMetrics.widthPixels;
            videoHeight = displayMetrics.heightPixels;
        }


        startCountDown("waiting");

        PeerConnectionClient.DataChannelParameters dataChannelParameters = null;
        if (intent.getBooleanExtra(AppRTCUtils.EXTRA_DATA_CHANNEL_ENABLED, false)) {
            dataChannelParameters = new PeerConnectionClient.DataChannelParameters(intent.getBooleanExtra(AppRTCUtils.EXTRA_ORDERED, true),
                    intent.getIntExtra(AppRTCUtils.EXTRA_MAX_RETRANSMITS_MS, -1),
                    intent.getIntExtra(AppRTCUtils.EXTRA_MAX_RETRANSMITS, -1), intent.getStringExtra(AppRTCUtils.EXTRA_PROTOCOL),
                    intent.getBooleanExtra(AppRTCUtils.EXTRA_NEGOTIATED, false), intent.getIntExtra(AppRTCUtils.EXTRA_ID, -1));
        }
        peerConnectionParameters =
                new PeerConnectionClient.PeerConnectionParameters(intent.getBooleanExtra(EXTRA_VIDEO_CALL, false), loopback,
                        tracing, videoWidth, videoHeight, intent.getIntExtra(EXTRA_VIDEO_FPS, 0),
                        intent.getIntExtra(EXTRA_VIDEO_BITRATE, 0), intent.getStringExtra(EXTRA_VIDEOCODEC),
                        intent.getBooleanExtra(EXTRA_HWCODEC_ENABLED, true),
                        intent.getBooleanExtra(EXTRA_FLEXFEC_ENABLED, false),
                        intent.getIntExtra(EXTRA_AUDIO_BITRATE, 0), intent.getStringExtra(EXTRA_AUDIOCODEC),
                        intent.getBooleanExtra(EXTRA_NOAUDIOPROCESSING_ENABLED, false),
                        intent.getBooleanExtra(EXTRA_AECDUMP_ENABLED, false),
                        intent.getBooleanExtra(EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED, false),
                        intent.getBooleanExtra(EXTRA_OPENSLES_ENABLED, false),
                        intent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_AEC, false),
                        intent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_AGC, false),
                        intent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_NS, false),
                        intent.getBooleanExtra(EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, false),
                        intent.getBooleanExtra(EXTRA_ENABLE_RTCEVENTLOG, false), dataChannelParameters);
        commandLineRun = intent.getBooleanExtra(EXTRA_CMDLINE, false);
        int runTimeMs = intent.getIntExtra(EXTRA_RUNTIME, 0);

        // Create connection client. Use DirectRTCClient if room name is an IP otherwise use the
        // standard WebSocketRTCClient.
        if (loopback || !DirectRTCClient.IP_PATTERN.matcher(roomId).matches()) {
            appRtcClient = new WebSocketRTCClient(this);
        } else {
            Log.i(TAG, "Using DirectRTCClient because room name looks like an IP.");
            appRtcClient = new DirectRTCClient(this);
        }
// Create connection parameters.
        String urlParameters = intent.getStringExtra(AppRTCUtils.EXTRA_URLPARAMETERS);
        roomConnectionParameters =
                new AppRTCClient.RoomConnectionParameters(roomUri.toString(), roomId, loopback, urlParameters);
        // Send intent arguments to fragments.
        controlsFragment.setContext(this);
        controlsFragment.setControlEvent(this);
        Bundle controlBundle = new Bundle();
        controlBundle.putString(Constants.TAG_CHAT_TYPE, chatType);
        controlBundle.putString(Constants.TAG_FROM, from);
        controlBundle.putBoolean(Constants.TAG_AUDIO, speaker);
        controlsFragment.setArguments(controlBundle);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.call_fragment_container, controlsFragment);
        ft.commit();

        // For command line execution run connection for <runTimeMs> and exit.
        if (commandLineRun && runTimeMs > 0) {
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    disconnect();
                }
            }, runTimeMs);
        }
        // Create peer connection client.
        peerConnectionClient = new PeerConnectionClient(
                getApplicationContext(), rootEglBase, peerConnectionParameters, CallActivity.this);
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        if (loopback) {
            options.networkIgnoreMask = 0;
        }
        peerConnectionClient.createPeerConnectionFactory(options);

        if (screenCaptureEnabled) {
            startScreenCapture();
        } else {
            startCall();
        }
    }


    private void startCall() {
        if (appRtcClient == null) {
            Log.e(TAG, "AppRTC client is not allocated for a call.");
            return;
        }
        // Start room connection.
        appRtcClient.connectToRoom(roomConnectionParameters);

        if (audioManager.hasWiredHeadset()) {
            audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.WIRED_HEADSET);
            speaker = false;
        } else {
            if (chatType.equals(Constants.TAG_VIDEO)) {
                audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
                speaker = true;
            } else {
                audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
                speaker = false;
            }
        }
        if (from.equals(Constants.TAG_SEND)) {
            startAudioManager();
        }
    }


    @TargetApi(21)
    private void startScreenCapture() {
        MediaProjectionManager mediaProjectionManager =
                (MediaProjectionManager) getApplication().getSystemService(
                        Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(), CAPTURE_PERMISSION_REQUEST_CODE);
    }


    private boolean checkCallPermissions(String[] permissionList, CallActivity activity) {
        boolean isPermissionsGranted = false;
        for (String permission : permissionList) {
            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
                isPermissionsGranted = true;
            } else {
                isPermissionsGranted = false;
                break;
            }
        }
        return isPermissionsGranted;
    }

    private boolean shouldShowRationale(String[] permissions, CallActivity activity) {
        boolean showRationale = false;
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                showRationale = true;
            } else {
                showRationale = false;
                break;
            }
        }
        return showRationale;
    }


    private void startAudioManager() {
        // Store existing audio settings and change audio mode to
        // MODE_IN_COMMUNICATION for best possible VoIP performance.
        Log.d(TAG, "Starting the audio manager...");
        audioManager.start(new AppRTCAudioManager.AudioManagerEvents() {
            // This method will be called each time the number of available audio
            // devices has changed.
            @Override
            public void onAudioDeviceChanged(
                    AppRTCAudioManager.AudioDevice audioDevice, Set<AppRTCAudioManager.AudioDevice> availableAudioDevices) {
                onAudioManagerDevicesChanged(audioDevice, availableAudioDevices);
            }
        });
    }

    private void onAudioManagerDevicesChanged(
            final AppRTCAudioManager.AudioDevice device,
            final Set<AppRTCAudioManager.AudioDevice> availableDevices) {
        Log.v(TAG, "onAudioManagerDevicesChanged: " + availableDevices + ", "
                + "selected: " + device);
        // TODO(henrika): add callback handler.
    }

    private void requestCallPermissions(String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(CallActivity.this, permissions, requestCode);
    }


    public void disconnect() {
        stopCallForegroundService();
        isInCall = false;
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel("howzucall", 0);
        }
        SignallingClient.getInstance().close(roomId);
        remoteProxyRenderer.setTarget(null);
        localProxyVideoSink.setTarget(null);
        activityRunning = false;

        if (appRtcClient != null) {
            appRtcClient.disconnectFromRoom();
            appRtcClient = null;
        }

        if (peerConnectionClient != null) {
            peerConnectionClient.close();
            peerConnectionClient = null;
        }
        if (localVideoView != null) {
            localVideoView.release();
            localVideoView = null;
        }
        if (remoteVideoView != null) {
            remoteVideoView.release();
            remoteVideoView = null;
        }
        if (audioManager != null) {
            audioManager.stop();
            audioManager = null;
        }
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
        if (ringtone != null) {
            ringtone.stop();
            ringtone = null;
        }
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }
        if (callTimer != null) {
            callTimer.cancel();
            callTimer = null;
        }

        finishActivity();
    }

    private void startTone() {
        if (toneGenerator != null)
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_NETWORK_USA_RINGBACK, CALL_TIME_OUT);
    }

    private void stopTone() {
        if (toneGenerator != null)
            toneGenerator.stopTone();
    }

    private void cancelCallEmitTimer() {
        if (callEmitTimer != null) {
            callEmitTimer.cancel();
            callEmitTimer = null;
        }
    }


    //function for rtcmessage api
    private void callApi(String from) {

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_CALLING,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        Log.v(TAG, "makecallApiRes" + res);
                        try {
                            JSONObject json = new JSONObject(res);
                            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                            if (response.equalsIgnoreCase("true")) {

                            } else {
                                Toast.makeText(CallActivity.this, json.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
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
                Map<String, String> map = new HashMap<String, String>();
                map.put(Constants.TAG_AUTHORIZATION, Constants.pref.getString(Constants.TAG_ACCESS_TOKENS, Constants.TAG_ACCESS_TOKENS_PREF));
                Log.i(TAG, "getHeaders: " + map);
                return map;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();

                map.put(Constants.TAG_FROMID, GetSet.getUserId());
                map.put(Constants.TAG_TOID, partnerId);
                map.put(Constants.TAG_CHATID, chatId);
                map.put(Constants.TAG_PLATFORM, Constants.TAG_ANDROID);
                map.put(Constants.TAG_ROOM_ID, roomId);
                CommonFunctions functions = new CommonFunctions(CallActivity.this);
                map.put(Constants.TAG_TIMESTAMP, functions.getCurrentUTCTime());
                if (from.equals("audio")) {
                    map.put(Constants.TAG_TYPE, from);
                } else {
                    map.put(Constants.TAG_TYPE, from);
                }
                map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
                map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
                Log.v(TAG, "makecallApiParams: " + new Gson().toJson(map));
                return map;
            }
        };

        JoysaleApplication.getInstance().addToRequestQueue(req, TAG);

    }


    public void stopAudioManager() {
        if (audioManager != null) {
            /*Reset audio manager to previous state*/
            audioManager.stop();
            audioManager = null;
        }
    }


    private void startCountDown(String callType) {
        if (callType.equals("waiting")) {
            if (from.equals(Constants.TAG_SEND)) {
                try {
                    roomId = GetSet.getUserId() + System.currentTimeMillis();
                    SignallingClient.getInstance().createOrJoin(roomId);
                    callApi(chatType);
                    callEmitTimer = new CountDownTimer(CALL_TIME_OUT, 1000) {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onTick(long millisUntilFinished) {
                            if (!NetworkReceiver.isConnected()) {
                                finishActivity();
                            } else if (isCallConnected && isCallAttend) {
                                cancelCallEmitTimer();
                            }
                        }

                        @Override
                        public void onFinish() {
                            cancelCallEmitTimer();
                            stopAudioManager();
                            releaseScreenWakeLock();
                            if (from.equals(Constants.TAG_SEND) && !connected) {
                                missedCallAlert(chatType);
                            }
                            finishActivity();
                        }
                    };
                    callEmitTimer.start();
                } catch (Exception e) {
                    Log.e(TAG, "startTimer: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                callEmitTimer = new CountDownTimer(CALL_TIME_OUT, 1000) {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onTick(long millisUntilFinished) {
                        if (!NetworkReceiver.isConnected()) {
                            finishActivity();
                        } else if (isCallConnected && isCallAttend) {
                            cancelCallEmitTimer();
                        }
                    }

                    @Override
                    public void onFinish() {
                        cancelCallEmitTimer();
                        releaseScreenWakeLock();
                        stopAudioManager();
                        disconnect();
                        finishActivity();
                    }
                };
                callEmitTimer.start();
//                txtCallType.setVisibility(View.VISIBLE);
//                txtCallType.setText(getString(R.string.connecting));
            }
        } else if (callType.equals("answer")) {
            long startTime = SystemClock.elapsedRealtime();
            callTimer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    //Function call every second
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.v(TAG, "onChronometerTick");
                            long hours = (((SystemClock.elapsedRealtime() - startTime) / 1000) / 60) / 60;
                            long minutes = ((SystemClock.elapsedRealtime() - startTime) / 1000) / 60;
                            long seconds = ((SystemClock.elapsedRealtime() - startTime) / 1000) % 60;
                            txtCallTime.setText(twoDigitString(hours) + ":" + twoDigitString(minutes) + ":" + twoDigitString(seconds));
                        }
                    });
                }
            }, 0, 1000);
        }
    }


    private void missedCallAlert(String type) {
        /*Constants.API_IOS_MISSED_ALERT It used only to show missed call alert in I-OS device*/
        StringRequest request = new StringRequest(Request.Method.POST, Constants.API_END_CALL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String request) {
                        Log.v(TAG, "endCallRes: " + request);
                        try {
                            JSONObject json = new JSONObject(request);
                            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                            if (response.equalsIgnoreCase("true")) {

                            } else {
                                Toast.makeText(CallActivity.this, json.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
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
                Map<String, String> map = new HashMap<String, String>();
                map.put(Constants.TAG_AUTHORIZATION, Constants.pref.getString(Constants.TAG_ACCESS_TOKENS, Constants.TAG_ACCESS_TOKENS_PREF));
                return map;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();

                map.put(Constants.TAG_FROMID, GetSet.getUserId());
                map.put(Constants.TAG_TOID, partnerId);
                map.put(Constants.TAG_ROOM_ID, roomId);
                map.put(Constants.TAG_CHAT_ID, chatId);
                map.put(Constants.TAG_TYPE, "bye");
                map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
                map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
                Log.v(TAG, "endCallParams: " + map);

                return map;
            }
        };

//        HowzuApplication.getInstance().addToRequestQueue(request, TAG);
        JoysaleApplication.getInstance().addToRequestQueue(request, TAG);

        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_MISSED_CALL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        Log.v(TAG, "missedCallRes: " + res);
                        try {
                            JSONObject json = new JSONObject(res);
                            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                            if (response.equalsIgnoreCase("true")) {

                            } else {
                                Toast.makeText(CallActivity.this, json.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
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
                Map<String, String> map = new HashMap<String, String>();
                map.put(Constants.TAG_AUTHORIZATION, Constants.pref.getString(Constants.TAG_ACCESS_TOKENS, Constants.TAG_ACCESS_TOKENS_PREF));
                return map;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put(Constants.TAG_FROMID, GetSet.getUserId());
                map.put(Constants.TAG_TOID, partnerId);
                map.put(Constants.TAG_CHATID, chatId);
                map.put(Constants.TAG_ROOM_ID, roomId);
                map.put(Constants.TAG_CHATTIME, String.valueOf(System.currentTimeMillis() / 1000L));
                map.put(Constants.TAG_TYPE, type);
                map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
                map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
                Log.v(TAG, "missedCallParams: " + map);

                return map;
            }
        };

//        HowzuApplication.getInstance().addToRequestQueue(req, TAG);
        JoysaleApplication.getInstance().addToRequestQueue(req, TAG);

    }

    private String twoDigitString(long number) {
        if (number == 0) {
            return "00";
        } else if (number / 10 == 0) {
            return "0" + number;
        }
        return String.valueOf(number);
    }

    private void onConnectedToRoomInternal(final AppRTCClient.SignalingParameters params) {

        signalingParameters = params;
        VideoCapturer videoCapturer = null;
        if (peerConnectionParameters != null && peerConnectionParameters.videoCallEnabled) {
            videoCapturer = createVideoCapturer();
        }
        peerConnectionClient.createPeerConnection(
                localProxyVideoSink, remoteSinks, videoCapturer, signalingParameters);

        if (signalingParameters != null) {
            if (signalingParameters.initiator) {
                // Create offer. Offer SDP will be sent to answering client in
                // PeerConnectionEvents.onLocalDescription event.
                peerConnectionClient.createOffer();
            } else {
                if (params.offerSdp != null) {
                    peerConnectionClient.setRemoteDescription(params.offerSdp);
                    // Create answer. Answer SDP will be sent to offering client in
                    // PeerConnectionEvents.onLocalDescription event.
                    isPeerReceived = true;
                    if (!from.equals(Constants.TAG_RECEIVE)) {
                        peerConnectionClient.createAnswer();
                    }
                }
                if (params.iceCandidates != null) {
                    // Add remote ICE candidates from room.
                    for (IceCandidate iceCandidate : params.iceCandidates) {
                        peerConnectionClient.addRemoteIceCandidate(iceCandidate);
                    }
                }
            }
        }
    }


    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(this) && intent.getBooleanExtra(EXTRA_CAMERA2, true);
    }

    private boolean captureToTexture() {
        return intent.getBooleanExtra(EXTRA_CAPTURETOTEXTURE_ENABLED, false);
    }

    private @Nullable
    VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Logging.d(TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        Logging.d(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }


    //initialize the view
    private void initView() {
        parentLay = findViewById(R.id.parentLay);
        callView = findViewById(R.id.callView);
        userImageView = findViewById(R.id.userImage);
        txtUserName = findViewById(R.id.userName);
        txtCallType = findViewById(R.id.callType);
        localVideoView = findViewById(R.id.local_video_view);
        remoteVideoView = findViewById(R.id.remote_video_view);
        acceptLay = findViewById(R.id.acceptLay);
        btnAccept = findViewById(R.id.btnAccept);
        btnDecline = findViewById(R.id.btnDecline);
        txtCallTime = findViewById(R.id.callTime);
        bgImg = findViewById(R.id.bgImg);
        txtSwipeUp = findViewById(R.id.swipeUp);
        txtSwipeDown = findViewById(R.id.swipeDown);
        toneGenerator = new ToneGenerator(AudioManager.STREAM_VOICE_CALL, 100);
        upAnimation = AnimationUtils.loadAnimation(CallActivity.this, R.anim.slide_up);
        downAnimation = AnimationUtils.loadAnimation(CallActivity.this, R.anim.slide_down);
        fadeInAnimation = AnimationUtils.loadAnimation(CallActivity.this, R.anim.fade_in);
        SignallingClient.getInstance().init(this);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        displayHeight = displayMetrics.heightPixels;
        displayWidth = displayMetrics.widthPixels;
        // Create and audio manager that will take care of audio routing,
        // audio modes, audio device enumeration etc.
        audioManager = AppRTCAudioManager.create(CallActivity.this);
        powerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        mSensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        wakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, CPU_WAKELOCK);
        isScreenOn = powerManager.isInteractive();
        controlsFragment = new CallControlsFragment();

        // Show/hide call control fragment on view click.
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleCallControlFragmentVisibility();
            }
        };

        // Swap feeds on pip view click.
        localVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSwappedFeeds(!isSwappedFeeds);
                if (!isFrontCamera) {
                    remoteVideoView.setMirror(false);
                    localVideoView.setMirror(false);
                } else {
                    setMirror(isSwappedFeeds);
                }
            }
        });

        remoteVideoView.setOnClickListener(listener);
        remoteSinks.add(remoteProxyRenderer);

        if (chatType.equals(Constants.TAG_AUDIO)) {
            bgImg.setVisibility(View.VISIBLE);
            Log.d(TAG, "initView: " + GetSet.getImageUrl());
            Picasso.get()
                    .load(GetSet.getImageUrl())
                    .placeholder(GetSet.getUserPlaceHolder())
                    .error(GetSet.getUserPlaceHolder())
//                    .centerCrop()
//                    .resize(Constants.IMG_WT_HT, Constants.IMG_WT_HT)
                    .into(bgImg);

            String partnerImage = intent.getExtras().getString(Constants.TAG_USERIMAGE_ONE);
            Log.d(TAG, "initView: " + partnerImage);
            Picasso.get()
                    .load(partnerImage)
                    .placeholder(R.drawable.user_placeholder)
                    .error(R.drawable.user_placeholder)
                    .centerCrop()
                    .resize(Constants.IMG_WT_HT, Constants.IMG_WT_HT)
                    .into(userImageView);

            txtCallType.setText(R.string.audio_calling);
        } else if (chatType.equals(Constants.TAG_VIDEO)) {
            txtCallType.setText(R.string.video_calling);
            userImageView.setVisibility(View.GONE);
            speaker = true;
        }

        if (from.equals(Constants.TAG_RECEIVE)) {
            Log.d(TAG, "initView: " + intent.getExtras().getString(Constants.TAG_USERNAME));
            SignallingClient.getInstance().createOrJoin(roomId);
//            txtUserName.setText(partnerData.get(Constants.TAG_USERNAME));
            txtUserName.setText(intent.getExtras().getString(Constants.TAG_USERNAME));
            acceptLay.setVisibility(View.VISIBLE);

            btnAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    releaseScreenWakeLock();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (ContextCompat.checkSelfPermission(CallActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                            if (NetworkReceiver.isConnected()) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (ringtone != null) {
                                            ringtone.stop();
                                            ringtone = null;
                                        }
                                        if (vibrator != null) {
                                            vibrator.cancel();
                                            vibrator = null;
                                        }
                                        isCallAttend = true;
                                        isCallConnected = true;
                                        btnAccept.setVisibility(View.GONE);
                                        btnDecline.setVisibility(View.GONE);
                                        onCallAccept();
                                    }
                                });
                            } else {
                                Toast.makeText(getApplicationContext(), getString(R.string.poor_internet_connection), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            JoysaleApplication.dialog(CallActivity.this, "Please Enable Phone Settings to attend the call", "call");
                        }
                    } else {
                        if (NetworkReceiver.isConnected()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (ringtone != null) {
                                        ringtone.stop();
                                        ringtone = null;
                                    }
                                    if (vibrator != null) {
                                        vibrator.cancel();
                                        vibrator = null;
                                    }
                                    isCallAttend = true;
                                    isCallConnected = true;
                                    btnAccept.setVisibility(View.GONE);
                                    btnDecline.setVisibility(View.GONE);
                                    onCallAccept();
                                }
                            });
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.poor_internet_connection), Toast.LENGTH_SHORT).show();
                        }
                    }

                }
            });

            btnDecline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onCallHangUp();
                }
            });

            Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            if (alert == null) {
                // alert is null, using backup
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                // I can'timer see this ever being null (as always have a default notification)
                // but just incase
                if (alert == null) {
                    // alert backup is null, using 2nd backup
                    alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL);
                }
            }
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            long[] pattern = {0, 100, 1000, 300, 200, 100, 500, 200, 100};
            switch (audio.getRingerMode()) {
                case AudioManager.RINGER_MODE_NORMAL:
                    ringtone = RingtoneManager.getRingtone(getApplicationContext(), alert);
                    if (!ringtone.isPlaying())
                        ringtone.play();
                    vibrator.vibrate(pattern, 0);
                    break;
                case AudioManager.RINGER_MODE_SILENT:
                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    vibrator.vibrate(pattern, 0);
                    break;
            }
            turnOnScreen(CALL_TIME_OUT);
        } else {
            btnAccept.setVisibility(View.GONE);
            txtSwipeUp.setVisibility(View.GONE);
            btnDecline.setVisibility(View.VISIBLE);
            txtSwipeDown.setVisibility(View.VISIBLE);

//            txtUserName.setText(partnerData.get(Constants.TAG_USERNAME));
            txtUserName.setText(intent.getExtras().getString(Constants.TAG_USERNAME));
            fromID = GetSet.getUserId();
            startTone();

            btnDecline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onCallHangUp();
                }
            });

            if (audioManager.hasWiredHeadset()) {
                audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.WIRED_HEADSET);
                speaker = false;
            } else {
                if (chatType.equals(Constants.TAG_VIDEO)) {
                    audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
                    speaker = true;
                }
            }

        }
    }

    @Override
    public void onCallHangUp() {
        releaseScreenWakeLock();
        if (from.equals(Constants.TAG_SEND) && !connected) {
            missedCallAlert(chatType);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopAudioManager();
                if (isCallAttend) {
                    toastText = getString(R.string.call_ended);
                } else {
                    toastText = getString(R.string.call_cancelled);
                }
                disconnect();
            }
        });
    }

    @Override
    public void onCameraSwitch() {

        if (peerConnectionClient != null) {
            peerConnectionClient.switchCamera();
            isFrontCamera = !isFrontCamera;
            if (!isFrontCamera) {
                remoteVideoView.setMirror(false);
                localVideoView.setMirror(false);
            } else {
                setMirror(false);
            }
        }

    }

    @Override
    public boolean onToggleMic(boolean isMicEnabled) {
        if (peerConnectionClient != null) {
            peerConnectionClient.setAudioEnabled(isMicEnabled);
        }
        return isMicEnabled;
        // return false;
    }

    @Override
    public boolean onToggleSpeaker() {
        return setSpeaker(!speaker);
        //return false;
    }

    public boolean setSpeaker(boolean speakerOn) {
        if (speakerOn) {
            speaker = true;
            audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
        } else {
            speaker = false;
            if (audioManager.hasWiredHeadset()) {
                audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.WIRED_HEADSET);
            } else {
                audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
            }
        }
        return speaker;
    }

    private Handler answerHandler = new Handler();
    int delay = 1000; //milliseconds
    Runnable answerRunnable = new Runnable() {

        @Override
        public void run() {
            onCallAccept();
        }
    };


    private void onCallAccept() {
        if (signalingParameters != null && !signalingParameters.initiator && isPeerReceived) {
            /*Remove handler once call connected*/
            answerHandler.removeCallbacks(answerRunnable);
            // Create answer. Answer SDP will be sent to offering client in
            // PeerConnectionEvents.onLocalDescription event.
            peerConnectionClient.createAnswer();
        } else {
            txtCallType.setText(getString(R.string.connecting));
            answerHandler.postDelayed(answerRunnable, delay);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (!checkCallPermissions(permissions, CallActivity.this)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRationale(permissions, CallActivity.this)) {
                        requestCallPermissions(permissions, 101);
                    } else {
                        openPermissionDialog("Camera, Record Audio, Phone");
                    }
                }
            } else {
                initRender();
            }
        }
    }


    private void openPermissionDialog(String permissionList) {
        permissionDialog = new Dialog(CallActivity.this);
        permissionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        permissionDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        permissionDialog.setContentView(R.layout.logout_dialog);
        permissionDialog.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels * 85 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        permissionDialog.setCancelable(false);
        permissionDialog.setCanceledOnTouchOutside(false);

        TextView title = permissionDialog.findViewById(R.id.headerTxt);
        TextView subTxt = permissionDialog.findViewById(R.id.subTxt);
        TextView yes = permissionDialog.findViewById(R.id.yes);
        TextView no = permissionDialog.findViewById(R.id.no);
        title.setVisibility(View.GONE);
        subTxt.setText("This app requires " + permissionList + " permissions to access the features. Please turn on");
        yes.setText(R.string.grant);
        no.setText(R.string.nope);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permissionDialog.dismiss();
                disconnect();
                missedCallAlert(chatType);
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
                finishActivity();
            }
        });

        no.setVisibility(View.GONE);

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissionDialog.isShowing())
                    permissionDialog.dismiss();
                disconnect();
            }
        });
        permissionDialog.show();
    }


    // Helper functions.
    private void toggleCallControlFragmentVisibility() {
        if (!connected || !controlsFragment.isAdded()) {
            return;
        }

        // Show/hide call control fragment
        callControlFragmentVisible = !callControlFragmentVisible;
        FragmentTransaction callTransaction = getSupportFragmentManager().beginTransaction();
        /*Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.anim_slide_up);
        Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.anim_slide_down);*/
        if (callControlFragmentVisible) {
            callTransaction.show(controlsFragment);
            /*slideUp.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    acceptLay.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            acceptLay.startAnimation(slideUp);*/
        } else {
            callTransaction.hide(controlsFragment);
            /*slideDown.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    acceptLay.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            acceptLay.startAnimation(slideDown);*/
        }
        callTransaction.commit();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getStringExtra("_ACTION_");
        if (action != null) {
            switch (action) {
                case "endcall":
                    stopService(new Intent(getBaseContext(), CallNotificationService.class));
                    if (!CallActivity.this.isFinishing())
                        finishActivity();
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        closeConfirmDialog();
    }


    // Activity interfaces
    @Override
    protected void onStop() {
        super.onStop();
        activityRunning = false;
        EventBus.getDefault().unregister(this);
        // Don't stop the video when using screencapture to allow user to show other apps to the remote
        // end.
        if (peerConnectionClient != null && !screenCaptureEnabled) {
            peerConnectionClient.stopVideoSource();
            if (chatType.equals(Constants.TAG_VIDEO)) {
                SignallingClient.getInstance().emitOnlineStatus("away", userId, partnerId, roomId);
            }
        }
    }

    private void showToast(String text) {
        Toast toast = Toast.makeText(CallActivity.this, text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        toastText = "";
    }


    @Override
    protected void onDestroy() {
        stopTone();

        Thread.setDefaultUncaughtExceptionHandler(null);
        activityRunning = false;
        isInCall = false;
        userId = "";
        try {
            releaseWakeLock();
        } catch (Exception e) {
            Log.e(TAG, "onDestroy: " + e.getMessage());
        }
        stopAudioManager();
        if (!TextUtils.isEmpty(toastText)) {
            showToast(toastText);
        }
        disconnect();
        controlsFragment = null;
        from = "";
        fromID = "";
        chatId = "";
        userId = "";
        super.onDestroy();
    }


    private void closeConfirmDialog() {
        try {
            final Dialog dialog = new Dialog(CallActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setContentView(R.layout.logout_dialog);
            dialog.getWindow().setLayout(displayWidth * 80 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(false);

            TextView titleTxt = dialog.findViewById(R.id.headerTxt);
            TextView subTxt = dialog.findViewById(R.id.subTxt);
            TextView yes = dialog.findViewById(R.id.yes);
            TextView no = dialog.findViewById(R.id.no);
            titleTxt.setText(getString(R.string.warning));
            subTxt.setText(getString(R.string.call_should_be_disconnected));
            yes.setText(getString(R.string.okay));
            no.setText(getString(R.string.nope));

            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    disconnect();
                }
            });

            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();
        } catch (WindowManager.BadTokenException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void setCallStatus(CallStatus callStatus) {
        Log.d(TAG, "setCallStatus: " + callStatus.getCallStatus());
        if (callStatus.getCallStatus() != null) {
            txtCallType.setText(userName + " " + getResources().getString(R.string.onanothercall));
        }
    }

    private void stopCallForegroundService() {
        stopService(new Intent(getBaseContext(), CallNotificationService.class));
    }


    private VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer;
        if (useCamera2()) {
            Logging.d(TAG, "Creating capturer using camera2 API.");
            videoCapturer = createCameraCapturer(new Camera2Enumerator(this));
        } else {
            Logging.d(TAG, "Creating capturer using camera1 API.");
            videoCapturer = createCameraCapturer(new Camera1Enumerator(captureToTexture()));
        }
        if (videoCapturer == null) {
            Log.d(TAG, "Failed to open camera");
            return null;
        }
        return videoCapturer;
    }


    public void finishActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask();
        } else {
            finish();
        }
    }

}