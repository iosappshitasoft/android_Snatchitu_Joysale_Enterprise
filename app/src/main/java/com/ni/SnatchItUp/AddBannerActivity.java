package com.ni.SnatchItUp;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_IMAGES;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.google.gson.Gson;
import com.ni.external.CustomEditText;
import com.ni.external.CustomTextView;
import com.ni.external.RoundedImageView;
import com.ni.external.imagepicker.ImagePicker;
import com.ni.external.multipleimageselect.models.Photo;
import com.ni.external.timessquare.CalendarCellDecorator;
import com.ni.external.timessquare.CalendarPickerView;
import com.ni.external.timessquare.DefaultDayViewAdapter;
import com.ni.helper.DateHelper;
import com.ni.helper.LocaleManager;
import com.ni.helper.NetworkReceiver;
import com.ni.helper.PermissionsUtils;
import com.ni.helper.callback.OkayCancelCallback;
import com.ni.helper.callback.OnButtonClick;
import com.ni.model.HomeItemResponse;
import com.ni.utils.ApiClient;
import com.ni.utils.ApiInterface;
import com.ni.utils.Constants;
import com.ni.utils.GetSet;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import okhttp3.HttpUrl;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddBannerActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = AddBannerActivity.class.getSimpleName();
    ApiInterface apiInterface;

    private RelativeLayout startDateLay, endDateLay;
    private RoundedImageView webBannerImage;
    private RoundedImageView addWebIcon;
    private ImageView btnDeleteWeb;
    private FrameLayout appBannerLay, webBannerLay;
    private RoundedImageView appBannerImage;
    private RoundedImageView addAppIcon;
    private ImageView btnDeleteApp;
    private CustomEditText edtWebBannerLink, edtAppBannerLink;
    private CustomTextView txtPerDay, txtBannerAmount, txtBannerValid, txtValidAmount;
    private CustomTextView txtStartDate;
    private CustomTextView txtEndDate, txtTitle, txtWebAlert, txtAppAlert;
    private ImageView iconEndCalendar, btnBack, iconStartCalendar;
    private Button btnPayNow;
    private CalendarPickerView calendarView;
    private String nonce;
    String from = "", appBannerPath = "", webBannerPath = "", currencySymbol, currencyCode,
            currencyMode, currencyPosition, formattedPriceDay;
    private Date startDate, endDate;
    public boolean isSelectWeb = false, isSelectApp = false;
    private DatePickerDialog startDatePicker, endDatePicker;
    private Date minEndDate = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
    double pricePerDay, totalPrice;
    public static final int WEB_BANNER_REQUEST_CODE = 500;
    public static final int APP_BANNER_REQUEST_CODE = 501;
    public static final int WEB_BANNER_MAX_WIDTH = 1920;
    public static final int WEB_BANNER_MAX_HEIGHT = 400;

