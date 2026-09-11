package com.ni.SnatchItUp;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;
import static com.ni.external.multipleimageselect.helpers.Constants.REQUEST_CODE;
import static com.ni.SnatchItUp.ChatActivity.templatMsgAry;

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
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ni.external.imagepicker.ImagePicker;
import com.ni.external.multipleimageselect.activities.AlbumSelectActivity;
import com.ni.external.multipleimageselect.models.Photo;
import com.ni.helper.ImageCompression;
import com.ni.helper.ImageStorage;
import com.ni.helper.LocaleManager;
import com.ni.helper.NetworkReceiver;
import com.ni.helper.PermissionsUtils;
import com.ni.helper.StorageUtils;
import com.ni.helper.callback.OkayCancelCallback;
import com.ni.helper.location.LocationHelper;
//import com.ni.mapbox.MainActivity;
import com.ni.model.AdminDataResponse;
import com.ni.model.ChatResponse;
import com.ni.model.ExchangeResponse;
import com.ni.model.HomeItemResponse;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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
 * This class is for Exchange Chat
 */

public class ExchangeView extends BaseActivity implements OnClickListener, TextWatcher, OnScrollListener {

    /**
     * Declare Layout Elements
     **/
    TextView title, failed, success, nullText;
    ListView listView;
    ImageView backBtn;


