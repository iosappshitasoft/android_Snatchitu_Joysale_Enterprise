package com.ni.SnatchItUp;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WAKE_LOCK;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;
import static com.ni.external.multipleimageselect.helpers.Constants.REQUEST_CODE;
import static com.ni.helper.SharedPrefManager.DEFAULT_CHAT_LANGUAGE;
import static com.ni.utils.Constants.ADDRESS_REQUEST_CODE;
import static com.ni.utils.Constants.TAG_MESSAGE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.devlomi.record_view.OnRecordClickListener;
import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordPermissionHandler;
import com.devlomi.record_view.RecordView;
import com.giphy.sdk.core.models.Media;
import com.giphy.sdk.ui.GPHContentType;
import com.giphy.sdk.ui.GPHSettings;
import com.giphy.sdk.ui.Giphy;
import com.giphy.sdk.ui.themes.GPHTheme;
//import com.giphy.sdk.ui.themes.GridType;
import com.giphy.sdk.ui.views.GiphyDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.mlkit.nl.smartreply.SmartReplySuggestion;
import com.google.mlkit.nl.smartreply.TextMessage;
import com.ni.addons.MediaPlayerUtils;
//import com.app.addons.call.appRTC.util.AppRTCUtils;
//import com.app.addons.chattranslate.ChatTranslateWorker;
//import com.app.addons.mapbox.MapboxLocationActivity;
//import com.app.addons.smartreply.GenerateSmartReplyWorker;
//import com.app.addons.smartreply.ReplyChipAdapter;
//import com.app.addons.buynow.AddAddress;
//import com.app.addons.buynow.Addresses;
//import com.app.addons.buynow.Checkout;
import com.ni.addons.buynow.AddAddress;
import com.ni.addons.buynow.Addresses;
import com.ni.addons.buynow.Checkout;
import com.ni.addons.call.appRTC.util.AppRTCUtils;
import com.ni.addons.chattranslate.ChatTranslateWorker;
import com.ni.addons.smartreply.GenerateSmartReplyWorker;
import com.ni.addons.smartreply.ReplyChipAdapter;
import com.ni.external.ProgressWheel;
import com.ni.external.imagepicker.ImagePicker;
import com.ni.external.multipleimageselect.activities.AlbumSelectActivity;
import com.ni.external.multipleimageselect.models.Photo;
import com.ni.helper.BitmapCompression;
import com.ni.helper.ImageCompression;
import com.ni.helper.ImageStorage;
import com.ni.helper.LocaleManager;
import com.ni.helper.NetworkReceiver;
import com.ni.helper.PermissionsUtils;
import com.ni.helper.SharedPrefManager;
import com.ni.helper.StorageUtils;
import com.ni.helper.callback.OkayCancelCallback;
import com.ni.helper.worker.DownloadFilesWorker;
import com.ni.helper.worker.UploadFilesWorker;
//import com.ni.mapbox.MainActivity;
import com.ni.model.AdminDataResponse;
import com.ni.model.ChatResponse;
import com.ni.model.HomeItemResponse;
import com.ni.model.ProfileResponse;
import com.ni.model.ReadMessageResponse;
import com.ni.model.ShippingAddressRes;
import com.ni.model.UpdateOfferResponse;
import com.ni.utils.ApiClient;
import com.ni.utils.ApiInterface;
import com.ni.utils.AppUtils;
import com.ni.utils.Constants;
import com.ni.utils.FileUtil;
import com.ni.utils.GetSet;
//import com.mapbox.api.staticmap.v1.MapboxStaticMap;
//import com.mapbox.api.staticmap.v1.StaticMapCriteria;
//import com.mapbox.api.staticmap.v1.models.StaticMarkerAnnotation;
//import com.mapbox.geojson.Point;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by hitasoft.
 * <p>
 * This class is for User Chat
 */