//    public static final int WEB_BANNER_MAX_WIDTH = 2560;
//    public static final int WEB_BANNER_MAX_HEIGHT = 674;
    public static final int APP_BANNER_MAX_WIDTH = 1024;
    public static final int APP_BANNER_MAX_HEIGHT = 500;
    private String webBannerImageURL, appBannerImageURL, uploadResponse, clientToken;
    static final int DROP_IN_REQUEST = 100;
    Context context;
    DateHelper dateHelper;
    private NumberFormat numberFormat;
    private Locale usLocale;
    private ActivityResultLauncher<String[]> galleryResultLauncher;
    private ActivityResultLauncher<Intent> galleryLauncherResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_banner);
        context = this;
        dateHelper = new DateHelper(context);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        initLauncher();
        initView();
    }

    private void initView() {

        btnBack = findViewById(R.id.backbtn);
        txtTitle = findViewById(R.id.title);
        startDateLay = (RelativeLayout) findViewById(R.id.startDateLay);
        endDateLay = (RelativeLayout) findViewById(R.id.endDateLay);
        webBannerLay = findViewById(R.id.webBannerLay);
        webBannerImage = (RoundedImageView) findViewById(R.id.webBannerImage);
        addWebIcon = (RoundedImageView) findViewById(R.id.addWebIcon);
        btnDeleteWeb = (ImageView) findViewById(R.id.btnDeleteWeb);
        appBannerLay = findViewById(R.id.appBannerLay);
        appBannerImage = (RoundedImageView) findViewById(R.id.appBannerImage);
        addAppIcon = (RoundedImageView) findViewById(R.id.addAppIcon);
        btnDeleteApp = (ImageView) findViewById(R.id.btnDeleteApp);
        edtWebBannerLink = (CustomEditText) findViewById(R.id.edtWebBannerLink);
        edtAppBannerLink = (CustomEditText) findViewById(R.id.edtAppBannerLink);
        txtBannerAmount = findViewById(R.id.txtBannerAmount);
        txtPerDay = findViewById(R.id.txtPerDay);
        txtBannerValid = findViewById(R.id.txtBannerValid);
        txtValidAmount = findViewById(R.id.txtValidAmount);
        txtStartDate = (CustomTextView) findViewById(R.id.txtStartDate);
        iconStartCalendar = (ImageView) findViewById(R.id.iconStartCalendar);
        txtEndDate = (CustomTextView) findViewById(R.id.txtEndDate);
        txtAppAlert = (CustomTextView) findViewById(R.id.txtAppAlert);
        txtWebAlert = (CustomTextView) findViewById(R.id.txtWebAlert);
        iconEndCalendar = (ImageView) findViewById(R.id.iconEndCalendar);
        btnPayNow = (Button) findViewById(R.id.btnPayNow);
        calendarView = findViewById(R.id.calendarView);
        Typeface normalTF, boldTF;
        normalTF = Typeface.createFromAsset(this.getAssets(), "font_regular.ttf");
        boldTF = Typeface.createFromAsset(this.getAssets(), "font_bold.ttf");
        calendarView.setDateTypeface(normalTF);
        calendarView.setTitleTypeface(boldTF);

        pricePerDay = getIntent().getDoubleExtra(Constants.TAG_PRICE, 0);
        formattedPriceDay = getIntent().getStringExtra(Constants.TAG_FORMATTED_PRICE);
        currencySymbol = getIntent().getStringExtra(Constants.TAG_CURRENCY_SYMBOL);
        currencyCode = getIntent().getStringExtra(Constants.TAG_CURRENCY_CODE);
        currencyMode = getIntent().getStringExtra(Constants.TAG_CURRENCY_MODE);
        currencyPosition = getIntent().getStringExtra(Constants.TAG_CURRENCY_POSITION);
        usLocale = new Locale("en", "US");
        numberFormat = NumberFormat.getInstance(usLocale);
        totalPrice = 0;
        final Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);
        final Calendar lastYear = Calendar.getInstance();

        calendarView.setCustomDayView(new DefaultDayViewAdapter());
        calendarView.setDecorators(Collections.<CalendarCellDecorator>emptyList());
        calendarView.init(lastYear.getTime(), nextYear.getTime(), usLocale) //
                .inMode(CalendarPickerView.SelectionMode.SINGLE) //
                .withSelectedDate(new Date());

        txtTitle.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);
        txtTitle.setText(getString(R.string.add_banner));
        txtBannerAmount.setText(formattedPriceDay);
        setBannerValidation("0", currencySymbol, currencyCode, "0");
        btnPayNow.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        addWebIcon.setOnClickListener(this);
        webBannerLay.setOnClickListener(this);
        btnDeleteWeb.setOnClickListener(this);
        appBannerImage.setOnClickListener(this);
        addAppIcon.setOnClickListener(this);
        appBannerLay.setOnClickListener(this);
        btnDeleteApp.setOnClickListener(this);
        txtStartDate.setOnClickListener(this);
        iconStartCalendar.setOnClickListener(this);
        txtEndDate.setOnClickListener(this);
        iconEndCalendar.setOnClickListener(this);
        btnPayNow.setOnClickListener(this);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(AddBannerActivity.this, isConnected);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.backbtn:
                onBackPressed();
                break;
            case R.id.addWebIcon:
            case R.id.webBannerLay:
                if (checkStoragePermission()) {
                    Log.d(TAG, "onClick1111111: ");
                    if (!isSelectWeb) {
                        Log.d(TAG, "onClick2222222222: ");
                        isSelectWeb = true;
                        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.UPSIDE_DOWN_CAKE){
                            Log.d(TAG, "onClick: "+333333333);
                            /*Intent in = new Intent(AddBannerActivity.this, AlbumSelectActivity.class);
                            startActivityForResult(in, WEB_BANNER_REQUEST_CODE);*/
                            FilePickerBuilder.getInstance()
                                    .setMaxCount(1)
//                      .setActivityTheme(R.style.AppTheme)
//                      .setActivityTitle(getString(R.string.please_select_media))
                                    .enableVideoPicker(false)
                                    .enableImagePicker(true)
                                    .enableCameraSupport(false)
                                    .showGifs(false)
                                    .showFolderView(false)
                                    .enableSelectAll(false)
                                    .withOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                                    .pickPhoto(AddBannerActivity.this, galleryLauncherResult);
                        }
                        else{
                            Log.d(TAG, "onClick44444444: ");
                        ImagePicker.pickImage(AddBannerActivity.this, WEB_BANNER_REQUEST_CODE);
                    }
                }
                } else {
                    Log.d(TAG, "onClick11111111111111: ");
                    /*if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.UPSIDE_DOWN_CAKE)   {
                        galleryResultLauncher.launch(PermissionsUtils.READ_MEDIA_IMAGESPERMISSION14);
                    }
                    else */
                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU)   {
                        isSelectWeb = true;
                        galleryResultLauncher.launch(PermissionsUtils.READ_MEDIA_IMAGESPERMISSION);
                    }
                    else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, Constants.STORAGE_PERMISSION_CODE);
                    } else {
                        ActivityCompat.requestPermissions(this, Constants.STORAGE_PERMISSION, Constants.STORAGE_PERMISSION_CODE);
                    }
                }
                break;
            case R.id.btnDeleteWeb:
                webBannerImage.setVisibility(View.INVISIBLE);
                addWebIcon.setVisibility(View.VISIBLE);
                btnDeleteWeb.setVisibility(View.GONE);
                isSelectWeb = false;
                webBannerPath = "";
                break;
            case R.id.addAppIcon:
            case R.id.appBannerLay:
                if (checkStoragePermission()) {
                    if (!isSelectApp) {
                        isSelectApp = true;

                        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.UPSIDE_DOWN_CAKE){
                            Log.d(TAG, "onClick: "+333333333);
                            /*Intent in = new Intent(AddBannerActivity.this, AlbumSelectActivity.class);
                            startActivityForResult(in, WEB_BANNER_REQUEST_CODE);*/
                            FilePickerBuilder.getInstance()
                                    .setMaxCount(1)
//                      .setActivityTheme(R.style.AppTheme)
//                      .setActivityTitle(getString(R.string.please_select_media))
                                    .enableVideoPicker(false)
                                    .enableImagePicker(true)
                                    .enableCameraSupport(false)
                                    .showGifs(false)
                                    .showFolderView(false)
                                    .enableSelectAll(false)
                                    .withOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                                    .pickPhoto(AddBannerActivity.this, galleryLauncherResult);
                        }
                        else{
                            Log.d(TAG, "onClick44444444: ");
                            ImagePicker.pickImage(AddBannerActivity.this, APP_BANNER_REQUEST_CODE);
                        }

                    }
                } else {
                   if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU)   {
                       isSelectApp = true;
                        galleryResultLauncher.launch(PermissionsUtils.READ_MEDIA_IMAGESPERMISSION);
                    }
                   else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, Constants.STORAGE_PERMISSION_CODE);
                    }
                    else {
                        ActivityCompat.requestPermissions(this, Constants.STORAGE_PERMISSION, Constants.STORAGE_PERMISSION_CODE);
                    }
                }
                break;
            case R.id.btnDeleteApp:
                appBannerImage.setVisibility(View.INVISIBLE);
                addAppIcon.setVisibility(View.VISIBLE);
                btnDeleteApp.setVisibility(View.GONE);
                isSelectApp = false;
                appBannerPath = "";
                break;
            case R.id.txtStartDate:
            case R.id.iconStartCalendar:
//                openStartCalendar();
                openStartPicker();
                break;
            case R.id.txtEndDate:
            case R.id.iconEndCalendar:
//                openEndCalendar();
                openEndPicker();
                break;
            case R.id.btnPayNow:
                if (TextUtils.isEmpty("" + webBannerPath)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.add_a_web_banner), Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty("" + appBannerPath)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.add_a_app_banner), Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty("" + edtWebBannerLink.getText())) {
                    edtWebBannerLink.setError(getString(R.string.enter_link_for_web_banner));
                } else if (!checkValidURL("" + edtWebBannerLink.getText())) {
                    edtWebBannerLink.setError(getString(R.string.enter_valid_link));
                } else if (TextUtils.isEmpty(edtAppBannerLink.getText())) {
                    edtAppBannerLink.setError(getString(R.string.enter_link_for_app_banner));
                } else if (!checkValidURL("" + edtAppBannerLink.getText())) {
                    edtAppBannerLink.setError(getString(R.string.enter_valid_link));
                } else if (TextUtils.isEmpty(startDate != null ? dateHelper.getUTCFromTimeStamp(startDate.getTime()) : "")) {
                    txtStartDate.setError(getString(R.string.select_start_date));
                } else if (TextUtils.isEmpty(endDate != null ? dateHelper.getUTCFromTimeStamp(endDate.getTime()) : "")) {
                    txtEndDate.setError(getString(R.string.select_end_date));
                } else {
                    checkAvailability();
                }
                break;

        }
    }

    private void openStartPicker() {
        final Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);
        final Calendar lastYear = Calendar.getInstance();

        calendarView.setCustomDayView(new DefaultDayViewAdapter());
        calendarView.setDecorators(Collections.<CalendarCellDecorator>emptyList());
        if (startDate != null) {
            calendarView.init(lastYear.getTime(), nextYear.getTime(), usLocale) //
                    .inMode(CalendarPickerView.SelectionMode.SINGLE) //
                    .withSelectedDate(startDate);
        } else {
            calendarView.init(lastYear.getTime(), nextYear.getTime(), usLocale) //
                    .inMode(CalendarPickerView.SelectionMode.SINGLE) //
                    .withSelectedDate(new Date());
        }

        calendarView.setVisibility(View.VISIBLE);
        btnPayNow.setVisibility(View.GONE);
        calendarView.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {
                btnPayNow.setVisibility(View.VISIBLE);
                calendarView.setVisibility(View.GONE);
                Log.i(TAG, "onDateSelected: " + new Gson().toJson(date));
                txtStartDate.setError(null);
                startDate = date;
                minEndDate = date;
                txtStartDate.setText("" + dateHelper.timeStampToDate(startDate.getTime()));

                if (endDate != null) {
                    long days;
                    if (startDate.after(endDate)) {
                        txtEndDate.setText("");
                        endDate = null;
                        days = 0;
                        totalPrice = 0;
                    } else if (startDate.equals(endDate)) {
                        days = 1;
                        totalPrice = pricePerDay * days;
                    } else {
                        long diff = endDate.getTime() - startDate.getTime();
                        days = (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)) + 1; //Add 1 for selected date itself
                        totalPrice = pricePerDay * days;
                    }
                    setBannerValidation("" + days, currencySymbol, currencyCode, "" + totalPrice);
                }
            }

            @Override
            public void onDateUnselected(Date date) {

            }
        });
    }

    private void openEndPicker() {
        Calendar nextYear = Calendar.getInstance();
        nextYear.setTime(minEndDate);
        nextYear.add(Calendar.YEAR, 1);
        if (endDate != null) {
            if (startDate != null) {
                calendarView.init(startDate, nextYear.getTime(), usLocale) //
                        .inMode(CalendarPickerView.SelectionMode.SINGLE).withSelectedDate(endDate);
            } else {
                calendarView.init(minEndDate, nextYear.getTime(), usLocale) //
                        .inMode(CalendarPickerView.SelectionMode.SINGLE).withSelectedDate(endDate);
            }
        } else {
            calendarView.init(minEndDate, nextYear.getTime(), usLocale) //
                    .inMode(CalendarPickerView.SelectionMode.SINGLE).withSelectedDate(minEndDate);
        }

        calendarView.setVisibility(View.VISIBLE);
        btnPayNow.setVisibility(View.GONE);
        calendarView.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {
                Log.i(TAG, "onDateSelected: " + new Gson().toJson(date));
                btnPayNow.setVisibility(View.VISIBLE);
                calendarView.setVisibility(View.GONE);
                txtEndDate.setError(null);
                endDate = date;
                minEndDate = date;
                txtEndDate.setText("" + dateHelper.timeStampToDate(endDate.getTime()));
                if (startDate != null) {
                    long days;
                    if (startDate.equals(endDate)) {
                        days = 1;
                        totalPrice = pricePerDay * days;
                    } else {
                        long diff = endDate.getTime() - startDate.getTime();
                        days = (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)) + 1; //Add 1 for selected date itself
                        totalPrice = (pricePerDay * days);
                    }
                    setBannerValidation("" + days, currencySymbol, currencyCode, "" + totalPrice);
                }
            }

            @Override
            public void onDateUnselected(Date date) {

            }
        });
    }

    public boolean checkValidURL(String url) {
        if (!Patterns.WEB_URL.matcher(url).matches()) {
            return false;
        } else if (HttpUrl.parse(url) == null) {
            return false;
        } else if (!URLUtil.isValidUrl(url)) {
            return false;
        } else if (!url.substring(0, 7).contains("http://") & !url.substring(0, 8).contains("https://")) {
            return false;
        } else
            return true;
    }

    @Override
    public void onBackPressed() {
        if (calendarView.getVisibility() == View.VISIBLE) {
            btnPayNow.setVisibility(View.VISIBLE);
            calendarView.setVisibility(View.GONE);
        } else {
            JoysaleApplication.dialogOkCancel(AddBannerActivity.this, getString(R.string.discard), getString(R.string.discard_changes), new OnButtonClick() {
                @Override
                public void onOkClicked() {
                    finish();
                }

                @Override
                public void onCancelClicked() {

                }
            });
        }
    }


    private void checkAvailability() {
        Toast.makeText(this,"Working" , Toast.LENGTH_SHORT).show();
        if (NetworkReceiver.isConnected()) {
            showProgress();
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            hashMap.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            hashMap.put(Constants.TAG_USERID, GetSet.getUserId());
            hashMap.put(Constants.TAG_START_DATE, dateHelper.getUTCFromTimeStamp(startDate.getTime()));
            hashMap.put(Constants.TAG_END_DATE, dateHelper.getUTCFromTimeStamp(endDate.getTime()));

            Log.i(TAG, "checkAvailability: " + new Gson().toJson(hashMap));
            Call<ResponseBody> call = apiInterface.checkDateAvailability(hashMap);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    dismissProgress();
                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            if (jsonObject.getString(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                                new UploadBannerTask().execute();
                            } else {
                                try {
                                    if (jsonObject.has(Constants.TAG_NO_DATES)) {
                                        JSONArray jsonArray = jsonObject.getJSONArray(Constants.TAG_NO_DATES);
                                        StringBuilder stringBuilder = new StringBuilder();
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            stringBuilder.append(jsonArray.getString(i));
                                            if (i != jsonArray.length() - 1) {
                                                stringBuilder.append(",");
                                            }
                                        }
                                        Toast.makeText(getApplicationContext(), getString(R.string.not_available_dates) + " " + stringBuilder, Toast.LENGTH_LONG).show();
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    dismissProgress();
                }
            });
        }
    }



    private class UploadBannerTask extends AsyncTask<String, String, String> {
        String status, message = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress();
        }

        @Override
        protected String doInBackground(String... strings) {
            for (int i = 0; i < 2; i++) {
                HttpURLConnection conn = null;
                String response = "error";
                DataOutputStream dos = null;
                DataInputStream inStream = null;
                StringBuilder builder = new StringBuilder();
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1024 * 1024;
                String urlString = Constants.API_UPLOAD_IMAGE;
                Bitmap bitmapImage = null;

                String existingFileName;
                if (i == 0) existingFileName = webBannerPath;
                else existingFileName = appBannerPath;
                File file = new File(existingFileName);
                try {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    URL url = new URL(urlString);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                    dos = new DataOutputStream(conn.getOutputStream());
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data;name=\"type\"" + lineEnd);
                    dos.writeBytes(lineEnd);
                    if (i == 0)
                        dos.writeBytes(Constants.TAG_WEB);
                    else
                        dos.writeBytes(Constants.TAG_APP);
                    dos.writeBytes(lineEnd);

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"images\";filename=\""
                            + existingFileName + "\"" + lineEnd);
                    dos.writeBytes(lineEnd);

                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // Read file
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    while (bytesRead > 0) {
                        try {
                            dos.write(buffer, 0, bufferSize);
                        } catch (OutOfMemoryError e) {
                            e.printStackTrace();
                            Log.e(TAG, "doInBackground: " + e.getMessage());
                        }
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                    // Responses from the server (code and message)
                    int serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn.getResponseMessage();
                    System.out.println("Server Response Code " + " " + serverResponseCode);
                    System.out.println("Server Response Message " + serverResponseMessage);
                    fileInputStream.close();
                    dos.flush();

                    conn.getInputStream();
                    //for android InputStream is = connection.getInputStream();
                    InputStream is = conn.getInputStream();

                    int ch;
                    StringBuilder b = new StringBuilder();
                    while ((ch = is.read()) != -1) {
                        b.append((char) ch);
                    }

                    String responseString = b.toString();
                    dos.close();
                    dos = null;
                    uploadResponse = responseString;
                    Log.i(TAG, "doInBackground:responseString " + responseString);

                    JSONObject jsonObject = new JSONObject(uploadResponse);
                    if (jsonObject.has("message")) {
                        message = jsonObject.getString("message");
                    }
                    status = jsonObject.getString("status");
                    if (status.equals("true")) {
                        JSONObject banner = jsonObject.getJSONObject(Constants.TAG_BANNER);
                        if (banner.getString(Constants.TAG_TYPE).equals(Constants.TAG_WEB)) {
                            webBannerImageURL = banner.getString(Constants.TAG_WEB_BANNER_URL);
                        } else if (banner.getString(Constants.TAG_TYPE).equals(Constants.TAG_APP)) {
                            appBannerImageURL = banner.getString(Constants.TAG_APP_BANNER_URL);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dismissProgress();
            if (status.equalsIgnoreCase("true")) {

                    payStripe();

            } else if (status.equalsIgnoreCase("false")) {
                if (message.equalsIgnoreCase("Image cannot be uploaded")) {
                    JoysaleApplication.uploadAlertDialog(AddBannerActivity.this, getString(R.string.alert), getString(R.string.image_cannot_moderator));

                }

            }

        }

    }

    /*private void openPaymentDialog() {
        BottomSheetDialog paymentDialog;
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_dialog_payment, null);
        paymentDialog = new BottomSheetDialog(this, R.style.Bottom_FilterDialog); // Style here
        paymentDialog.setContentView(sheetView);
        FrameLayout btnPayPal = paymentDialog.findViewById(R.id.btnPayPal);
        FrameLayout btnStripe = paymentDialog.findViewById(R.id.btnStripe);

        btnPayPal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paymentDialog.dismiss();
                getClientToken();
            }
        });

        btnStripe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payStripe();
                paymentDialog.dismiss();
            }
        });

        paymentDialog.show();
    }*/

    private void payStripe() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
        hashMap.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
        hashMap.put(Constants.TAG_USERID, GetSet.getUserId());
        hashMap.put(Constants.TAG_CURRENCY_CODE, currencyCode);
        hashMap.put(Constants.TAG_PRICE, "" + totalPrice);
        hashMap.put(Constants.TAG_APP_BANNER_URL, appBannerImageURL);
        hashMap.put(Constants.TAG_WEB_BANNER_URL, webBannerImageURL);
        hashMap.put(Constants.TAG_WEB_BANNER_LINK, "" + edtWebBannerLink.getText());
        hashMap.put(Constants.TAG_APP_BANNER_LINK, "" + edtAppBannerLink.getText());
        hashMap.put(Constants.TAG_START_DATE, dateHelper.getUTCFromTimeStamp(startDate.getTime()));
        hashMap.put(Constants.TAG_END_DATE, dateHelper.getUTCFromTimeStamp(endDate.getTime()));
        hashMap.put(Constants.TAG_PAYMENT_TYPE, Constants.TAG_STRIPE);
       /* Intent intent = new Intent(AddBannerActivity.this, AddCardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(Constants.TAG_FROM, Constants.TAG_BANNER);
        intent.putExtra(Constants.TAG_REQUEST_DATA, hashMap);
        startActivityForResult(intent, Constants.PAYMENT_REQUEST_CODE);*/

        Intent intent = new Intent(AddBannerActivity.this, UpdateCardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(Constants.TAG_FROM, Constants.TAG_BANNER);
        intent.putExtra(Constants.TAG_REQUEST_DATA, hashMap);
        startActivityForResult(intent, Constants.PAYMENT_REQUEST_CODE);
    }



    protected boolean checkStoragePermission() {
        Log.d(TAG, "checkStoragePermission22222222222: ");
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return ContextCompat.checkSelfPermission(this,
                    Arrays.toString(PermissionsUtils.READ_MEDIA_IMAGESPERMISSION)) == PackageManager.PERMISSION_GRANTED;
        }
        else if (Build.VERSION.SDK_INT==Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this,
                    READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ContextCompat.checkSelfPermission(this,
                    READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.STORAGE_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (isSelectApp) {
                ImagePicker.pickImage(AddBannerActivity.this, APP_BANNER_REQUEST_CODE);
            } else if (isSelectWeb) {
                ImagePicker.pickImage(AddBannerActivity.this, WEB_BANNER_REQUEST_CODE);
            }
        } else {
          if(Build.VERSION.SDK_INT==Build.VERSION_CODES.UPSIDE_DOWN_CAKE){

          }
          else {
              Toast.makeText(getApplicationContext(), getString(R.string.need_permission_to_access), Toast.LENGTH_SHORT).show();
              Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
              Uri uri = Uri.fromParts("package", getPackageName(), null);
              intent.setData(uri);
              intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
              startActivity(intent);
          }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: "+requestCode+"   "+resultCode+"   "+data);

        switch (requestCode) {
            case WEB_BANNER_REQUEST_CODE:
            case APP_BANNER_REQUEST_CODE:
                String tempPath = "";
                Bitmap bitmap = null;
                if (resultCode == Activity.RESULT_OK) {

                    if (requestCode == WEB_BANNER_REQUEST_CODE && isSelectWeb) {
                        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.UPSIDE_DOWN_CAKE){
                            ArrayList<Photo> photoList = (ArrayList<Photo>) data.getExtras().get("galleryData");
                            Log.d(TAG, "onActivityResult: "+photoList);
                            for (Photo selectedImage : photoList) {
                                HomeItemResponse.Photo photo = new HomeItemResponse().new Photo();
                                photo.setType(selectedImage.getType());
                                photo.setImage("" + selectedImage.getImage());
                                photo.setPathType(Constants.TAG_GALLERY);

                                Log.e("selectedImage", "-" + selectedImage.getImage());
                                tempPath = selectedImage.getImage();
                                bitmap = BitmapFactory.decodeFile(tempPath);
                                Log.d(TAG, "onActivityResult222222: " + tempPath);
                            }

                        }
                        else {
                            tempPath  = ImagePicker.getBannerFilePath(AddBannerActivity.this, requestCode, resultCode, data, "isSelectWeb");
                            bitmap = BitmapFactory.decodeFile(tempPath);
                            Log.d(TAG, "onActivityResult: " + tempPath);
                            if (bitmap == null) {
                                InputStream is = null;
                                try {
                                    is = getContentResolver().openInputStream(data.getData());
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }

                                bitmap = BitmapFactory.decodeStream(is);
                            }
                        }

                        if (bitmap.getHeight() != WEB_BANNER_MAX_HEIGHT || bitmap.getWidth() != WEB_BANNER_MAX_WIDTH) {
                            isSelectWeb = false;
                            txtWebAlert.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                            Toast.makeText(context, String.format(getString(R.string.upload_banner_in_exact_size), WEB_BANNER_MAX_WIDTH, WEB_BANNER_MAX_HEIGHT), Toast.LENGTH_SHORT).show();
                        } else {
                            webBannerPath = tempPath;
                            Picasso.get()
                                    .load("file://" + webBannerPath)
                                    .into(webBannerImage);
                            webBannerImage.setVisibility(View.VISIBLE);
                            btnDeleteWeb.setVisibility(View.VISIBLE);
                            addWebIcon.setVisibility(View.GONE);
                            txtWebAlert.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.primaryText));
                        }

                    } else if (requestCode == APP_BANNER_REQUEST_CODE && isSelectApp) {
                         tempPath = ImagePicker.getBannerFilePath(AddBannerActivity.this, requestCode, resultCode, data, "isSelectApp");
                         bitmap = BitmapFactory.decodeFile(tempPath);
                        if (bitmap == null) {
                            InputStream is = null;
                            try {
                                is = getContentResolver().openInputStream(data.getData());
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }

                            bitmap = BitmapFactory.decodeStream(is);
                        }
                        if (bitmap.getHeight() != APP_BANNER_MAX_HEIGHT || bitmap.getWidth() != APP_BANNER_MAX_WIDTH) {
                            isSelectApp = false;
                            txtAppAlert.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                            Toast.makeText(context, String.format(getString(R.string.upload_banner_in_exact_size), APP_BANNER_MAX_WIDTH, APP_BANNER_MAX_HEIGHT), Toast.LENGTH_SHORT).show();
                        } else {
                            appBannerPath = tempPath;
                            Picasso.get()
                                    .load("file://" + appBannerPath)
                                    .into(appBannerImage);
                            btnDeleteApp.setVisibility(View.VISIBLE);
                            addAppIcon.setVisibility(View.GONE);
                            appBannerImage.setVisibility(View.VISIBLE);
                            txtAppAlert.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.primaryText));
                        }
                    } else {
                        if (isSelectWeb) isSelectWeb = false;
                        if (isSelectApp) isSelectApp = false;
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    if (isSelectWeb) isSelectWeb = false;
                    if (isSelectApp) isSelectApp = false;
                }
                break;

            case Constants.PAYMENT_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent(getApplicationContext(), AdHistoryActivity.class);
                    startActivity(intent);
                    finish();
                }
                break;
        }
    }

    private void openStartCalendar() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), usLocale);
        calendar.setTimeInMillis(System.currentTimeMillis());
        startDatePicker = new DatePickerDialog(AddBannerActivity.this, R.style.DatePickerTheme, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                txtStartDate.setError(null);
                Calendar newCalendar = Calendar.getInstance();
                newCalendar.set(Calendar.YEAR, year);
                newCalendar.set(Calendar.MONTH, monthOfYear);
                newCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                newCalendar.set(Calendar.HOUR, 0);
                newCalendar.set(Calendar.MINUTE, 0);
                newCalendar.set(Calendar.SECOND, 0);
                newCalendar.set(Calendar.MILLISECOND, 0);
                newCalendar.set(Calendar.AM_PM, 0);

                startDate = newCalendar.getTime();
                txtStartDate.setText("" + dateHelper.timeStampToDate(startDate.getTime()));
                minEndDate = startDate;

                if (endDate != null) {
                    long days;
                    if (startDate.after(endDate)) {
                        txtEndDate.setText("");
                        endDate = null;
                        days = 0;
                        totalPrice = 0;
                    } else if (startDate.equals(endDate)) {
                        days = 1;
                        totalPrice = pricePerDay * days;
                    } else {
                        long diff = endDate.getTime() - startDate.getTime();
                        days = (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)) + 1; //Add 1 for selected date itself
                        totalPrice = pricePerDay * days;
                    }
                    setBannerValidation("" + days, currencySymbol, currencyCode, "" + totalPrice);
                }
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        startDatePicker.getDatePicker().setMinDate(System.currentTimeMillis());
        startDatePicker.getDatePicker().getTouchables().get(1).performClick();
        startDatePicker.show();
    }

    public void openEndCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        endDatePicker = new DatePickerDialog(AddBannerActivity.this, R.style.DatePickerTheme, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                txtEndDate.setError(null);
                Calendar newCalendar = Calendar.getInstance();
                newCalendar.set(Calendar.YEAR, year);
                newCalendar.set(Calendar.MONTH, monthOfYear);
                newCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                newCalendar.set(Calendar.HOUR, 0);
                newCalendar.set(Calendar.MINUTE, 0);
                newCalendar.set(Calendar.SECOND, 0);
                newCalendar.set(Calendar.MILLISECOND, 0);
                newCalendar.set(Calendar.AM_PM, 0);

                endDate = newCalendar.getTime();
                txtEndDate.setText("" + dateHelper.timeStampToDate(endDate.getTime()));
                if (startDate != null) {
                    long days;
                    if (startDate.equals(endDate)) {
                        days = 1;
                        totalPrice = pricePerDay * days;
                    } else {
                        long diff = endDate.getTime() - startDate.getTime();
                        days = (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)) + 1; //Add 1 for selected date itself
                        totalPrice = (pricePerDay * days);
                    }
                    setBannerValidation("" + days, currencySymbol, currencyCode, "" + totalPrice);
                }
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        endDatePicker.getDatePicker().setMinDate(minEndDate.getTime());
        endDatePicker.getDatePicker().getTouchables().get(1).performClick();
        endDatePicker.show();
    }

    @Override
    protected void onDestroy() {
        LocaleManager.setNewLocale(this, LocaleManager.getLanguageCode(this), false);
        super.onDestroy();
    }

    private void setBannerValidation(String days, String currencySymbol, String currencyCode, String amount) {
        Spanned tempDescription, tempAmount;
        String formattedPrice;
        numberFormat.setMaximumFractionDigits(2);
        amount = numberFormat.format(Double.parseDouble(amount));
        if (currencyMode.equals("symbol")) {
            if (currencyPosition.equals("prefix")) {
                if (LocaleManager.isRTL(this)) {
                    formattedPrice = amount + " " + currencySymbol;
                } else {
                    formattedPrice = currencySymbol + " " + amount;
                }
            } else {
                if (LocaleManager.isRTL(this)) {
                    formattedPrice = currencySymbol + " " + amount;
                } else {
                    formattedPrice = amount + " " + currencySymbol;
                }
            }
        } else {
            if (currencyPosition.equals("prefix")) {
                if (LocaleManager.isRTL(this)) {
                    formattedPrice = amount + " " + currencyCode;
                } else {
                    formattedPrice = currencyCode + " " + amount;
                }
            } else {
                if (LocaleManager.isRTL(this)) {
                    formattedPrice = currencyCode + " " + amount;
                } else {
                    formattedPrice = amount + " " + currencyCode;
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tempDescription = Html.fromHtml(getString(R.string.you_ads_will_run_for) + " <b>" + days + "</b> " +
                    getString(R.string.days_you_will_spend_no_more_than), Html.FROM_HTML_MODE_LEGACY);

            tempAmount = Html.fromHtml(" <b>" + formattedPrice + "</b>", Html.FROM_HTML_MODE_LEGACY);
        } else {
            tempDescription = Html.fromHtml(getString(R.string.you_ads_will_run_for) + " <b>" + days + "</b> " +
                    getString(R.string.days_you_will_spend_no_more_than));

            tempAmount = Html.fromHtml(" <b>" + formattedPrice + "</b>");
        }
        txtBannerValid.setText(tempDescription);
        txtValidAmount.setText(tempAmount);
    }
    public void initLauncher() {
        galleryResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                boolean granted = true;
                boolean partialAccess = false;
                Log.d(TAG, "onActivityResult: " + result);
                for (Map.Entry<String, Boolean> x : result.entrySet()) {
                    if (!x.getValue()) granted = false;
                    if (ActivityCompat.shouldShowRequestPermissionRationale(AddBannerActivity.this, x.getKey())) {
                        partialAccess = true;
                    }
                }

                if (granted || partialAccess) {
                    Log.d(TAG, "onActivityResult11111111111: ");
/*
                    Intent in = new Intent(AddBannerActivity.this, AlbumSelectActivity.class);
                    in.putExtra("fromEditProfile", true);
                     startActivityForResult(in,WEB_BANNER_REQUEST_CODE);*/
                    FilePickerBuilder.getInstance()
                            .setMaxCount(1)
//                      .setActivityTheme(R.style.AppTheme)
//                      .setActivityTitle(getString(R.string.please_select_media))
                            .enableVideoPicker(false)
                            .enableImagePicker(true)
                            .enableCameraSupport(false)
                            .showGifs(false)
                            .showFolderView(false)
                            .enableSelectAll(false)
                            .withOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                            .pickPhoto(AddBannerActivity.this, galleryLauncherResult);

                } else {
                    boolean neverAsked = false;
                    Log.d(TAG, "requestPermissions: ");
                    for (Map.Entry<String, Boolean> x : result.entrySet()) {
                        if (!x.getValue()) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(AddBannerActivity.this, x.getKey())) {
                                galleryResultLauncher.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                PermissionsUtils.openPermissionDialog(AddBannerActivity.this, new OkayCancelCallback() {
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
                                }, AddBannerActivity.this.getString(R.string.read_media_image));
                            }
                            break;
                        }
                    }
                }

            }
        });

        galleryLauncherResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {

                            ArrayList<Uri> selectedPhotos = result.getData().getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA);
                            Log.d(TAG, "onActivityResult:222 "+selectedPhotos);
                            if (selectedPhotos != null && !selectedPhotos.isEmpty()) {
                                Log.d(TAG, "onActivityResult1: "+new Gson().toJson(selectedPhotos));
                                Uri selectedImageUri = selectedPhotos.get(0);
                                Log.d(TAG, "onActivityResult33333333: "+selectedImageUri);
                                if (selectedImageUri != null) {
                                    handleImageSelection(selectedImageUri);
                                } else {
                                    Toast.makeText(getApplicationContext(), getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                });
    }
    private void handleImageSelection(Uri selectedImageUri) {
        // Use ContentResolver to get the input stream from the URI
        Bitmap bitmap = null;
        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
            if (inputStream != null) {
                bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close(); // Close the InputStream after use
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the bitmap was successfully decoded
        if (bitmap == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "handleImageSize: "+bitmap.getHeight() +"     "+bitmap.getWidth());
        Log.d(TAG, "selectapp: "+isSelectApp+"  selectweb"+isSelectWeb);
        // Check dimensions and update UI
        if (isSelectWeb) {
            isSelectWeb = false;
            Log.d(TAG, "selectedWeb: "+isSelectWeb);
            if (bitmap.getHeight() != WEB_BANNER_MAX_HEIGHT || bitmap.getWidth() != WEB_BANNER_MAX_WIDTH) {

                txtWebAlert.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                Toast.makeText(context, String.format(getString(R.string.upload_banner_in_exact_size), WEB_BANNER_MAX_WIDTH, WEB_BANNER_MAX_HEIGHT), Toast.LENGTH_SHORT).show();
            } else {

                webBannerPath = getFilePathFromUri(selectedImageUri);
                // Use Picasso to load the image from the URI
                Picasso.get()
                        .load(selectedImageUri)
                        .into(webBannerImage);
                webBannerImage.setVisibility(View.VISIBLE);
                btnDeleteWeb.setVisibility(View.VISIBLE);
                addWebIcon.setVisibility(View.GONE);
                txtWebAlert.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.primaryText));
            }
        } else if (isSelectApp) {
            isSelectApp = false;
            Log.d(TAG, "SelectedApp: "+isSelectApp);
            if (bitmap.getHeight() != APP_BANNER_MAX_HEIGHT || bitmap.getWidth() != APP_BANNER_MAX_WIDTH) {

                txtAppAlert.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                Toast.makeText(context, String.format(getString(R.string.upload_banner_in_exact_size), APP_BANNER_MAX_WIDTH, APP_BANNER_MAX_HEIGHT), Toast.LENGTH_SHORT).show();
            } else {
                appBannerPath = getFilePathFromUri(selectedImageUri);
                // Use Picasso to load the image from the URI
                Picasso.get()
                        .load(selectedImageUri)
                        .into(appBannerImage);
                btnDeleteApp.setVisibility(View.VISIBLE);
                addAppIcon.setVisibility(View.GONE);
                appBannerImage.setVisibility(View.VISIBLE);
                txtAppAlert.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.primaryText));
            }
        }
        else {
            if (isSelectWeb) isSelectWeb = false;
            if (isSelectApp) isSelectApp = false;
            Log.d(TAG, "handleImageSelection11111111111111: ");
        }
    }
    private String getFilePathFromUri(Uri uri) {
        String filePath = "";
        if (uri != null) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                if (index != -1) {
                    filePath = cursor.getString(index);
                }
                cursor.close();
            }
        }
        return filePath;
    }
}