    AVLoadingIndicatorView progress, topProgress, typing;
    EditText editText;
    ViewGroup header, footer;
    LinearLayout send, shareImg, sharelocation, sendMsgLay;
    ProgressDialog pd;
    RecyclerView recyclerView;
    RecyclerView mSmartRepliesRecyler;
    private Socket mSocket;
    ChatAdapter chatAdapter;
    InputMethodManager imm;
    Handler handler = new Handler();
    Runnable runnable;
    RecyclerAdapter recyclerAdapter;
    private Dialog permissionDialog;
    private File currentPhotoFile;
    private Uri mCurrentPhotoUri;
    StorageUtils storageUtils;
    private static final int REQUEST_CODE_PICK_IMAGE = 100;
    private ActivityResultLauncher<String[]> locations;
    private ActivityResultLauncher<String[]> storagePermissionResult;
    private ActivityResultLauncher<String[]> cameraResultLauncher;
    /**
     * Declare Variables
     **/
    static final String TAG = "ExchangeView";
    public static String fullName = "";
    String userName = "", clickedBtn, chatId, type, existingFileName,
            exchangeItemId, myItemId, exchangerId;
    boolean pulldown = false, loading = false, meTyping, receiverTyping;
    int black, currentPage = 0, position;
    String stringChatURL = "";
    ArrayList<ChatResponse.Chat> chats = new ArrayList<>(), tempAry = new ArrayList<>();
    ExchangeResponse.Exchange exchangeData = new ExchangeResponse().new Exchange();
    ApiInterface apiInterface;
    ImageStorage imageStorage;
    private ActivityResultLauncher<Intent> locationResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exchange_view);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        imageStorage = new ImageStorage(this);
        backBtn = (ImageView) findViewById(R.id.backbtn);
        shareImg = (LinearLayout) findViewById(R.id.shareImg);
        sharelocation = (LinearLayout) findViewById(R.id.sharelocation);
        title = (TextView) findViewById(R.id.title);
        listView = (ListView) findViewById(R.id.listView);
        send = (LinearLayout) findViewById(R.id.send);
        editText = (EditText) findViewById(R.id.editText);
        failed = (TextView) findViewById(R.id.failed);
        success = (TextView) findViewById(R.id.success);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
        sendMsgLay = findViewById(R.id.sendMsgLay);
        sendMsgLay.setVisibility(View.GONE);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerAdapter = new RecyclerAdapter(templatMsgAry);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mSmartRepliesRecyler = findViewById(R.id.smartRepliesRecycler);
        storageUtils = new StorageUtils(this);
        initActivityResult();

        LayoutInflater inflater = getLayoutInflater();
        header = (ViewGroup) inflater.inflate(R.layout.chat_header, null, false);
        listView.addHeaderView(header, null, false);

        footer = (ViewGroup) getLayoutInflater().inflate(R.layout.chat_footer, null);
        listView.addFooterView(footer);

        listView.setSmoothScrollbarEnabled(true);
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);

        topProgress = (AVLoadingIndicatorView) header.findViewById(R.id.topProgress);
        nullText = (TextView) header.findViewById(R.id.nulltext);
        typing = (AVLoadingIndicatorView) footer.findViewById(R.id.typing);

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        black = getResources().getColor(R.color.black);
        title.setText(getString(R.string.myexchange));
        exchangeData = (ExchangeResponse.Exchange) getIntent().getExtras().get(Constants.TAG_DATA);
        exchangeItemId = exchangeData.getExchangeProduct().getItemId();
        myItemId = exchangeData.getMyProduct().getItemId();
        exchangerId = exchangeData.getExchangerId();
        if (exchangeData.getExchangerName() != null)
            fullName = exchangeData.getExchangerName();
        position = (int) getIntent().getExtras().get(Constants.TAG_POSITION);
        type = (String) getIntent().getExtras().get(Constants.TAG_TYPE);

        // initialize dialog
        pd = new ProgressDialog(ExchangeView.this, R.style.AppCompatAlertDialogStyle);
        pd.setMessage(ExchangeView.this.getString(R.string.loading));
        pd.setCancelable(false);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = new ProgressBar(this).getIndeterminateDrawable().mutate();
            drawable.setColorFilter(ContextCompat.getColor(this, R.color.progressColor),
                    PorterDuff.Mode.SRC_IN);
            pd.setIndeterminateDrawable(drawable);
        }
        pd.show();
        LinearLayoutManager layoutManager = new LinearLayoutManager(ExchangeView.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerAdapter = new ExchangeView.RecyclerAdapter(templatMsgAry);
        Log.d(TAG, "onBindViewHolder: " + templatMsgAry);
        DividerItemDecoration itemDecorator = new DividerItemDecoration(ExchangeView.this, DividerItemDecoration.HORIZONTAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(ExchangeView.this, R.drawable.chat_template_divider));
        recyclerView.addItemDecoration(itemDecorator);
        recyclerView.setAdapter(recyclerAdapter);
        userName = exchangeData.getExchangerUsername();
        String status = exchangeData.getStatus();
        Log.v(TAG, "userName=" + userName);

        LinearLayoutManager chipManager = new LinearLayoutManager(this);
        chipManager.setOrientation(RecyclerView.HORIZONTAL);

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
        mSocket.on("exmessage", onMessage);
        mSocket.on("exmessageTyping", onTyping);
        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.v("EVENT_CONNECT", "EVENT_CONNECT");
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.v("EVENT_DISCONNECT", "EVENT_DISCONNECT");
            }
        });
        mSocket.connect();

        mSocket.emit("exchangejoin", jobj);

        if (exchangeData.getRequestByMe().equals("true")) {
            if (status.equals("Pending")) {
                success.setText(getString(R.string.cancel));
                failed.setVisibility(View.GONE);
            } else if (status.equals("Accepted")) {
                failed.setText(getString(R.string.failed));
                success.setText(getString(R.string.success));
            }

        } else {
            if (status.equals("Pending")) {
                failed.setText(getString(R.string.decline));
                success.setText(getString(R.string.accept));
            } else if (status.equals("Accepted")) {
                failed.setText(getString(R.string.failed));
                success.setText(getString(R.string.success));
            }
        }

        backBtn.setOnClickListener(this);
        send.setOnClickListener(this);
        editText.addTextChangedListener(this);
        failed.setOnClickListener(this);
        success.setOnClickListener(this);
        shareImg.setOnClickListener(this);
        sharelocation.setOnClickListener(this);
        editText.setFilters(new InputFilter[]{JoysaleApplication.EMOJI_FILTER});

        // initialize Adapter class
        chatAdapter = new ChatAdapter(ExchangeView.this, chats);
        listView.setAdapter(chatAdapter);

        try {
            initializeChatUI();
            getChat(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        initLauncher();
        initPermissionLauncher();
    }

    private void initPermissionLauncher() {
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
                            if (ActivityCompat.shouldShowRequestPermissionRationale(ExchangeView.this, x.getKey())) {
                                storagePermissionResult.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                PermissionsUtils.openPermissionDialog(ExchangeView.this, new OkayCancelCallback() {
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
                                }, getResources().getString(R.string.storage_error));
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
                            if (ActivityCompat.shouldShowRequestPermissionRationale(ExchangeView.this, x.getKey())) {
                                cameraResultLauncher.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                PermissionsUtils.openPermissionDialog(ExchangeView.this, new OkayCancelCallback() {
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
                                }, ExchangeView.this.getString(R.string.camera_error));
                            }
                            break;
                        }
                    }
                }

            }
        });
    }

    /**
     * Method for receiving the instant messages & typing status
     **/
    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        Log.v("onTyping", "onTyping=" + args[0]);
                        JSONObject data = (JSONObject) args[0];
                        if (data.getString(Constants.SOCK_RECEIVER).equals(userName) && data.getString(Constants.TAG_MESSAGE).equals(Constants.TAG_TYPE)) {
                            if (!receiverTyping) {
                                receiverTyping = true;
                                typing.setVisibility(View.VISIBLE);
                                if (chats.size() > 0) {
                                    listView.setSelection(chats.size() - 1);
                                }
                                typing.startAnimation(AnimationUtils.loadAnimation(ExchangeView.this, com.google.android.material.R.anim.abc_slide_in_bottom));
                            }
                        } else {
                            receiverTyping = false;
                            typing.setVisibility(View.GONE);
                            typing.startAnimation(AnimationUtils.loadAnimation(ExchangeView.this, com.google.android.material.R.anim.abc_slide_out_bottom));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.v(TAG, "onMessage=" + args[0]);
            JSONObject data = (JSONObject) args[0];
            try {
                ChatResponse.Message message = new ChatResponse().new Message();
                ChatResponse.Chat chat = new ChatResponse().new Chat();
                chat.setSender(data.getString(Constants.TAG_RECEIVER));
                message.setChatTime(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_CHATTIME));
                if (!data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.SOCK_VIEW_URL).equals("")) {
                    chat.setType(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_TYPE));
                    message.setUploadImage(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.SOCK_VIEW_URL));
                } else if (!data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_LAT).equals("")) {
                    message.setLatitude(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_LAT));
                    message.setLongitude(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_LON));
                    chat.setType("share_location");
                } else if (!data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_MESSAGE).equals("")) {
                    message.setMessage(data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_MESSAGE));
                    chat.setType("message");
                }
                chat.setMessage(message);
             /*   if (isAddons) {
                    if (data.getString(Constants.TAG_RECEIVER).equals(userName)) {
                        if (chat.getType().equals("message")) {
                            chats.add(chat);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    generateRepliesSocket(chat.getMessage().getMessage());
                                }
                            });

                        } else {
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
                } else {
                    chats.add(chat);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            chatAdapter.notifyDataSetChanged();
                            if (chats.size() > 0) {
                                listView.setSelection(chats.size() - 1);
                            }
                        }
                    });
                }*/
                chats.add(chat);
                runOnUiThread(new Runnable() {
                    public void run() {
                        chatAdapter.notifyDataSetChanged();
                        if (chats.size() > 0) {
                            listView.setSelection(chats.size() - 1);
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

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
                            mSocket.emit("exmessage", jsonObject);
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

    public void dialog(String title, String content) {
        final Dialog dialog = new Dialog(ExchangeView.this, R.style.AlertDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        dialog.getWindow().setLayout(displayMetrics.widthPixels * 80 / 100, LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        TextView alertTitle = (TextView) dialog.findViewById(R.id.alert_title);
        TextView alertMsg = (TextView) dialog.findViewById(R.id.alert_msg);
        ImageView alertIcon = (ImageView) dialog.findViewById(R.id.alert_icon);
        TextView alertOk = (TextView) dialog.findViewById(R.id.alert_button);

        alertTitle.setText(title);
        alertMsg.setText(content);
        alertIcon.setImageResource(R.drawable.success_icon);

        alertOk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                //	doActionOnClick();
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                doActionOnClick();
            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    private void doActionOnClick() {
        if (failed.getText().toString().equals(getString(R.string.decline)) && success.getText().toString().equals(getString(R.string.accept))) {
            if (clickedBtn.equals("success")) {
                failed.setText(getString(R.string.failed));
                success.setText(getString(R.string.success));
                if (type.equals("incoming")) {
                    Log.v(TAG, "checkstatus" + success.isEnabled());
                    IncomeExchange.incomingAry.get(position).setStatus("Accepted");
                    IncomeExchange.exchangeAdapter.notifyDataSetChanged();
                } else if (type.equals("outgoing")) {
                    OutgoingExchange.outgoingAry.get(position).setStatus("Accepted");
                    OutgoingExchange.exchangeAdapter.notifyDataSetChanged();
                }
            } else {
                ExchangeActivity.type = "failed";
                ExchangeActivity.statusChanged = true;
                finish();
            }

        } else if (failed.getText().toString().equals(getString(R.string.failed)) && success.getText().toString().equals(getString(R.string.success))) {
            if (clickedBtn.equals("success")) {
                ExchangeActivity.type = "success";
                ExchangeActivity.statusChanged = true;
                finish();
            } else {
                ExchangeActivity.type = "failed";
                ExchangeActivity.statusChanged = true;
                finish();
            }

        } else if (success.getText().toString().equals(getString(R.string.cancel))) {
            ExchangeActivity.type = "failed";
            ExchangeActivity.statusChanged = true;
            finish();
        }
    }

    private void disconnectSocket() {
        if (mSocket != null) {
            mSocket.off("exmessage");
            mSocket.off("exmessageTyping");
            mSocket.disconnect();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        disconnectSocket();
        fullName = "";
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem == 0 && !(loading)) {
            loading = true;
            topProgress.setVisibility(View.VISIBLE);
            nullText.setVisibility(View.GONE);
            currentPage++;
            pulldown = true;
            if (JoysaleApplication.isNetworkAvailable(ExchangeView.this)) {
                initializeChatUI();
                getChat(currentPage);
            }
        }
    }

    /**
     * Function to Call a Socket to update a message
     **/

    private void callSocket(long time, String type, String data) {
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
            }
            jobj.put(Constants.SOCK_RECEIVERID, GetSet.getUserName().trim());
            jobj.put(Constants.SOCK_SENDERID, userName.trim());
            jobj.put(Constants.SOCK_SOURCE_ID, exchangeData.getExchangeId());
            jobj.put(Constants.TAG_MESSAGE, message);
            Log.v(TAG, "sendDataSocketjson=" + jobj);
            mSocket.emit("exmessage", jobj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        fullName = "";
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (exchangeData.getExchangerName() != null)
            fullName = exchangeData.getExchangerName();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(ExchangeView.this, isConnected);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "onActivityResult"+"requestCode "+requestCode+"resultCode "+resultCode+"data "+data);
        if(resultCode==-1 && requestCode == 2000){
            ArrayList<Photo> photoList = (ArrayList<Photo>) data.getExtras().get("galleryData");
            Log.d(TAG, "onActivityResult: "+photoList);
            for (Photo selectedImage : photoList) {
                HomeItemResponse.Photo photo = new HomeItemResponse().new Photo();
                photo.setType(selectedImage.getType());
                photo.setImage("" + selectedImage.getImage());
                photo.setPathType(Constants.TAG_GALLERY);

                Log.e("selectedImage", "-" + selectedImage.getImage());


                ImageCompression imageCompression = new ImageCompression(ExchangeView.this) {
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
//                        new ChatActivity.UploadImage(ExchangeView.this, "Chat", userImg, unixTime, chat).execute(imagePath);
                        new UploadImage(ExchangeView.this, "Chat", unixTime, chat).execute(imagePath);
                    }
                };
                imageCompression.execute(selectedImage.getImage());
            }
        }
        else if (resultCode == -1 && requestCode == 234) {
            final File file = new File(ImagePicker.getImageFilePath(this, requestCode, resultCode, data));
            if (file.exists()) {
                String filepath = file.getAbsolutePath();
                Log.v(TAG, "selectedImageFile: " + filepath);
                ImageCompression imageCompression = new ImageCompression(ExchangeView.this) {
                    @Override
                    protected void onPostExecute(String imagePath) {
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
                        new UploadImage(ExchangeView.this, "Chat", unixTime, chat).execute(imagePath);
                    }
                };
                imageCompression.execute(filepath);
            } else {
                Toast.makeText(this, getString(R.string.profile_problem), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK) {
            //          Log.d(TAG, "onActivityResult: "+data.getData());
            try {
                boolean isCamera = (data == null
                        || data.getData() == null);
                String fileName = getString(R.string.app_name) + System.currentTimeMillis() + ".jpg";
                Uri file = null;
                if (isCamera) {     /** CAMERA **/
                    Log.d(TAG, "onActivityResult: " + mCurrentPhotoUri.getPath());
                    Bitmap bitmap = FileUtil.decodeBitmap(this, mCurrentPhotoUri);
//                    file = storageUtils.saveToSDCard(bitmap, Constants.TAG_SENT, fileName);
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        file = mCurrentPhotoUri;
                    } else {
//                        file = storageUtils.saveToSDCard(FileUtil.decodeBitmap(this, mCurrentPhotoUri), Constants.TAG_SENT, fileName);
                        file = storageUtils.saveToSDCard(bitmap, Constants.TAG_SENT, fileName);
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
                    String thumbnail = storageUtils.saveThumbNail(FileUtil.decodeBitmap(this, file), fileName);
                    try {
                        ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(file, "r");
                        //byte[] imageBytes = ByteStreams.toByteArray(new FileInputStream(pfd.getFileDescriptor()));
                        //byte[] imageBytes = ByteStreams.toByteArray(new FileInputStream(pfd.getFileDescriptor()));
                        Log.i(TAG, "onPostExecute: " + thumbnail);

                        @SuppressLint("StaticFieldLeak")
                        ImageCompression imageCompression = new ImageCompression(ExchangeView.this) {
                            @Override
                            protected void onPostExecute(String imagePath) {
                                Log.d(TAG, "onPostExecuteimage: "+imagePath);
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
                                new UploadImage(ExchangeView.this, "Chat", unixTime, chat).execute(imagePath);
                            }
                        };
                        imageCompression.execute(thumbnail);
                    } catch (Exception e) {

                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (requestCode == Constants.LOCATION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String location = data.getStringExtra(Constants.TAG_LOCATION);
                Double lat = data.getDoubleExtra(Constants.TAG_LAT, 0);
                Double lng = data.getDoubleExtra(Constants.TAG_LON, 0);
                sendLocationToServer("exchange", "share_location", "", lat, lng);
            }
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

    /**
     * class for upload Image to Server
     */
    @SuppressLint("StaticFieldLeak")
    class UploadImage extends AsyncTask<String, String, String> {
        String jsonResponse = "", status, imageName = "", mFrom, message = "";
        Context mContext;
        ImageView mUserImage;
        long mUnixTimeStamp;
        ChatResponse.Chat chat;
        String existingFileName = null;
        JSONObject jsonobject = null;

        UploadImage(Context context, String from, long unixTimeStamp, ChatResponse.Chat chat) {
            mContext = context;
            mFrom = from;
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

            } catch (MalformedURLException ex) {
                Log.e(TAG, "MediaPlayer-error: " + ex.getMessage(), ex);
            } catch (IOException ioe) {
                Log.e(TAG, "MediaPlayer-error: " + ioe.getMessage(), ioe);
            }

            Log.d(TAG, "uploadImageResponse: " + jsonResponse);
            try {
                jsonobject = new JSONObject(jsonResponse);
                status = jsonobject.getString(Constants.TAG_STATUS);
                if (jsonobject.has("message")) {
                    message = jsonobject.getString("message");
                }

                if (status.equals("true")) {
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

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return jsonResponse;
        }

        @Override
        protected void onPreExecute() {
            pd.show();
        }

        @Override
        protected void onPostExecute(String res) {
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
            try {
                if (status.equals(Constants.TAG_TRUE)) {
                    JSONObject image = jsonobject.getJSONObject("Image");
                    callSocket(mUnixTimeStamp, "image", image.getString("View_url"));
                    sendChat("image", "", imageName);

                    runOnUiThread(new Runnable() {
                        public void run() {
                            ChatResponse.Message message = chat.getMessage();
                            message.setMessage(imageName);
                            chatAdapter.notifyDataSetChanged();
                            if (chats.size() > 0) {
                                listView.setSelection(chats.size() - 1);
                            }
                        }
                    });
                } else if (status.equalsIgnoreCase("false")) {
                    if (message.equalsIgnoreCase("Image cannot be uploaded")) {
                        pd.dismiss();
                        JoysaleApplication.uploadAlertDialog(ExchangeView.this, getString(R.string.alert), getString(R.string.image_cannot_moderator));

                    }

                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Class for Download Image from Server and Store in External Storage
     **/

    class DownloadAndStoreImg extends AsyncTask<Void, Void, Void> {
        String mType, mImageUrl, mTimeStamp;

        public DownloadAndStoreImg(String type, String imageUrl, String timeStamp) {
            mType = type;
            mImageUrl = imageUrl;
            mTimeStamp = timeStamp;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (mType.equals("image")) {
                if (mImageUrl != null && !mImageUrl.equals("")) {
                    Bitmap image = imageStorage.downloadImage(mImageUrl);
                    if (image != null) {
                        ImageStorage imageStorage = new ImageStorage(ExchangeView.this);
                        String imageName = AppUtils.getImageName(mImageUrl);
                        //Store Images outside a Sent Folder
                        if (!imageStorage.checkIfImageExists("", imageName)) {
                            imageStorage.saveToAppDir(image, "", imageName, mTimeStamp);
                        }
                    }
                }
            }
            return null;
        }
    }

    /**
     * Function for get the chatid between two users
     **/

    private void getChatId() {
        Map<String, String> map = new HashMap<String, String>();
        map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
        map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
        map.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
        map.put(Constants.TAG_RECEIVER_ID, exchangeData.getExchangerId());
        Call<HashMap<String, String>> call = apiInterface.getChatId(map);
        call.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                if (response.isSuccessful()) {
                    if (response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                        chatId = response.body().get(Constants.TAG_CHAT_ID);
                    } else if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase(Constants.TAG_FALSE_CODE)) {
                        JoysaleApplication.disabledialog(ExchangeView.this, "" + response.body().get(Constants.TAG_MESSAGE), GetSet.getUserId());
                    }
                }
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
                topProgress.setVisibility(View.GONE);
                progress.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                call.cancel();
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
                topProgress.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
            }
        });
    }

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

    /**
     * Function for get the last conversation
     **/

    private void getChat(final int pageCount) {
        Map<String, String> map = new HashMap<String, String>();
        int offset = (pageCount * 20);
        map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
        map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
        map.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
        map.put(Constants.TAG_RECEIVER_ID, exchangeData.getExchangerId());
        map.put(Constants.TAG_TYPE, "exchange");
        map.put(Constants.TAG_OFFSET, Integer.toString(offset));
        map.put(Constants.TAG_LIMIT, "20");
        map.put(Constants.TAG_SOURCE_ID, exchangeData.getExchangeId());
        map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(getApplicationContext()));

        Call<ChatResponse> call = apiInterface.getChat(map);
        call.enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, retrofit2.Response<ChatResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        stringChatURL = response.body().getChatUrl();
                        chatId = response.body().getChatId();
                        tempAry.clear();
                        tempAry.addAll(response.body().getChats().getChats());
                        Collections.reverse(tempAry);
                        ArrayList<ChatResponse.Chat> backup = new ArrayList<>();
                        backup.addAll(chats);
                        chats.clear();
                        chats.addAll(tempAry);
                        chats.addAll(backup);
                        try {
                            if (chats.size() == 0) {
                                listView.setOnScrollListener(null);
                            } else {
                                listView.setOnScrollListener(ExchangeView.this);
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
                                    if (chats.size() > 18) {
                                        if (tempAry.size() == 0) {
                                            nullText.setVisibility(View.VISIBLE);
                                            topProgress.setVisibility(View.GONE);
                                        }
                                    }
                                    loading = false;
                                    topProgress.setVisibility(View.GONE);
                                    listView.setVisibility(View.VISIBLE);
                                    progress.setVisibility(View.GONE);
                                    chatAdapter.notifyDataSetChanged();
                                    if (pd != null && pd.isShowing()) {
                                        pd.dismiss();
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (pd != null && pd.isShowing()) {
                                pd.dismiss();
                            }
                        }
                    } else if (response.body().getStatus().equalsIgnoreCase("error")) {
                        JoysaleApplication.disabledialog(ExchangeView.this, response.body().getMessage(), GetSet.getUserId());
                    } else if (response.body().getStatus().equalsIgnoreCase(Constants.TAG_FALSE_CODE)) {
                        JoysaleApplication.disabledialog(ExchangeView.this, "" + response.body().getMessage(), GetSet.getUserId());
                    } else {
                        listView.setVisibility(View.VISIBLE);
                        getChatId();
                    }
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                call.cancel();
            }
        });
    }

    /**
     * Function for Initialize chat
     **/

    private void initializeChatUI() {
        loading = true;
        if (pulldown) {
            listView.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
            pulldown = false;
            topProgress.setVisibility(View.GONE);
        } else {
            listView.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.VISIBLE);
        }
    }

    /**
     * adapter for list the conversation in listview
     **/


    public class ChatAdapter extends BaseAdapter {
        ArrayList<ChatResponse.Chat> Items;
        ViewHolder holder = null;
        Context mContext;

        public ChatAdapter(Context ctx, ArrayList<ChatResponse.Chat> data) {
            mContext = ctx;
            Items = data;
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
                holder.date = (TextView) convertView.findViewById(R.id.date);
                holder.leftMsg = (TextView) convertView.findViewById(R.id.leftMsg);
                holder.rightMsg = (TextView) convertView.findViewById(R.id.rightMsg);
                holder.leftTime = (TextView) convertView.findViewById(R.id.leftTime);
                holder.rightTime = (TextView) convertView.findViewById(R.id.rightTime);
                holder.dateLay = (RelativeLayout) convertView.findViewById(R.id.dateLay);
                holder.leftLay = (RelativeLayout) convertView.findViewById(R.id.leftLay);
                holder.rightLay = (RelativeLayout) convertView.findViewById(R.id.rightLay);
                holder.itemLay = (RelativeLayout) convertView.findViewById(R.id.itemLay);
                holder.itemName = (TextView) convertView.findViewById(R.id.itemName);
                holder.aboutDate = (TextView) convertView.findViewById(R.id.aboutDate);
                holder.aboutMsg = (TextView) convertView.findViewById(R.id.aboutMsg);
                holder.itemImage = (ImageView) convertView.findViewById(R.id.itemImage);
                holder.price = (TextView) convertView.findViewById(R.id.price);
                holder.rightImage = (ImageView) convertView.findViewById(R.id.right_image);
                holder.leftImage = (ImageView) convertView.findViewById(R.id.left_image);
                holder.left_image_lay = (RelativeLayout) convertView.findViewById(R.id.left_image_lay);
                holder.right_image_lay = (RelativeLayout) convertView.findViewById(R.id.right_image_lay);
                holder.left_msg_layout = (RelativeLayout) convertView.findViewById(R.id.left_msg_layout);
                holder.right_msg_layout = (RelativeLayout) convertView.findViewById(R.id.right_msg_layout);
                holder.leftImgTime = (TextView) convertView.findViewById(R.id.leftImgTime);
                holder.rightImgTime = (TextView) convertView.findViewById(R.id.rightImgTime);
                holder.leftDelete = (ImageView) convertView.findViewById(R.id.leftDelete);
                holder.rightDelete = (ImageView) convertView.findViewById(R.id.rightDelete);
                holder.translateBtn = convertView.findViewById(R.id.translatebtn);
                holder.rightgiflay = convertView.findViewById(R.id.right_gifView);
                holder.leftgiflay = convertView.findViewById(R.id.left_gifView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final ChatResponse.Chat chat = Items.get(position);
            holder.leftLay.setVisibility(View.GONE);
            holder.rightLay.setVisibility(View.GONE);
            holder.dateLay.setVisibility(View.GONE);
            holder.itemLay.setVisibility(View.GONE);

            try {
                long date = Long.parseLong(chat.getMessage().getChatTime()) * 1000;
                String chatDate = JoysaleApplication.getDate(date);
                switch (chat.getType()) {
                    case "message":
                    case "normal":
                        if (chat.getSender().equals(GetSet.getUserName())) {
                            holder.rightLay.setVisibility(View.VISIBLE);
                            holder.right_msg_layout.setVisibility(View.VISIBLE);
                            holder.right_image_lay.setVisibility(View.GONE);
                            holder.rightDelete.setVisibility(View.GONE);
                            holder.rightgiflay.setVisibility(View.GONE);
                            holder.rightMsg.setText(chat.getMessage().getMessage());
                            holder.rightTime.setText(getTime(Long.parseLong(Items.get(position).getMessage().getChatTime()) * 1000));

                        } else {
                            holder.leftLay.setVisibility(View.VISIBLE);
                            holder.left_msg_layout.setVisibility(View.VISIBLE);
                            holder.left_image_lay.setVisibility(View.GONE);
                            holder.leftgiflay.setVisibility(View.GONE);
                            holder.leftDelete.setVisibility(View.GONE);
                            holder.translateBtn.setVisibility(View.GONE);
                            holder.leftMsg.setText(chat.getMessage().getMessage());
                            holder.leftTime.setText(getTime(Long.parseLong(Items.get(position).getMessage().getChatTime()) * 1000));
                        }
                        break;

                    case "image":
                        if (chat.getSender().equals(GetSet.getUserName())) {//Right Side
                            holder.rightLay.setVisibility(View.VISIBLE);
                            holder.right_msg_layout.setVisibility(View.GONE);
                            holder.right_image_lay.setVisibility(View.VISIBLE);

                            int imgSize = JoysaleApplication.dpToPx(mContext, 150);
                            if (imageStorage.checkIfImageExists("sent", chat.getMessage().getMessage())) {
                                if (imageStorage.checkIfImageExists("sent", chat.getMessage().getMessage())) {
                                    File file = new File(String.valueOf(imageStorage.getImage("sent", chat.getMessage().getMessage())));
                                    Picasso.get()
                                            .load(file).resize(imgSize, imgSize)
                                            .centerCrop().tag(mContext)
                                            .error(R.drawable.applogo)
                                            .into(holder.rightImage);
                                }
                            } else {
                                Picasso.get()
                                        .load(chat.getMessage().getUploadImage())
                                        .resize(imgSize, imgSize)
                                        .centerCrop().tag(mContext)
                                        .error(R.drawable.applogo)
                                        .into(holder.rightImage);
                            }
                        } else {
                            //left
                            holder.leftLay.setVisibility(View.VISIBLE);
                            holder.left_msg_layout.setVisibility(View.GONE);
                            holder.left_image_lay.setVisibility(View.VISIBLE);

                            int imgSize = JoysaleApplication.dpToPx(mContext, 150);
                            String imageName = AppUtils.getImageName(chat.getMessage().getUploadImage());
                            if (imageStorage.checkIfImageExists("", imageName)) {
                                Log.i(TAG, "getView: " + "local");
                                File file = imageStorage.getImage("", imageName);
                                Picasso.get()
                                        .load(file).resize(imgSize, imgSize)
                                        .centerCrop().tag(mContext)
                                        .error(R.drawable.applogo)
                                        .into(holder.leftImage);
                                holder.leftImgTime.setText(getTime(Long.parseLong(Items.get(position).getMessage().getChatTime()) * 1000));
                            } else {
                                Log.i(TAG, "getView: " + "remote");
                                Picasso.get()
                                        .load(AppUtils.getValidUrl(chat.getMessage().getUploadImage()))
                                        .resize(imgSize, imgSize).centerCrop()
                                        .error(R.drawable.applogo)
                                        .tag(mContext).into(holder.leftImage);
                            }
                        }
                        holder.rightImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(ExchangeView.this, WRITE_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(ExchangeView.this, new String[]{WRITE_EXTERNAL_STORAGE}, 100);
                                } else {
                                    if (imageStorage.checkIfImageExists("sent", chat.getMessage().getMessage())) {
                                        File file = imageStorage.getImage("sent", chat.getMessage().getMessage());
                                        Intent intent = new Intent(ExchangeView.this, ViewFullImage.class);
                                        intent.putExtra(Constants.IMAGETYPE, "local");
                                        intent.putExtra(Constants.TAG_FROM, "sent");
                                        intent.putExtra(Constants.KEY_IMAGE, file.getAbsolutePath());
                                        Pair<View, String> bodyPair = Pair.create(view, file.getAbsolutePath());
                                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ExchangeView.this, bodyPair);
                                        ActivityCompat.startActivity(ExchangeView.this, intent, options.toBundle());
                                    } else {
                                        Intent intent = new Intent(ExchangeView.this, ViewFullImage.class);
                                        intent.putExtra(Constants.IMAGETYPE, "remote");
                                        intent.putExtra(Constants.TAG_FROM, "sent");
                                        intent.putExtra(Constants.KEY_IMAGE, chat.getMessage().getUploadImage());
                                        Pair<View, String> bodyPair = Pair.create(view, chat.getMessage().getUploadImage());
                                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ExchangeView.this, bodyPair);
                                        ActivityCompat.startActivity(ExchangeView.this, intent, options.toBundle());
                                    }
                                }
                            }
                        });

                        holder.leftImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (SDK_INT < Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(ExchangeView.this, WRITE_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(ExchangeView.this, new String[]{WRITE_EXTERNAL_STORAGE}, 100);
                                } else {
                                    if (imageStorage.checkIfImageExists("", chat.getMessage().getMessage())) {
                                        Log.v(TAG, "Already Downloaded");
                                        File file = imageStorage.getImage("", chat.getMessage().getMessage());
                                        Intent intent = new Intent(ExchangeView.this, ViewFullImage.class);
                                        intent.putExtra(Constants.IMAGETYPE, "local");
                                        intent.putExtra(Constants.TAG_FROM, "");
                                        intent.putExtra(Constants.KEY_IMAGE, file.getAbsolutePath());
                                        Pair<View, String> bodyPair = Pair.create(view, file.getAbsolutePath());
                                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ExchangeView.this, bodyPair);
                                        ActivityCompat.startActivity(ExchangeView.this, intent, options.toBundle());
                                    } else {
                                        //Instant Received Image
                                        Intent intent = new Intent(ExchangeView.this, ViewFullImage.class);
                                        intent.putExtra(Constants.IMAGETYPE, "remote");
                                        intent.putExtra(Constants.TAG_FROM, "");
                                        intent.putExtra(Constants.KEY_IMAGE, chat.getMessage().getUploadImage());
                                        Pair<View, String> bodyPair = Pair.create(view, chat.getMessage().getUploadImage());
                                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ExchangeView.this, bodyPair);
                                        ActivityCompat.startActivity(ExchangeView.this, intent, options.toBundle());
                                    }
                                }
                            }
                        });
                        break;

                    case "share_location":
                        int imgSize = JoysaleApplication.dpToPx(ExchangeView.this, 150);
                        String latitude = chat.getMessage().getLatitude().trim();
                        String longitude = chat.getMessage().getLongitude().trim();

                        Log.d(TAG, "getViewLatLng: " + latitude + "Lon" + longitude);
                        int width = JoysaleApplication.dpToPx(ExchangeView.this, 280);
                        int height = JoysaleApplication.dpToPx(ExchangeView.this, 190);
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
                                    .build().url().toString();*/
                        } else {
                            mapImgUrl = LocationHelper.getStaticMapUrl(ExchangeView.this, latitude, longitude, width, height);
                        }
                        Log.d(TAG, "getViewMapbox: " + mapImgUrl);
                        if (chat.getSender().equals(GetSet.getUserName())) {//Right Side
                            holder.rightLay.setVisibility(View.VISIBLE);
                            holder.right_msg_layout.setVisibility(View.GONE);
                            holder.right_image_lay.setVisibility(View.VISIBLE);

                            Picasso.get().load(mapImgUrl).resize(imgSize, imgSize).centerCrop().tag(ExchangeView.this).into(holder.rightImage);
                            holder.rightImage.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //   AppUtils.callMap(chat.getMessage().getLatitude(), chat.getMessage().getLongitude(), ExchangeView.this);
                                    int res = 0;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        res = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                                    }
                                    int res1 = 0;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        res1 = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
                                    }
                                    if (res != PackageManager.PERMISSION_GRANTED && res1 != PackageManager.PERMISSION_GRANTED) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            locations.launch(PermissionsUtils.LOCATIONS);
                                        }
                                    } else {
                                        //Addon Mapbox
                                        if (Constants.ADDON_MAP_BOX_ENABLED) {
                                            AppUtils.callMap(chat.getMessage().getLatitude(), chat.getMessage().getLongitude(), ExchangeView.this);
                                        } else {
                                            AppUtils.callMap(chat.getMessage().getLatitude(), chat.getMessage().getLongitude(), ExchangeView.this);
                                        }
                                    }
                                }
                            });
                            holder.rightImgTime.setText(getTime(Long.parseLong(Items.get(position).getMessage().getChatTime()) * 1000));
                        } else {
                            holder.leftLay.setVisibility(View.VISIBLE);
                            holder.left_msg_layout.setVisibility(View.GONE);
                            holder.left_image_lay.setVisibility(View.VISIBLE);

                            holder.leftImage.setTag("location");
                            Picasso.get().load(mapImgUrl).resize(imgSize, imgSize).centerCrop().tag(mContext).into(holder.leftImage);
                            holder.leftImage.setOnClickListener(new OnClickListener() {
                                @RequiresApi(api = Build.VERSION_CODES.M)
                                @Override
                                public void onClick(View v) {
                                    //  AppUtils.callMap(chat.getMessage().getLatitude(), chat.getMessage().getLongitude(), ExchangeView.this);
                                    int res = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                                    int res1 = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
                                    if (res != PackageManager.PERMISSION_GRANTED || res1 != PackageManager.PERMISSION_GRANTED) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            locations.launch(PermissionsUtils.LOCATIONS);
                                        }
                                    } else {
                                        //Addon Mapbox
                                        if (Constants.ADDON_MAP_BOX_ENABLED) {
                                            AppUtils.callMap(chat.getMessage().getLatitude(), chat.getMessage().getLongitude(), ExchangeView.this);
                                        } else {
                                            AppUtils.callMap(chat.getMessage().getLatitude(), chat.getMessage().getLongitude(), ExchangeView.this);
                                        }
                                    }
                                }
                            });
                            holder.leftImgTime.setText(getTime(Long.parseLong(Items.get(position).getMessage().getChatTime()) * 1000));
                        }
                        break;

                    case "about":
                        if (chat.getItemStatus().equals("1")) {
                            holder.itemLay.setVisibility(View.VISIBLE);
                            holder.aboutMsg.setVisibility(View.VISIBLE);

                            Picasso.get().load(chat.getItemImage()).into(holder.itemImage);
                            String name = getString(R.string.about) + " " + "<font color='" + String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.colorPrimary))) + "'>" + chat.getItemTitle() + "</font>";
                            holder.itemName.setText(Html.fromHtml(name));
                            holder.aboutDate.setText(chatDate);
                            holder.aboutMsg.setText(JoysaleApplication.stripHtml(chat.getMessage().getMessage()));
                        } else {
                            holder.rightLay.setVisibility(View.VISIBLE);
                            holder.right_msg_layout.setVisibility(View.VISIBLE);
                            holder.right_image_lay.setVisibility(View.GONE);
                            holder.rightDelete.setVisibility(View.VISIBLE);

                            holder.rightMsg.setText(getString(R.string.product_removed_msg));
                            holder.rightTime.setText(getTime(Long.parseLong(Items.get(position).getMessage().getChatTime()) * 1000));
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

            } catch (
                    Exception e) {
                Log.e(TAG, "Exception in adapter=>" + e);
                e.printStackTrace();
            }

            return convertView;
        }


        private class ViewHolder {
            LinearLayout main;
            TextView message, itemName, aboutDate, price, aboutMsg, date, leftMsg, rightMsg, leftTime,
                    rightTime, leftImgTime, rightImgTime, translateBtn;
            RelativeLayout leftLay, rightLay, dateLay, itemLay, left_image_lay, right_image_lay, left_msg_layout, right_msg_layout;
            ImageView itemImage, leftImage, rightImage, leftDelete, rightDelete, rightgiflay, leftgiflay;
        }

    }

    //addons
    private boolean checkPermissions(String[] permissionList, ExchangeView activity) {
        boolean isPermissionsGranted = false;
        for (String permission : permissionList) {
            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
                isPermissionsGranted = true;
            } else {
                isPermissionsGranted = false;
                break;
            }
        }
        Log.d("newpermission:", "newpermission:" + isPermissionsGranted);
        return isPermissionsGranted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 124) {
            if (checkPermissions(permissions, ExchangeView.this)) {

            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRationale(permissions, ExchangeView.this)) {
                        ActivityCompat.requestPermissions(ExchangeView.this, permissions, 124);
                    } else {
                        openPermissionDialog("Location");
                    }
                }
            }
        } else if (requestCode == 100) {
            if (checkPermissions(permissions, ExchangeView.this)) {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                    ImagePicker.pickImage(this, "Select your image:");
                } else {
                    pickImage();
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRationale(permissions, ExchangeView.this)) {
                        ActivityCompat.requestPermissions(ExchangeView.this, permissions, 100);
                    }
                }
            }
        }
    }

    //addons
    private boolean shouldShowRationale(String[] permissions, ExchangeView activity) {
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
        permissionDialog = new Dialog(ExchangeView.this);
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

    /**
     * Fucntion for send the message to user
     **/

    private void sendChat(final String type, final String params, final String imageName) {
        Map<String, String> map = new HashMap<String, String>();
        long unixTime = System.currentTimeMillis() / 1000L;
        try {
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
            map.put(Constants.TAG_CHAT_ID, chatId);
            Log.d(TAG, "sendChat: "+exchangeData.getExchangeId());
            map.put(Constants.TAG_SOURCE_ID, exchangeData.getExchangeId());
            map.put(Constants.TAG_TYPE, type);
            map.put(Constants.TAG_CHAT_TYPE, "exchange");
            map.put(Constants.TAG_CREATED_DATE, Long.toString(unixTime));
            map.put(Constants.TAG_MESSAGE, params);
            map.put(Constants.TAG_IMAGE_URL, imageName);
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                public void run() {
                    chats.remove(chats.size() - 1);
                    chatAdapter.notifyDataSetChanged();
                    JoysaleApplication.dialog(ExchangeView.this, getString(R.string.alert), getString(R.string.symbols_not_supported));
                }
            });
        }
        Call<HashMap<String, String>> call = apiInterface.sendChat(map);
        call.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase(Constants.TAG_FALSE_CODE)) {
                    JoysaleApplication.disabledialog(ExchangeView.this, "" + response.body().get(Constants.TAG_MESSAGE), GetSet.getUserId());
                }
            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }

    /**
     * Function for change the status of exchanges
     **/

    private void changeStatus(final String status) {
        if (NetworkReceiver.isConnected()) {
            if (pd != null && !pd.isShowing()) pd.show();
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_EXCHANGEID, exchangeData.getExchangeId());
            map.put(Constants.TAG_STATUS, status);

            Call<HashMap<String, String>> call = apiInterface.changeExchangeStatus(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    if (pd.isShowing()) pd.dismiss();
                    if (response.isSuccessful()) {
                        if (response.body().get(Constants.TAG_STATUS).equals("true")) {
                            dialog(getString(R.string.success), getString(R.string.exchange_status_chngd));
                        } else if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase(Constants.TAG_FALSE_CODE)) {
                            JoysaleApplication.disabledialog(ExchangeView.this, "" + response.body().get(Constants.TAG_MESSAGE), GetSet.getUserId());
                        } else {
                            JoysaleApplication.dialog(ExchangeView.this, getString(R.string.alert), response.body().get(Constants.TAG_MESSAGE));
                        }
                    }
                    failed.setEnabled(true);
                    success.setEnabled(true);
                    success.setOnClickListener(ExchangeView.this);
                    failed.setOnClickListener(ExchangeView.this);
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    success.setOnClickListener(ExchangeView.this);
                    failed.setOnClickListener(ExchangeView.this);
                    if (pd.isShowing()) pd.dismiss();
                    call.cancel();
                }
            });
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {

    }

    /**
     * Method for send typing status to other end user
     **/

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Log.v(TAG, "on typing");
        if (runnable != null)
            handler.removeCallbacks(runnable);
        if (!meTyping) {
            meTyping = true;
            JSONObject jobj = new JSONObject();
            try {
                jobj.put(Constants.SOCK_SENDERID, userName.trim());
                jobj.put(Constants.SOCK_RECEIVERID, GetSet.getUserName().trim());
                jobj.put(Constants.SOCK_SOURCE_ID, exchangeData.getExchangeId());
                jobj.put(Constants.TAG_MESSAGE, Constants.TAG_TYPE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "onTextChanged: " + jobj);
            mSocket.emit("exmessageTyping", jobj);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        Log.v(TAG, "afterTextChanged");
        runnable = new Runnable() {

            public void run() {
                Log.v(TAG, "stop typing");
                meTyping = false;
                JSONObject jobj = new JSONObject();
                try {
                    jobj.put(Constants.SOCK_SENDERID, userName.trim());
                    jobj.put(Constants.SOCK_RECEIVERID, GetSet.getUserName().trim());
                    jobj.put(Constants.SOCK_SOURCE_ID, exchangeData.getExchangeId());
                    jobj.put(Constants.TAG_MESSAGE, "untype");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "afterTextChanged: " + jobj);
                mSocket.emit("exmessageTyping", jobj);
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    /**
     * On click Events
     */

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                fullName = "";
                disconnectSocket();
                finish();
                break;
            /*Location Share*/
            case R.id.sharelocation: {
                //Addon Mapbox
                if (Constants.ADDON_MAP_BOX_ENABLED) {
                  /*  Intent in = new Intent(ExchangeView.this, MainActivity.class);
                    in.putExtra(Constants.TAG_FROM, Constants.TAG_EXCHANGE);
                    in.putExtra(Constants.TAG_USERNAME, userName);
                    in.putExtra(Constants.TAG_USER_ID, exchangeData.getExchangerId());
                    in.putExtra(Constants.TAG_SOURCE_ID, exchangeData.getExchangeId());
                    in.putExtra(Constants.TAG_USERIMAGE_M, exchangeData.getExchangerImage());
                    in.putExtra(Constants.TAG_FULL_NAME, exchangeData.getExchangerName());
                    in.putExtra(Constants.CHATID, chatId);
                    in.putExtra(Constants.TAG_CHAT_TYPE, "exchange");
                    in.putExtra(Constants.TAG_EXCHANGEID, exchangeData.getExchangeId());
                    startActivityForResult(in,Constants.LOCATION_REQUEST_CODE);*/
                } else {
                    Intent in = new Intent(ExchangeView.this, LocationActivity.class);
                    in.putExtra(Constants.TAG_FROM, "chat");
                    in.putExtra(Constants.TAG_USERNAME, userName);
                    in.putExtra(Constants.TAG_USER_ID, exchangeData.getExchangerId());
                    in.putExtra(Constants.TAG_SOURCE_ID, exchangeData.getExchangeId());
                    in.putExtra(Constants.TAG_USERIMAGE_M, exchangeData.getExchangerImage());
                    in.putExtra(Constants.TAG_FULL_NAME, exchangeData.getExchangerName());
                    in.putExtra(Constants.CHATID, chatId);
                    in.putExtra(Constants.TAG_CHAT_TYPE, "exchange");
                    in.putExtra(Constants.TAG_EXCHANGEID, exchangeData.getExchangeId());
                    locationResult.launch(in);
                }
            }
            break;
            /*Image Share*/
            case R.id.shareImg:
            /*    if (ContextCompat.checkSelfPermission(ExchangeView.this, CAMERA) != PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(ExchangeView.this, WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ExchangeView.this, new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, 100);
                } else {
                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                        ImagePicker.pickImage(this, "Select your image:");
                    } else {
                        pickImage();
                    }
                }*/
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
                    Log.d(TAG, "onClick11111: ");
                    if (ContextCompat.checkSelfPermission(ExchangeView.this, READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)
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
                if (editText.getText().toString().trim().length() > 0) {
                    long unixTime = System.currentTimeMillis() / 1000L;
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

                    callSocket(unixTime, "text", editText.getText().toString().trim());

                    ChatResponse.Chat chat = new ChatResponse().new Chat();
                    HashMap<String, String> hmap = new HashMap<String, String>();
                    ChatResponse.Message message = new ChatResponse().new Message();
                    message.setMessage(editText.getText().toString().trim());
                    message.setChatTime("" + unixTime);
                    chat.setMessage(message);
                    chat.setSender(GetSet.getUserName());
                    chat.setType("message");
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
                    try {
                        sendChat("normal", editText.getText().toString().trim(), "");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    editText.setText("");
                    runOnUiThread(new Runnable() {
                        public void run() {
                            chatAdapter.notifyDataSetChanged();
                            if (chats.size() > 0) {
                                listView.setSelection(chats.size() - 1);
                            }
                        }
                    });
                } else {
                    editText.setError(getResources().getString(R.string.please_enter_message));
                }

                break;
            case R.id.failed:
                failed.setEnabled(false);
                failed.setOnClickListener(null);
                clickedBtn = "failed";

                String status = failed.getText().toString();
                if (status.equals(getString(R.string.failed))) {
                    changeStatus("failed");
                } else if (status.equals(getString(R.string.decline))) {
                    changeStatus("decline");
                }
                Log.v(TAG, "clicked");
                break;
            case R.id.success:
                success.setEnabled(false);
                success.setOnClickListener(null);

                clickedBtn = "success";

                String stat = success.getText().toString();
                Log.v(TAG, "clickedsucces" + stat);
                if (stat.equals(getString(R.string.success))) {
                    changeStatus("success");
                } else if (stat.equals(getString(R.string.accept))) {
                    changeStatus("accept");
                } else if (stat.equals(getString(R.string.cancel))) {
                    changeStatus("cancel");
                } else {
                    Log.v(TAG, "checkstatus" + stat);
                }
                break;
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
            }
            else{
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
         /*   Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
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
            pickerOptionsSheet.dismiss();*/

        });
        layoutGallery.setOnClickListener(v -> {
           /* Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            Intent pickIntent = new Intent(Intent.ACTION_PICK, collection);
            pickIntent.setType("image/jpeg");

            Intent chooserIntent = Intent.createChooser(pickIntent, "Select a picture");

            if (chooserIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(chooserIntent, REQUEST_CODE_PICK_IMAGE);
            }
            pickerOptionsSheet.dismiss();*/
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE){
                Log.d(TAG, "pickImage:11111111111111 ");
                /*ArrayList<String> testUris = new ArrayList<>();
                testUris.add("content://Phone/Download/4wy10xithu681.jpg");*/
//                openGalleryFragment(selectedImageUris);

                Intent in = new Intent(ExchangeView.this, AlbumSelectActivity.class);
                in.putExtra("from","profile");
                in.putExtra("fromEditProfile", true);
                startActivityForResult(in, REQUEST_CODE);

                pickerOptionsSheet.dismiss();
            }
            else{
                 Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            Intent pickIntent = new Intent(Intent.ACTION_PICK, collection);
            pickIntent.setType("image/jpeg");

            Intent chooserIntent = Intent.createChooser(pickIntent, "Select a picture");

            if (chooserIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(chooserIntent, REQUEST_CODE_PICK_IMAGE);
            }
            pickerOptionsSheet.dismiss();
            }
        });

        pickerOptionsSheet.show();
    }


    private class RecyclerAdapter extends RecyclerView.Adapter<ExchangeView.Viewholder> {
        ArrayList<AdminDataResponse.ChatTemplate> items;

        public RecyclerAdapter(ArrayList<AdminDataResponse.ChatTemplate> templatMsgAry) {
            this.items = templatMsgAry;
        }

        @Override
        public ExchangeView.Viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_template_text, parent, false);
            return new ExchangeView.Viewholder(view);
        }

        @Override
        public void onBindViewHolder(ExchangeView.Viewholder holder, final int adapterPosition) {
            int position = holder.getAbsoluteAdapterPosition();
            Log.d(TAG, "onBindViewHolder: " + items);
            holder.templateMsg.setText(items.get(position).getName());
            holder.templateMsg.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessage(items.get(position).getName());
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

    private class Viewholder extends RecyclerView.ViewHolder {
        TextView templateMsg;

        public Viewholder(View itemView) {
            super(itemView);
            templateMsg = itemView.findViewById(R.id.templateMsg);
        }

    }


    private void sendMessage(String msg) {
        long unixTime = System.currentTimeMillis() / 1000L;
        callSocket(unixTime, "text", msg);
        ChatResponse.Message message = new ChatResponse().new Message();
        ChatResponse.Chat chat = new ChatResponse().new Chat();

        try {

            message.setMessage(msg);
            chat.setSender(GetSet.getUserName());
            message.setChatTime(Long.toString(unixTime));
            chat.setType("message");

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

            sendChat("normal", msg, "");

        } catch (Exception e) {
            e.printStackTrace();
        }
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
            map.put(Constants.TAG_SOURCE_ID, exchangeData.getExchangeId());
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
                            mSocket.emit("exmessage", jsonObject1);
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
                    JoysaleApplication.dialog(ExchangeView.this, getResources().getString(R.string.alert), getString(R.string.somethingwrong));
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
            jobj.put(Constants.SOCK_SENDERID, exchangeData.getExchangerUsername());
            jobj.put("offerId", "0");
            //if (getIntent().getStringExtra(Constants.TAG_SOURCE_ID) != null) {
                jobj.put(Constants.SOCK_SOURCE_ID, exchangeData.getExchangeId());
            //} else {
                //jobj.put(Constants.SOCK_SOURCE_ID, "0");
            //}
            Log.v(TAG, "sendLocationJSON=" + jobj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jobj;
    }
    private void initLauncher() {
        locations = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                boolean granted = true;
                Log.d(TAG, "onActivityResult: " + result);

                for (Map.Entry<String, Boolean> x : result.entrySet()) {
                    if (!x.getValue()) {
                        if (ContextCompat.checkSelfPermission(ExchangeView.this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(ExchangeView.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                    if (ContextCompat.checkSelfPermission(ExchangeView.this, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(ExchangeView.this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    }
                    Log.d(TAG, "requestPermissions: ");
                    for (Map.Entry<String, Boolean> x : result.entrySet()) {
                        if (!x.getValue()) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(ExchangeView.this, x.getKey())) {
                                locations.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                if (ContextCompat.checkSelfPermission(ExchangeView.this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(ExchangeView.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    PermissionsUtils.openPermissionDialog(ExchangeView.this, new OkayCancelCallback() {
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
                                    }, ExchangeView.this.getString(R.string.ask_for_locations));
                                }
                                break;
                            }
                        }
                    }
                }

            }
        });
    }


}