public class ChatActivity extends BaseActivity implements OnClickListener, TextWatcher, OnScrollListener
        , ReplyChipAdapter.ClickListener, GiphyDialogFragment.GifSelectionListener {

    static final String TAG = ChatActivity.class.getSimpleName();
    public static final int LOCATION_FETCH_ACTION = 1000;
    private static final int REQUEST_CODE_PICK_IMAGE = 100;
    public static boolean isExternalPlay = false;
    String stringChatURL = "";
    public static boolean imageUploading = false, fromChat = false;
    private boolean isRecording = false;
    /**
     * Declare Variables
     **/
    public static ArrayList<AdminDataResponse.ChatTemplate> templatMsgAry = new ArrayList<>();
    public static String fullName = "";

    public String[] callPermissions = {CAMERA, RECORD_AUDIO, READ_PHONE_STATE, WAKE_LOCK};
    /**
     * Declare Layout Elements
     **/
    boolean isRTL = false;
    ImageView shareImg, sharelocation, send, audiovideocallbtn;

    //Addon Gif
    ImageView layoutGif;
    GiphyDialogFragment giphyDialogFragment;
    GPHSettings settings = new GPHSettings();

    ShippingAddressRes.Result addressResultData = null;
    ImageView backBtn, userImg, settingbtn, btnCall, chattranslate;
    LinearLayout bottom, sendMsgLay, sendLay;
    TextView title, nullText, dateTxt, blockMsg;
    ListView listView;
    String stringOfferCountryCode;
    AVLoadingIndicatorView progress, topProgress, typing;
    EditText editText;
    ViewGroup header, footer;
    RelativeLayout main, blockUserLay;
    RecyclerView recyclerView;
    ProgressDialog dialog;
    RecyclerAdapter recyclerAdapter;
    ChatAdapter chatAdapter;
    Handler handler = new Handler();
    Runnable runnable;
    StorageUtils storageUtils;
    //Addon SmartReply
    RecyclerView mSmartRepliesRecycler;
    ReplyChipAdapter mChipAdapter;
    //    private SeekBar seekBar;

    //Addon Voice Message
    RecordButton btnRecord;
    RecordView recordView;
    String recordVoicePath = null;
    MediaRecorder mediaRecorder;
    int playingPosition = -1;
    boolean mediaPlay = false;

    ArrayList<ChatResponse.Chat> chats = new ArrayList<>(), tempAry = new ArrayList<>();
    HomeItemResponse.Item data = new HomeItemResponse().new Item();
    ArrayList<String> values = new ArrayList<>();
    String userName, userId, chatId, userImage, from = "", mobileNo, showMobile;
    private ActivityResultLauncher<String[]> storagePermissionResult;
    private ActivityResultLauncher<String[]> recordPermissionResult;
    private ActivityResultLauncher<String[]> phonepermissionresult;
    private ActivityResultLauncher<Intent> locationResult;
    private Context mContext;
    private ActivityResultLauncher<String[]> locations;
    public ActivityResultLauncher<String[]> galleryResultLauncher;
    private ActivityResultLauncher<String[]> cameraResultLauncher;
    /**
     * Function for receiving the instant messages & typing status
     **/

    private final Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.v(TAG, "onTyping=" + args[0]);
            runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        JSONObject data = (JSONObject) args[0];
                        if (data.getString(Constants.SOCK_RECEIVER).equals(userName) && data.getString(Constants.TAG_MESSAGE).equals(Constants.TAG_TYPE)) {
                            if (!receiverTyping) {
                                receiverTyping = true;
                                typing.setVisibility(View.VISIBLE);
                                if (chats.size() > 0) {
                                    listView.setSelection(chats.size() - 1);
                                }
                                typing.startAnimation(AnimationUtils.loadAnimation(ChatActivity.this, com.google.android.material.R.anim.abc_slide_in_bottom));
                            }
                        } else {
                            receiverTyping = false;
                            typing.setVisibility(View.GONE);
                            typing.startAnimation(AnimationUtils.loadAnimation(ChatActivity.this, com.google.android.material.R.anim.abc_slide_out_bottom));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
    boolean pulldown = false, loading = false, aboutMessageSent = false, tempMsgSent = false, meTyping, receiverTyping, isUserBlocked = false, isUserDeleted = false;
    int black, currentPage = 0, topPadding, leftPadding, rightPadding;
    ApiInterface apiInterface;
    HomeItemResponse.Item itemData = new HomeItemResponse().new Item();
    ImageStorage imageStorage;
    SharedPrefManager sharedPrefManager;

    private final Emitter.Listener onMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.v(TAG, "onMessage=" + args[0]);
            JSONObject data = (JSONObject) args[0];
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Addon smart reply
                    mSmartRepliesRecycler.setVisibility(View.GONE);
                }
            });
            try {
                ChatResponse.Message message = new ChatResponse().new Message();
                final ChatResponse.Chat chat = new ChatResponse().new Chat();
                chat.setSender(data.getString(Constants.TAG_RECEIVER));
                message.setChatTime(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_CHATTIME));
                Log.d(TAG, "call: "+userName);
                Log.d(TAG, "call: "+chat.getSender().equalsIgnoreCase(userName));
                if (chat.getSender().equalsIgnoreCase(userName)) {
                    if (!data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.SOCK_VIEW_URL).equals("")) {
                        if (Constants.ADDON_VOICE_ENABLED && data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_TYPE).equals("audio_msg")) {
                            chat.setType(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_TYPE));
                            message.setUploadAudio(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.SOCK_VIEW_URL));
                            message.setAudioDuration(data.getJSONObject(TAG_MESSAGE).getString(Constants.TAG_AUDIO_DURATION));
                        } else if (Constants.ADDON_GIF_ENABLED && data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_TYPE).equals("gif")) {
                            chat.setType(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_TYPE));
                            message.setUploadImage(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.SOCK_VIEW_URL));
                        } else {
                            chat.setType(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_TYPE));
                            message.setUploadImage(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.SOCK_VIEW_URL));
                        }
                        chat.setMessage(message);
                        insertNewMessage(chat);
                    } else if (!data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_LAT).equals("")) {
                        message.setLatitude(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_LAT));
                        message.setLongitude(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_LON));
                        chat.setType("share_location");
                        chat.setMessage(message);
                        insertNewMessage(chat);
                    } else if (!data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_MESSAGE).equals("")) {
                        message.setMessage(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_MESSAGE));
                        chat.setMessage(message);
                        chat.setType("message");
                        //Addon Chat Translate
                        if (Constants.ADDON_TRANSLATE_ENABLED && data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_TYPE).equals("normal")) {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d(TAG, "run: smartreply addon");
                                    generateReplies(chat.getMessage().getMessage());
                                }
                            });

                            if (!TextUtils.isEmpty(SharedPrefManager.getString(DEFAULT_CHAT_LANGUAGE, "")) &&
                                    !SharedPrefManager.getString(DEFAULT_CHAT_LANGUAGE, "").equalsIgnoreCase(mContext.getString(R.string.none))) {
                                Data.Builder builder = new Data.Builder();
                                builder.putString(Constants.TAG_MESSAGE, data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_MESSAGE));
                                final OneTimeWorkRequest translateWorkRequest = new OneTimeWorkRequest.Builder(ChatTranslateWorker.class)
                                        .setConstraints(new Constraints.Builder()
                                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                                .build())
                                        .setInputData(builder.build())
                                        .build();
                                final WorkManager workManager = WorkManager.getInstance(ChatActivity.this);
                                workManager.enqueue(translateWorkRequest);
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    public void run() {
                                        workManager.getWorkInfoByIdLiveData(translateWorkRequest.getId()).observe(ChatActivity.this, workInfo -> {
                                            if (workInfo.getState().isFinished()) {
                                                if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                                                    String translatedMsg = workInfo.getOutputData().getString(Constants.TAG_DATA);
                                                    chat.getMessage().setMessage(Html.fromHtml(translatedMsg).toString());
                                                    insertNewMessage(chat);
                                                }
                                            }
                                        });
                                    }
                                });
                            } else {
                                message.setMessage(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_MESSAGE));
                                chat.setMessage(message);
                                insertNewMessage(chat);
                            }
                        } else {
                        message.setMessage(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_MESSAGE));
                        chat.setMessage(message);
                        insertNewMessage(chat);
                            }
                    } else {
                        chat.setType("offer");
                        String currencyMode, currencyPosition, currencySymbol, currencyCode, offerPrice, offerShippingPrice, offerTotalPrice;
                        currencyMode = data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_CURRENCY_MODE);
                        currencyPosition = data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_CURRENCY_POSITION);
                        currencySymbol = data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_OFFER_CURRENCY);
                        currencyCode = data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_OFFER_CURRENCY_CODE);
                        offerPrice = data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_OFFER_PRICE);
                        offerShippingPrice = data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_SHIPPING_COST);
                        offerTotalPrice = data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_OFFER_TOTAL);

                        chat.setSender(data.getString(Constants.TAG_SENDER));
                        chat.setReceiver(data.getString(Constants.TAG_RECEIVER));
                        chat.setItemId(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_ITEM_ID));
                        chat.setItemImage(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_ITEM_IMAGE));
                        chat.setOfferId(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_OFFER_ID));
                        chat.setOfferPrice(offerPrice);
                        chat.setCurrencyMode(currencyMode);
                        chat.setCurrencyPosition(currencyPosition);
                        chat.setTotalOfferPrice(offerTotalPrice);
                        chat.setOfferType(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_OFFER_TYPE));
                        chat.setOfferStatus(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_OFFER_STATUS));
                        chat.setBuyNowStatus(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_BUYNOW_STATUS));
                        chat.setInstantBuy(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_INSTANT_BUY));
                        chat.setOfferCurrency(currencySymbol);
                        chat.setOfferCurrencyCode(currencyCode);

                        chat.setFormattedOfferPrice(JoysaleApplication.getFormattedPrice(ChatActivity.this, currencySymbol, currencyCode,
                                currencyMode, currencyPosition, offerPrice));
                        chat.setFormattedShippingPrice(JoysaleApplication.getFormattedPrice(ChatActivity.this, currencySymbol, currencyCode,
                                currencyMode, currencyPosition, offerShippingPrice));
                        chat.setFormattedTotalOfferPrice(JoysaleApplication.getFormattedPrice(ChatActivity.this, currencySymbol, currencyCode,
                                currencyMode, currencyPosition, offerTotalPrice));
                        chat.setItemStatus("1");
                        chat.setMessage(message);
                        insertNewMessage(chat);

                        Log.d(TAG, "call11111111111111: ");
                        //Addon SmartReply
                        if (data.getString(Constants.TAG_RECEIVER).equals(userName)) {
                            if (Constants.ADDON_SMART_REPLY_ENABLED && data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_TYPE).equals("normal")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.d(TAG, "run11111111111: ");
                                        generateReplies(chat.getMessage().getMessage());
                                    }
                                });
                            }
                        }
                    }
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };

    private void insertNewMessage(ChatResponse.Chat chat) {
        if (!isUserBlocked) {
            chats.add(chat);
            runOnUiThread(new Runnable() {
                public void run() {
                    chatAdapter.notifyDataSetChanged();
                    if (chats.size() > 0) {
                        listView.setSelection(chats.size() - 1);
                    }
                }
            });
        }
    }

    //Addon Call
    Dialog dialogforaudiovidocall;
    private MediaPlayer player = new MediaPlayer();
    private Socket mSocket;
    private boolean visible = false;
    private Uri mCurrentPhotoUri;
    private ChatResponse.Chat selectedOfferChat;
    private Dialog permissionDialog;
    private File currentPhotoFile;
    private Handler seekHandler = new Handler();

    /**
     * Function to get a Time from Time Stamp
     */

    public static String getTime(long timeStamp) {
        try {
            DateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);
        mContext = this;
        isRTL = LocaleManager.isRTL(this);
        storageUtils = StorageUtils.getInstance(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        imageStorage = new ImageStorage(ChatActivity.this);
        sharedPrefManager = SharedPrefManager.getInstance(this);
        SharedPrefManager.initPref(this);
        initPermission();
        initActivityResult();
        initView();
        phonepermissionresult = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                boolean granted = true;
                Log.d(TAG, "onActivityResult: " + result);
                for (Map.Entry<String, Boolean> x : result.entrySet()) {
                    if (!x.getValue()) granted = false;
                }

                if (granted) {


                } else {
                    boolean neverAsked = false;
                    Log.d(TAG, "requestPermissions: ");
                    for (Map.Entry<String, Boolean> x : result.entrySet()) {
                        if (!x.getValue()) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(ChatActivity.this, x.getKey())) {
                                phonepermissionresult.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                openPermissionDialogs(ChatActivity.this, new OkayCancelCallback() {
                                    @Override
                                    public void onOkayClicked(Object o) {
                                        Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(i);
                                    }

                                    @Override
                                    public void onCancelClicked(Object o) {

                                    }
                                }, ChatActivity.this.getString(R.string.storage_error));
                            }
                            break;
                        }
                    }
                }

            }
        });
        dialog.setMessage(getString(R.string.pleasewait));
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = new ProgressBar(ChatActivity.this).getIndeterminateDrawable().mutate();
            drawable.setColorFilter(ContextCompat.getColor(ChatActivity.this, R.color.progressColor),
                    PorterDuff.Mode.SRC_IN);
            dialog.setIndeterminateDrawable(drawable);
        }

        footer = (ViewGroup) getLayoutInflater().inflate(R.layout.chat_footer, null);
        listView.addFooterView(footer);
        listView.setSmoothScrollbarEnabled(true);
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);

        topProgress = header.findViewById(R.id.topProgress);
        nullText = header.findViewById(R.id.nulltext);
        typing = footer.findViewById(R.id.typing);

        userName = getIntent().getExtras().getString(Constants.TAG_USERNAME);
        userId = getIntent().getExtras().getString(Constants.TAG_USER_ID);
        chatId = getIntent().getExtras().getString(Constants.CHATID);
        userImage = getIntent().getExtras().getString(Constants.TAG_USERIMAGE_M);
        from = getIntent().getExtras().getString(Constants.TAG_FROM);
        fullName = getIntent().getExtras().getString(Constants.TAG_FULL_NAME);
        mobileNo = getIntent().getExtras().getString(Constants.TAG_MOBILE_NO);
        showMobile = getIntent().getExtras().getString(Constants.TAG_SHOW_MOBILE_NO);

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        topProgress.setVisibility(View.GONE);
        nullText.setVisibility(View.GONE);
        typing.setVisibility(View.GONE);
        userImg.setVisibility(View.VISIBLE);
        dateTxt.setVisibility(View.GONE);
        settingbtn.setVisibility(View.VISIBLE);

        topPadding = JoysaleApplication.dpToPx(this, 10);
        leftPadding = JoysaleApplication.dpToPx(this, 18);
        rightPadding = JoysaleApplication.dpToPx(this, 12);
        setCallOption();
        getProfile();
        LinearLayoutManager layoutManager = new LinearLayoutManager(ChatActivity.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerAdapter = new RecyclerAdapter(templatMsgAry);
        DividerItemDecoration itemDecorator = new DividerItemDecoration(ChatActivity.this, DividerItemDecoration.HORIZONTAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(ChatActivity.this, R.drawable.chat_template_divider));
        recyclerView.addItemDecoration(itemDecorator);
        recyclerView.setAdapter(recyclerAdapter);
        blockMsg.setText(getString(R.string.block_user_msg));
        if (from.equals("detail")) {
            data = (HomeItemResponse.Item) getIntent().getExtras().get("data");
        }

        black = getResources().getColor(R.color.black);

        /** Method for join the user to chat **/

        JSONObject jobj = new JSONObject();
        try {
            jobj.put("joinid", GetSet.getUserName());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            mSocket = IO.socket(Constants.SOCKET_URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        JoysaleApplication app = (JoysaleApplication) getApplication();
        mSocket = mSocket;
        mSocket.on("message", onMessage);
        mSocket.on("messageTyping", onTyping);
        mSocket.io().on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.v("EVENT_CONNECT", "EVENT_CONNECT");
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.v("EVENT_DISCONNECT", "EVENT_DISCONNECT");
            }
        }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.v("EVENT_CONNECT_ERROR", "EVENT_CONNECT_ERROR=" + args[0]);
            }
        }).on(Socket.EVENT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.v("EVENT_ERROR", "EVENT_ERROR");
            }
        });
        mSocket.connect();

        mSocket.emit("join", jobj);

        backBtn.setOnClickListener(this);
        btnCall.setOnClickListener(this);
        send.setOnClickListener(this);
        shareImg.setOnClickListener(this);

        sharelocation.setOnClickListener(this);
        editText.addTextChangedListener(this);
        settingbtn.setOnClickListener(this);
        userImg.setOnClickListener(this);

        initAddOns();

        editText.setFilters(new InputFilter[]{JoysaleApplication.EMOJI_FILTER});
        chatAdapter = new ChatAdapter(ChatActivity.this, chats, sharedPrefManager);
        listView.setAdapter(chatAdapter);

        Picasso.get().load(userImage).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(userImg);
        title.setText(fullName);

        values.add(getString(R.string.safety_tips));
        values.add(getString(R.string.block_user));

        initializeChat();
        getChat(0);
    }

    private void initView() {
        backBtn = findViewById(R.id.backbtn);
        title = findViewById(R.id.username);
        shareImg = findViewById(R.id.shareImg);
        sharelocation = findViewById(R.id.sharelocation);
        listView = findViewById(R.id.listView);
        send = findViewById(R.id.send);
        editText = findViewById(R.id.editText);
        progress = findViewById(R.id.progress);
        main = findViewById(R.id.main);
        userImg = findViewById(R.id.userImg);
        dateTxt = findViewById(R.id.dateTxt);
        bottom = findViewById(R.id.bottom);
        recyclerView = findViewById(R.id.recyclerView);
        sendMsgLay = findViewById(R.id.sendMsgLay);
        settingbtn = findViewById(R.id.settingbtn);
        btnCall = findViewById(R.id.selectbtn);
        dialog = new ProgressDialog(ChatActivity.this, R.style.AppCompatAlertDialogStyle);
        blockUserLay = findViewById(R.id.blockUserLay);
        blockMsg = findViewById(R.id.blockMsg);
        sendLay = findViewById(R.id.sendLay);
        header = (ViewGroup) getLayoutInflater().inflate(R.layout.chat_header, null);
        listView.addHeaderView(header);
    }

    private void initPermission() {
        galleryResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                boolean granted = true;
                Log.d(TAG, "onActivityResult1111111: " + result);
                for (Map.Entry<String, Boolean> x : result.entrySet()) {
                    if (ContextCompat.checkSelfPermission(ChatActivity.this,CAMERA) == PackageManager.PERMISSION_GRANTED  && ContextCompat.checkSelfPermission(ChatActivity.this,READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                        granted = true;
                    } else {
                        granted = false;
                    }
                }

                if (granted) {

pickImage();
                } else {
                    boolean neverAsked = false;
                    Log.d(TAG, "requestPermissions: ");
                    for (Map.Entry<String, Boolean> x : result.entrySet()) {
                        if (!x.getValue()) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(ChatActivity.this, x.getKey())) {
                                galleryResultLauncher.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                openPermissionDialog13(ChatActivity.this, new OkayCancelCallback() {
                                    @Override
                                    public void onOkayClicked(Object o) {
                                        Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(i);
                                    }

                                    @Override
                                    public void onCancelClicked(Object o) {
                                        Toast.makeText(ChatActivity.this, getResources().getString(R.string.camera_error), Toast.LENGTH_SHORT).show();
                                    }
                                }, getResources().getString(R.string.camera_error));

                            }
                            break;
                        }
                    }
                }

            }
        });
        locations = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                boolean granted = true;
                Log.d(TAG, "onActivityResult: " + result);

                for (Map.Entry<String, Boolean> x : result.entrySet()) {
                    if (!x.getValue()) {
                        if (ContextCompat.checkSelfPermission(ChatActivity.this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(ChatActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            granted = false;
                        } else {
                            granted = true;
                        }
                    }
                    Log.d(TAG, "onActivityResultk: " + granted);
                }

                if (granted) {


                } else {
                    boolean neverAsked = false;
                    if (ContextCompat.checkSelfPermission(ChatActivity.this, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(ChatActivity.this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    }
                    Log.d(TAG, "requestPermissions: ");
                    for (Map.Entry<String, Boolean> x : result.entrySet()) {
                        if (!x.getValue()) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(ChatActivity.this, x.getKey())) {
                                locations.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                if (ContextCompat.checkSelfPermission(ChatActivity.this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(ChatActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    PermissionsUtils.openPermissionDialog(ChatActivity.this, new OkayCancelCallback() {
                                        @Override
                                        public void onOkayClicked(Object o) {
                                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                    Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        }

                                        @Override
                                        public void onCancelClicked(Object o) {

                                        }
                                    }, ChatActivity.this.getString(R.string.ask_for_locations));
                                }
                                break;
                            }
                        }
                    }
                }

            }
        });
        storagePermissionResult = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                boolean granted = true;
                Log.d(TAG, "onActivityResult: " + result);
                for (Map.Entry<String, Boolean> x : result.entrySet()) {
                    if (!x.getValue()) granted = false;
                }

                if (granted) {
pickImage();
                } else {
                    boolean neverAsked = false;
                    Log.d(TAG, "requestPermissions: ");
                    for (Map.Entry<String, Boolean> x : result.entrySet()) {
                        if (!x.getValue()) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(ChatActivity.this, x.getKey())) {
                                storagePermissionResult.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                PermissionsUtils.openPermissionDialog(mContext, new OkayCancelCallback() {
                                    @Override
                                    public void onOkayClicked(Object o) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onCancelClicked(Object o) {

                                    }
                                }, mContext.getString(R.string.storage_error));
                            }
                            break;
                        }
                    }
                }

            }
        });

        recordPermissionResult = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                boolean granted = true;
                Log.d(TAG, "recordPermissionResult: " + result);
                for (Map.Entry<String, Boolean> x : result.entrySet()) {
                    if (!x.getValue()) granted = false;
                }

                if (!granted) {
                    String errorString;
                    if (SDK_INT >= Build.VERSION_CODES.Q) {
                        errorString = mContext.getString(R.string.mirco_phone_permission_description);
                    } else {
                        errorString = mContext.getString(R.string.storage_microphone_permission_description);
                    }
                    boolean neverAsked = false;
                    Log.d(TAG, "requestPermissions: ");
                    for (Map.Entry<String, Boolean> x : result.entrySet()) {
                        if (!x.getValue()) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(ChatActivity.this, x.getKey())) {
                                recordPermissionResult.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                PermissionsUtils.openPermissionDialog(mContext, new OkayCancelCallback() {
                                    @Override
                                    public void onOkayClicked(Object o) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onCancelClicked(Object o) {

                                    }
                                }, errorString);
                            }
                            break;
                        }
                    }
                }

            }
        });

        cameraResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                boolean granted = true;
                Log.d(TAG, "onActivityResult: " + result);
                for (Map.Entry<String, Boolean> x : result.entrySet()) {
                    if (!x.getValue()) granted = false;
                }

                if (granted) {
                   /* Intent in = new Intent(EditProfile.this, AlbumSelectActivity.class);
                    in.putExtra("from","profile");
                    in.putExtra("fromEditProfile", true);
                    startActivityForResult(in, REQUEST_CODE);*/

                } else {
                    boolean neverAsked = false;
                    Log.d(TAG, "requestPermissions: ");
                    for (Map.Entry<String, Boolean> x : result.entrySet()) {
                        if (!x.getValue()) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(ChatActivity.this, x.getKey())) {
                                cameraResultLauncher.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                PermissionsUtils.openPermissionDialog(ChatActivity.this, new OkayCancelCallback() {
                                    @Override
                                    public void onOkayClicked(Object o) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onCancelClicked(Object o) {

                                    }
                                }, ChatActivity.this.getString(R.string.camera_error));
                            }
                            break;
                        }
                    }
                }

            }
        });

    }

    private void initActivityResult() {
        locationResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        String latitude = result.getData().getStringExtra("current_latitude");
                        String longitude = result.getData().getStringExtra("current_longitude");

                        ChatResponse.Message message = new ChatResponse().new Message();
                        ChatResponse.Chat chat = new ChatResponse().new Chat();
                        chat.setSender(GetSet.getUserName());
                        message.setChatTime("" + System.currentTimeMillis() / 1000L);
                        message.setLatitude(latitude);
                        message.setLongitude(longitude);
                        chat.setMessage(message);
                        chat.setType("share_location");
                        String jsonStr = result.getData().getStringExtra("jsonObject");
                        try {
                            JSONObject jsonObject = new JSONObject(jsonStr);
                            Log.i(TAG, "onActivityResult: " + jsonObject);
                            mSocket.emit("message", jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        chats.add(chat);
                        chatAdapter.notifyDataSetChanged();
                        listView.setSelection(chats.size() - 1);
                        if (chats.size() > 0 && listView.getVisibility() != View.VISIBLE) {
                            listView.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });
    }


    private void initAddOns() {
        send.setVisibility(View.VISIBLE);
        layoutGif = findViewById(R.id.btngif);
        chattranslate = findViewById(R.id.langtrans);
        //Addon smart reply
        mSmartRepliesRecycler = findViewById(R.id.smartRepliesRecycler);
        audiovideocallbtn = findViewById(R.id.audiovideocallbtn);
        recordView = findViewById(R.id.recordView);
        btnRecord = findViewById(R.id.btnRecord);
        //Addon Voice Message
        if (Constants.ADDON_VOICE_ENABLED) {
            btnRecord.setVisibility(View.VISIBLE);
            send.setVisibility(View.INVISIBLE);
            setVoiceRecorder();
        } else {
            send.setVisibility(View.VISIBLE);
        }

        if (Constants.ADDON_CALL_ENABLED) {
            audiovideocallbtn.setOnClickListener(this);
            audiovideocallbtn.setVisibility(View.VISIBLE);
        }

        if (Constants.ADDON_TRANSLATE_ENABLED) {
            chattranslate.setVisibility(View.VISIBLE);
            chattranslate.setOnClickListener(this);
        } else {
            chattranslate.setVisibility(View.GONE);
        }

        //Addon Gif
        if (Constants.ADDON_GIF_ENABLED) {
            layoutGif.setVisibility(View.VISIBLE);
            Giphy.INSTANCE.configure(this, Constants.KEY_GIPHY_API, false);
//            settings.setGridType(GridType.waterfall);
            settings.setTheme(GPHTheme.Light);
            settings.setMediaTypeConfig(new GPHContentType[]{GPHContentType.sticker, GPHContentType.emoji, GPHContentType.gif, GPHContentType.text});
            layoutGif.setOnClickListener(this);
        }

        //Addon SmartReply
        if (Constants.ADDON_SMART_REPLY_ENABLED) {
            LinearLayoutManager chipManager = new LinearLayoutManager(this);
            chipManager.setOrientation(RecyclerView.HORIZONTAL);
            mChipAdapter = new ReplyChipAdapter(this);
            mSmartRepliesRecycler.setLayoutManager(chipManager);
            mSmartRepliesRecycler.setAdapter(mChipAdapter);
        }
    }

    private void getProfile() {

        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            hashMap.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            hashMap.put(Constants.TAG_USERID, GetSet.getUserId());
            hashMap.put(Constants.TAG_PROFILE_ID, userId);

            Call<ProfileResponse> call = apiInterface.getProfileInformation(hashMap);
            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(Call<ProfileResponse> call, retrofit2.Response<ProfileResponse> response) {
                    if (response.isSuccessful()) {
                        ProfileResponse profile = response.body();
                        if (profile.getStatus().equals(Constants.TAG_TRUE)) {
                            ProfileResponse.Result result = profile.getResult();
                            if (result != null) {
                                mobileNo = result.getMobileNo();
                                showMobile = result.getShowMobileNo();
                                setCallOption();
                            }
                        } else if (response.body().getStatus().equalsIgnoreCase("error")) {
                            if (response.body().getDeleteuser().equalsIgnoreCase("true") && response.body().getDeleteduserid().equalsIgnoreCase(GetSet.getUserId())) {
                                JoysaleApplication.disabledialog(ChatActivity.this, response.body().getMessage(), userId);
                            } else if (response.body().getDeleteuser().equalsIgnoreCase("false")) {
                                JoysaleApplication.disabledialog(ChatActivity.this, response.body().getMessage(), userId);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<ProfileResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }

    }

    private void setCallOption() {
        if (mobileNo != null && showMobile != null && showMobile.equals(Constants.TAG_TRUE)) {
            btnCall.setVisibility(View.VISIBLE);
            btnCall.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.call));
        }
    }

    private void disconnectSocket() {
        if (mSocket != null) {
            mSocket.off("message");
            mSocket.off("messageTyping");
            mSocket.disconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isRecording) {
            recordView.cancelRecord();
        }
        fullName = "";
        stopAudio();
        MediaPlayerUtils.pauseMediaPlayer();
        //Addons AudioVideoCall
        if (audiovideocallbtn.isEnabled()) {
            JoysaleApplication.hideSoftKeyboard(ChatActivity.this);
            editText.requestFocus();
            setButtonVisibility(View.VISIBLE);
            setToolBarClick(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fullName = getIntent().getExtras().getString(Constants.TAG_FULL_NAME);
        stopAudio();
        //Addons AudioVideoCall
        if (audiovideocallbtn.isEnabled()) {
            JoysaleApplication.hideSoftKeyboard(ChatActivity.this);
            editText.requestFocus();
            setButtonVisibility(View.VISIBLE);
            setToolBarClick(true);
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(ChatActivity.this, isConnected);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        fullName = "";
        disconnectSocket();
        if (from.equals(Constants.TAG_MESSAGE)) {
            MessageActivity.fromChat = true;
        }
        callSeenChat();
        JoysaleApplication.hideSoftKeyboard(ChatActivity.this);
    }

    private void callSeenChat() {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            hashMap.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            hashMap.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
            hashMap.put(Constants.TAG_RECEIVER_ID, userId);
            hashMap.put(Constants.TAG_TYPE, "normal");

            Call<ReadMessageResponse> call = apiInterface.updateReadMessage(hashMap);
            call.enqueue(new Callback<ReadMessageResponse>() {
                @Override
                public void onResponse(Call<ReadMessageResponse> call, retrofit2.Response<ReadMessageResponse> response) {
                    if (response.isSuccessful()) {
                        ReadMessageResponse profile = response.body();
                        if (profile.getStatus().equals(Constants.TAG_TRUE)) {

                        }
                    }
                }

                @Override
                public void onFailure(Call<ReadMessageResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            Log.v(TAG, "scrolling stopped...");
            dateTxt.setVisibility(View.GONE);
        } else {
            if (dateTxt.getVisibility() != View.VISIBLE) {
                dateTxt.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        try {
            String chatDate = JoysaleApplication.getDate(ChatActivity.this, Long.parseLong(chats.get(firstVisibleItem).getMessage().getChatTime()));
            dateTxt.setText(chatDate);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (firstVisibleItem == 0 && !(loading)) {
            loading = true;
            topProgress.setVisibility(View.VISIBLE);
            dateTxt.setVisibility(View.GONE);
            nullText.setVisibility(View.GONE);
            currentPage++;
            pulldown = true;
            if (JoysaleApplication.isNetworkAvailable(ChatActivity.this)) {
                initializeChat();
                getChat(currentPage);
            }
        }
    }

    /**
     * Function for get the conversation between the selected user
     **/

    private void getChat(final int pageCount) {
        if (NetworkReceiver.isConnected()) {
            final boolean[] isUsrBlocked = {false};
            final boolean[] isUsrBlockedByMe = {false};
            Map<String, String> map = new HashMap<String, String>();
            int offset = (pageCount * 20);
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
            map.put(Constants.TAG_RECEIVER_ID, userId);
            map.put(Constants.TAG_TYPE, "normal");
            map.put(Constants.TAG_OFFSET, Integer.toString(offset));
            map.put(Constants.TAG_LIMIT, "20");
            map.put(Constants.TAG_SOURCE_ID, "0");
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(getApplicationContext()));
            Log.d(TAG, "getChat: " + map);
            Call<ChatResponse> call = apiInterface.getChat(map);
            call.enqueue(new Callback<ChatResponse>() {
                @Override
                public void onResponse(@NonNull Call<ChatResponse> call, @NonNull retrofit2.Response<ChatResponse> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "onResponse: chat" + response.body().getChatUrl());
                        Log.d(TAG, "onResponse: chat" + response.body().toString());
                        Log.d(TAG, "onResponse: chat" + response.body().getBlock());
                        if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                            stringChatURL = response.body().getChatUrl();
                            if (response.body().getBlock() != null) {
                                isUsrBlocked[0] = Boolean.parseBoolean(response.body().getBlock());
                            }
                            if (response.body().getBlockedByMe() != null) {
                                isUsrBlockedByMe[0] = Boolean.parseBoolean(response.body().getBlockedByMe());
                            }
                            blockChat(isUsrBlocked[0], isUsrBlockedByMe[0]);
                            if (response.body().getUserstatus().equalsIgnoreCase("2")) {
                                isuserdeleted(true);
                                isUserDeleted = true;
                            } else {
                                isuserdeleted(false);
                                isUserDeleted = false;
                            }
                            tempAry.clear();
                            tempAry.addAll(response.body().getChats().getChats());
                            Collections.reverse(tempAry);
                            ArrayList<ChatResponse.Chat> backup = new ArrayList<>();
                            backup.addAll(chats);
                            chats.clear();
                            chats.addAll(tempAry);
                            chats.addAll(backup);

                            if (Constants.ADDON_SMART_REPLY_ENABLED) {
                                if (pageCount == 0) {
                                    if (!response.body().getChats().getChats().get(0).getSender().equalsIgnoreCase(GetSet.getUserName())) {
                                        if (response.body().getChats().getChats().get(0).getType().equalsIgnoreCase("normal")) {
                                            //generateReplies(response.body().getChats().getChats().get(0).getMessage().getMessage());
                                        }
                                    }
                                }
                            }

                            stringOfferCountryCode = chats.get(0).getOfferCurrencyCode();
                            if (tempAry.size() == 0) {
                                listView.setOnScrollListener(null);
                            } else {
                                listView.setOnScrollListener(ChatActivity.this);
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (pulldown) {
                                        pulldown = false;
                                        listView.setSelection(chats.size() - 1);
                                        chatAdapter.notifyDataSetChanged();
                                        listView.setSelection(tempAry.size());

                                    } else {
                                        chatAdapter.notifyDataSetChanged();
                                        if (chats.size() > 0) {
                                            listView.setSelection(chats.size() - 1);
                                        }
                                    }
                                }
                            });

                            if (chats.size() == 0) {
                                dateTxt.setVisibility(View.GONE);
                            } else {
                                dateTxt.setVisibility(View.VISIBLE);
                            }

                            if (chats.size() > 18) {
                                if (tempAry.size() == 0) {
                                    nullText.setVisibility(View.GONE);
                                    topProgress.setVisibility(View.GONE);
                                    dateTxt.setVisibility(View.GONE);
                                }
                            }

                            loading = false;
                            //  bottom.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.VISIBLE);
                            topProgress.setVisibility(View.GONE);
                            listView.setVisibility(View.VISIBLE);
                            progress.setVisibility(View.GONE);
                        } else if (response.body().getStatus().equalsIgnoreCase("false")) {
                            if (response.body().getMessage().equals("No Chat History Found")) {
                                if (isUserDeleted) {
                                    isuserdeleted(true);
                                }
                                progress.setVisibility(View.GONE);
                                topProgress.setVisibility(View.GONE);
                            }
                            if (response.body().getBlock() != null) {
                                isUsrBlocked[0] = Boolean.parseBoolean(response.body().getBlock());
                            }
                            if (response.body().getBlockedByMe() != null) {
                                isUsrBlockedByMe[0] = Boolean.parseBoolean(response.body().getBlockedByMe());
                            }
                            //Toast.makeText(this, json.optString(Constants.TAG_MESSAGE), Toast.LENGTH_SHORT).show();
                            blockChat(isUsrBlocked[0], isUsrBlockedByMe[0]);
                        } else if (response.body().getStatus().equalsIgnoreCase("error")) {
                            JoysaleApplication.disabledialog(ChatActivity.this, response.body().getMessage(), userId);
                        } else if (response.body().getStatus().equalsIgnoreCase(Constants.TAG_FALSE_CODE)) {
                            JoysaleApplication.disabledialog(ChatActivity.this, "" + response.body().getMessage(), GetSet.getUserId());
                        }
                    }
                }

                @Override
                public void onFailure(Call<ChatResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }


    //Addon SmartReply
    private void generateReplies(String lastMessage) {
        // If the last message in the chat thread is not sent by the "other" user, don't generate
        // smart replies.
        if (!TextUtils.isEmpty(lastMessage)) {
            List<TextMessage> chatHistory = new ArrayList<>();
            chatHistory.add(TextMessage.createForRemoteUser(lastMessage,
                    System.currentTimeMillis(), GetSet.getUserId()));
            Data.Builder builder = new Data.Builder();
            builder.putString(Constants.TAG_MESSAGE, lastMessage);
            OneTimeWorkRequest locationWorkRequest = new OneTimeWorkRequest.Builder(GenerateSmartReplyWorker.class)
                    .setConstraints(new Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build())
                    .setInputData(builder.build())
                    .build();
            WorkManager workManager = WorkManager.getInstance(this);
            workManager.enqueue(locationWorkRequest);
            workManager.getWorkInfoByIdLiveData(locationWorkRequest.getId()).observe(this, workInfo -> {
                if (workInfo.getState().isFinished()) {
                    if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                        final List<SmartReplySuggestion> suggestion = new ArrayList<>();
                        String suggestionJson = workInfo.getOutputData().getString(Constants.TAG_DATA);
                        Log.d(TAG, "GenerateSmartReplyWorker: " + suggestionJson);
                        if (!TextUtils.isEmpty(suggestionJson)) {
                            Type userListType = new TypeToken<List<SmartReplySuggestion>>() {
                            }.getType();
                            suggestion.addAll(new Gson().fromJson(suggestionJson, userListType));
                            mChipAdapter.setSuggestions(suggestion);
                            mSmartRepliesRecycler.setVisibility(View.VISIBLE);
                        }
                    } else {
                        mSmartRepliesRecycler.setVisibility(View.GONE);
                    }
                }
            });
        } else {
            hideSmartReply();
        }
    }

    /**
     * Function for Initialize chat
     **/

    private void initializeChat() {
        loading = true;
        if (pulldown) {
            listView.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
        } else {
            listView.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.VISIBLE);
            bottom.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    /**
     * Function for Offer Accept or Declined
     **/

    private void offerAcceptOrDeclined(final ChatResponse.Chat chat, final String offerStatus) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
            map.put(Constants.TAG_OFFER_ID, chat.getOfferId());
            map.put(Constants.TAG_STATUS, offerStatus);
            map.put(Constants.TAG_CHAT_ID, chatId);
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(getApplicationContext()));

            Call<UpdateOfferResponse> call = apiInterface.updateOfferStatus(map);
            call.enqueue(new Callback<UpdateOfferResponse>() {
                @Override
                public void onResponse(Call<UpdateOfferResponse> call, retrofit2.Response<UpdateOfferResponse> response) {
                    dismissLoading();
                    String offerStatus = "", offerType = "";
                    try {
                        if (response.isSuccessful() && response.body().getStatus().equals("true")) {
                            UpdateOfferResponse offerResponse = response.body();
                            offerStatus = offerResponse.getResult().getOfferStatus();
                            offerType = offerResponse.getResult().getOfferType();
                            callOfferSocket(System.currentTimeMillis() / 1000L, offerResponse.getResult());
                            stringOfferCountryCode = offerResponse.getResult().getOfferCurrencyCode();
                            String finalOfferStatus = offerStatus;
                            String finalOfferType = offerType;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    chat.setOfferStatus(finalOfferStatus);

                                    ChatResponse.Chat newChat = new ChatResponse().new Chat();
                                    newChat.setType(chat.getType());
                                    newChat.setSender(chat.getSender());
                                    newChat.setMessage(chat.getMessage());
                                    newChat.setReceiver(chat.getReceiver());
                                    newChat.setBuyNowStatus(chat.getBuyNowStatus());
                                    newChat.setInstantBuy(chat.getInstantBuy());
                                    newChat.setItemId(chat.getItemId());
                                    newChat.setItemImage(chat.getItemImage());
                                    newChat.setItemStatus(chat.getItemStatus());
                                    newChat.setItemTitle(chat.getItemTitle());
                                    newChat.setOfferCurrency(offerResponse.getResult().getOfferCurrency());
                                    newChat.setOfferCurrencyCode(offerResponse.getResult().getOfferCurrencyCode());
                                    newChat.setCurrencyMode(offerResponse.getResult().getCurrencyMode());
                                    newChat.setCurrencyPosition(offerResponse.getResult().getCurrencyPosition());
                                    newChat.setOfferId(chat.getOfferId());
                                    newChat.setShippingCost(offerResponse.getResult().getShippingCost());
                                    newChat.setOfferPrice(chat.getOfferPrice());
                                    newChat.setTotalOfferPrice(offerResponse.getResult().getTotalPrice());
                                    newChat.setFormattedOfferPrice(offerResponse.getResult().getFormattedOfferPrice());
                                    newChat.setFormattedTotalOfferPrice(offerResponse.getResult().getFormattedTotalPrice());
                                    newChat.setFormattedShippingPrice(offerResponse.getResult().getFormattedShippingPrice());
                                    newChat.setOfferStatus(chat.getOfferStatus());
                                    newChat.setOfferType(finalOfferType);
                                    chats.add(newChat);
                                    chatAdapter.notifyDataSetChanged();
                                    if (chats.size() > 0) {
                                        listView.setSelection(chats.size() - 1);
                                    }
                                }
                            });
                        } else if (response.isSuccessful() && response.body().getMessage().equals("block status unable to make process")) {
                            Toast.makeText(ChatActivity.this, getString(R.string.conversation_blocked), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ChatActivity.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<UpdateOfferResponse> call, Throwable t) {
                    Log.e(TAG, "offerAcceptOrDeclinedError: " + t.getMessage());
                    call.cancel();
                    dismissLoading();
                }
            });
        }
    }

    private void getItemDetails(ChatResponse.Chat chat) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_ITEM_ID, chat.getItemId());
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(getApplicationContext()));
            Call<HomeItemResponse> call = apiInterface.getItemData(map);
            call.enqueue(new Callback<HomeItemResponse>() {
                @Override
                public void onResponse(Call<HomeItemResponse> call, retrofit2.Response<HomeItemResponse> response) {
                    try {
                        if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                            if (response.body().getResult().getItems().size() > 0) {
                                itemData = response.body().getResult().getItems().get(0);
                                getAddress(chat, addressResultData);
                            }
                        } else if (response.body().getStatus().equalsIgnoreCase(Constants.TAG_FALSE_CODE)) {
                            JoysaleApplication.disabledialog(ChatActivity.this, "" + response.body().getMessage(), GetSet.getUserId());
                        } else if (response.body().getStatus().equalsIgnoreCase("error")) {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            JoysaleApplication.openDeleteDialog(getString(R.string.account_delete), getString(R.string.ok));
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "getItemDetailsError: " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<HomeItemResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void getAddress(ChatResponse.Chat chat, ShippingAddressRes.Result addressResult) {

        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_ITEM_ID, chat.getItemId());

            Call<ShippingAddressRes> call = apiInterface.getShippingAddress(map);
            call.enqueue(new Callback<ShippingAddressRes>() {
                @Override
                public void onResponse(Call<ShippingAddressRes> call, retrofit2.Response<ShippingAddressRes> response) {
                    try {
                        ArrayList<ShippingAddressRes.Result> addressAry = new ArrayList<>();
                        if (response.isSuccessful() && response.body().getStatus().equalsIgnoreCase("true")) {
                            addressAry.addAll(response.body().getResult());

                            if (dialog != null && dialog.isShowing()) {
                                dialog.dismiss();
                            }

                            if (addressAry.size() > 0) {
                                Log.d(TAG, "onResponse: " + addressAry.get(0).getAddress1());
                                ShippingAddressRes.Result defaultAddress = new ShippingAddressRes().new Result();
                                boolean haveDefaultAddress = false;
                                for (ShippingAddressRes.Result result : addressAry) {
                                    if (result.getDefaultShipping().equals("1")) {
                                        haveDefaultAddress = true;
                                        defaultAddress = result;
                                        break;
                                    }
                                }

                                //Addon BuyNow
                                if (!haveDefaultAddress) {
                                    if (addressResult != null) {
                                        Intent checkout = new Intent(ChatActivity.this, Checkout.class);
                                        checkout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        checkout.putExtra(Constants.TAG_ITEM_DATA, itemData);
                                        checkout.putExtra(Constants.TAG_OFFER_ID, chat.getOfferId());
                                        checkout.putExtra(Constants.TAG_OFFER_PRICE, chat.getOfferPrice());
                                        checkout.putExtra(Constants.TAG_OFFER_TOTAL, chat.getTotalOfferPrice());
                                        checkout.putExtra(Constants.TAG_FORMATTED_OFFER_PRICE, chat.getFormattedOfferPrice());
                                        checkout.putExtra(Constants.TAG_FORMATTED_OFFER_TOTAL, chat.getFormattedTotalOfferPrice());
                                        checkout.putExtra(Constants.TAG_FORMATTED_SHIPPING_PRICE, chat.getFormattedShippingPrice());
                                        checkout.putExtra(Constants.TAG_OFFER_CURRENCY, chat.getOfferCurrency());
                                        checkout.putExtra(Constants.TAG_OFFER_CURRENCY_CODE, chat.getOfferCurrencyCode());
                                        checkout.putExtra(Constants.TAG_OFFER_STATUS, chat.getOfferStatus());
                                        checkout.putExtra(Constants.TAG_SHIPPING_DATA, addressResult);
                                        startActivity(checkout);
                                    } else {
                                        Intent l = new Intent(ChatActivity.this, Addresses.class);
                                        l.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        l.putExtra(Constants.TAG_FROM, "checkout");
                                        l.putExtra(Constants.TAG_SHIPPING_ID, "" + 0);
                                        l.putExtra(Constants.TAG_OFFER_ID, chat.getOfferId());
                                        l.putExtra(Constants.TAG_OFFER_PRICE, chat.getOfferPrice());
                                        l.putExtra(Constants.TAG_OFFER_CURRENCY, chat.getOfferCurrency());
                                        l.putExtra(Constants.TAG_OFFER_CURRENCY_CODE, chat.getOfferCurrencyCode());
                                        l.putExtra(Constants.TAG_OFFER_STATUS, chat.getOfferStatus());
                                        l.putExtra(Constants.TAG_ITEM_ID, itemData.getId());
                                        l.putExtra(Constants.TAG_COUNTRYID, "" + itemData.getCountryId());
                                        startActivityForResult(l, ADDRESS_REQUEST_CODE);
                                    }
                                } else {
                                    Intent checkout = new Intent(ChatActivity.this, Checkout.class);
                                    checkout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    checkout.putExtra(Constants.TAG_ITEM_DATA, itemData);
                                    checkout.putExtra(Constants.TAG_OFFER_ID, chat.getOfferId());
                                    checkout.putExtra(Constants.TAG_OFFER_PRICE, chat.getOfferPrice());
                                    checkout.putExtra(Constants.TAG_OFFER_TOTAL, chat.getTotalOfferPrice());
                                    checkout.putExtra(Constants.TAG_FORMATTED_OFFER_PRICE, chat.getFormattedOfferPrice());
                                    checkout.putExtra(Constants.TAG_FORMATTED_OFFER_TOTAL, chat.getFormattedTotalOfferPrice());
                                    checkout.putExtra(Constants.TAG_FORMATTED_SHIPPING_PRICE, chat.getFormattedShippingPrice());
                                    checkout.putExtra(Constants.TAG_OFFER_CURRENCY, chat.getOfferCurrency());
                                    checkout.putExtra(Constants.TAG_OFFER_CURRENCY_CODE, chat.getOfferCurrencyCode());
                                    checkout.putExtra(Constants.TAG_OFFER_STATUS, chat.getOfferStatus());
                                    checkout.putExtra(Constants.TAG_SHIPPING_DATA, defaultAddress);
                                    startActivity(checkout);
                                }
                            }
                        } else if (response.body().getStatus().equalsIgnoreCase(Constants.TAG_FALSE_CODE)) {
                            JoysaleApplication.disabledialog(ChatActivity.this, "" + response.body().getMessage(), GetSet.getUserId());
                        } else if (response.isSuccessful() && response.body().getStatus().equalsIgnoreCase("false") && response.body().getMessage().equals("Product already sold out")) {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            Toast.makeText(ChatActivity.this, getString(R.string.product_already_sold), Toast.LENGTH_LONG).show();
                        } else if (response.isSuccessful() && response.body().getStatus().equalsIgnoreCase("false") && response.body().getMessage().equals("Product in disabled status.")) {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            Toast.makeText(ChatActivity.this, getString(R.string.product_disabled_message), Toast.LENGTH_LONG).show();
                        } else if (response.isSuccessful() && response.body().getStatus().equalsIgnoreCase("false") && response.body().getMessage().equals("conversation blocked.")) {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            Toast.makeText(ChatActivity.this, getString(R.string.conversation_blocked), Toast.LENGTH_LONG).show();
                        } else if (response.isSuccessful() && response.body().getStatus().equalsIgnoreCase("error")) {
                            JoysaleApplication.disabledialog(ChatActivity.this, response.body().getMessage(), GetSet.getUserId());
                        } else {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            //Addon BuyNow
                            Intent add = new Intent(ChatActivity.this, AddAddress.class);
                            add.putExtra(Constants.TAG_FROM, "checkout");
                            add.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            add.putExtra(Constants.TAG_OFFER_ID, chat.getOfferId());
                            add.putExtra(Constants.TAG_OFFER_PRICE, chat.getOfferPrice());
                            add.putExtra(Constants.TAG_OFFER_CURRENCY, chat.getOfferCurrency());
                            add.putExtra(Constants.TAG_OFFER_CURRENCY_CODE, chat.getOfferCurrencyCode());
                            add.putExtra(Constants.TAG_OFFER_STATUS, chat.getOfferStatus());
                            add.putExtra("to", "primary");
                            add.putExtra(Constants.TAG_ITEM_ID, itemData.getId());
                            add.putExtra(Constants.TAG_COUNTRYID, "" + itemData.getCountryId());
                            startActivityForResult(add, ADDRESS_REQUEST_CODE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ShippingAddressRes> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    /**
     * Function for Sent a Offer Status to Socket
     */

    private void callOfferSocket(long time, UpdateOfferResponse.Result offer) {
        try {
            JSONObject jobj = new JSONObject();
            JSONObject message = new JSONObject();
            message.put(Constants.TAG_CHATTIME, Long.toString(time));
            message.put(Constants.SOCK_USERIMAGE, GetSet.getImageUrl().replace("/150/", "/40/"));
            message.put(Constants.SOCK_USERNAME, GetSet.getUserName());
            message.put(Constants.TAG_MESSAGE, "");
            message.put(Constants.SOCK_VIEW_URL, "");
            message.put(Constants.TAG_TYPE, "offer");
            message.put(Constants.TAG_LAT, "");
            message.put(Constants.TAG_LON, "");
            message.put(Constants.SOCK_MESSAGE_CONTENT, "1");

            message.put(Constants.TAG_OFFER_ID, offer.getOfferId());
            message.put(Constants.TAG_OFFER_TYPE, offer.getOfferType());
            message.put(Constants.TAG_OFFER_PRICE, offer.getOfferPrice());
            message.put(Constants.TAG_SHIPPING_COST, offer.getShippingCost());
            message.put(Constants.TAG_OFFER_TOTAL, offer.getTotalPrice());
            message.put(Constants.TAG_OFFER_CURRENCY, offer.getOfferCurrency());
            message.put(Constants.TAG_OFFER_CURRENCY_CODE, offer.getOfferCurrencyCode());
            message.put(Constants.TAG_CURRENCY_MODE, offer.getCurrencyMode());
            message.put(Constants.TAG_CURRENCY_POSITION, offer.getCurrencyPosition());
            message.put(Constants.TAG_OFFER_STATUS, offer.getOfferStatus());
            message.put(Constants.TAG_BUYNOW_STATUS, offer.getBuynowStatus());
            message.put(Constants.TAG_INSTANT_BUY, offer.getInstantBuy());
            message.put(Constants.SOCK_SOLD_ITEM, offer.getSoldItem());
            message.put(Constants.SOCK_SITE_BUYNOW_PAYMENT_MODE, offer.getSiteBuynowPaymentMode());
            message.put(Constants.TAG_ITEMIMAGE, offer.getItemImage());
            message.put(Constants.TAG_ITEM_ID, offer.getItemId());
            message.put(Constants.SOCK_BUYNOW_URL, offer.getBuynowUrl());
            message.put(Constants.TAG_ITEM_STATUS, "1");
            message.put(Constants.TAG_SELLERNAME, GetSet.getUserName());
            message.put(Constants.TAG_FORMATTED_OFFER_PRICE, offer.getFormattedOfferPrice());
            message.put(Constants.TAG_FORMATTED_SHIPPING_PRICE, offer.getFormattedShippingPrice());
            message.put(Constants.TAG_FORMATTED_OFFER_TOTAL, offer.getFormattedTotalPrice());

            jobj.put(Constants.SOCK_RECEIVERID, GetSet.getUserName());
            jobj.put(Constants.SOCK_SENDERID, userName);
            jobj.put(Constants.TAG_MESSAGE, message);
            jobj.put("offerId", offer.getOfferId());
            Log.v(TAG, "callOfferSocket: " + jobj);
            mSocket.emit("message", jobj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showLoading() {
        if (dialog != null && !dialog.isShowing())
            dialog.show();
    }

    public void dismissLoading() {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
    }

    /**
     * Function for send message through socket and API
     */
    private void sendMessage(String msg) {
        long unixTime = System.currentTimeMillis() / 1000L;
        callSocket(unixTime, "text", msg, "");
        ChatResponse.Message message = new ChatResponse().new Message();
        ChatResponse.Chat chat = new ChatResponse().new Chat();

        try {
            if (from.equals("detail") && !aboutMessageSent && !tempMsgSent) {
                tempMsgSent = true;
                message.setMessage(msg);
                chat.setSender(GetSet.getUserName());
                message.setChatTime("" + unixTime);
                chat.setType("about");
                chat.setItemId(data.getId());
                chat.setItemTitle(JoysaleApplication.stripHtml(data.getItemTitle()));
                chat.setItemImage(data.getPhotos().get(0).getItemUrlMainOriginal());
//                chat.setOfferPrice(data.getBestOffer(Constants.TAG_OFFER_PRICE));
                chat.setItemStatus("1");
            } else {
                message.setMessage(msg);
                chat.setSender(GetSet.getUserName());
                message.setChatTime(Long.toString(unixTime));
                chat.setType("message");

            }
            chat.setMessage(message);
            chats.add(chat);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listView.getVisibility() != View.VISIBLE) {
                        listView.setVisibility(View.VISIBLE);
                    }
                    chatAdapter.notifyDataSetChanged();
                }
            });

            sendChat("normal", msg, "", 0.0, 0.0, "");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Function for send the message
     **/

    private void sendChat(final String type, final String message, final String imageName, double lat, double lon, String duration) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            long unixTime = System.currentTimeMillis() / 1000L;
            try {
                map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
                map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
                map.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
                map.put(Constants.TAG_CHAT_ID, chatId);
                if (from.equals("detail") && !aboutMessageSent) {
                    aboutMessageSent = true;
                    map.put(Constants.TAG_SOURCE_ID, data.getId());
                } else {
                    map.put(Constants.TAG_SOURCE_ID, "0");
                }
                map.put(Constants.TAG_TYPE, type);
                map.put(Constants.TAG_CHAT_TYPE, "normal");
                map.put(Constants.TAG_CREATED_DATE, Long.toString(unixTime));
                map.put(Constants.TAG_MESSAGE, message);
                if (type.equals("gif"))
                    map.put(Constants.TAG_IMAGE_URL, imageName);
                else
                    map.put(Constants.TAG_IMAGE_URL, imageName);

                //Addons Voice Message
                if (type.equals("audio_msg"))
                    map.put(Constants.TAG_AUDIO_URL, imageName);
                else
                    map.put(Constants.TAG_AUDIO_URL, "");

                if (Constants.ADDON_VOICE_ENABLED) {
                    map.put(Constants.TAG_AUDIO_DURATION, duration);
                }
            } catch (Exception e) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        chats.remove(chats.size() - 1);
                        chatAdapter.notifyDataSetChanged();
                        try {
                            JoysaleApplication.dialog(ChatActivity.this, getString(R.string.alert), getString(R.string.symbols_not_supported));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                e.printStackTrace();
            }

            Log.d(TAG, "sendChatRequest: " + new Gson().toJson(map));
            Call<HashMap<String, String>> call = apiInterface.sendChat(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    dismissProgressDialog();
                    if (response.body().get(Constants.TAG_STATUS).equals("true")) {
                        aboutMessageSent = true;
                        send.setOnClickListener(ChatActivity.this);
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    } else if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase(Constants.TAG_FALSE_CODE)) {
                        JoysaleApplication.disabledialog(ChatActivity.this, "" + response.body().get(Constants.TAG_MESSAGE), GetSet.getUserId());
                    } else {
                        blockChat(true, false);
                    }

                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                    send.setOnClickListener(ChatActivity.this);
                    dismissProgressDialog();
                }
            });
        }
    }

    private void deleteFileForUri(Uri uri) {
        if (uri != null) {
            ContentResolver contentResolver = getContentResolver();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                if (contentResolver.delete(uri, null, null) > 0) {
                    // Timber.i("Save to gallery: deleted %s", uri.getPath());
                } else {
                    //   Timber.i("Save to gallery: failed deleting %s", uri.getPath());
                }
            } else {
                /* For Android 11 (R) and above, use this snippet */
                        /*PendingIntent trashPendingIntent = MediaStore.createTrashRequest(contentResolver, Collections.singleton(mTempSavedFileUri), true);
                        try {
                            startIntentSenderForResult(trashPendingIntent.getIntentSender(), 120, null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                            Timber.e("Save to gallery: failed deleting %s", mTempSavedFileUri.getPath());
                        }*/
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult: " + requestCode + ", " + resultCode);
        if(resultCode==-1 && requestCode == 2000){
            ArrayList<Photo> photoList = (ArrayList<Photo>) data.getExtras().get("galleryData");
            Log.d(TAG, "onActivityResult: "+photoList);
            for (Photo selectedImage : photoList) {
                HomeItemResponse.Photo photo = new HomeItemResponse().new Photo();
                photo.setType(selectedImage.getType());
                photo.setImage("" + selectedImage.getImage());
                photo.setPathType(Constants.TAG_GALLERY);

                Log.e("selectedImage", "-" + selectedImage.getImage());


                ImageCompression imageCompression = new ImageCompression(ChatActivity.this) {
                    @Override
                    protected void onPostExecute(String imagePath) {
//                        new UploadImage().execute(imagePath);

                        long unixTime = System.currentTimeMillis() / 1000L;
                        ChatResponse.Message message = new ChatResponse().new Message();
                        ChatResponse.Chat chat = new ChatResponse().new Chat();
                        chat.setSender(GetSet.getUserName());
                        message.setChatTime("" + unixTime);
                        message.setImageName(imagePath);
                        chat.setMessage(message);
                        chat.setType("image");
                        chats.add(chat);
                        chatAdapter.notifyDataSetChanged();
                        new UploadImage(ChatActivity.this, "Chat", userImg, unixTime, chat).execute(imagePath);
                    }
                };
                imageCompression.execute(selectedImage.getImage());
            }
        }
       else if (resultCode == -1 && requestCode == 234) {
            final File file = new File(ImagePicker.getImageFilePath(this, requestCode, resultCode, data));
            if (file.exists()) {
                Log.v(TAG, "selectedImageFile=" + file.getAbsolutePath());
                ImageCompression imageCompression = new ImageCompression(ChatActivity.this) {
                    @Override
                    protected void onPostExecute(String imagePath) {
                        Log.i(TAG, "onPostExecute: " + imagePath);
                        long unixTime = System.currentTimeMillis() / 1000L;
                        ChatResponse.Message message = new ChatResponse().new Message();
                        ChatResponse.Chat chat = new ChatResponse().new Chat();
                        chat.setSender(GetSet.getUserName());
                        message.setChatTime("" + unixTime);
                        message.setImageName(imagePath);
                        chat.setMessage(message);
                        chat.setType("image");
                        chats.add(chat);
                        chatAdapter.notifyDataSetChanged();
                        new UploadImage(ChatActivity.this, "Chat", userImg, unixTime, chat).execute(imagePath);
                    }
                };
                Log.d(TAG, "onActivityResult: " + file.getAbsolutePath());
                imageCompression.execute(file.getAbsolutePath());
            } else {
                Toast.makeText(this, getString(R.string.profile_problem), Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_PICK_IMAGE) {
            try {
                boolean isCamera = (data == null
                        || data.getData() == null);
                String fileName = getString(R.string.app_name) + System.currentTimeMillis() + ".jpg";
                Uri file = null;
                if (isCamera) {     /** CAMERA **/
                    Log.d(TAG, "onActivityResultcamera: " + mCurrentPhotoUri.getPath());
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        file = mCurrentPhotoUri;
                    } else {
                        file = storageUtils.saveToSDCard(FileUtil.decodeBitmap(this, mCurrentPhotoUri), Constants.TAG_SENT, fileName);
                    }
                    deleteFileForUri(mCurrentPhotoUri);
                    mCurrentPhotoUri = null;
                    //      Timber.d("Picked: %s fromCamera: %s", imageUri, true);
                } else {            /** ALBUM **/
                    String mimeType = getContentResolver().getType(data.getData());
                    if (mimeType != null && mimeType.startsWith("image")) {
                        file = storageUtils.saveToSDCard(FileUtil.decodeBitmap(this, data.getData()), Constants.TAG_SENT, fileName);
                        //   Timber.d("Picked: %s", imageUri);
                    } else {
                        //    Toasty.info(this, "Videos are not supported yet").show();
                        return;
                    }
                }
                if (file != null) {
                    try {
                        BitmapCompression imageCompression = new BitmapCompression(getApplicationContext(), file, file.getPath()) {
                            @Override
                            protected void onPostExecute(Bitmap compressedBitmap) {
                                String thumbnail = storageUtils.saveThumbNail(compressedBitmap, fileName);
                                long unixTime = System.currentTimeMillis() / 1000L;
                                ChatResponse.Message message = new ChatResponse().new Message();
                                ChatResponse.Chat chat = new ChatResponse().new Chat();
                                chat.setSender(GetSet.getUserName());
                                message.setChatTime("" + unixTime);
                                message.setImageName(thumbnail);
                                chat.setMessage(message);
                                chat.setType("image");
                                chats.add(chat);
                                chatAdapter.notifyDataSetChanged();
                                new UploadImage(ChatActivity.this, "Chat", userImg, unixTime, chat).execute(thumbnail);
                            }
                        };
                        imageCompression.execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == ADDRESS_REQUEST_CODE && resultCode == RESULT_OK) {
            if (selectedOfferChat != null) {
                if (dialog != null && !dialog.isShowing())
                    dialog.show();
                ShippingAddressRes.Result selectedAddress = (ShippingAddressRes.Result) data.getSerializableExtra(Constants.TAG_SHIPPING_DATA);
                // stringCountryCode = data.getStringExtra("shippingCountryCode");
                getAddress(selectedOfferChat, selectedAddress);
            }

        } else if (requestCode == Constants.TAG_DEFAULT_REQUEST_CODE) {
            chatAdapter.notifyDataSetChanged();
        } else if (requestCode == Constants.LOCATION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String location = data.getStringExtra(Constants.TAG_LOCATION);
                Double lat = data.getDoubleExtra(Constants.TAG_LAT, 0);
                Double lng = data.getDoubleExtra(Constants.TAG_LON, 0);
                sendLocationToServer("normal", "share_location", "", lat, lng);
            }
        }
    }


    /**
     * Function for Sent a message to Socket
     */

    private void callSocket(long time, String type, String data, String duration) {

        try {
            JSONObject jobj = new JSONObject();
            JSONObject message = new JSONObject();
            message.put(Constants.TAG_CHATTIME, Long.toString(time));
            message.put(Constants.SOCK_USERIMAGE, GetSet.getImageUrl().replace("/150/", "/40/"));
            message.put(Constants.SOCK_USERNAME, GetSet.getUserName());
            message.put(Constants.SOCK_CHATURL, stringChatURL);
            if (type.equals("text")) {
                message.put(Constants.TAG_MESSAGE, data);
                message.put(Constants.SOCK_VIEW_URL, "");
                message.put(Constants.TAG_TYPE, "normal");
                message.put(Constants.TAG_LAT, "");
                message.put(Constants.TAG_LON, "");
                message.put(Constants.SOCK_MESSAGE_CONTENT, "1");

            } else if (type.equals("image")) {
                message.put(Constants.TAG_MESSAGE, "");
                message.put(Constants.SOCK_VIEW_URL, data);
                message.put(Constants.TAG_TYPE, "image");
                message.put(Constants.TAG_LAT, "");
                message.put(Constants.TAG_LON, "");
                message.put(Constants.SOCK_MESSAGE_CONTENT, "2");
            } else if (type.equals("audio_msg")) {
                message.put(Constants.TAG_TYPE, "audio_msg");
                message.put(Constants.TAG_AUDIO_DURATION, duration);
                message.put(Constants.TAG_LAT, "");
                message.put(Constants.TAG_LON, "");
                message.put(Constants.SOCK_MESSAGE_CONTENT, "2");
                message.put(Constants.TAG_MESSAGE, "");
                message.put(Constants.SOCK_VIEW_URL, data);
                Log.v(TAG, "sendjsoninsocket=audio" + message);
            } else if (type.equals("gif")) {
                message.put(Constants.TAG_MESSAGE, "");
                message.put(Constants.SOCK_VIEW_URL, data);
                message.put(Constants.TAG_TYPE, "gif");
                message.put(Constants.TAG_LAT, "");
                message.put(Constants.TAG_LON, "");
                message.put(Constants.SOCK_MESSAGE_CONTENT, "2");
            }
            jobj.put(Constants.SOCK_RECEIVERID, GetSet.getUserName());
            jobj.put(Constants.SOCK_SENDERID, userName);
            jobj.put("offerId", "0");
            jobj.put(Constants.TAG_MESSAGE, message);
            Log.v(TAG, "sendjsoninsocket=" + jobj);
            mSocket.emit("message", jobj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Function for Open a particular Screen based on a selected choice
     */

    public void openAction(String from) {
        Log.v(TAG, "openAction-from=" + from);
        if (from.equals(getString(R.string.make_an_offer))) {

        } else if (from.equals(getString(R.string.safety_tips))) {
            dialog.setMessage(getString(R.string.pleasewait));
            dialog.show();
            showSafetyTips();
        } else if (from.equals(getString(R.string.block_user))) {
            final Dialog alertDialog = new Dialog(ChatActivity.this, R.style.AlertDialog);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog.setContentView(R.layout.default_dialog);
            alertDialog.getWindow().setLayout(displayMetrics.widthPixels * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
            alertDialog.setCancelable(true);
            alertDialog.setCanceledOnTouchOutside(false);

            TextView alertMsg = alertDialog.findViewById(R.id.alert_msg);
            TextView alertOk = alertDialog.findViewById(R.id.alert_button);
            TextView alertCancel = alertDialog.findViewById(R.id.cancel_button);

            alertMsg.setText(getString(R.string.reallyBlock));
            alertOk.setText(getString(R.string.block));
            alertCancel.setText(getString(R.string.cancel));

            alertCancel.setVisibility(View.VISIBLE);

            alertOk.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    dialog.setMessage(getString(R.string.pleasewait));
                    dialog.show();
                    blockorUnblockUser(userId, "block");
                }
            });

            alertCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });

            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
        } else if (from.equals(getString(R.string.unblock_user))) {
            dialog.setMessage(getString(R.string.pleasewait));
            dialog.show();
            blockorUnblockUser(userId, "unblock");
        }
    }

    /**
     * Function for Block or Unblock User
     */

    private void blockorUnblockUser(final String blockUserId, final String value) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_ACTION_ID, blockUserId);
            map.put(Constants.TAG_ACTION_VALUE, value);
            Call<HashMap<String, String>> call = apiInterface.updateBlockStatus(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    if (dialog.isShowing())
                        dialog.dismiss();
                    if (response.body().get(Constants.TAG_STATUS).equals("true")) {
                        if (response.body().get(Constants.TAG_MESSAGE).equals("Blocked Successfully")) {
                            blockChat(false, true);
                            finish();
                        } else {

                            blockChat(false, false);
                        }
                    } else if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase(Constants.TAG_FALSE_CODE)) {
                        JoysaleApplication.disabledialog(ChatActivity.this, "" + response.body().get(Constants.TAG_MESSAGE), GetSet.getUserId());
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    /**
     * Function for Show Safety Tips
     */

    private void showSafetyTips() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(this));
            Log.d(TAG, "showSafetyTips: " + map);
            Call<HashMap<String, String>> call = apiInterface.getSafetyTips(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    if (dialog.isShowing())
                        dialog.dismiss();

                    Log.d(TAG, "showSafetyTips: " + response.body().toString());
                    if (response.isSuccessful()) {
                        if (response.body().get(Constants.TAG_STATUS).equals("true")) {
                            if (response.body().get(TAG_MESSAGE) != null) {
                                Intent i = new Intent(ChatActivity.this, AboutUs.class);
                                i.putExtra(Constants.TAG_TITLE_M, getString(R.string.safety_tips));
                                i.putExtra(Constants.CONTENT, response.body().get(Constants.TAG_MESSAGE));
                                startActivity(i);
                            } else {
                                Toast.makeText(getApplicationContext(), getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();

                            }
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    /**
     * Function for block a Chat
     */

    private void blockChat(boolean isBlocked, boolean isUsrBlockedByMe) {
        if (isUsrBlockedByMe) {
            isUserBlocked = true;
            //Addons AudioVideoCall
            audiovideocallbtn.setVisibility(View.GONE);
            blockUserLay.setVisibility(View.VISIBLE);
            if (values.size() > 1)
                values.set(1, getString(R.string.unblock_user));
            sendMsgLay.setVisibility(View.GONE);
            bottom.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            btnCall.setVisibility(View.GONE);
        } else if (isBlocked) {
            isUserBlocked = true;
            if (Constants.ADDON_CALL_ENABLED) {
                audiovideocallbtn.setVisibility(View.GONE);
            }
            blockUserLay.setVisibility(View.VISIBLE);
            blockMsg.setText(getString(R.string.block_user_msg_receiver));
            if (values.size() > 1)
                values.remove(1);
            sendMsgLay.setVisibility(View.GONE);
            bottom.setVisibility(View.GONE);
            btnCall.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        } else {
            isUserBlocked = false;
            if (Constants.ADDON_CALL_ENABLED) {
                audiovideocallbtn.setVisibility(View.VISIBLE);
            }
            if (!isUserDeleted) {
                blockUserLay.setVisibility(View.GONE);
            }
            if (values.size() > 1)
                values.set(1, getString(R.string.block_user));
            if (!isUserDeleted) {
                bottom.setVisibility(View.VISIBLE);
                sendMsgLay.setVisibility(View.VISIBLE);
            }
            recyclerView.setVisibility(View.VISIBLE);
            //Addon SmartReply
            if (Constants.ADDON_SMART_REPLY_ENABLED) {
                if (mChipAdapter != null && mChipAdapter.getItemCount() > 0) {
                    mSmartRepliesRecycler.setVisibility(View.VISIBLE);
                } else {
                    mSmartRepliesRecycler.setVisibility(View.GONE);
                }
            }
            recyclerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
        Log.v(TAG, "beforeTextChanged");
    }

    /**
     * Method for send typing status to other end user
     **/

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //Addon Voice Message
        if (count == 0) {
            send.setVisibility(View.VISIBLE);
            setRecordButtonVisibility(Constants.ADDON_VOICE_ENABLED ? View.VISIBLE : View.GONE);
        } else {
            send.setVisibility(View.VISIBLE);
            setRecordButtonVisibility(View.GONE);
            setRecordViewVisibility(View.GONE);
        }

        if (runnable != null)
            handler.removeCallbacks(runnable);
        if (!meTyping) {
            meTyping = true;
            JSONObject jobj = new JSONObject();
            try {
                jobj.put(Constants.SOCK_SENDERID, userName);
                jobj.put(Constants.SOCK_RECEIVERID, GetSet.getUserName());
                jobj.put(Constants.TAG_MESSAGE, "type");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mSocket.emit("messageTyping", jobj);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        runnable = new Runnable() {
            public void run() {
                meTyping = false;
                JSONObject jobj = new JSONObject();
                try {
                    jobj.put(Constants.SOCK_SENDERID, userName);
                    jobj.put(Constants.SOCK_RECEIVERID, GetSet.getUserName());
                    jobj.put(Constants.TAG_MESSAGE, "untype");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mSocket.emit("messageTyping", jobj);
            }
        };
        handler.postDelayed(runnable, 1000);
    }


    private void getItemData(String itemId) {
        if (NetworkReceiver.isConnected()) {
            final ArrayList<HomeItemResponse.Item> HomeItems = new ArrayList<>();
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_ITEM_ID, itemId);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(getApplicationContext()));
            Call<HomeItemResponse> call = apiInterface.getItemData(map);
            call.enqueue(new Callback<HomeItemResponse>() {
                @Override
                public void onResponse(Call<HomeItemResponse> call, retrofit2.Response<HomeItemResponse> response) {
                    if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        HomeItems.addAll(response.body().getResult().getItems());
                    } else if (response.body().getStatus().equalsIgnoreCase(Constants.TAG_FALSE_CODE)) {
                        JoysaleApplication.disabledialog(ChatActivity.this, "" + response.body().getMessage(), GetSet.getUserId());
                    }
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    if (HomeItems.size() == 0) {
                        Toast.makeText(ChatActivity.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                    } else {
                        if (isUserDeleted) {
                            //Toast.makeText(getApplicationContext(), "This Account Has Been Deleted", Toast.LENGTH_LONG).show();
                            JoysaleApplication.openDeleteDialog(getString(R.string.account_delete), getString(R.string.ok));
                        } else {
                            Intent i = new Intent(ChatActivity.this, DetailActivity.class);
                            i.putExtra(Constants.TAG_DATA, HomeItems.get(0));
                            i.putExtra(Constants.TAG_POSITION, 0);
                            i.putExtra(Constants.TAG_FROM, "chat");
                            startActivity(i);
                        }
                    }
                }

                @Override
                public void onFailure(Call<HomeItemResponse> call, Throwable t) {
                    call.cancel();
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            });
        }
    }

    public void dismissProgress(ProgressDialog pd) {
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
    }

    /**
     * Function for Onclick Events
     */

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                onBackPressed();
                break;
            case R.id.btngif:
                //Addon Gif
                giphyDialogFragment = GiphyDialogFragment.Companion.newInstance(settings, Constants.KEY_GIPHY_API, false);
                giphyDialogFragment.show(getSupportFragmentManager(), "giphy_dialog");
                giphyDialogFragment.setGifSelectionListener(this);
                break;
            case R.id.sharelocation:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    int res = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                    int res1 = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
                    if (res != PackageManager.PERMISSION_GRANTED && res1 != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 123);
                    } else {
                        //Addon Mapbox
                        if (Constants.ADDON_MAP_BOX_ENABLED) {
                           /* Intent in = new Intent(ChatActivity.this, MainActivity.class);
                            in.putExtra(Constants.TAG_FROM, "chat");
                            in.putExtra(Constants.CHATID, chatId);
                            in.putExtra(Constants.TAG_USERNAME, userName);
                            in.putExtra(Constants.TAG_USER_ID, userId);
                            in.putExtra(Constants.TAG_USERIMAGE_M, userImage);
                            in.putExtra(Constants.TAG_FULL_NAME, fullName);
                            startActivityForResult(in, Constants.LOCATION_REQUEST_CODE);*/
                        } else {
                            Intent in = new Intent(ChatActivity.this, LocationActivity.class);
                            in.putExtra(Constants.TAG_FROM, "chat");
                            in.putExtra(Constants.CHATID, chatId);
                            in.putExtra(Constants.TAG_USERNAME, userName);
                            in.putExtra(Constants.TAG_USER_ID, userId);
                            in.putExtra(Constants.TAG_USERIMAGE_M, userImage);
                            in.putExtra(Constants.TAG_FULL_NAME, fullName);
                            locationResult.launch(in);
                            break;
                        }
                    }

                } else {
                    int res = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                    int res1 = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
                    if (res != PackageManager.PERMISSION_GRANTED || res1 != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 123);
                    } else {
                        //Addon Mapbox
                        if (Constants.ADDON_MAP_BOX_ENABLED) {
                          /*  Intent in = new Intent(ChatActivity.this, MainActivity.class);
                            in.putExtra(Constants.TAG_FROM, "chat");
                            in.putExtra(Constants.CHATID, chatId);
                            in.putExtra(Constants.TAG_USERNAME, userName);
                            in.putExtra(Constants.TAG_USER_ID, userId);
                            in.putExtra(Constants.TAG_USERIMAGE_M, userImage);
                            in.putExtra(Constants.TAG_FULL_NAME, fullName);
                            locationResult.launch(in);*/
                        } else {
                            Intent in = new Intent(ChatActivity.this, LocationActivity.class);
                            in.putExtra(Constants.TAG_FROM, "chat");
                            in.putExtra(Constants.CHATID, chatId);
                            in.putExtra(Constants.TAG_USERNAME, userName);
                            in.putExtra(Constants.TAG_USER_ID, userId);
                            in.putExtra(Constants.TAG_USERIMAGE_M, userImage);
                            in.putExtra(Constants.TAG_FULL_NAME, fullName);
                            locationResult.launch(in);
                            break;
                        }
                    }
                }
                break;

            case R.id.shareImg:
if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
    Log.d(TAG, "onClick11111: ");
    if (ContextCompat.checkSelfPermission(ChatActivity.this, READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)
    {
        storagePermissionResult.launch(new String[]{READ_MEDIA_IMAGES});
    }
    else{
        pickImage();
    }

//                    if (ContextCompat.checkSelfPermission(ChatActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED
//                          && ContextCompat.checkSelfPermission(ChatActivity.this, READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED  ) {
//                        galleryResultLauncher.launch(PermissionsUtils.CHAT_READ_MEDIA_IMAGE);
//                    storagePermissionResult.launch(PermissionsUtils.CHAT_READ_MEDIA_IMAGE);
//                    }
//                    else {
//                        Log.d(TAG, "onClick22222222222: ");
//                        pickImage();
//                    }
}
else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            ImagePicker.pickImage(this, "Select your image:");
                        } else {
                            pickImage();
                        }
                    }



                break;
            case R.id.send:
                Log.d(TAG, "onClick: " + Constants.SOCKET_URL);
                if (editText.getText().toString().trim().length() > 0) {
                    String j = editText.getText().toString();
                    send.setOnClickListener(null);
                    if (j.length() > 0) {
                        if (j.contains("/>") || j.contains("</")) {
                            Toast.makeText(getApplicationContext(), "hai!! null", Toast.LENGTH_LONG).show();
                            editText.setText("");
                        } else if (j.contains(">") && j.contains("<")) {
                            editText.setText("");
                        }
                    }
                    editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            btnRecord.setVisibility(View.GONE);
                        }
                    });

                    sendMessage(editText.getText().toString());
                    //Addon smart reply
                    //hideSmartReply();
                    editText.setText("");
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.please_enter_message), Toast.LENGTH_LONG).show();
                }

                break;
            case R.id.settingbtn:
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        R.layout.share_new, android.R.id.text1, values);
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = layoutInflater.inflate(R.layout.share, null);
                layout.setAnimation(AnimationUtils.loadAnimation(this, R.anim.grow_from_topright_to_bottomleft));
                final PopupWindow popup = new PopupWindow(ChatActivity.this);
                popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                popup.setContentView(layout);
                popup.setWidth(displayMetrics.widthPixels * 60 / 100);
                popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                popup.setFocusable(true);
                popup.showAtLocation(main, Gravity.TOP | Gravity.RIGHT, 0, 20);

                final ListView lv = layout.findViewById(R.id.lv);
                lv.setAdapter(adapter);
                popup.showAsDropDown(v);

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        popup.dismiss();
                        openAction(values.get(position));
                    }
                });
                break;
            case R.id.userImg:
                if (isUserDeleted) {
                    //Toast.makeText(getApplicationContext(), "This Account Has Been Deleted", Toast.LENGTH_SHORT).show();
                    JoysaleApplication.openDeleteDialog(getString(R.string.account_delete), getString(R.string.ok));
                } else {
                    Intent u = new Intent(ChatActivity.this, ProfileActivity.class);
                    u.putExtra(Constants.TAG_USER_ID, userId);
                    startActivity(u);
                }
                break;
            case R.id.selectbtn:
                /*if (ActivityCompat.checkSelfPermission(ChatActivity.this,
                        CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{CALL_PHONE}, 11111);
                } else*/
                if (isUserBlocked) {
                    Toast.makeText(getApplicationContext(), getString(R.string.conversation_blocked), Toast.LENGTH_SHORT).show();
                } else {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + mobileNo));
                    startActivity(callIntent);
                }
                break;
            case R.id.langtrans:
                Intent trans = new Intent(ChatActivity.this, LanguageActivity.class);
                trans.putExtra(Constants.TAG_FROM, "Chat");
                startActivityForResult(trans, Constants.TAG_DEFAULT_REQUEST_CODE);
                break;
            case R.id.audiovideocallbtn:
                if (!isUserDeleted) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (isPermissionsGranted(AppRTCUtils.MANDATORY_PERMISSIONSFORANDROID12)) {
                            audioVideoScreen();
                        } else
                            Toast.makeText(getApplicationContext(), getString(R.string.read_phone_state_permission_forandroid12), Toast.LENGTH_LONG).show();
                        request(AppRTCUtils.MANDATORY_PERMISSIONSFORANDROID12);
                    } else {
                        audioVideoScreen();
                    }
                }else if (isUserDeleted){
                    //     Toast.makeText(getApplicationContext(),"This Account has been Deleted",Toast.LENGTH_LONG).show();
                    JoysaleApplication.openDeleteDialog(getString(R.string.account_delete),getString(R.string.ok));
                }
                break;
        }
    }

    //Addon Smart Reply
    private void hideSmartReply() {
        runOnUiThread(new Runnable() {
            public void run() {
                chatAdapter.notifyDataSetChanged();
                if (chats.size() > 0) {
                    listView.setSelection(chats.size() - 1);
                }
                mSmartRepliesRecycler.setVisibility(View.GONE);
            }
        });
    }

    private boolean checkPermissions(String[] permissionList, FragmentMainActivity activity) {
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


    //Addon SmartReply
    @Override
    public void onChipClick(@NonNull String chipText) {
        Log.i(TAG, "onChipClick: " + chipText);
        long unixTime = System.currentTimeMillis() / 1000L;
        callSocket(unixTime, "text", chipText, "");
        ChatResponse.Message message = new ChatResponse().new Message();
        ChatResponse.Chat chat = new ChatResponse().new Chat();
        message.setMessage(chipText);
        message.setChatTime(String.valueOf(System.currentTimeMillis() / 1000L));
        chat.setMessage(message);
        chat.setSender(GetSet.getUserName());
        chat.setType("normal");
        chats.add(chat);

        sendChat("normal", chipText, "chat", 0.0, 0.0, "");

        runOnUiThread(new Runnable() {
            public void run() {
                chatAdapter.notifyDataSetChanged();
                if (chats.size() > 0) {
                    listView.setSelection(chats.size() - 1);
                }
                mSmartRepliesRecycler.setVisibility(View.GONE);
            }
        });

    }


    //Addon Voice Message
    private void setVoiceRecorder() {
        recordView.setCounterTimeColor(Color.parseColor("#a3a3a3"));
        recordView.setSmallMicColor(ContextCompat.getColor(ChatActivity.this, R.color.colorPrimary));
        recordView.setSlideToCancelTextColor(Color.parseColor("#a3a3a3"));
        recordView.setLessThanSecondAllowed(false);
        recordView.setSlideToCancelText(getString(R.string.slide_to_cancel));
        recordView.setCustomSounds(com.devlomi.record_view.R.raw.record_start, com.devlomi.record_view.R.raw.record_finished, com.devlomi.record_view.R.raw.record_error);
        //recordView.setCancelBounds(80);

        recordView.setRecordPermissionHandler(new RecordPermissionHandler() {
            @Override
            public boolean isPermissionGranted() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    return true;
                }

                boolean recordPermissionAvailable = PermissionsUtils.checkRecordAudioPermission(ChatActivity.this);
                if (recordPermissionAvailable) {
                    return true;
                }

                requestRecordPermission();

                return false;

            }
        });

        btnRecord.setOnRecordClickListener(new OnRecordClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnRecord.setRecordView(recordView);
        setRecordListener();
    }

    private void setToolBarClick(boolean value) {
        Log.i(TAG, "setToolBarClick: " + value);
        backBtn.setEnabled(value);
        if (Constants.ADDON_CALL_ENABLED) {
            audiovideocallbtn.setEnabled(value);
        }

        if (Constants.ADDON_TRANSLATE_ENABLED) {
            chattranslate.setEnabled(value);
        }
        settingbtn.setEnabled(value);
        userImg.setEnabled(value);
        btnCall.setEnabled(value);
        voicePlaying();
        recyclerView.setClickable(value);
        recyclerView.setEnabled(value);
        listView.setEnabled(value);
        listView.setClickable(value);
        //  chatAdapter.notifyDataSetChanged();
    }

    //Addon Voice Message
    private void setRecordListener() {
        recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                isRecording = true;
                voicePlaying();
                MediaPlayerUtils.releaseMediaPlayer();
                sharelocation.setVisibility(View.GONE);
                shareImg.setVisibility(View.GONE);

                if (!PermissionsUtils.checkRecordAudioPermission(ChatActivity.this)) {
                    recordView.setOnRecordListener(null);
                    requestRecordPermission();
                } else {
                    if (isUserBlocked) {
                        Toast.makeText(ChatActivity.this, getString(R.string.you_blocked), Toast.LENGTH_SHORT).show();
                    } else {
                        startRecord();
                    }
                }
            }

            @Override
            public void onFinish(long recordTime, boolean limitReached) {

                isRecording = false;
                if (recordTime > 1000) {
                    editText.requestFocus();
                    if (!NetworkReceiver.isConnected()) {
//                        HowzuApplication.showSnack(ChatActivity.this, findViewById(R.id.mainLay), isConnected);
                    } else if (isUserBlocked) {
                        Toast.makeText(ChatActivity.this, getString(R.string.you_blocked), Toast.LENGTH_SHORT).show();
                    } else {
                        if (null != mediaRecorder) {
                            try {
                                mediaRecorder.stop();
                            } catch (RuntimeException ignored) {
                            }
                        }
                        setToolBarClick(true);
                        btnRecord.setVisibility(View.VISIBLE);
                        setButtonVisibility(View.VISIBLE);
                        setRecordViewVisibility(View.GONE);
                        startUploadWork(recordVoicePath, ("" + milliSecondsToTimer(recordTime)));
                    }
                } else {
                    setToolBarClick(true);
                    MediaPlayerUtils.releaseMediaPlayer();
                    setButtonVisibility(View.VISIBLE);
                    setRecordViewVisibility(View.GONE);
                    showDialog(ChatActivity.this, getString(R.string.audio_length_error));
                }
            }

            @Override
            public void onCancel() {
                isRecording = false;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        JoysaleApplication.hideSoftKeyboard(ChatActivity.this);
                        editText.requestFocus();
                        showDialog(ChatActivity.this, getString(R.string.cancel_success));
                        setButtonVisibility(View.VISIBLE);
                        setRecordViewVisibility(View.GONE);
                        setToolBarClick(true);
                    }
                }, 1000);

            }

            @Override
            public void onLessThanSecond() {



                isRecording = false;
                setRecordViewVisibility(View.GONE);
                setToolBarClick(true);
                showDialog(ChatActivity.this, getString(R.string.audio_length_error));
                setButtonVisibility(View.VISIBLE);
            }
        });
    }

    private void startRecord() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startVoice();

            }
        }, 500);
        setRecordViewVisibility(View.VISIBLE);
        setButtonVisibility(View.GONE);
        setToolBarClick(false);
    }

    private void requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            storagePermissionResult.launch(PermissionsUtils.READ_STORAGE_PERMISSION);
        } else {
            storagePermissionResult.launch(PermissionsUtils.READ_WRITE_PERMISSIONS);
        }
    }

    private void requestRecordPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            recordPermissionResult.launch(PermissionsUtils.AUDIO_RECORD_V11_PERMISSION);
        } else {
            recordPermissionResult.launch(PermissionsUtils.AUDIO_RECORD_PERMISSION);
        }
    }

    //Addons VoiceMessage
    private void showDialog(final Context context, String message) {
        final androidx.appcompat.app.AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Addon Voice Message
                recordView.setVisibility(View.GONE);
                btnRecord.setVisibility(View.VISIBLE);
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    //Addons Voice Message
    private void setRecordButtonVisibility(int visibility) {
        btnRecord.setVisibility(visibility);
    }

    //Addons Voice Record
    private void setRecordViewVisibility(int visibility) {
        recordView.setVisibility(visibility);
    }

    private void setButtonVisibility(int visibility) {
        editText.setVisibility(visibility);
        if (Constants.ADDON_GIF_ENABLED) {
            layoutGif.setVisibility(visibility);
        }
        sharelocation.setVisibility(visibility);
        shareImg.setVisibility(visibility);

        if (Constants.ADDON_VOICE_ENABLED) {
            send.setVisibility(TextUtils.isEmpty(editText.getText()) ? View.INVISIBLE : View.VISIBLE);
        } else {
            setRecordButtonVisibility(View.GONE);
            setRecordViewVisibility(View.GONE);
        }
    }

    //Addon Voice Message
    private void startVoice() {
        setRecordViewVisibility(View.VISIBLE);
        if (!PermissionsUtils.checkRecordAudioPermission(ChatActivity.this)) {
            recordView.setOnRecordListener(null);
            requestRecordPermission();
        } else {
            if (NetworkReceiver.isConnected()) {
                String fileName = (getString(R.string.app_name) + "_" + System.currentTimeMillis() + ".mp3").replaceAll(" ", "");
                File mSrcDir = new File(imageStorage.getExtCacheDir(), fileName);
                recordVoicePath = mSrcDir.getAbsolutePath();
                MediaRecorderReady();
                try {
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                } catch (Exception e) {
                    Log.e(TAG, "startVoice: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private void startUploadWork(String recordVoicePath, String recordTime) {
        Log.d(TAG, "startUploadWork: " + recordTime);
        if (NetworkReceiver.isConnected()) {
            if (!ChatActivity.this.isFinishing()) {
                showProgressDialog();
            }
            Data.Builder builder = new Data.Builder();
            builder.putString(Constants.TAG_AUDIO, recordVoicePath);
            builder.putString(Constants.TAG_AUDIO_DURATION, recordTime);

            OneTimeWorkRequest uploadWorkRequest = new OneTimeWorkRequest.Builder(UploadFilesWorker.class)
                    .setConstraints(new Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build())
                    .setInputData(builder.build())
                    .build();
            WorkManager workManager = WorkManager.getInstance(getApplicationContext());
            workManager.enqueue(uploadWorkRequest);
            workManager.getWorkInfoByIdLiveData(uploadWorkRequest.getId()).observe(ChatActivity.this, workInfo -> {
                if (workInfo.getState().isFinished()) {
                    if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                        String uploadResponse = workInfo.getOutputData().getString(Constants.TAG_DATA);
                        String duration = workInfo.getOutputData().getString(Constants.TAG_AUDIO_DURATION);
                        Log.d(TAG, "startUploadWork: " + duration);
                        try {
                            imageUploading = false;
                            long mUnixTimeStamp = System.currentTimeMillis() / 1000L;
                            JSONObject response = new JSONObject(uploadResponse);
                            JSONObject audioObject = response.getJSONObject("Audio");
                            File tempFile = imageStorage.saveFileToSent(new File(recordVoicePath), Constants.TAG_AUDIO_SENT, audioObject.getString("Name"));
                            callSocket(mUnixTimeStamp, "audio_msg", audioObject.getString("View_url"), duration);
                            ChatResponse.Message message = new ChatResponse().new Message();
                            ChatResponse.Chat chat = new ChatResponse().new Chat();
                            chat.setSender(GetSet.getUserName());
                            message.setChatTime("" + mUnixTimeStamp);
                            message.setUploadAudio(audioObject.getString("View_url"));
                            message.setAudioName(audioObject.getString("Name"));
                            message.setDuration(duration);
                            chat.setAudioDuration(duration);
                            chat.setMessage(message);
                            chat.setType("audio_msg");
                            chats.add(chat);

                            sendChat("audio_msg", "", audioObject.getString("Name"), 0.0, 0.0, duration);
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Log.i(TAG, "chatsrun: " + chats.size());
                                    chatAdapter.notifyDataSetChanged();
                                    if (chats.size() > 0) {
                                        if (listView.getVisibility() != View.VISIBLE)
                                            listView.setVisibility(View.VISIBLE);
                                        listView.setSelection(chats.size() - 1);
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                            dismissProgressDialog();
                        }
                    } else {
                        dismissProgressDialog();
                    }
                }
            });
        } else {
            AppUtils.showToast(getApplicationContext(), getString(R.string.poor_internet_connection));
        }
    }

    private void showProgressDialog() {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoFile = image;
        return image;
    }


    private void pickImage() {
        View contentView = getLayoutInflater().inflate(R.layout.bottom_sheet_image_pick_options, findViewById(R.id.parentLay), false);
        BottomSheetDialog pickerOptionsSheet = new BottomSheetDialog(this, R.style.SimpleBottomDialog);
        pickerOptionsSheet.setCanceledOnTouchOutside(true);
        pickerOptionsSheet.setContentView(contentView);
        //    pickerOptionsSheet.setDismissWithAnimation(true);

        View layoutCamera = contentView.findViewById(R.id.container_camera_option);
        View layoutGallery = contentView.findViewById(R.id.container_gallery_option);

        layoutCamera.setOnClickListener(v -> {

                    if (ContextCompat.checkSelfPermission(this,CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        cameraResultLauncher.launch(new String[]{CAMERA});
                    } else {
                        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        captureIntent.addFlags(
                                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                        if (captureIntent.resolveActivity(getPackageManager()) != null) {
                            // Create the File where the photo should go
                            File photoFile = null;
                            try {
                                photoFile = createImageFile();
                            } catch (IOException ex) {
                                // Error occurred while creating the File
                                //     Timber.e(ex);
                            }
                            // Continue only if the File was successfully created
                            if (photoFile != null) {
                                mCurrentPhotoUri = FileProvider.getUriForFile(this,
                                        getString(R.string.file_provider_authority),
                                        photoFile);
                                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentPhotoUri);
                                startActivityForResult(captureIntent, REQUEST_CODE_PICK_IMAGE);
                            }
                        }
                        pickerOptionsSheet.dismiss();
                    }

        });
        layoutGallery.setOnClickListener(v -> {
            Uri collection = null;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE){
                Log.d(TAG, "pickImage:11111111111111 ");
                /*ArrayList<String> testUris = new ArrayList<>();
                testUris.add("content://Phone/Download/4wy10xithu681.jpg");*/
//                openGalleryFragment(selectedImageUris);

                Intent in = new Intent(ChatActivity.this, AlbumSelectActivity.class);
                in.putExtra("from","profile");
                in.putExtra("fromEditProfile", true);
                startActivityForResult(in, REQUEST_CODE);

                pickerOptionsSheet.dismiss();
            }
            else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            } else {
                collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            }
            Intent pickIntent = new Intent(Intent.ACTION_PICK);
            pickIntent.setDataAndType(collection, "image/*");

            Intent chooserIntent = Intent.createChooser(pickIntent, "Select a picture");

            if (chooserIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(chooserIntent, REQUEST_CODE_PICK_IMAGE);
            }
            pickerOptionsSheet.dismiss();}
        });

        pickerOptionsSheet.show();
    }

    //Addon Voice Message
    public void MediaRecorderReady() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setOutputFile(recordVoicePath);
    }

    private String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        Log.d(TAG, "seconds: "+(milliseconds % (1000 * 60 * 60)) % (1000 * 60));
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds++ < 10) {
            secondsString = "0" + seconds++;
        } else {
            secondsString = "" + seconds++;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;
//        Log.d(TAG, "milliSecondsToTimer: " + finalTimerString);
        // return timer string
        return finalTimerString;

    }

    public void pauseExternalAudio(Context context) {
        Log.d(TAG, "pauseExternalAudio: " + "income");
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (manager.isMusicActive()) {
            isExternalPlay = true;
            Intent i = new Intent("com.android.music.musicservicecommand");
            i.putExtra("command", "pause");
            context.sendBroadcast(i);
        }
    }

    public void resumeExternalAudio(Context context) {
        if (ChatActivity.isExternalPlay) {
            isExternalPlay = false;
            Intent i = new Intent("com.android.music.musicservicecommand");
            i.putExtra("command", "play");
            context.sendBroadcast(i);
        }
    }

    private String getFileName(String url) {
        String imgSplit = url;
        int endIndex = imgSplit.lastIndexOf("/");
        if (endIndex != -1) {
            imgSplit = imgSplit.substring(endIndex + 1);
        }
        return imgSplit;
    }

    //Addon Call
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Addon Call
    /*    if (requestCode == Constants.AUDIO_PERMISSION_CODE) {
            if (checkPermissions(permissions, ChatActivity.this)) {
                if (NetworkReceiver.isConnected()) {
                    AppRTCUtils appRTCUtils = new AppRTCUtils(getApplicationContext());
                    Intent callIntent = appRTCUtils.connectToRoom(userId, Constants.TAG_SEND, Constants.TAG_AUDIO);
                    callIntent.putExtra(Constants.TAG_USER_ID, userId);
                    callIntent.putExtra(Constants.TAG_USERNAME, userName);
                    callIntent.putExtra(Constants.TAG_CHATID, chatId);
                    callIntent.putExtra(Constants.TAG_USERIMAGE_ONE, userImage);

                    callIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(callIntent);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.poor_internet_connection), Toast.LENGTH_SHORT).show();
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRationale(permissions, ChatActivity.this)) {
                        ActivityCompat.requestPermissions(ChatActivity.this, permissions, Constants.AUDIO_PERMISSION_CODE);
                    } else {
                        openPermissionDialog("Camera, Record Audio, Phone");
                    }
                }
            }
        } else if (requestCode == Constants.VIDEO_PERMISSION_CODE) {
            if (checkPermissions(permissions, ChatActivity.this)) {
                if (NetworkReceiver.isConnected()) {*//*
                    AppRTCUtils appRTCUtils = new AppRTCUtils(getApplicationContext());
                    Intent callIntent = appRTCUtils.connectToRoom(userId, Constants.TAG_SEND, Constants.TAG_VIDEO);

                    callIntent.putExtra(Constants.TAG_USER_ID, userId);
                    callIntent.putExtra(Constants.TAG_USERNAME, userName);
                    callIntent.putExtra(Constants.TAG_CHATID, chatId);
                    callIntent.putExtra(Constants.TAG_USERIMAGE_ONE, userImage);

                    callIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(callIntent);
                *//*
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.poor_internet_connection), Toast.LENGTH_SHORT).show();
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRationale(permissions, ChatActivity.this)) {
                        ActivityCompat.requestPermissions(ChatActivity.this, permissions, Constants.VIDEO_PERMISSION_CODE);
                    } else {
                        openPermissionDialog("Camera, Record Audio, Phone");
                    }
                }
            }
        } else*/
        if (requestCode == 100) {
            if (checkPermissions(permissions, ChatActivity.this)) {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                    ImagePicker.pickImage(this, "Select your image:");
                } else {
                    pickImage();
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!shouldShowRationale(permissions, ChatActivity.this)) {
                        Toast.makeText(getApplicationContext(), "Enable storage permissions", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(ChatActivity.this, permissions, 100);
                    }
                }
            }
        } else if (requestCode == 123) {
            if (checkPermissions(permissions, ChatActivity.this)) {
                Intent in = null;
                if (Constants.ADDON_MAP_BOX_ENABLED) {
                    //Addon MapBox
//                    in = new Intent(ChatActivity.this, MainActivity.class);
                } else {
                    in = new Intent(ChatActivity.this, LocationActivity.class);
                }
                in.putExtra(Constants.TAG_FROM, "chat");
                in.putExtra(Constants.CHATID, chatId);
                in.putExtra(Constants.TAG_USERNAME, userName);
                in.putExtra(Constants.TAG_USER_ID, userId);
                in.putExtra(Constants.TAG_USERIMAGE_M, userImage);
                in.putExtra(Constants.TAG_FULL_NAME, fullName);
                locationResult.launch(in);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRationale(permissions, ChatActivity.this)) {
                        ActivityCompat.requestPermissions(ChatActivity.this, permissions, 123);
                    } else {
                        openPermissionDialog("Location");
                    }
                }
            }
        } else if (requestCode == 124) {
            if (checkPermissions(permissions, ChatActivity.this)) {

            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRationale(permissions, ChatActivity.this)) {
                        ActivityCompat.requestPermissions(ChatActivity.this, permissions, 124);
                    } else {
                        openPermissionDialog("Location");
                    }
                }
            }
        } else if (requestCode == 11111) {

            if (checkPermissions(permissions, ChatActivity.this)) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + mobileNo));
                startActivity(callIntent);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRationale(permissions, ChatActivity.this)) {
                        ActivityCompat.requestPermissions(ChatActivity.this, permissions, 11111);
                    } else {
                        openPermissionDialog("Call Permission");
                    }
                }
            }

        }
    }

    //Addon Call
    private void audioVideoScreen() {
        dialogforaudiovidocall = new Dialog(ChatActivity.this);
        Display display = getWindowManager().getDefaultDisplay();
        dialogforaudiovidocall.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogforaudiovidocall.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogforaudiovidocall.setContentView(R.layout.audio_video_call_popup);
        dialogforaudiovidocall.getWindow().setLayout(display.getWidth() * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogforaudiovidocall.setCancelable(true);
        dialogforaudiovidocall.setCanceledOnTouchOutside(true);


        if (!dialogforaudiovidocall.isShowing()) {
            dialogforaudiovidocall.show();
        }

        TextView title = dialogforaudiovidocall.findViewById(R.id.title_audiovideo);
        LinearLayout videolay = dialogforaudiovidocall.findViewById(R.id.videolay);
        LinearLayout audiolay = dialogforaudiovidocall.findViewById(R.id.audiolay);
        TextView cancelbtn = dialogforaudiovidocall.findViewById(R.id.cancelbtn);


        videolay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogforaudiovidocall.dismiss();
                if (checkPermissions(callPermissions, ChatActivity.this)) {
                    if (NetworkReceiver.isConnected()) {
                        AppRTCUtils appRTCUtils = new AppRTCUtils(getApplicationContext());
//                        Intent callIntent = appRTCUtils.connectToRoom(data.get(Constants.TAG_USERID), Constants.TAG_SEND, Constants.TAG_VIDEO);
                        Intent callIntent = appRTCUtils.connectToRoom(userId, Constants.TAG_SEND, Constants.TAG_VIDEO);

                        callIntent.putExtra(Constants.TAG_USER_ID, userId);
                        callIntent.putExtra(Constants.TAG_USERNAME, userName);
                        callIntent.putExtra(Constants.TAG_CHATID, chatId);
                        callIntent.putExtra(Constants.TAG_USERIMAGE_ONE, userImage);

//                        callIntent.putExtra("data", data);
                        callIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(callIntent);
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.poor_internet_connection), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    ActivityCompat.requestPermissions(ChatActivity.this, callPermissions, Constants.VIDEO_PERMISSION_CODE);
                }


            }
        });


        audiolay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogforaudiovidocall.dismiss();
                if (checkPermissions(callPermissions, ChatActivity.this)) {
                    if (NetworkReceiver.isConnected()) {
                        AppRTCUtils appRTCUtils = new AppRTCUtils(getApplicationContext());
//                        Intent callIntent = appRTCUtils.connectToRoom(data.get(Constants.TAG_USERID), Constants.TAG_SEND, Constants.TAG_AUDIO);
                        Intent callIntent = appRTCUtils.connectToRoom(userId, Constants.TAG_SEND, Constants.TAG_AUDIO);

                        callIntent.putExtra(Constants.TAG_USER_ID, userId);
                        callIntent.putExtra(Constants.TAG_USERNAME, userName);
                        callIntent.putExtra(Constants.TAG_CHATID, chatId);
                        callIntent.putExtra(Constants.TAG_USERIMAGE_ONE, userImage);

//                        callIntent.putExtra("data", data);
                        callIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(callIntent);
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.poor_internet_connection), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    ActivityCompat.requestPermissions(ChatActivity.this, callPermissions, Constants.AUDIO_PERMISSION_CODE);
                }

            }
        });


        cancelbtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogforaudiovidocall.dismiss();
            }
        });


    }

    //Addon Call
    private boolean checkPermissions(String[] permissionList, ChatActivity activity) {
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

    private void voicePlaying() {
        if (playingPosition != -1) {
            chats.get(playingPosition).setIsPlaying(false);
            chatAdapter.notifyDataSetChanged();
        }
    }

    //addons for audioVideo
    private boolean shouldShowRationale(String[] permissions, ChatActivity activity) {
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

    private void openPermissionDialog(String permissionList) {
        permissionDialog = new Dialog(ChatActivity.this);
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
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 100);
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissionDialog.isShowing())
                    permissionDialog.dismiss();
            }
        });
        permissionDialog.show();
    }

    //Addon Gif
    @Override
    public void didSearchTerm(@NotNull String s) {

    }

    @Override
    public void onDismissed(@NotNull GPHContentType gphContentType) {

    }

    @Override
    public void onGifSelected(@NotNull Media media, @Nullable String s, @NotNull GPHContentType gphContentType) {
        try {

            long unixTime = System.currentTimeMillis() / 1000L;
            callSocket(unixTime, "gif", media.getImages().getFixedHeightDownsampled().getGifUrl(), "");

            ChatResponse.Message message = new ChatResponse().new Message();
            ChatResponse.Chat chat = new ChatResponse().new Chat();
            String mediaUrl = media.getImages().getFixedHeightDownsampled().getGifUrl();

            chat.setSender(GetSet.getUserName());
            message.setChatTime("" + unixTime);
            message.setImageName(mediaUrl);
            chat.setMessage(message);
            message.setMessage(mediaUrl);
            message.setUploadImage(mediaUrl);
            chat.setType("gif");
            chats.add(chat);
            chatAdapter.notifyDataSetChanged();

            sendChat("gif", "", mediaUrl, 0.0, 0.0, "");

            runOnUiThread(new Runnable() {
                public void run() {

                    Log.i(TAG, "rungif: " + chats.size());
                    chatAdapter.notifyDataSetChanged();
                    if (chats.size() > 0) {
                        if (listView.getVisibility() != View.VISIBLE)
                            listView.setVisibility(View.VISIBLE);
                        listView.setSelection(chats.size() - 1);
                    }

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopAudio() {
        if (MediaPlayerUtils.isPlaying()) {
            MediaPlayerUtils.mediaPlayer.stop();
            MediaPlayerUtils.mediaPlayer.reset();
        }
    }


    /**
     * adapter for list the conversation in listview
     **/

    public class ChatAdapter extends BaseAdapter {
        private final Context mContext;
        ArrayList<ChatResponse.Chat> Items;
        ViewHolder holder = null;
        String lastDate = "";
        SharedPrefManager sharedPrefManager;

        public ChatAdapter(Context ctx, ArrayList<ChatResponse.Chat> data, SharedPrefManager sharedPrefManager) {
            mContext = ctx;
            Items = data;
            this.sharedPrefManager = sharedPrefManager;
        }


        @Override
        public int getCount() {
            return Items.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressWarnings("deprecation")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.chat_item, parent, false);//layout

                holder = new ViewHolder();
                holder.date = convertView.findViewById(R.id.date);
                holder.leftMsg = convertView.findViewById(R.id.leftMsg);
                holder.rightMsg = convertView.findViewById(R.id.rightMsg);
                holder.leftTime = convertView.findViewById(R.id.leftTime);
                holder.rightTime = convertView.findViewById(R.id.rightTime);
                holder.left_makeoffer_name = convertView.findViewById(R.id.left_makeoffer_name);
                holder.right_makeoffer_name = convertView.findViewById(R.id.right_makeoffer_name);
                holder.left_makeoffer_duration = convertView.findViewById(R.id.left_makeoffer_duration);
                holder.right_makeoffer_duration = convertView.findViewById(R.id.right_makeoffer_duration);
                holder.left_makeoffer_price = convertView.findViewById(R.id.left_makeoffer_price);
                holder.right_makeoffer_price = convertView.findViewById(R.id.right_makeoffer_price);
                holder.left_makeoffer_msg = convertView.findViewById(R.id.left_makeoffer_msg);
                holder.right_makeoffer_msg = convertView.findViewById(R.id.right_makeoffer_msg);
                holder.dateLay = convertView.findViewById(R.id.dateLay);
                holder.leftLay = convertView.findViewById(R.id.leftLay);
                holder.rightLay = convertView.findViewById(R.id.rightLay);
                holder.itemLay = convertView.findViewById(R.id.itemLay);
                holder.itemName = convertView.findViewById(R.id.itemName);
                holder.aboutDate = convertView.findViewById(R.id.aboutDate);
                holder.aboutMsg = convertView.findViewById(R.id.aboutMsg);
                holder.itemImage = convertView.findViewById(R.id.itemImage);
                holder.price = convertView.findViewById(R.id.price);
                holder.rightImage = convertView.findViewById(R.id.right_image);
                holder.leftImage = convertView.findViewById(R.id.left_image);
                holder.leftOfferImg = convertView.findViewById(R.id.leftOfferImg);
                holder.rightOfferImg = convertView.findViewById(R.id.rightOfferImg);
                holder.offerResultImg = convertView.findViewById(R.id.offerResultImg);

                holder.left_image_lay = convertView.findViewById(R.id.left_image_lay);
                holder.right_image_lay = convertView.findViewById(R.id.right_image_lay);
                holder.left_msg_layout = convertView.findViewById(R.id.left_msg_layout);
                holder.right_msg_layout = convertView.findViewById(R.id.right_msg_layout);
                holder.left_makeoffer_lay = convertView.findViewById(R.id.left_makeoffer_lay);
                holder.right_makeoffer_lay = convertView.findViewById(R.id.right_makeoffer_lay);
                holder.offer_result_lay = convertView.findViewById(R.id.offer_result_lay);
                holder.leftImgTime = convertView.findViewById(R.id.leftImgTime);
                holder.right_audio_lay = convertView.findViewById(R.id.right_audio_layout);

                holder.rightImgTime = convertView.findViewById(R.id.rightImgTime);
                holder.buyOfferPrd = convertView.findViewById(R.id.buyOfferPrd);
                holder.offerAccept = convertView.findViewById(R.id.offerAccept);
                holder.offerDeclined = convertView.findViewById(R.id.offerDeclined);
                holder.offerResultPrice = convertView.findViewById(R.id.offerResultPrice);
                holder.offerResultTime = convertView.findViewById(R.id.offerResultTime);
                holder.offerResultMsg = convertView.findViewById(R.id.offerResultMsg);
                holder.offerstatusLay = convertView.findViewById(R.id.offerstatusLay);
                holder.leftDelete = convertView.findViewById(R.id.leftDelete);
                holder.rightDelete = convertView.findViewById(R.id.rightDelete);
                holder.offerResultIcon = convertView.findViewById(R.id.offerResultIcon);

                //Addon Chat Translate
                holder.chattrans = convertView.findViewById(R.id.translatebtn);
                holder.translatebtnAbout = convertView.findViewById(R.id.translatebtnAbout);
                holder.translatebtnOfferLeft = convertView.findViewById(R.id.translatebtnOfferLeft);

                //Addon Gif
                holder.rightGif = convertView.findViewById(R.id.right_gifView);
                holder.leftGif = convertView.findViewById(R.id.left_gifView);
                //Addon Call
                holder.centerlay = convertView.findViewById(R.id.missedcalllay);
                holder.missedcallimage = convertView.findViewById(R.id.missedcallimage);
                holder.missedcall = convertView.findViewById(R.id.missedcall);
                holder.missedcalltime = convertView.findViewById(R.id.missedcalltime);

                //Addons Voice Message
                holder.audioReceiveLay = convertView.findViewById(R.id.audioReceiveLay);
                holder.left_audio_lay = convertView.findViewById(R.id.left_audio_layout);
                holder.downloadLay = convertView.findViewById(R.id.downloadLay);
                holder.receiveSeekbar = convertView.findViewById(R.id.receiveSeekbar);
                holder.downloadProgress = convertView.findViewById(R.id.downloadProgress);
                holder.btnReceivePlay = convertView.findViewById(R.id.btnReceivePlay);
                holder.txtReceiveDuration = convertView.findViewById(R.id.txtReceiveDuration);
                holder.audioSentLay = convertView.findViewById(R.id.audioSentLay);
                holder.sentSeekbar = convertView.findViewById(R.id.sentSeekbar);
                holder.txtSentDuration = convertView.findViewById(R.id.txtSentDuration);
                holder.btnSentPlay = convertView.findViewById(R.id.btnSentPlay);
                holder.leftDownloadIcon = convertView.findViewById(R.id.left_download_icon);
                holder.sentSeekbar.getProgressDrawable().setColorFilter(ContextCompat.getColor(mContext, R.color.white), PorterDuff.Mode.SRC_IN);
                holder.sentSeekbar.getThumb().setColorFilter(ContextCompat.getColor(mContext, R.color.white), PorterDuff.Mode.SRC_IN);
                holder.receiveSeekbar.getProgressDrawable().setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
                holder.receiveSeekbar.getThumb().setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);

                convertView.setTag(holder);

                //Addons VoiceMessage
                holder.audioSentLay.setTag(position);
                holder.sentSeekbar.setTag(position);
                holder.btnSentPlay.setTag(position);
                holder.btnReceivePlay.setTag(position);
                holder.audioReceiveLay.setTag(position);
                holder.receiveSeekbar.setTag(position);
            } else {

                //Addon Voice Message
                holder = (ViewHolder) convertView.getTag();
                holder.audioSentLay.setTag(position);
                holder.sentSeekbar.setTag(position);
                holder.audioReceiveLay.setTag(position);
                holder.receiveSeekbar.setTag(position);
                holder.btnSentPlay.setTag(position);
                holder.btnReceivePlay.setTag(position);
            }
            holder.leftLay.setVisibility(View.GONE);
            holder.rightLay.setVisibility(View.GONE);
            holder.dateLay.setVisibility(View.GONE);
            holder.offer_result_lay.setVisibility(View.GONE);
            holder.itemLay.setVisibility(View.GONE);
            holder.right_image_lay.setVisibility(View.GONE);
            holder.left_image_lay.setVisibility(View.GONE);

            //Addon Gif
            holder.rightGif.setVisibility(View.GONE);
            holder.leftGif.setVisibility(View.GONE);

            //Addon Voice Message
            holder.audioReceiveLay.setVisibility(View.GONE);
            holder.left_audio_lay.setVisibility(View.GONE);
            holder.audioSentLay.setVisibility(View.GONE);
            holder.right_audio_lay.setVisibility(View.GONE);
            holder.txtSentDuration.setVisibility(View.GONE);
            holder.txtReceiveDuration.setVisibility(View.INVISIBLE);
            holder.leftDownloadIcon.setVisibility(View.GONE);

            try {
                final ChatResponse.Chat chat = Items.get(position);
                long date = Long.parseLong(chat.getMessage().getChatTime()) * 1000;
                String chatDate = JoysaleApplication.getDate(date);
                switch (chat.getType()) {
                    case "message":
                    case "normal": {
                        //Addon call
                        holder.missedcallimage.setVisibility(View.GONE);
                        holder.centerlay.setVisibility(View.GONE);
                        holder.chattrans.setVisibility(View.GONE);

                        if (chat.getSender().equals(GetSet.getUserName())) {
                            holder.rightLay.setVisibility(View.VISIBLE);
                            holder.right_msg_layout.setVisibility(View.VISIBLE);
                            holder.right_image_lay.setVisibility(View.GONE);
                            holder.right_makeoffer_lay.setVisibility(View.GONE);
                            holder.rightDelete.setVisibility(View.GONE);
                            holder.rightMsg.setText(chat.getMessage().getMessage());
                            holder.rightTime.setText(getTime(Long.parseLong(Items.get(position).getMessage().getChatTime()) * 1000));

                        } else {
                            holder.leftLay.setVisibility(View.VISIBLE);
                            holder.left_msg_layout.setVisibility(View.VISIBLE);
                            holder.left_image_lay.setVisibility(View.GONE);
                            holder.left_makeoffer_lay.setVisibility(View.GONE);
                            holder.leftDelete.setVisibility(View.GONE);
                            holder.leftMsg.setText(chat.getMessage().getMessage());
                            holder.leftTime.setText(getTime(Long.parseLong(Items.get(position).getMessage().getChatTime()) * 1000));
                            //Addon Chat Translate
                            if (Constants.ADDON_TRANSLATE_ENABLED) {
                                if (!TextUtils.isEmpty(SharedPrefManager.getString(DEFAULT_CHAT_LANGUAGE, "")) &&
                                        !SharedPrefManager.getString(DEFAULT_CHAT_LANGUAGE, "").equalsIgnoreCase(mContext.getString(R.string.none))) {
                                    holder.chattrans.setVisibility(View.VISIBLE);
                                } else {
                                    holder.chattrans.setVisibility(View.GONE);
                                }

                                holder.chattrans.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        //Addon Chat Translate
                                        if (!SharedPrefManager.getString(DEFAULT_CHAT_LANGUAGE, "").equalsIgnoreCase("")) {
                                            JoysaleApplication.preventMultipleClick(view);
                                            Data.Builder builder = new Data.Builder();
                                            builder.putString(Constants.TAG_MESSAGE, chat.getMessage().getMessage());
                                            OneTimeWorkRequest translateWorkRequest = new OneTimeWorkRequest.Builder(ChatTranslateWorker.class)
                                                    .setConstraints(new Constraints.Builder()
                                                            .setRequiredNetworkType(NetworkType.CONNECTED)
                                                            .build())
                                                    .setInputData(builder.build())
                                                    .build();
                                            WorkManager workManager = WorkManager.getInstance(mContext);
                                            workManager.enqueue(translateWorkRequest);
                                            workManager.getWorkInfoByIdLiveData(translateWorkRequest.getId()).observe(ChatActivity.this, workInfo -> {
                                                if (workInfo.getState().isFinished()) {
                                                    if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                                                        String translatedMsg = workInfo.getOutputData().getString(Constants.TAG_DATA);
                                                        chat.getMessage().setMessage(Html.fromHtml(translatedMsg).toString());
                                                        notifyDataSetChanged();
                                                    }
                                                }
                                            });
                                        } else {
                                            AppUtils.showToast(mContext, mContext.getString(R.string.select_language));
                                        }
                                    }
                                });
                            }
                        }
                    }
                    break;
                    //Addon Gif
                    case "gif": {
                        holder.missedcallimage.setVisibility(View.GONE);
                        holder.centerlay.setVisibility(View.GONE);
                        if (chat.getSender().equals(GetSet.getUserName())) {
                            holder.rightLay.setVisibility(View.VISIBLE);
                            holder.right_msg_layout.setVisibility(View.GONE);
                            holder.right_image_lay.setVisibility(View.GONE);
                            holder.right_makeoffer_lay.setVisibility(View.GONE);
                            holder.rightGif.setVisibility(View.VISIBLE);

                            int imgSize = JoysaleApplication.dpToPx(mContext, 150);
                            Glide.with(mContext)
                                    .asGif()
                                    .centerCrop()
                                    .load(chat.getMessage().getUploadImage())
                                    .into(holder.rightGif);

                            holder.rightGif.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    try {
                                        Intent intent = new Intent(ChatActivity.this, ViewFullImage.class);
                                        intent.putExtra(Constants.IMAGETYPE, "gif");
                                        intent.putExtra(Constants.TAG_FROM, "sent");
                                        intent.putExtra(Constants.KEY_IMAGE, chat.getMessage().getUploadImage());
                                        Pair<View, String> bodyPair = Pair.create(view, chat.getMessage().getUploadImage());
                                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ChatActivity.this, bodyPair);
                                        ActivityCompat.startActivity(ChatActivity.this, intent, options.toBundle());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } else {
                            int imgSize = JoysaleApplication.dpToPx(mContext, 150);
                            holder.leftLay.setVisibility(View.VISIBLE);
                            holder.left_msg_layout.setVisibility(View.GONE);
                            holder.left_image_lay.setVisibility(View.GONE);
                            holder.left_makeoffer_lay.setVisibility(View.GONE);
                            holder.leftGif.setVisibility(View.VISIBLE);

                            Glide.with(mContext)
                                    .asGif()
                                    .centerCrop()
                                    .load(chat.getMessage().getUploadImage())
                                    .into(holder.leftGif);


                            holder.leftGif.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //Instant Received Image
                                    Intent intent = new Intent(ChatActivity.this, ViewFullImage.class);
                                    intent.putExtra(Constants.IMAGETYPE, "gif");
                                    intent.putExtra(Constants.TAG_FROM, "sent");
                                    intent.putExtra(Constants.KEY_IMAGE, chat.getMessage().getUploadImage());
                                    Pair<View, String> bodyPair = Pair.create(view, chat.getMessage().getUploadImage());
                                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ChatActivity.this, bodyPair);
                                    ActivityCompat.startActivity(ChatActivity.this, intent, options.toBundle());
                                }
                            });
                        }
                    }
                    break;
                    //Addon Call
                    case "audio": {
                        if (!chat.getSender().equalsIgnoreCase(GetSet.getUserName())) {
                            holder.missedcallimage.setVisibility(View.VISIBLE);
                            holder.missedcallimage.setImageDrawable(getResources().getDrawable(R.drawable.missed_call));
                            holder.centerlay.setVisibility(View.VISIBLE);
                            holder.missedcall.setText(R.string.missed_audio);
                            holder.missedcalltime.setText(getTime(Long.parseLong(Items.get(position).getMessage().getChatTime()) * 1000));
                        } else {
                            holder.missedcallimage.setVisibility(View.VISIBLE);
                            holder.missedcallimage.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_call_made_24));
                            holder.centerlay.setVisibility(View.VISIBLE);
                            holder.missedcall.setText(getResources().getString(R.string.make_call));
                            holder.missedcalltime.setText(getTime(Long.parseLong(Items.get(position).getMessage().getChatTime()) * 1000));
                        }
                    }
                    break;
                    case "video": {
                        if (!chat.getSender().equalsIgnoreCase(GetSet.getUserName())) {
                            holder.missedcallimage.setVisibility(View.VISIBLE);
                            holder.missedcallimage.setImageDrawable(getResources().getDrawable(R.drawable.missed_call));
                            holder.centerlay.setVisibility(View.VISIBLE);
                            holder.missedcall.setText(R.string.missed_video);
                            holder.missedcalltime.setText(getTime(Long.parseLong(Items.get(position).getMessage().getChatTime()) * 1000));
                        } else {
                            holder.missedcallimage.setVisibility(View.VISIBLE);
                            holder.missedcallimage.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_call_made_24));
                            holder.centerlay.setVisibility(View.VISIBLE);
                            holder.missedcall.setText(R.string.make_callvideo);
                            holder.missedcalltime.setText(getTime(Long.parseLong(Items.get(position).getMessage().getChatTime()) * 1000));
                        }
                    }
                    break;

                    //Addon Voice Message
                    case "audio_msg": {
                        holder.missedcallimage.setVisibility(View.GONE);
                        holder.centerlay.setVisibility(View.GONE);

                        if (chat.getSender().equals(GetSet.getUserName())) {
                            final String audio = getFileName(chat.getMessage().getUploadAudio());
                            holder.audioSentLay.setVisibility(View.VISIBLE);
                            holder.right_audio_lay.setVisibility(View.VISIBLE);
                            holder.rightLay.setVisibility(View.VISIBLE);
                            holder.right_msg_layout.setVisibility(View.GONE);
                            holder.right_makeoffer_lay.setVisibility(View.GONE);
                            String duration = chat.getAudioDuration();
                            Log.d(TAG, "getView: "+chat.getAudioDuration());
                            holder.txtSentDuration.setText(duration != null ? duration : "0:00");

                            if (chat.getIsPlaying() != null && chat.getIsPlaying()) {
                                holder.btnSentPlay.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_pause_white));
                            } else {
                                holder.btnSentPlay.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_play_white));
                            }

                            File audioFile = imageStorage.getFile(Constants.TAG_AUDIO_SENT, audio);
                            if (audioFile != null) {
                                holder.txtSentDuration.setVisibility(View.VISIBLE);
                                int fileDuration = imageStorage.getMediaDuration(audioFile);
                                Log.d(TAG, "getView: "+fileDuration);
                                holder.txtSentDuration.setText(milliSecondsToTimer(fileDuration));
                                Log.d(TAG, "getView: "+milliSecondsToTimer(fileDuration));
                                holder.sentSeekbar.setMax(fileDuration);
                            } else {
                                holder.txtSentDuration.setVisibility(View.INVISIBLE);
                                holder.sentSeekbar.setMax(0);
                            }

                            holder.sentSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                @Override
                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                    if (fromUser) {
                                        if (playingPosition == -1) {
                                            playingPosition = position;
                                        }
                                        if (playingPosition == position) {
                                            if (MediaPlayerUtils.isPlaying()) {
                                                MediaPlayerUtils.applySeekBarValue(progress);
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onStartTrackingTouch(SeekBar seekBar) {

                                }

                                @Override
                                public void onStopTrackingTouch(SeekBar seekBar) {

                                }
                            });

                            holder.btnSentPlay.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (audiovideocallbtn.isEnabled()) {
                                        File audioFile = imageStorage.getFile(Constants.TAG_AUDIO_SENT, audio);
                                        if (audioFile != null) {
                                            if (playingPosition != position) {
                                                pauseExternalAudio(mContext);
                                                if (playingPosition != -1) {
                                                    chats.get(playingPosition).setIsPlaying(false);
                                                    notifyDataSetChanged();
                                                }

                                                playingPosition = position;
                                                chats.get(playingPosition).setIsPlaying(true);
                                                notifyDataSetChanged();
                                                Uri voiceURI = Uri.fromFile(audioFile);
                                                RelativeLayout r = (RelativeLayout) ((ViewGroup) view.getParent());
                                                SeekBar seekBar = r.findViewById(R.id.sentSeekbar);
                                                TextView txtDuration = r.findViewById(R.id.txtSentDuration);
                                                if (mediaPlay) {
                                                    seekBar.setProgress(0);
                                                    notifyDataSetChanged();
                                                    mediaPlay = false;
                                                }

                                                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                                    @Override
                                                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                                        if (fromUser) {
                                                            if (MediaPlayerUtils.isPlaying()) {
                                                                MediaPlayerUtils.applySeekBarValue(progress);
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onStartTrackingTouch(SeekBar seekBar) {

                                                    }

                                                    @Override
                                                    public void onStopTrackingTouch(SeekBar seekBar) {

                                                    }
                                                });
                                                try {
                                                    MediaPlayerUtils.startAndPlayMediaPlayer(mContext, voiceURI, new MediaPlayerUtils.Listener() {
                                                        @Override
                                                        public void onAudioComplete() {
                                                            if (seekBar != null) {
                                                                seekBar.setProgress(0);
                                                            }
                                                            if (playingPosition != -1) {
                                                                chats.get(playingPosition).setIsPlaying(false);
                                                                chatAdapter.notifyDataSetChanged();
                                                            }
                                                        }

                                                        @Override
                                                        public void onAudioUpdate(int currentPosition) {
                                                            if (seekBar != null) {
                                                                seekBar.setProgress(currentPosition);
                                                                txtDuration.setText("" + milliSecondsToTimer(currentPosition));
                                                            }
                                                        }

                                                        @Override
                                                        public void onTotalLength(int duration) {
                                                            seekBar.setMax(duration);
                                                        }
                                                    }, seekBar.getProgress());
                                                } catch (IOException e) {
                                                    Log.e(TAG, "startPlayer: " + e.getMessage());
                                                    e.printStackTrace();
                                                }

                                            } else {
                                                view.setTag(position);
                                                if (MediaPlayerUtils.isPlaying()) {
                                                    MediaPlayerUtils.pauseMediaPlayer();
                                                    holder.btnSentPlay.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_play_white));
                                                    chats.get(playingPosition).setIsPlaying(false);
                                                    notifyDataSetChanged();
                                                } else {
                                                    playingPosition = position;
                                                    chats.get(playingPosition).setIsPlaying(true);
                                                    notifyDataSetChanged();
                                                    audioFile = imageStorage.getFile(Constants.TAG_AUDIO_SENT, audio);
                                                    Uri voiceURI = Uri.fromFile(audioFile);
                                                    RelativeLayout r = (RelativeLayout) ((ViewGroup) view.getParent());
                                                    SeekBar seekBar = r.findViewById(R.id.sentSeekbar);
                                                    TextView txtDuration = r.findViewById(R.id.txtSentDuration);
                                                    if (mediaPlay) {
                                                        seekBar.setProgress(0);
                                                        notifyDataSetChanged();
                                                        mediaPlay = false;
                                                    }

                                                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                                        @Override
                                                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                                            if (fromUser) {
                                                                if (MediaPlayerUtils.isPlaying()) {
                                                                    MediaPlayerUtils.applySeekBarValue(progress);
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onStartTrackingTouch(SeekBar seekBar) {

                                                        }

                                                        @Override
                                                        public void onStopTrackingTouch(SeekBar seekBar) {

                                                        }
                                                    });
                                                    try {
                                                        MediaPlayerUtils.startAndPlayMediaPlayer(mContext, voiceURI, new MediaPlayerUtils.Listener() {
                                                            @Override
                                                            public void onAudioComplete() {
                                                                if (seekBar != null) {
                                                                    seekBar.setProgress(0);
                                                                }
                                                                if (playingPosition != -1) {
                                                                    chats.get(playingPosition).setIsPlaying(false);
                                                                    chatAdapter.notifyDataSetChanged();
                                                                }
                                                            }

                                                            @Override
                                                            public void onAudioUpdate(int currentPosition) {
                                                                seekBar.setProgress(currentPosition);
                                                                txtDuration.setText("" + milliSecondsToTimer(currentPosition));

                                                            }

                                                            @Override
                                                            public void onTotalLength(int duration) {
                                                                seekBar.setMax(duration);
                                                            }
                                                        }, seekBar.getProgress());
                                                    } catch (IOException e) {
                                                        Log.e(TAG, "startPlayer: " + e.getMessage());
                                                    }
                                                }
                                            }
                                        } else if (chat.getMessage().getUploadAudio() != null) {
                                            //     pauseExternalAudio(mContext);
                                            if (playingPosition != -1) {
                                                chats.get(playingPosition).setIsPlaying(false);
                                                notifyDataSetChanged();
                                            }
                                            playingPosition = position;
                                            chats.get(playingPosition).setIsPlaying(true);
                                            notifyDataSetChanged();

                                            RelativeLayout audioSentLay = (RelativeLayout) view.getParent();
                                            SeekBar seekBar = audioSentLay.findViewById(R.id.sentSeekbar);
                                            TextView txtDuration = audioSentLay.findViewById(R.id.txtSentDuration);
                                            Uri voiceURI = Uri.parse(chat.getMessage().getUploadAudio());
                                            try {
                                                MediaPlayerUtils.startAndPlayMediaPlayer(mContext, voiceURI, new MediaPlayerUtils.Listener() {
                                                    @Override
                                                    public void onAudioComplete() {
                                                        if (playingPosition != -1) {
                                                            chats.get(playingPosition).setIsPlaying(false);
                                                            chatAdapter.notifyDataSetChanged();
                                                        }
                                                    }

                                                    @Override
                                                    public void onAudioUpdate(int currentPosition) {
                                                        if (seekBar != null) {
                                                            seekBar.setProgress(currentPosition);
                                                        }
                                                        if (txtDuration != null) {
                                                            txtDuration.setVisibility(View.VISIBLE);
                                                            txtDuration.setText("" + milliSecondsToTimer(currentPosition));
                                                        }
                                                    }

                                                    public void onTotalLength(int duration) {
                                                        seekBar.setMax(duration);
                                                    }
                                                }, seekBar.getProgress());
                                            } catch (IOException e) {
                                                Log.e(TAG, "startPlayer: " + e.getMessage());
                                                e.printStackTrace();
                                            }
                                        } else {
                                            Toast.makeText(mContext, getString(R.string.file_not_found), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });
                        } else {
                            //Receiver Layout
                            holder.leftLay.setVisibility(View.VISIBLE);
                            holder.left_msg_layout.setVisibility(View.GONE);
                            holder.audioReceiveLay.setVisibility(View.VISIBLE);
                            holder.left_audio_lay.setVisibility(View.VISIBLE);
                            holder.left_makeoffer_lay.setVisibility(View.GONE);
                            holder.txtReceiveDuration.setVisibility(View.VISIBLE);
                            holder.leftLay.setVisibility(View.VISIBLE);

                            if (chat.getIsPlaying() != null && chat.getIsPlaying()) {
                                holder.btnReceivePlay.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_pause_white));
                            } else {
                                holder.btnReceivePlay.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_play_white));
                            }
                            final String audio = getFileName(chat.getMessage().getUploadAudio());
                            holder.audioReceiveLay.setVisibility(View.VISIBLE);
                            String duration = chats.get(position).getAudioDuration();
                            if (duration == null)
                                duration = chat.getMessage().getAudioDuration();

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                Uri audioUri = imageStorage.getAudioUri(Constants.TAG_AUDIO_RECEIVE, audio);
                                if (audioUri != null) {
                                    holder.btnReceivePlay.setVisibility(View.VISIBLE);
                                    holder.downloadProgress.setVisibility(View.GONE);
                                    holder.leftDownloadIcon.setVisibility(View.GONE);
                                    holder.txtReceiveDuration.setVisibility(View.VISIBLE);
                                    holder.txtReceiveDuration.setText(milliSecondsToTimer(imageStorage.getMediaDuration(mContext, audioUri)));
                                    holder.downloadProgress.stopSpinning();
                                } else {
                                    holder.btnReceivePlay.setVisibility(View.GONE);
                                    holder.leftDownloadIcon.setVisibility(View.VISIBLE);
                                    holder.downloadProgress.setVisibility(View.VISIBLE);
                                    holder.downloadProgress.stopSpinning();
                                    holder.txtReceiveDuration.setVisibility(View.INVISIBLE);
                                }
                            } else {
                                File audioFile = imageStorage.getFile(Constants.TAG_AUDIO_RECEIVE, audio);
                                if (audioFile != null) {
                                    holder.btnReceivePlay.setVisibility(View.VISIBLE);
                                    holder.downloadProgress.setVisibility(View.GONE);
                                    holder.leftDownloadIcon.setVisibility(View.GONE);
                                    holder.txtReceiveDuration.setVisibility(View.VISIBLE);
                                    holder.txtReceiveDuration.setText(milliSecondsToTimer(imageStorage.getMediaDuration(audioFile)));
                                    holder.downloadProgress.stopSpinning();
                                } else {
                                    holder.btnReceivePlay.setVisibility(View.GONE);
                                    holder.leftDownloadIcon.setVisibility(View.VISIBLE);
                                    holder.downloadProgress.setVisibility(View.VISIBLE);
                                    holder.downloadProgress.stopSpinning();
                                    holder.txtReceiveDuration.setVisibility(View.INVISIBLE);
                                }
                            }

                            holder.leftDownloadIcon.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (NetworkReceiver.isConnected()) {
                                        FrameLayout downloadLay = (FrameLayout) view.getParent();
                                        RelativeLayout f = (RelativeLayout) (ViewGroup) downloadLay.getParent();
                                        RelativeLayout r = (RelativeLayout) ((ViewGroup) f.getParent());
                                        SeekBar seekBar = r.findViewById(R.id.receiveSeekbar);
                                        TextView txtDuration = r.findViewById(R.id.txtReceiveDuration);

                                        ImageView leftDownloadIcon = downloadLay.findViewById(R.id.left_download_icon);
                                        ProgressWheel downloadProgress = downloadLay.findViewById(R.id.downloadProgress);
                                        ImageView btnReceivePlay = downloadLay.findViewById(R.id.btnReceivePlay);
                                        downloadProgress.setVisibility(View.VISIBLE);
                                        downloadProgress.spin();
                                        leftDownloadIcon.setVisibility(View.GONE);
                                        txtDuration.setVisibility(View.GONE);
                                        downloadProgress.spin();

                                        txtDuration.setText("0:00");
                                        Data.Builder builder = new Data.Builder();
                                        builder.putString(Constants.TAG_URL, chat.getMessage().getUploadAudio());
                                        builder.putString(Constants.TAG_FROM, Constants.TAG_AUDIO_RECEIVE);
                                        OneTimeWorkRequest downloadWorkRequest = new OneTimeWorkRequest.Builder(DownloadFilesWorker.class)
                                                .setConstraints(new Constraints.Builder()
                                                        .setRequiredNetworkType(NetworkType.CONNECTED)
                                                        .build())
                                                .setInputData(builder.build())
                                                .build();
                                        WorkManager workManager = WorkManager.getInstance(mContext);
                                        workManager.enqueue(downloadWorkRequest);
                                        workManager.getWorkInfoByIdLiveData(downloadWorkRequest.getId()).observe(ChatActivity.this, workInfo -> {
                                            if (workInfo.getState().isFinished()) {
                                                if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                                                    String savedPath = workInfo.getOutputData().getString(Constants.TAG_DATA);
                                                    Log.d(TAG, "downloadFilesWork: " + savedPath);
                                                    downloadProgress.stopSpinning();
                                                    downloadProgress.setVisibility(View.GONE);
                                                    btnReceivePlay.setVisibility(View.VISIBLE);
                                                    txtDuration.setVisibility(View.VISIBLE);
                                                    String duration;
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                        duration = milliSecondsToTimer(imageStorage.getMediaDuration(mContext, Uri.parse(savedPath)));
                                                    } else {
                                                        duration = milliSecondsToTimer(imageStorage.getMediaDuration(new File(savedPath)));
                                                    }
                                                    Log.d(TAG, "onClick: " + duration);
                                                    txtDuration.setText(duration != null ? duration : "0:00");
                                                } else {
                                                    downloadProgress.stopSpinning();
                                                    downloadProgress.setVisibility(View.GONE);
                                                    leftDownloadIcon.setVisibility(View.VISIBLE);
                                                    btnReceivePlay.setVisibility(View.GONE);
                                                    txtDuration.setVisibility(View.GONE);
                                                }
                                            }
                                        });
                                    }
                                }
                            });

                            holder.audioReceiveLay.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (audiovideocallbtn.isEnabled()) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                            Uri voiceURI = imageStorage.getAudioUri(Constants.TAG_AUDIO_RECEIVE, audio);
                                            if (voiceURI != null) {
                                                if (playingPosition != position) {
                                                    if (playingPosition != -1) {
                                                        chats.get(playingPosition).setIsPlaying(false);
                                                        notifyDataSetChanged();
                                                    }
                                                    playingPosition = position;
                                                    chats.get(playingPosition).setIsPlaying(true);
                                                    notifyDataSetChanged();
                                                    RelativeLayout f = (RelativeLayout) (ViewGroup) view.getParent();
                                                    RelativeLayout r = (RelativeLayout) ((ViewGroup) f.getParent());
                                                    SeekBar seekBar = r.findViewById(R.id.receiveSeekbar);
                                                    TextView txtDuration = r.findViewById(R.id.txtReceiveDuration);

                                                    FrameLayout downloadLay = f.findViewById(R.id.downloadLay);
                                                    ImageView leftDownloadIcon = downloadLay.findViewById(R.id.left_download_icon);
                                                    ProgressWheel downloadProgress = downloadLay.findViewById(R.id.downloadProgress);
                                                    ImageView btnReceivePlay = downloadLay.findViewById(R.id.btnReceivePlay);

                                                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                                        @Override
                                                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                                            if (fromUser) {
                                                                if (MediaPlayerUtils.isPlaying()) {
                                                                    MediaPlayerUtils.applySeekBarValue(progress);
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onStartTrackingTouch(SeekBar seekBar) {

                                                        }

                                                        @Override
                                                        public void onStopTrackingTouch(SeekBar seekBar) {

                                                        }
                                                    });
                                                    try {
                                                        MediaPlayerUtils.startAndPlayMediaPlayer(mContext, voiceURI, new MediaPlayerUtils.Listener() {
                                                            @Override
                                                            public void onAudioComplete() {
                                                                if (seekBar != null) {
                                                                    seekBar.setProgress(0);
                                                                }
                                                                if (playingPosition != -1) {
                                                                    chats.get(playingPosition).setIsPlaying(false);
                                                                    chatAdapter.notifyDataSetChanged();
                                                                }
                                                            }

                                                            @Override
                                                            public void onAudioUpdate(int currentPosition) {
                                                                if (seekBar != null) {
                                                                    seekBar.setProgress(currentPosition);
                                                                    txtDuration.setText("" + milliSecondsToTimer(currentPosition));
                                                                }
                                                            }

                                                            @Override
                                                            public void onTotalLength(int duration) {
                                                                seekBar.setMax(duration);
                                                            }
                                                        }, seekBar.getProgress());

                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                } else {
                                                    view.setTag(position);
                                                    if (MediaPlayerUtils.isPlaying()) {
                                                        MediaPlayerUtils.pauseMediaPlayer();
                                                        chats.get(position).setIsPlaying(false);
                                                        notifyDataSetChanged();
                                                    } else {
                                                        playingPosition = position;
                                                        chats.get(playingPosition).setIsPlaying(true);
                                                        notifyDataSetChanged();
                                                        voiceURI = imageStorage.getAudioUri(Constants.TAG_AUDIO_RECEIVE, audio);
                                                        RelativeLayout f = (RelativeLayout) (ViewGroup) view.getParent();
                                                        RelativeLayout r = (RelativeLayout) ((ViewGroup) f.getParent());

                                                        SeekBar seekBar = r.findViewById(R.id.receiveSeekbar);
                                                        TextView txtDuration = r.findViewById(R.id.txtReceiveDuration);

                                                        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                                            @Override
                                                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                                                if (fromUser) {
                                                                    if (MediaPlayerUtils.isPlaying()) {
                                                                        MediaPlayerUtils.applySeekBarValue(progress);
                                                                    }
                                                                }
                                                            }

                                                            @Override
                                                            public void onStartTrackingTouch(SeekBar seekBar) {

                                                            }

                                                            @Override
                                                            public void onStopTrackingTouch(SeekBar seekBar) {

                                                            }
                                                        });

                                                        if (voiceURI != null) {
                                                            try {
                                                                MediaPlayerUtils.startAndPlayMediaPlayer(mContext, voiceURI, new MediaPlayerUtils.Listener() {
                                                                    @Override
                                                                    public void onAudioComplete() {
                                                                        if (seekBar != null) {
                                                                            seekBar.setProgress(0);
                                                                        }
                                                                        if (playingPosition != -1) {
                                                                            chats.get(playingPosition).setIsPlaying(false);
                                                                            chatAdapter.notifyDataSetChanged();
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onAudioUpdate(int currentPosition) {
                                                                        seekBar.setProgress(currentPosition);
                                                                        txtDuration.setText("" + milliSecondsToTimer(currentPosition));
                                                                    }

                                                                    @Override
                                                                    public void onTotalLength(int duration) {
                                                                        seekBar.setMax(duration);
                                                                    }
                                                                }, seekBar.getProgress());
                                                            } catch (IOException e) {
                                                                Log.e(TAG, "startPlayer: " + e.getMessage());
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                if (NetworkReceiver.isConnected()) {
                                                    RelativeLayout f = (RelativeLayout) (ViewGroup) view.getParent();
                                                    RelativeLayout r = (RelativeLayout) ((ViewGroup) f.getParent());
                                                    SeekBar seekBar = r.findViewById(R.id.receiveSeekbar);
                                                    TextView txtDuration = r.findViewById(R.id.txtReceiveDuration);
                                                    FrameLayout downloadLay = f.findViewById(R.id.downloadLay);
                                                    ImageView leftDownloadIcon = downloadLay.findViewById(R.id.left_download_icon);
                                                    ProgressWheel downloadProgress = downloadLay.findViewById(R.id.downloadProgress);
                                                    ImageView btnReceivePlay = downloadLay.findViewById(R.id.btnReceivePlay);
                                                    downloadProgress.setVisibility(View.VISIBLE);
                                                    downloadProgress.spin();
                                                    leftDownloadIcon.setVisibility(View.GONE);
                                                    downloadProgress.spin();
                                                    txtDuration.setVisibility(View.GONE);

                                                    txtDuration.setText("0:00");
                                                    Data.Builder builder = new Data.Builder();
                                                    builder.putString(Constants.TAG_URL, chat.getMessage().getUploadAudio());
                                                    builder.putString(Constants.TAG_FROM, Constants.TAG_AUDIO_RECEIVE);
                                                    OneTimeWorkRequest downloadWorkRequest = new OneTimeWorkRequest.Builder(DownloadFilesWorker.class)
                                                            .setConstraints(new Constraints.Builder()
                                                                    .setRequiredNetworkType(NetworkType.CONNECTED)
                                                                    .build())
                                                            .setInputData(builder.build())
                                                            .build();
                                                    WorkManager workManager = WorkManager.getInstance(mContext);
                                                    workManager.enqueue(downloadWorkRequest);
                                                    workManager.getWorkInfoByIdLiveData(downloadWorkRequest.getId()).observe(ChatActivity.this, workInfo -> {
                                                        if (workInfo.getState().isFinished()) {
                                                            if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                                                                String savedPath = workInfo.getOutputData().getString(Constants.TAG_DATA);
                                                                Log.d(TAG, "downloadFilesWork: " + savedPath);
                                                                String duration;
                                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                                    duration = milliSecondsToTimer(imageStorage.getMediaDuration(mContext, Uri.parse(savedPath)));
                                                                } else {
                                                                    duration = milliSecondsToTimer(imageStorage.getMediaDuration(new File(savedPath)));
                                                                }
                                                                txtDuration.setText(duration);
                                                                downloadProgress.stopSpinning();
                                                                downloadProgress.setVisibility(View.GONE);
                                                                btnReceivePlay.setVisibility(View.VISIBLE);
                                                                txtDuration.setVisibility(View.VISIBLE);
                                                            } else {
                                                                downloadProgress.stopSpinning();
                                                                downloadProgress.setVisibility(View.GONE);
                                                                leftDownloadIcon.setVisibility(View.VISIBLE);
                                                                btnReceivePlay.setVisibility(View.GONE);
                                                                txtDuration.setVisibility(View.GONE);
                                                            }
                                                        }
                                                    });
                                                } else {

                                                }
                                            }
                                        } else {
                                            File audioFile = imageStorage.getFile(Constants.TAG_AUDIO_RECEIVE, audio);
                                            if (audioFile != null) {
                                                if (playingPosition != position) {
                                                    if (playingPosition != -1) {
                                                        chats.get(playingPosition).setIsPlaying(false);
                                                        notifyDataSetChanged();
                                                    }
                                                    playingPosition = position;
                                                    chats.get(playingPosition).setIsPlaying(true);
                                                    notifyDataSetChanged();

                                                    Uri voiceURI = imageStorage.getUriFromFile(audioFile);
                                                    RelativeLayout f = (RelativeLayout) (ViewGroup) view.getParent();
                                                    RelativeLayout r = (RelativeLayout) ((ViewGroup) f.getParent());

                                                    SeekBar seekBar = r.findViewById(R.id.receiveSeekbar);
                                                    TextView txtDuration = r.findViewById(R.id.txtReceiveDuration);

                                                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                                        @Override
                                                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                                            if (fromUser) {
                                                                if (MediaPlayerUtils.isPlaying()) {
                                                                    MediaPlayerUtils.applySeekBarValue(progress);
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onStartTrackingTouch(SeekBar seekBar) {

                                                        }

                                                        @Override
                                                        public void onStopTrackingTouch(SeekBar seekBar) {

                                                        }
                                                    });

                                                    try {
                                                        MediaPlayerUtils.startAndPlayMediaPlayer(mContext, voiceURI, new MediaPlayerUtils.Listener() {
                                                            @Override
                                                            public void onAudioComplete() {
                                                                if (seekBar != null) {
                                                                    seekBar.setProgress(0);
                                                                }
                                                                if (playingPosition != -1) {
                                                                    chats.get(playingPosition).setIsPlaying(false);
                                                                    chatAdapter.notifyDataSetChanged();
                                                                }
                                                            }

                                                            @Override
                                                            public void onAudioUpdate(int currentPosition) {
                                                                if (seekBar != null) {
                                                                    seekBar.setProgress(currentPosition);
                                                                    txtDuration.setText("" + milliSecondsToTimer(currentPosition));
                                                                }
                                                            }

                                                            @Override
                                                            public void onTotalLength(int duration) {
                                                                seekBar.setMax(duration);
                                                            }
                                                        }, seekBar.getProgress());

                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                } else {
                                                    view.setTag(position);
                                                    if (MediaPlayerUtils.isPlaying()) {
                                                        MediaPlayerUtils.pauseMediaPlayer();
                                                        chats.get(position).setIsPlaying(false);
                                                        notifyDataSetChanged();
                                                    } else {
                                                        playingPosition = position;
                                                        chats.get(playingPosition).setIsPlaying(true);
                                                        notifyDataSetChanged();
                                                        audioFile = imageStorage.getFile(Constants.TAG_AUDIO_RECEIVE, audio);
                                                        Uri voiceURI = FileProvider.getUriForFile(getApplicationContext(), getString(R.string.file_provider_authority), audioFile);
                                                        RelativeLayout f = (RelativeLayout) (ViewGroup) view.getParent();
                                                        RelativeLayout r = (RelativeLayout) ((ViewGroup) f.getParent());

                                                        SeekBar seekBar = r.findViewById(R.id.receiveSeekbar);
                                                        TextView txtDuration = r.findViewById(R.id.txtReceiveDuration);

                                                        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                                            @Override
                                                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                                                if (fromUser) {
                                                                    if (MediaPlayerUtils.isPlaying()) {
                                                                        MediaPlayerUtils.applySeekBarValue(progress);
                                                                    }
                                                                }
                                                            }

                                                            @Override
                                                            public void onStartTrackingTouch(SeekBar seekBar) {

                                                            }

                                                            @Override
                                                            public void onStopTrackingTouch(SeekBar seekBar) {

                                                            }
                                                        });

                                                        try {
                                                            MediaPlayerUtils.startAndPlayMediaPlayer(mContext, voiceURI, new MediaPlayerUtils.Listener() {
                                                                @Override
                                                                public void onAudioComplete() {
                                                                    if (seekBar != null) {
                                                                        seekBar.setProgress(0);
                                                                    }
                                                                    if (playingPosition != -1) {
                                                                        chats.get(playingPosition).setIsPlaying(false);
                                                                        chatAdapter.notifyDataSetChanged();
                                                                    }
                                                                }

                                                                @Override
                                                                public void onAudioUpdate(int currentPosition) {
                                                                    seekBar.setProgress(currentPosition);
                                                                    txtDuration.setText("" + milliSecondsToTimer(currentPosition));
                                                                }

                                                                @Override
                                                                public void onTotalLength(int duration) {
                                                                    seekBar.setMax(duration);
                                                                }
                                                            }, seekBar.getProgress());
                                                        } catch (IOException e) {
                                                            Log.e(TAG, "startPlayer: " + e.getMessage());
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }
                                            } else {
                                                if (NetworkReceiver.isConnected()) {
                                                    RelativeLayout f = (RelativeLayout) (ViewGroup) view.getParent();
                                                    RelativeLayout r = (RelativeLayout) ((ViewGroup) f.getParent());
                                                    SeekBar seekBar = r.findViewById(R.id.receiveSeekbar);
                                                    TextView txtDuration = r.findViewById(R.id.txtReceiveDuration);

                                                    FrameLayout downloadLay = f.findViewById(R.id.downloadLay);
                                                    ImageView leftDownloadIcon = downloadLay.findViewById(R.id.left_download_icon);
                                                    ProgressWheel downloadProgress = downloadLay.findViewById(R.id.downloadProgress);
                                                    ImageView btnReceivePlay = downloadLay.findViewById(R.id.btnReceivePlay);
                                                    downloadProgress.setVisibility(View.VISIBLE);
                                                    downloadProgress.spin();
                                                    leftDownloadIcon.setVisibility(View.GONE);
                                                    downloadProgress.spin();
                                                    txtDuration.setVisibility(View.GONE);

                                                    txtDuration.setText("0:00");
                                                    Data.Builder builder = new Data.Builder();
                                                    builder.putString(Constants.TAG_URL, chat.getMessage().getUploadAudio());
                                                    builder.putString(Constants.TAG_FROM, Constants.TAG_AUDIO_RECEIVE);
                                                    OneTimeWorkRequest downloadWorkRequest = new OneTimeWorkRequest.Builder(DownloadFilesWorker.class)
                                                            .setConstraints(new Constraints.Builder()
                                                                    .setRequiredNetworkType(NetworkType.CONNECTED)
                                                                    .build())
                                                            .setInputData(builder.build())
                                                            .build();
                                                    WorkManager workManager = WorkManager.getInstance(mContext);
                                                    workManager.enqueue(downloadWorkRequest);
                                                    workManager.getWorkInfoByIdLiveData(downloadWorkRequest.getId()).observe(ChatActivity.this, workInfo -> {
                                                        if (workInfo.getState().isFinished()) {
                                                            if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                                                                String savedPath = workInfo.getOutputData().getString(Constants.TAG_DATA);
                                                                Log.d(TAG, "downloadFilesWork: " + savedPath);
                                                                downloadProgress.stopSpinning();
                                                                downloadProgress.setVisibility(View.GONE);
                                                                btnReceivePlay.setVisibility(View.VISIBLE);
                                                                String duration = milliSecondsToTimer(imageStorage.getMediaDuration(new File(savedPath)));
                                                                txtDuration.setText(duration);
                                                                txtDuration.setVisibility(View.VISIBLE);
                                                            } else {
                                                                downloadProgress.stopSpinning();
                                                                downloadProgress.setVisibility(View.GONE);
                                                                leftDownloadIcon.setVisibility(View.VISIBLE);
                                                                btnReceivePlay.setVisibility(View.GONE);
                                                                txtDuration.setVisibility(View.GONE);
                                                            }
                                                        }
                                                    });
                                                } else {

                                                }
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    }
                    break;
                    case "image":
                        //Addon call
                        holder.missedcallimage.setVisibility(View.GONE);
                        holder.centerlay.setVisibility(View.GONE);
                        if (chat.getSender().equals(GetSet.getUserName())) {//Right Side
                            holder.rightLay.setVisibility(View.VISIBLE);
                            holder.right_msg_layout.setVisibility(View.GONE);
                            holder.right_image_lay.setVisibility(View.VISIBLE);
                            holder.right_makeoffer_lay.setVisibility(View.GONE);

                            int imgSize = JoysaleApplication.dpToPx(mContext, 150);
                            if (imageStorage.checkIfImageExists("sent", chat.getMessage().getMessage())) {
                                File file = new File(String.valueOf(imageStorage.getImage("sent", chat.getMessage().getMessage())));
                                Picasso.get()
                                        .load(file).resize(imgSize, imgSize)
                                        .centerCrop().tag(mContext)
                                        .error(R.drawable.image_placeholder)
                                        .into(holder.rightImage);
                            } else {
                                Picasso.get()
                                        .load(chat.getMessage().getUploadImage())
                                        .resize(imgSize, imgSize)
                                        .centerCrop().tag(mContext)
                                        .error(R.drawable.image_placeholder)
                                        .into(holder.rightImage);
                            }
                        } else {
                            //left
                            holder.leftLay.setVisibility(View.VISIBLE);
                            holder.left_msg_layout.setVisibility(View.GONE);
                            holder.left_image_lay.setVisibility(View.VISIBLE);
                            holder.left_makeoffer_lay.setVisibility(View.GONE);

                            int imgSize = JoysaleApplication.dpToPx(mContext, 150);
                            String imageName = AppUtils.getImageName(chat.getMessage().getUploadImage());
                            if (imageStorage.checkIfImageExists("", imageName)) {
                                File file = imageStorage.getImage("", imageName);
                                Picasso.get()
                                        .load(file).resize(imgSize, imgSize)
                                        .centerCrop().tag(mContext)
                                        .error(R.drawable.image_placeholder)
                                        .into(holder.leftImage);
                                holder.leftImgTime.setText(getTime(Long.parseLong(Items.get(position).getMessage().getChatTime()) * 1000));
                            } else {
                                Picasso.get().
                                        load(AppUtils.getValidUrl(chat.getMessage().getUploadImage()))
                                        .resize(imgSize, imgSize).centerCrop()
                                        .centerCrop()
                                        .error(R.drawable.image_placeholder)
                                        .tag(mContext).into(holder.leftImage);
                            }
                        }


                        holder.rightImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (audiovideocallbtn.isEnabled() && !isRecording) {
                                    if (SDK_INT < Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(ChatActivity.this, WRITE_EXTERNAL_STORAGE)
                                            != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(ChatActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, 100);
                                    } else {
                                        if (imageStorage.checkIfImageExists("sent", chat.getMessage().getMessage())) {
                                            File file = imageStorage.getImage("sent", chat.getMessage().getMessage());
                                            Intent intent = new Intent(ChatActivity.this, ViewFullImage.class);
                                            intent.putExtra(Constants.IMAGETYPE, "local");
                                            intent.putExtra(Constants.TAG_FROM, "sent");
                                            intent.putExtra(Constants.KEY_IMAGE, file.getAbsolutePath());
                                            Pair<View, String> bodyPair = Pair.create(view, file.getAbsolutePath());
                                            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ChatActivity.this, bodyPair);
                                            ActivityCompat.startActivity(ChatActivity.this, intent, options.toBundle());
                                        } else {
                                            Intent intent = new Intent(ChatActivity.this, ViewFullImage.class);
                                            intent.putExtra(Constants.IMAGETYPE, "remote");
                                            intent.putExtra(Constants.TAG_FROM, "sent");
                                            intent.putExtra(Constants.KEY_IMAGE, chat.getMessage().getUploadImage());
                                            Pair<View, String> bodyPair = Pair.create(view, chat.getMessage().getUploadImage());
                                            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ChatActivity.this, bodyPair);
                                            ActivityCompat.startActivity(ChatActivity.this, intent, options.toBundle());
                                        }
                                    }
                                }
                            }
                        });


                        holder.leftImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (audiovideocallbtn.isEnabled() && !isRecording) {
                                    if (!Constants.ADDON_VOICE_ENABLED || audiovideocallbtn.isEnabled()) {
                                        if (SDK_INT < Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(ChatActivity.this, WRITE_EXTERNAL_STORAGE)
                                                != PackageManager.PERMISSION_GRANTED) {
                                            ActivityCompat.requestPermissions(ChatActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, 100);
                                        } else {
                                            if (imageStorage.checkIfImageExists("", chat.getMessage().getMessage())) {
                                                Log.v(TAG, "Already Downloaded");
                                                File file = imageStorage.getImage("", chat.getMessage().getMessage());
                                                Intent intent = new Intent(ChatActivity.this, ViewFullImage.class);
                                                intent.putExtra(Constants.IMAGETYPE, "local");
                                                intent.putExtra(Constants.TAG_FROM, "");
                                                intent.putExtra(Constants.KEY_IMAGE, file.getAbsolutePath());
                                                Pair<View, String> bodyPair = Pair.create(view, file.getAbsolutePath());
                                                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ChatActivity.this, bodyPair);
                                                ActivityCompat.startActivity(ChatActivity.this, intent, options.toBundle());
                                            } else {
                                                //Instant Received Image
                                                Intent intent = new Intent(ChatActivity.this, ViewFullImage.class);
                                                intent.putExtra(Constants.IMAGETYPE, "remote");
                                                intent.putExtra(Constants.TAG_FROM, "");
                                                intent.putExtra(Constants.KEY_IMAGE, chat.getMessage().getUploadImage());
                                                Pair<View, String> bodyPair = Pair.create(view, chat.getMessage().getUploadImage());
                                                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ChatActivity.this, bodyPair);
                                                ActivityCompat.startActivity(ChatActivity.this, intent, options.toBundle());
                                            }
                                        }
                                    }
                                }
                            }
                        });
                        break;
                    case "share_location":
                        //Addon call
                        holder.missedcallimage.setVisibility(View.GONE);
                        holder.centerlay.setVisibility(View.GONE);
                        int imgSize = JoysaleApplication.dpToPx(ChatActivity.this, 150);
                        String latitude = chat.getMessage().getLatitude().trim();
                        String longitude = chat.getMessage().getLongitude().trim();
                        int width = JoysaleApplication.dpToPx(ChatActivity.this, 280);
                        int height = JoysaleApplication.dpToPx(ChatActivity.this, 190);
                        String mapImgUrl = null;
                        //Addon Mapbox
                        if (Constants.ADDON_MAP_BOX_ENABLED) {
                           /* StaticMarkerAnnotation marker = StaticMarkerAnnotation.builder()
                                    .name(StaticMapCriteria.MEDIUM_PIN).lnglat(Point.fromLngLat(Double.parseDouble(longitude), Double.parseDouble(latitude)))
                                    .build();
                            List<StaticMarkerAnnotation> markers = new ArrayList<>();
                            markers.add(marker);
                            mapImgUrl = MapboxStaticMap.builder()
                                    .accessToken(getString(R.string.mapbox_access_token))
                                    .styleId(StaticMapCriteria.STREET_STYLE)
                                    .cameraPoint(Point.fromLngLat(Double.parseDouble(longitude), Double.parseDouble(latitude)))
                                    .cameraZoom(13)
                                    .staticMarkerAnnotations(markers)
                                    .width(width)
                                    .height(height)
                                    .retina(true)
                                    .build().url().toString();
                            Log.d(TAG, "getViewMapbox: " + mapImgUrl);*/
                        } else {
                            mapImgUrl = Constants.GOOGLE_MAPS_API + "staticmap?center=" + latitude + "," + longitude +
                                    "&zoom=15&size=" + width + "x" + height + "&sensor=false&markers=" + latitude + "," + longitude +
                                    "|color:red" + "&key=" + Constants.API_KEY;
                            Log.d(TAG, "getView: " + mapImgUrl);
                        }
                        if (chat.getSender().equals(GetSet.getUserName())) {//Right Side
                            holder.rightLay.setVisibility(View.VISIBLE);
                            holder.right_msg_layout.setVisibility(View.GONE);
                            holder.right_image_lay.setVisibility(View.VISIBLE);
                            holder.right_makeoffer_lay.setVisibility(View.GONE);

                            Picasso.get().load(mapImgUrl).resize(imgSize, imgSize).centerCrop().tag(ChatActivity.this).into(holder.rightImage);
                            holder.rightImage.setOnClickListener(new OnClickListener() {
                                @RequiresApi(api = Build.VERSION_CODES.M)
                                @Override
                                public void onClick(View v) {
                                    //Addons VoiceMessage
                                    if (!Constants.ADDON_VOICE_ENABLED || (audiovideocallbtn.isEnabled() && !isRecording)) {
                                        int res = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                                        int res1 = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
                                        if (res != PackageManager.PERMISSION_GRANTED && res1 != PackageManager.PERMISSION_GRANTED) {
                                            locations.launch(PermissionsUtils.LOCATIONS);
                                        } else {
                                            //Addon Mapbox
                                            if (Constants.ADDON_MAP_BOX_ENABLED) {
                                                AppUtils.callMap(chat.getMessage().getLatitude(), chat.getMessage().getLongitude(), ChatActivity.this);
                                            } else {
                                                AppUtils.callMap(chat.getMessage().getLatitude(), chat.getMessage().getLongitude(), ChatActivity.this);
                                            }

                                        }
                                    }
                                }
                            });

                            holder.rightImgTime.setText(getTime(Long.parseLong(Items.get(position).getMessage().getChatTime()) * 1000));
                        } else {
                            holder.leftLay.setVisibility(View.VISIBLE);
                            holder.left_msg_layout.setVisibility(View.GONE);
                            holder.left_image_lay.setVisibility(View.VISIBLE);
                            holder.left_makeoffer_lay.setVisibility(View.GONE);

                            holder.leftImage.setTag("location");
                            Picasso.get().load(mapImgUrl).resize(imgSize, imgSize).centerCrop().tag(mContext).into(holder.leftImage);
                            holder.leftImage.setOnClickListener(new OnClickListener() {
                                @RequiresApi(api = Build.VERSION_CODES.M)
                                @Override
                                public void onClick(View v) {
                                    if (!Constants.ADDON_VOICE_ENABLED || (audiovideocallbtn.isEnabled() && !isRecording)) {
                                        int res = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                                        int res1 = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
                                        if (res != PackageManager.PERMISSION_GRANTED || res1 != PackageManager.PERMISSION_GRANTED) {
                                            locations.launch(PermissionsUtils.LOCATIONS);
                                        } else {
                                            //Addon Mapbox
                                            if (Constants.ADDON_MAP_BOX_ENABLED) {
                                                AppUtils.callMap(chat.getMessage().getLatitude(), chat.getMessage().getLongitude(), ChatActivity.this);
                                            } else {
                                                AppUtils.callMap(chat.getMessage().getLatitude(), chat.getMessage().getLongitude(), ChatActivity.this);
                                            }
                                        }
                                    }
                                }
                            });

                            holder.leftImgTime.setText(getTime(Long.parseLong(Items.get(position).getMessage().getChatTime()) * 1000));
                        }
                        break;

                    case "about":
                        //Addon call
                        holder.missedcallimage.setVisibility(View.GONE);
                        holder.centerlay.setVisibility(View.GONE);
                        //Addon call
                        holder.translatebtnAbout.setVisibility(View.GONE);

                        //Addon Chat Translate
                        if (Constants.ADDON_TRANSLATE_ENABLED) {
                            if (chat.getSender().equals(GetSet.getUserName())) {
                                holder.translatebtnAbout.setVisibility(View.GONE);
                            } else {
                                if (!TextUtils.isEmpty(SharedPrefManager.getString(DEFAULT_CHAT_LANGUAGE, "")) &&
                                        !SharedPrefManager.getString(DEFAULT_CHAT_LANGUAGE, "").equalsIgnoreCase(mContext.getString(R.string.none))) {
                                    holder.translatebtnAbout.setVisibility(View.VISIBLE);
                                } else {
                                    holder.translatebtnAbout.setVisibility(View.GONE);
                                }
                                holder.translatebtnAbout.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        //Addon Chat Translate
                                        JoysaleApplication.preventMultipleClick(view);
                                        Data.Builder builder = new Data.Builder();
                                        builder.putString(Constants.TAG_MESSAGE, chat.getMessage().getMessage());
                                        OneTimeWorkRequest translateWorkRequest = new OneTimeWorkRequest.Builder(ChatTranslateWorker.class)
                                                .setConstraints(new Constraints.Builder()
                                                        .setRequiredNetworkType(NetworkType.CONNECTED)
                                                        .build())
                                                .setInputData(builder.build())
                                                .build();
                                        WorkManager workManager = WorkManager.getInstance(mContext);
                                        workManager.enqueue(translateWorkRequest);
                                        workManager.getWorkInfoByIdLiveData(translateWorkRequest.getId()).observe(ChatActivity.this, workInfo -> {
                                            if (workInfo.getState().isFinished()) {
                                                if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                                                    String translatedMsg = workInfo.getOutputData().getString(Constants.TAG_DATA);
                                                    chat.getMessage().setMessage(Html.fromHtml(translatedMsg).toString());
                                                    chatAdapter.notifyDataSetChanged();
                                                    notifyDataSetChanged();
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        }
                        if (chat.getItemStatus().equals("1")) {
                            holder.itemLay.setVisibility(View.VISIBLE);
                            holder.aboutMsg.setVisibility(View.VISIBLE);

                            Log.d(TAG, "getView1111111: "+chat.getItemImage());
                            Picasso.get().load(chat.getItemImage()).into(holder.itemImage);
                            String name = getString(R.string.about) + " " + "<font color='" + String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.primary))) + "'>" + chat.getItemTitle() + "</font>";
                            holder.itemName.setText(Html.fromHtml(name));
                            holder.aboutDate.setText(chatDate);
                            holder.aboutMsg.setText(JoysaleApplication.stripHtml(chat.getMessage().getMessage()));
                            holder.itemName.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (dialog != null && !dialog.isShowing()) {
                                        dialog.show();
                                    }
                                    getItemData(chat.getItemId());
                                }
                            });
                            holder.itemImage.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (dialog != null && !dialog.isShowing()) {
                                        dialog.show();
                                    }
                                    getItemData(chat.getItemId());
                                }
                            });
                        } else {
                            holder.rightLay.setVisibility(View.VISIBLE);
                            holder.right_msg_layout.setVisibility(View.VISIBLE);
                            holder.right_image_lay.setVisibility(View.GONE);
                            holder.right_makeoffer_lay.setVisibility(View.GONE);
                            holder.rightDelete.setVisibility(View.VISIBLE);
                            holder.rightMsg.setText(getString(R.string.product_removed_msg));
                            holder.rightTime.setText(getTime(Long.parseLong(Items.get(position).getMessage().getChatTime()) * 1000));
                        }
                        break;

                    case "offer":
                        //Addon call
                        holder.missedcallimage.setVisibility(View.GONE);
                        holder.centerlay.setVisibility(View.GONE);
                        //Addon chat translate
                        holder.translatebtnOfferLeft.setVisibility(View.GONE);
                        //Sender
                        if (chat.getSender().equals(GetSet.getUserName())) {//Right Side
                            if (chat.getItemStatus().equals("1")) {
                                if (chat.getOfferType().equals("sendreceive")) {
                                    holder.rightLay.setVisibility(View.VISIBLE);
                                    holder.right_msg_layout.setVisibility(View.GONE);
                                    holder.right_image_lay.setVisibility(View.GONE);
                                    holder.right_makeoffer_lay.setVisibility(View.VISIBLE);
                                    Log.d(TAG, "getView22222222: "+chat.getItemImage());
                                    Picasso.get().load(chat.getItemImage()).into(holder.rightOfferImg);
                                    String name2 = getString(R.string.sent_offer_request_on) + " " + chat.getItemTitle();
                                    holder.right_makeoffer_name.setText(Html.fromHtml(name2));
                                    holder.right_makeoffer_duration.setText(chatDate);
                                    holder.right_makeoffer_price.setTextDirection(View.TEXT_DIRECTION_LTR);
                                    holder.right_makeoffer_price.setText(chat.getFormattedOfferPrice());
                                    holder.right_makeoffer_msg.setText(JoysaleApplication.stripHtml(chat.getMessage().getMessage()));

                                    holder.right_makeoffer_name.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (dialog != null && !dialog.isShowing()) {
                                                dialog.show();
                                            }
                                            getItemData(chat.getItemId());
                                        }
                                    });

                                    holder.rightOfferImg.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (dialog != null && !dialog.isShowing()) {
                                                dialog.show();
                                            }
                                            getItemData(chat.getItemId());
                                        }
                                    });
                                } else if (chat.getOfferType().equals("accept") && (chat.getOfferStatus().equals("1"))) {
                                    holder.offer_result_lay.setVisibility(View.VISIBLE);

                                    holder.offerResultMsg.setText(getString(R.string.offer_accept_msg));
                                    holder.offerResultMsg.setTextColor(ContextCompat.getColor(mContext, R.color.green_color));
                                    holder.offerResultIcon.setImageResource(R.drawable.offer_accept_icon);
                                    holder.offerResultPrice.setText(chat.getFormattedOfferPrice());
                                    holder.offerResultPrice.setTextDirection(View.TEXT_DIRECTION_LTR);
                                    holder.offerResultTime.setText(chatDate);
                                    Log.d(TAG, "getView33333333: "+chat.getItemImage());
                                    Picasso.get().load(chat.getItemImage()).into(holder.offerResultImg);
                                    holder.offerResultImg.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (dialog != null && !dialog.isShowing()) {
                                                dialog.show();
                                            }
                                            getItemData(chat.getItemId());
                                        }
                                    });
                                    if (chat.getBuyNowStatus().equals("0") && chat.getInstantBuy().equals("1") && Constants.BUYNOW) {
                                        holder.buyOfferPrd.setVisibility(View.VISIBLE);
                                        holder.buyOfferPrd.setOnClickListener(new OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog.setMessage(getString(R.string.pleasewait));
                                                dialog.show();
                                                if (!isUserBlocked) {
                                                    getItemDetails(chat);
                                                    selectedOfferChat = chat;
                                                } else if (isUserDeleted) {
                                                    dialog.dismiss();
                                                    JoysaleApplication.openDeleteDialog(getString(R.string.account_delete), getString(R.string.ok));
                                                } else {
                                                    dialog.dismiss();
                                                    Toast.makeText(mContext, getString(R.string.conversation_blocked), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    } else {
                                        holder.buyOfferPrd.setVisibility(View.GONE);
                                    }
                                } else if (chat.getOfferType().equals("decline") && (chat.getOfferStatus().equals("2"))) {
                                    holder.offer_result_lay.setVisibility(View.VISIBLE);
                                    holder.buyOfferPrd.setVisibility(View.GONE);

                                    holder.offerResultMsg.setText(getString(R.string.offer_decline_msg));
                                    holder.offerResultMsg.setTextColor(ContextCompat.getColor(mContext, R.color.red_color));
                                    holder.offerResultIcon.setImageResource(R.drawable.offer_decline_icon);
                                    holder.offerResultPrice.setTextDirection(View.TEXT_DIRECTION_LTR);
                                    holder.offerResultPrice.setText(chat.getFormattedOfferPrice());
                                    holder.offerResultTime.setText(chatDate);
                                    Log.d(TAG, "getView55555555: "+chat.getItemImage());
                                    Picasso.get().load(chat.getItemImage()).into(holder.offerResultImg);
                                    holder.offerResultImg.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (dialog != null && !dialog.isShowing()) {
                                                dialog.show();
                                            }
                                            getItemData(chat.getItemId());
                                        }
                                    });
                                }
                            } else {
                                holder.rightLay.setVisibility(View.VISIBLE);
                                holder.right_msg_layout.setVisibility(View.VISIBLE);
                                holder.right_image_lay.setVisibility(View.GONE);
                                holder.right_makeoffer_lay.setVisibility(View.GONE);
                                holder.rightDelete.setVisibility(View.VISIBLE);

                                holder.rightMsg.setText(getString(R.string.product_removed_msg));
                                holder.rightTime.setText(getTime(Long.parseLong(Items.get(position).getMessage().getChatTime()) * 1000));
                            }
                        } else {//Receiver
                            //Addons ChatTranslate
                            if (Constants.ADDON_TRANSLATE_ENABLED) {
                                if (!TextUtils.isEmpty(SharedPrefManager.getString(DEFAULT_CHAT_LANGUAGE, "")) &&
                                        !SharedPrefManager.getString(DEFAULT_CHAT_LANGUAGE, "").equalsIgnoreCase(mContext.getString(R.string.none))) {
                                    holder.translatebtnOfferLeft.setVisibility(View.VISIBLE);
                                } else {
                                    holder.translatebtnOfferLeft.setVisibility(View.GONE);
                                }

                                holder.translatebtnOfferLeft.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        //Addon Chat Translate
                                        JoysaleApplication.preventMultipleClick(view);
                                        Data.Builder builder = new Data.Builder();
                                        builder.putString(Constants.TAG_MESSAGE, chat.getMessage().getMessage());
                                        OneTimeWorkRequest translateWorkRequest = new OneTimeWorkRequest.Builder(ChatTranslateWorker.class)
                                                .setConstraints(new Constraints.Builder()
                                                        .setRequiredNetworkType(NetworkType.CONNECTED)
                                                        .build())
                                                .setInputData(builder.build())
                                                .build();
                                        WorkManager workManager = WorkManager.getInstance(mContext);
                                        workManager.enqueue(translateWorkRequest);
                                        workManager.getWorkInfoByIdLiveData(translateWorkRequest.getId()).observe(ChatActivity.this, workInfo -> {
                                            if (workInfo.getState().isFinished()) {
                                                if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                                                    String translatedMsg = workInfo.getOutputData().getString(Constants.TAG_DATA);
                                                    chat.getMessage().setMessage(Html.fromHtml(translatedMsg).toString());
                                                    chatAdapter.notifyDataSetChanged();
                                                    notifyDataSetChanged();
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                            if (chat.getItemStatus().equals("1")) {
                                if (chat.getOfferType().equals("sendreceive")) {
                                    holder.leftLay.setVisibility(View.VISIBLE);
                                    holder.left_msg_layout.setVisibility(View.GONE);
                                    holder.left_image_lay.setVisibility(View.GONE);
                                    holder.left_makeoffer_lay.setVisibility(View.VISIBLE);

                                    Log.d(TAG, "getView6666666: "+chat.getItemImage());
                                    Picasso.get().load(chat.getItemImage()).into(holder.leftOfferImg);
                                    String name2 = getString(R.string.receive_offer_request_on) + " " + chat.getItemTitle();
                                    holder.left_makeoffer_name.setText(Html.fromHtml(name2));
                                    holder.left_makeoffer_duration.setText(chatDate);
                                    holder.left_makeoffer_price.setTextDirection(View.TEXT_DIRECTION_LTR);
                                    holder.left_makeoffer_price.setText(chat.getFormattedOfferPrice());
                                    holder.left_makeoffer_msg.setText(JoysaleApplication.stripHtml(chat.getMessage().getMessage()));
                                    if (chat.getOfferStatus().equals("0"))
                                        holder.offerstatusLay.setVisibility(View.VISIBLE);
                                    else {
                                        holder.offerstatusLay.setVisibility(View.GONE);
                                    }
                                    holder.offerAccept.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialog.setMessage(getString(R.string.pleasewait));
                                            dialog.show();
                                            offerAcceptOrDeclined(chat, "accept");
                                        }
                                    });
                                    holder.offerDeclined.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialog.setMessage(getString(R.string.pleasewait));
                                            dialog.show();
                                            offerAcceptOrDeclined(chat, "decline");
                                        }
                                    });

                                    holder.leftOfferImg.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (dialog != null && !dialog.isShowing()) {
                                                dialog.show();
                                            }
                                            getItemData(chat.getItemId());
                                        }
                                    });
                                } else if (chat.getOfferType().equals("accept") && (chat.getOfferStatus().equals("1"))) {
                                    holder.offer_result_lay.setVisibility(View.VISIBLE);
                                    holder.buyOfferPrd.setVisibility(View.GONE);

                                    holder.offerResultMsg.setText(getString(R.string.offer_accept_msg));
                                    holder.offerResultMsg.setTextColor(ContextCompat.getColor(mContext, R.color.green_color));
                                    holder.offerResultIcon.setImageResource(R.drawable.offer_accept_icon);
                                    holder.offerResultPrice.setTextDirection(View.TEXT_DIRECTION_LTR);
                                    holder.offerResultPrice.setText(chat.getFormattedOfferPrice());
                                    holder.offerResultTime.setText(chatDate);
                                    Log.d(TAG, "getView777777: "+chat.getItemImage());
                                    Picasso.get().load(chat.getItemImage()).into(holder.offerResultImg);
                                    holder.offerResultImg.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (dialog != null && !dialog.isShowing()) {
                                                dialog.show();
                                            }
                                            getItemData(chat.getItemId());
                                        }
                                    });
                                } else if (chat.getOfferType().equals("decline") && (chat.getOfferStatus().equals("2"))) {
                                    holder.offer_result_lay.setVisibility(View.VISIBLE);
                                    holder.buyOfferPrd.setVisibility(View.GONE);

                                    holder.offerResultMsg.setText(getString(R.string.offer_decline_msg));
                                    holder.offerResultMsg.setTextColor(ContextCompat.getColor(mContext, R.color.red_color));
                                    holder.offerResultIcon.setImageResource(R.drawable.offer_decline_icon);
                                    holder.offerResultPrice.setTextDirection(View.TEXT_DIRECTION_LTR);
                                    holder.offerResultPrice.setText(chat.getFormattedOfferPrice());
                                    holder.offerResultTime.setText(chatDate);
                                    Log.d(TAG, "getView8888888: "+chat.getItemImage());
                                    Picasso.get().load(chat.getItemImage()).into(holder.offerResultImg);
                                    holder.offerResultImg.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (dialog != null && !dialog.isShowing()) {
                                                dialog.show();
                                            }
                                            getItemData(chat.getItemId());
                                        }
                                    });
                                }
                            } else {
                                holder.leftLay.setVisibility(View.VISIBLE);
                                holder.left_msg_layout.setVisibility(View.VISIBLE);
                                holder.left_image_lay.setVisibility(View.GONE);
                                holder.left_makeoffer_lay.setVisibility(View.GONE);
                                holder.leftDelete.setVisibility(View.VISIBLE);

                                holder.leftMsg.setText(getString(R.string.product_removed_msg));
                                holder.leftTime.setText(getTime(Long.parseLong(Items.get(position).getMessage().getChatTime()) * 1000));
                            }
                        }
                        break;
                }

                if (position == 0) {
                    holder.dateLay.setVisibility(View.VISIBLE);
                    holder.date.setText(chatDate);
                } else {
                    String ldate = JoysaleApplication.getDate(Long.parseLong(Items.get(position - 1).getMessage().getChatTime()) * 1000);
                    if (ldate.equals(chatDate)) {
                        holder.dateLay.setVisibility(View.GONE);
                    } else {
                        holder.dateLay.setVisibility(View.VISIBLE);
                        holder.date.setText(chatDate);
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Exception in adapter=>" + e);
                e.printStackTrace();
            }
            return convertView;
        }


        public class ViewHolder {
            LinearLayout main, offerstatusLay;
            TextView message, itemName, left_makeoffer_name, right_makeoffer_name, aboutDate, price, aboutMsg, date, leftMsg, rightMsg, leftTime,
                    rightTime, left_makeoffer_duration, left_makeoffer_msg, right_makeoffer_msg, right_makeoffer_duration, left_makeoffer_price,
                    right_makeoffer_price, leftImgTime, rightImgTime, buyOfferPrd, offerAccept, offerDeclined, offerResultPrice, offerResultTime,
                    offerResultMsg, chattrans, translatebtnAbout, translatebtnOfferLeft, txtReceiveDuration, txtSentDuration, missedcall, missedcalltime;
            ImageView rightCorner, leftCorner, missedcallimage, outgoingImage, leftImage, rightImage, itemImage, leftOfferImg, rightOfferImg, offerResultImg, leftDelete, rightDelete, offerResultIcon, rightGif, leftGif, btnReceivePlay, btnSentPlay, leftDownloadIcon;
            RelativeLayout right_audio_lay, left_audio_lay, centerlay, dateLay, itemLay, leftLay, rightLay, left_makeoffer_lay, right_makeoffer_lay, left_image_lay, left_msg_layout, right_msg_layout, right_image_lay, offer_result_lay, audioSentLay, audioReceiveLay;
            SeekBar receiveSeekbar, sentSeekbar;
            ProgressWheel downloadProgress;
            FrameLayout downloadLay;
        }
    }

    /**
     * Adapter for Chat Template
     */

    private class RecyclerAdapter extends RecyclerView.Adapter<Viewholder> {
        ArrayList<AdminDataResponse.ChatTemplate> items;

        public RecyclerAdapter(ArrayList<AdminDataResponse.ChatTemplate> templatMsgAry) {
            this.items = templatMsgAry;
        }

        @Override
        public Viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_template_text, parent, false);
            return new Viewholder(view);
        }

        @Override
        public void onBindViewHolder(Viewholder holder, final int position) {
            holder.templateMsg.setText(items.get(holder.getAbsoluteAdapterPosition()).getName());
            holder.templateMsg.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessage(items.get(holder.getAbsoluteAdapterPosition()).getName());
                    chatAdapter.notifyDataSetChanged();
                    if (chats.size() > 0) {
                        listView.setSelection(chats.size() - 1);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    /*
        public void stop(ChatAdapter.ViewHolder viewHolder, String type, int pos){
            if (chat.getIsPlaying() != null && chat.getIsPlaying()) {
                holder.btnSentPlay.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_pause_white));
            } else {
                holder.btnSentPlay.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_play_white));
            }
        }
    */
    private class Viewholder extends RecyclerView.ViewHolder {
        TextView templateMsg;

        public Viewholder(View itemView) {
            super(itemView);
            templateMsg = itemView.findViewById(R.id.templateMsg);
        }

    }

    /**
     * class for upload Image to Server
     */

    @SuppressLint("StaticFieldLeak")
    class UploadImage extends AsyncTask<String, String, String> {
        String jsonResponse = "", status, imageName = "", mFrom, message = "";
        ProgressDialog pd;
        Context mContext;
        ImageView mUserImage;
        long mUnixTimeStamp;
        ChatResponse.Chat chat;
        String existingFileName = null;
        JSONObject jsonobject = null;

        UploadImage(Context context, String from, ImageView userImage, long unixTimeStamp, ChatResponse.Chat chat) {
            mContext = context;
            mFrom = from;
            mUserImage = userImage;
            mUnixTimeStamp = unixTimeStamp;
            this.chat = chat;
        }

        @Override
        protected String doInBackground(String... imgpath) {
            StringBuilder builder = new StringBuilder();
            String twoHyphens = "--", boundary = "*****", urlString = Constants.API_UPLOAD_IMAGE;
            int bytesRead, bytesAvailable, bufferSize, maxBufferSize = 1 * 1024 * 1024;
            byte[] buffer;
            final String LINE_END = "\r\n";
            try {
                existingFileName = imgpath[0];
                FileInputStream inputStream = new FileInputStream(new File(existingFileName));
                URL url = new URL(urlString);
                HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                httpConn.setDoInput(true);
                httpConn.setDoOutput(true); // indicates POST method
                httpConn.setUseCaches(false);
                httpConn.setRequestMethod("POST");
                httpConn.setRequestProperty("Connection", "Keep-Alive");
                httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                DataOutputStream dos = new DataOutputStream(httpConn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + LINE_END);
                dos.writeBytes("Content-Disposition: form-data;name=\"type\"" + LINE_END);
                dos.writeBytes(LINE_END);
                dos.writeBytes("chat");
                dos.writeBytes(LINE_END);
                dos.writeBytes(twoHyphens + boundary + LINE_END);
                dos.writeBytes("Content-Disposition: form-data; name=\"images\";filename=\""
                        + existingFileName + "\"" + LINE_END);
                dos.writeBytes(LINE_END);

                bytesAvailable = inputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                }

                dos.writeBytes(LINE_END);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + LINE_END);
                BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    builder.append(inputLine);
                inputStream.close();
                jsonResponse = builder.toString();
                dos.flush();
                dos.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.v(TAG, "uploadImageResponse: " + jsonResponse);
            try {
                jsonobject = new JSONObject(jsonResponse);
                status = jsonobject.getString(Constants.TAG_STATUS);
                if (jsonobject.has("message")) {
                    message = jsonobject.getString("message");
                }
                if (status.equals(Constants.TAG_TRUE)) {
                    JSONObject image = jsonobject.getJSONObject("Image");
                    imageName = image.getString("Name");
                    File from = new File(existingFileName);
                    File destinationFolder = imageStorage.getFileSavePath("sent");
                    if (!destinationFolder.exists())
                        destinationFolder.mkdirs();
                    File to = new File(destinationFolder, imageName);
                    InputStream in = new FileInputStream(from);
                    OutputStream out = new FileOutputStream(to);

                    // Copy the bits from instream to outstream
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }

            return jsonResponse;
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(mContext, R.style.AppCompatAlertDialogStyle);
            pd.setMessage(mContext.getString(R.string.loading));
            pd.setCanceledOnTouchOutside(false);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                Drawable drawable = new ProgressBar(ChatActivity.this).getIndeterminateDrawable().mutate();
                drawable.setColorFilter(ContextCompat.getColor(ChatActivity.this, R.color.progressColor),
                        PorterDuff.Mode.SRC_IN);
                pd.setIndeterminateDrawable(drawable);
            }
            pd.show();
        }

        @Override
        protected void onPostExecute(String res) {
            try {
                if (status.equals(Constants.TAG_TRUE)) {
                    JSONObject image = jsonobject.getJSONObject("Image");
                    callSocket(mUnixTimeStamp, "image", image.getString("View_url"), "");
                    dismissProgress(pd);

                    sendChat(Constants.TAG_IMAGE, "", imageName, 0.0, 0.0, "");
                    runOnUiThread(new Runnable() {
                        public void run() {
                            ChatResponse.Message message = chat.getMessage();
                            message.setMessage(imageName);
                            chatAdapter.notifyDataSetChanged();
                            if (chats.size() > 0) {
                                if (listView.getVisibility() != View.VISIBLE)
                                    listView.setVisibility(View.VISIBLE);
                                listView.setSelection(chats.size() - 1);
                            }
                        }
                    });
                } else if (status.equalsIgnoreCase("false")) {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    dismissProgress(pd);
                    JoysaleApplication.uploadAlertDialog(ChatActivity.this, getString(R.string.alert), getString(R.string.image_cannot_moderator));

                }
            } catch (Exception e) {
                status = "false";
                e.printStackTrace();
                dismissProgress(pd);
            }
        }
    }

    public boolean isPermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void request(String[] permissions) {
        phonepermissionresult.launch(permissions);
    }

    public static void openPermissionDialogs(Context mContext, OkayCancelCallback callback, String description) {
        new AlertDialog.Builder(mContext)
                .setMessage(R.string.read_phone_state_permission_forandroid12)
                .setPositiveButton(mContext.getString(R.string.okay), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.onOkayClicked("");
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.onCancelClicked("");
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }
    public static void openPermissionDialog13(Context mContext, OkayCancelCallback callback, String description) {
        new AlertDialog.Builder(mContext)
                .setMessage(R.string.camera_error)
                .setPositiveButton(mContext.getString(R.string.okay), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.onOkayClicked("");
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.onCancelClicked("");
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    public Boolean isuserdeleted(Boolean isuserdeleted) {
        if (isuserdeleted) {
            blockUserLay.setVisibility(View.VISIBLE);
            sendMsgLay.setVisibility(View.GONE);
            bottom.setVisibility(View.GONE);
            blockMsg.setText("This Account Has Been Deleted");
            audiovideocallbtn.setVisibility(View.VISIBLE);
            //   values.remove(1);

        } else {

        }
        return null;
    }

    private void sendLocationToServer(final String chat_type, final String type, final String params, final double latitude, final double longitude) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            long unixTime = System.currentTimeMillis() / 1000L;
            double lat = latitude;
            double lon = longitude;
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
            map.put(Constants.TAG_CHAT_ID, chatId);
            if (getIntent().getStringExtra(Constants.TAG_SOURCE_ID) != null)
                map.put(Constants.TAG_SOURCE_ID, getIntent().getStringExtra(Constants.TAG_SOURCE_ID));
            else
                map.put(Constants.TAG_SOURCE_ID, "0");
            map.put(Constants.TAG_IMAGE_URL, "");
            map.put(Constants.TAG_TYPE, type);
            map.put(Constants.TAG_CREATED_DATE, Long.toString(unixTime));
            map.put(Constants.TAG_MESSAGE, params);
            if (chat_type != null && chat_type.length() != 0) {
                map.put(Constants.TAG_CHAT_TYPE, chat_type);
            } else {
                map.put(Constants.TAG_CHAT_TYPE, "normal");
            }

            if (lat != 0.0 && longitude != 0.0) {
                try {
                    NumberFormat nf = NumberFormat.getInstance(new Locale("en", "US"));
                    DecimalFormat dFormat = new DecimalFormat("#.######");
                    if (LocaleManager.isRTL(getApplicationContext())) {
                        lat = Double.valueOf(nf.format(lat));
                        lon = Double.valueOf(nf.format(lon));
                    } else {

                        lat = Double.valueOf(dFormat.format(lat));
                        lon = Double.valueOf(dFormat.format(lon));
                    }
                    map.put(Constants.TAG_CURRENT_LATITUDE, String.valueOf(lat));
                    map.put(Constants.TAG_CURRENT_LONGITUDE, String.valueOf(lon));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            } else {
                map.put(Constants.TAG_CURRENT_LATITUDE, "");
                map.put(Constants.TAG_CURRENT_LONGITUDE, "");
            }

            Call<HashMap<String, String>> call = apiInterface.sendChat(map);
            double finalLat = lat;
            double finalLon = lon;
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                    if (response.isSuccessful()) {
                        long unixTime = System.currentTimeMillis() / 1000L;
                        JSONObject jsonObject = socket(unixTime, "share_location", latitude, longitude);
                        Intent i = new Intent();
                        i.putExtra(Constants.TAG_CURRENT_LATITUDE, "" + latitude);
                        i.putExtra(Constants.TAG_CURRENT_LONGITUDE, "" + longitude);
                        i.putExtra("jsonObject", String.valueOf(jsonObject));
                        String latitude = String.valueOf(finalLat);
                        String longitude = String.valueOf(finalLon);

                        ChatResponse.Message message = new ChatResponse().new Message();
                        ChatResponse.Chat chat = new ChatResponse().new Chat();
                        chat.setSender(GetSet.getUserName());
                        message.setChatTime("" + System.currentTimeMillis() / 1000L);
                        message.setLatitude(latitude);
                        message.setLongitude(longitude);
                        chat.setMessage(message);
                        chat.setType("share_location");
                        String jsonStr = String.valueOf(jsonObject);
                        try {
                            JSONObject jsonObject1 = new JSONObject(jsonStr);
                            Log.i(TAG, "onActivityResult: " + jsonObject1);
                            mSocket.emit("message", jsonObject1);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        chats.add(chat);
                        chatAdapter.notifyDataSetChanged();
                        listView.setSelection(chats.size() - 1);
                        if (chats.size() > 0 && listView.getVisibility() != View.VISIBLE) {
                            listView.setVisibility(View.VISIBLE);
                        }
                        //setResult(RESULT_OK, i);
                        /*Intent intent = new Intent();
                        intent.putExtra(Constants.TAG_FROM, "chat");
                        intent.putExtra(Constants.CHATID, chatId);
                        intent.putExtra(Constants.TAG_USERNAME, userName);
                        intent.putExtra(Constants.TAG_USER_ID, userId);
                        intent.putExtra(Constants.TAG_USERIMAGE_M, userImage);
                        intent.putExtra(Constants.TAG_FULL_NAME, fullName);*/
                    } else {
                        //remLoc.setEnabled(true);
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    //remLoc.setEnabled(true);
                    call.cancel();
                    if (dialog.isShowing() && dialog != null) {
                        dialog.dismiss();
                    }
                    JoysaleApplication.dialog(ChatActivity.this, getResources().getString(R.string.alert), getString(R.string.somethingwrong));
                }
            });
        } else {
            //remLoc.setEnabled(true);
        }
    }

    private JSONObject socket(long time, String type, double lat, double lon) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject message = new JSONObject();
            message.put(Constants.TAG_CHATTIME, Long.toString(time));
            message.put(Constants.SOCK_USERIMAGE, GetSet.getImageUrl().replace("/150/", "/40/"));
            message.put(Constants.SOCK_USERNAME, GetSet.getUserName());
            message.put(Constants.TAG_MESSAGE, "");
            message.put(Constants.SOCK_VIEW_URL, "");
            message.put(Constants.TAG_TYPE, "share_location");
            message.put(Constants.TAG_LAT, lat);
            message.put(Constants.TAG_LON, lon);
            message.put(Constants.SOCK_MESSAGE_CONTENT, "3");
            jobj.put(Constants.TAG_MESSAGE, message);
            jobj.put(Constants.SOCK_RECEIVERID, GetSet.getUserName());
            jobj.put(Constants.SOCK_SENDERID, getIntent().getStringExtra(Constants.TAG_USERNAME));
            jobj.put("offerId", "0");
            if (getIntent().getStringExtra(Constants.TAG_SOURCE_ID) != null) {
                jobj.put(Constants.SOCK_SOURCE_ID, getIntent().getStringExtra(Constants.TAG_SOURCE_ID));
            } else {
                jobj.put(Constants.SOCK_SOURCE_ID, "0");
            }
            Log.v(TAG, "sendLocationJSON=" + jobj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jobj;
    }


}