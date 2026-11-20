package com.ni.SnatchItUp;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;
import static com.ni.utils.Constants.GIVINGAWAY;
import static com.ni.utils.Constants.LOCATION_PERMISSION_CODE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.gson.Gson;
import com.ni.camerax.CameraViewActivity;
import com.ni.external.GPSTracker;
import com.ni.helper.LocaleManager;
import com.ni.helper.MyData;
import com.ni.helper.NetworkReceiver;
import com.ni.helper.callback.OnButtonClick;
import com.ni.helper.location.LocationHelper;
import com.ni.helper.worker.GetLocationWorker;
//import com.ni.mapbox.MainActivity;
import com.ni.model.AddProductResponse;
import com.ni.model.AdminDataResponse;
import com.ni.model.BeforeAddResponse;
import com.ni.model.HomeItemResponse;
import com.ni.model.ReadMessageResponse;
import com.ni.utils.ApiClient;
import com.ni.utils.ApiInterface;
import com.ni.utils.AppUtils;
import com.ni.utils.Constants;
import com.ni.utils.GetSet;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddProductDetail extends BaseActivity implements View.OnClickListener {

    private static final String TAG = AddProductDetail.class.getSimpleName();
    // Widget Declaration
    public static LinearLayout bottomLay, instantLay, buynowLay, offerLay;
    public static RelativeLayout exchangeLay, conditionLay,shippingLay;
    public static TextView itemCondition, txtLocation, txtCategory;
    public static ImageView condArrow, locArrow, catArrow;
    public static SwitchCompat chatSwitch, exchangeSwitch, buySwitch, givingAwaySwitch;
    public static AddProductDetail activity;
    public static String location = "", locationmapbox = "";
    public static double lat, lng;
    private final ArrayList<String> currencyID = new ArrayList<String>();
    private final ArrayList<String> currencySpin = new ArrayList<String>();
    private final ArrayList<String> countryId = new ArrayList<String>();
    private final ArrayList<String> countryName = new ArrayList<String>();
    private final ArrayList<String> countryCode = new ArrayList<String>();
    private final ArrayList<BeforeAddResponse.Filters> filterList = new ArrayList<>();
    private final ArrayList<BeforeAddResponse.Filters> tempFilterList = new ArrayList<>();
    public boolean isCategoryChanged;
    public ImagesAdapter imagesAdapter;
    public int failedImageCount = 0;
    public ArrayList<HomeItemResponse.Photo> images = new ArrayList<>();
    public ArrayList<String> originalImagesName = new ArrayList<>();
    public ArrayList<String> removeAry = new ArrayList<String>();
    public HomeItemResponse.Item itemMap = new HomeItemResponse().new Item();
    // addons
    public String categoryType = "", categoryId = "", subCategoryId = "", childCategoryId = null, mcountryId = "",
            conditionName = "", conditionId;
    ImageView backBtn, cancelIcon;
    TextView title, cancel, post, promote, alert_title, uploadStatus, successText;
    EditText productName, price, paypalId, shippingFee, edtYoutube, productDes;
    Spinner currency;
    ProgressBar loadingProgress;
    RecyclerView imageList;
    LinearLayout parentLay, saveLay, priceLay;
    RelativeLayout uploadSuccessLay, imageLoadingLay, locationLay, categoryLay, givingLay, buyLay;
    AVLoadingIndicatorView loadingView, postProgress;
    ArrayAdapter currencyadapter;
    Dialog dialog;
    RecyclerView filterRecycler;
    FilterAdapter filterAdapter;
    // for categoryList
    ArrayList<BeforeAddResponse.Category> categoryList = new ArrayList<>();
    ArrayList<BeforeAddResponse.ProductCondition> conditionAry = new ArrayList<>();
    ArrayList<String> uploadedImage = new ArrayList<String>();
    boolean isValidPrice = false;
    String from = "", currencyid = "", productUrl = "", postPrice = "", posteditemId = "";
    int count;
    ApiInterface apiInterface;
    private BeforeAddResponse.Category category;
    private BeforeAddResponse.Subcategory subCategory;
    private BeforeAddResponse.ChildCategory childCategory;
    private String groupPosition;
    private int lastClickedPosition;
    private boolean isRTL = false;

    private static final float maxHeight = 1280.0f;
    private static final float maxWidth = 1280.0f;

    Spinner deliveryTypeSpinner;
    // Addon MapBox
    /*
     * private ReverseGeocodingSearchEngine reverseGeocoding;
     * private SearchRequestTask reverseSearchRequestTask;
     * private SearchCallback reverseSearchCallback = new SearchCallback() {
     * 
     * @Override
     * public void onResults(@NonNull List<? extends SearchResult> list, @NonNull
     * ResponseInfo responseInfo) {
     * if (list.isEmpty()) {
     * Log.i(TAG, "No reverse geocoding results");
     * } else {
     * String tempLocation =
     * MapboxLocationActivity.getFormattedAddress(list.get(0));
     * Log.d(TAG, "onResults: " + tempLocation);
     * location = tempLocation;
     * txtLocation.setText(tempLocation);
     * txtLocation.setTextColor(getResources().getColor(R.color.primaryText));
     * locArrow.setColorFilter(getResources().getColor(R.color.primaryText));
     * }
     * }
     * 
     * @Override
     * public void onError(@NonNull Exception e) {
     * e.printStackTrace();
     * }
     * };
     */

    // for converting full html format
    public static Spannable linkifyHtml(String html, int linkifyMask) {
        Spanned text = Html.fromHtml(html);
        URLSpan[] currentSpans = text.getSpans(0, text.length(), URLSpan.class);

        SpannableString buffer = new SpannableString(text);
        Linkify.addLinks(buffer, linkifyMask);

        for (URLSpan span : currentSpans) {
            int end = text.getSpanEnd(span);
            int start = text.getSpanStart(span);
            buffer.setSpan(span, start, end, 0);
        }
        return buffer;
    }

    public static boolean isYoutubeUrl(String youTubeURl) {
        boolean success;
        String pattern = "^(http(s)?:\\/\\/)?((w){3}.)?youtu(be|.be)?(\\.com)?\\/.+";
        // Not Valid youtube URL
        success = !youTubeURl.isEmpty() && youTubeURl.matches(pattern);
        return success;
    }

    private static int getImageRotation(final File imageFile) {

        ExifInterface exif = null;
        int exifRotation = 0;

        try {
            exif = new ExifInterface(imageFile.getPath());
            exifRotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (exif == null)
            return 0;
        else
            return exifToDegrees(exifRotation);
    }

    private static int exifToDegrees(int rotation) {
        if (rotation == ExifInterface.ORIENTATION_ROTATE_90)
            return 90;
        else if (rotation == ExifInterface.ORIENTATION_ROTATE_180)
            return 180;
        else if (rotation == ExifInterface.ORIENTATION_ROTATE_270)
            return 270;

        return 0;
    }

    private static Bitmap getBitmapRotatedByDegree(Bitmap bitmap, int rotationDegree) {
        Matrix matrix = new Matrix();
        matrix.preRotate(rotationDegree);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public static String getFileSize(long size) {
        if (size <= 0)
            return "0";

        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addproduct_detail);
        // Addon Mapbox
        // reverseGeocoding = MapboxSearchSdk.getReverseGeocodingSearchEngine();
        isRTL = LocaleManager.isRTL(AddProductDetail.this);
        lat = 0;
        lng = 0;

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        activity = this;
        isCategoryChanged = false;

        // Elements Initialization
        backBtn = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);
        cancel = (TextView) findViewById(R.id.cancel);
        post = (TextView) findViewById(R.id.post);
        productName = (EditText) findViewById(R.id.productName);
        price = (EditText) findViewById(R.id.price);
        currency = (Spinner) findViewById(R.id.currency);
        chatSwitch = (SwitchCompat) findViewById(R.id.chatSwitch);
        exchangeSwitch = (SwitchCompat) findViewById(R.id.exchangeSwitch);
        imageList = findViewById(R.id.imageList);
        parentLay = (LinearLayout) findViewById(R.id.parentLay);
        saveLay = (LinearLayout) findViewById(R.id.saveLay);
        loadingView = (AVLoadingIndicatorView) findViewById(R.id.progress);
        catArrow = (ImageView) findViewById(R.id.catArrow);
        condArrow = (ImageView) findViewById(R.id.condArrow);
        categoryLay = (RelativeLayout) findViewById(R.id.categoryLay);
        conditionLay = (RelativeLayout) findViewById(R.id.conditionLay);
        locationLay = (RelativeLayout) findViewById(R.id.locationLay);
        exchangeLay = (RelativeLayout) findViewById(R.id.exchangeLay);
        offerLay = findViewById(R.id.offerLay);
        txtLocation = (TextView) findViewById(R.id.location);
        itemCondition = (TextView) findViewById(R.id.itemCondition);
        txtCategory = (TextView) findViewById(R.id.category);
        locArrow = (ImageView) findViewById(R.id.locArrow);
        bottomLay = (LinearLayout) findViewById(R.id.bottomLay);
        instantLay = (LinearLayout) findViewById(R.id.instantLay);
        buySwitch = (SwitchCompat) findViewById(R.id.buySwitch);
        paypalId = (EditText) findViewById(R.id.paypalId);
        shippingFee = (EditText) findViewById(R.id.shippingFee);
        buynowLay = (LinearLayout) findViewById(R.id.buynowLay);
        givingAwaySwitch = (SwitchCompat) findViewById(R.id.givingAwaySwitch);
        priceLay = (LinearLayout) findViewById(R.id.priceLay);
        buyLay = findViewById(R.id.buyLay);
        givingLay = (RelativeLayout) findViewById(R.id.givingLay);
        shippingLay = (RelativeLayout) findViewById(R.id.shippingLay);
        filterRecycler = findViewById(R.id.filterRecycler);
        edtYoutube = findViewById(R.id.edtYoutube);
        productDes = findViewById(R.id.productDes);
        deliveryTypeSpinner = findViewById(R.id.deliveryTypeSpinner);

        String[] deliveryOptions = {
                getString(R.string.localmeetup),
                getString(R.string.buynoworshipping)
        };

        ArrayAdapter<String> deliveryAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                deliveryOptions
        );

        deliveryAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        deliveryTypeSpinner.setAdapter(deliveryAdapter);

        deliveryTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // Local Meetup: Buy Now is off
                    buySwitch.setChecked(false);
                    buynowLay.setVisibility(View.GONE);
                } else if (position == 1) {
                    // Buy Now / Shipping: Buy Now is on
                    buySwitch.setChecked(true);
                    buynowLay.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });



        productDes.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getId() == R.id.productDes) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_UP:
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });

        // Init Dialog
        dialog = new Dialog(AddProductDetail.this, R.style.PostDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.product_upload_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        promote = (TextView) dialog.findViewById(R.id.promote);
        alert_title = (TextView) dialog.findViewById(R.id.alert_title);
        uploadStatus = (TextView) dialog.findViewById(R.id.uploadStatus);
        cancelIcon = (ImageView) dialog.findViewById(R.id.cancelIcon);
        loadingProgress = (ProgressBar) dialog.findViewById(R.id.loadingProgress);
        postProgress = (AVLoadingIndicatorView) dialog.findViewById(R.id.postProgress);
        uploadSuccessLay = (RelativeLayout) dialog.findViewById(R.id.uploadSuccessLay);
        imageLoadingLay = (RelativeLayout) dialog.findViewById(R.id.imageLoadingLay);
        successText = (TextView) dialog.findViewById(R.id.success_txt);

        // Dialog elements listener
        promote.setOnClickListener(this);
        cancelIcon.setOnClickListener(this);

        productDes.addTextChangedListener(new addListenerForDes(this, productDes));
        /* Scroll EditText smooth */
        /*
         * productDes.setOnTouchListener(new View.OnTouchListener() {
         * 
         * @Override
         * public boolean onTouch(View view, MotionEvent motionEvent) {
         * if (productDes.hasFocus()) {
         * view.getParent().requestDisallowInterceptTouchEvent(true);
         * switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
         * case MotionEvent.ACTION_SCROLL:
         * view.getParent().requestDisallowInterceptTouchEvent(false);
         * return true;
         * }
         * }
         * return false;
         * }
         * });
         */

        from = getIntent().getExtras().getString("from");
        if (getIntent().hasExtra(Constants.TAG_LOCATION)) {
            locationmapbox = getIntent().getStringExtra(Constants.TAG_LOCATION);
        }
        itemMap = (HomeItemResponse.Item) getIntent().getExtras().get(Constants.TAG_DATA);
        filterAdapter = new FilterAdapter(getApplicationContext(), filterList);
        filterRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        filterRecycler.setAdapter(filterAdapter);
        filterAdapter.notifyDataSetChanged();

        title.setText(getString(R.string.add_your_stuff));
        if (itemMap != null) {
            images.clear();
            for (HomeItemResponse.Photo photo : itemMap.getPhotos()) {
                String imageurl = "";
                if (photo.getItemUrlMainOriginal() != null) {
                    imageurl = photo.getItemUrlMainOriginal();
                    photo.setType(Constants.KEY_URL);
                } else if (photo.getType() != null) {
                    imageurl = photo.getImage();
                }
                photo.setImage(imageurl);
                if (from != null && photo.getItemImage() != null) {
                    if (from.equals(Constants.TAG_EDIT)) {
                        originalImagesName.add(photo.getItemImage());
                    }
                }
                images.add(photo);
            }

        }

        // Elements visibility
        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        // Elements set listener
        backBtn.setOnClickListener(this);
        cancel.setOnClickListener(this);
        post.setOnClickListener(this);
        conditionLay.setOnClickListener(this);
        locationLay.setOnClickListener(this);
        categoryLay.setOnClickListener(this);
        // productName.addTextChangedListener(new addListenerOnTextChange(this,
        // productName));
        /*
         * edtDes.setOnTouchListener(new View.OnTouchListener() {
         * 
         * @Override
         * public boolean onTouch(View view, MotionEvent motionEvent) {
         * if (edtDes.hasFocus()) {
         * view.getParent().requestDisallowInterceptTouchEvent(true);
         * switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
         * case MotionEvent.ACTION_SCROLL:
         * view.getParent().requestDisallowInterceptTouchEvent(false);
         * return true;
         * }
         * }
         * return false;
         * }
         * });
         */

        if (isRTL) {
            isValidPrice = true;
            price.setFilters(
                    new InputFilter[] { new JoysaleApplication.DecimalDigitsInputFilter(SplashActivity.beforeDecimal,
                            SplashActivity.afterDecimal) });
            shippingFee.setFilters(
                    new InputFilter[] { new JoysaleApplication.DecimalDigitsInputFilter(SplashActivity.beforeDecimal,
                            SplashActivity.beforeDecimal) });
        } else {
            price.setFilters(
                    new InputFilter[] { new JoysaleApplication.DecimalDigitsInputFilter(SplashActivity.beforeDecimal,
                            SplashActivity.afterDecimal) });
            shippingFee.setFilters(
                    new InputFilter[] { new JoysaleApplication.DecimalDigitsInputFilter(SplashActivity.beforeDecimal,
                            SplashActivity.beforeDecimal) });
        }
        productName.setFilters(new InputFilter[] { JoysaleApplication.EMOJI_FILTER, new InputFilter.LengthFilter(70) });
        productDes.setFilters(new InputFilter[] { JoysaleApplication.EMOJI_FILTER });

        parentLay.setVisibility(View.GONE);
        saveLay.setVisibility(View.GONE);

        if (Constants.GIVINGAWAY) {
            givingLay.setVisibility(View.VISIBLE);
        } else {
            givingLay.setVisibility(View.GONE);
            if (offerLay.getVisibility() == View.GONE) {
                offerLay.setVisibility(View.VISIBLE);
            }
        }
        loadingView.setVisibility(View.VISIBLE);

        // for categoryList
        if (categoryList.size() > 0) {
            setAdminCondition(currencyID, currencySpin);
            Log.d(TAG, "onCreateadmin: " + currencyID.size());
            Log.d(TAG, "onCreateadminspin: " + currencySpin.size());

        } else {
            getCategories();
        }

        // for categoryList
        getCountries();

        if (!from.equals(Constants.TAG_EDIT)) {
            setLocationTxt();
            setImageAdapter();
        }

        JoysaleApplication.setupUI(AddProductDetail.this, parentLay);

        givingAwaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCreateOfferlay: " + isChecked);

                if (isChecked) {
                    if (GIVINGAWAY)
                        priceLay.setVisibility(View.GONE);

                    offerLay.setVisibility(View.GONE);
                    instantLay.setVisibility(View.VISIBLE);
                    if (buySwitch.isChecked()) {
                        buySwitch.setChecked(false);
                        buyLay.setVisibility(View.GONE);
                    } else {
                        buyLay.setVisibility(View.GONE);
                    }
                    buynowLay.setVisibility(View.GONE);
                } else {
                    priceLay.setVisibility(View.VISIBLE);
                    price.setText("0");
                    offerLay.setVisibility(View.VISIBLE);
                    Log.d(TAG, "onCreateOfferlay: " + "position2");
                    instantLay.setVisibility(View.VISIBLE);

                    if (Constants.BUYNOW) {
                        buyLay.setVisibility(View.VISIBLE);
                        if (buySwitch.isChecked()) {
                            buySwitch.setChecked(true);
                            buynowLay.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });

        buySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    buynowLay.setVisibility(View.VISIBLE);
                } else {
                    buynowLay.setVisibility(View.GONE);
                }
            }
        });

        if (isRTL) {
            catArrow.setRotation(180);
            condArrow.setRotation(180);
            locArrow.setRotation(180);
            txtLocation.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            txtCategory.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            itemCondition.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        } else {
            catArrow.setRotation(0);
            condArrow.setRotation(0);
            locArrow.setRotation(0);
            txtLocation.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            txtCategory.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            itemCondition.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        }
    }

    /**
     * Function for set current location from Gps
     **/
    private void setLocationTxt() {
        GPSTracker gps = new GPSTracker(AddProductDetail.this);
        if (lat == 0 && lng == 0) {
            if (gps.canGetLocation()) {
                if (JoysaleApplication.isNetworkAvailable(AddProductDetail.this)) {
                    lat = gps.getLatitude();
                    lng = gps.getLongitude();
                    if (Constants.ADDON_MAP_BOX_ENABLED) {
                      /*  MainActivity mainActivity = new MainActivity();
                        Executor executor = Executors.newSingleThreadExecutor();
                        mainActivity.reverseSearchforAddproduct(lat, lng, executor);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                location = mainActivity.getTempLocation();
                                txtLocation.setText(location);
                            }
                        }, 2000);*/

                    } else {
                        getGoogleLocation(lat, lng);
                    }
                }
            }
        }
    }

    /**
     * for converting lat, lon to address
     **/

    private void getGoogleLocation(double lat, double lng) {
        Data.Builder builder = new Data.Builder();
        builder.putDouble(Constants.TAG_LAT, lat);
        builder.putDouble(Constants.TAG_LON, lng);
        builder.putString(Constants.TAG_FROM, "");
        builder.putString(Constants.TAG_COUNTRY_CODE, "");

        OneTimeWorkRequest locationWorkRequest = new OneTimeWorkRequest.Builder(GetLocationWorker.class)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .setInputData(builder.build())
                .build();
        WorkManager workManager = WorkManager.getInstance(AddProductDetail.this);
        workManager.enqueue(locationWorkRequest);
        workManager.getWorkInfoByIdLiveData(locationWorkRequest.getId()).observe(AddProductDetail.this, workInfo -> {
            if (workInfo.getState().isFinished()) {
                if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                    location = workInfo.getOutputData().getString(Constants.TAG_LOCATION);
                    txtLocation.setText(location);
                    txtLocation.setTextColor(getResources().getColor(R.color.primaryText));
                    locArrow.setColorFilter(getResources().getColor(R.color.primaryText));
                }
            }
        });
    }

    // Addon Mapbox
    private void getMapBoxLocation(double lat, double lng) {/*
                                                             * final ReverseGeoOptions options = new
                                                             * ReverseGeoOptions.Builder(Point.fromLngLat(lng, lat))
                                                             * .limit(1)
                                                             * .build();
                                                             * 
                                                             * if (reverseSearchRequestTask != null) {
                                                             * reverseSearchRequestTask.cancel();
                                                             * }
                                                             * reverseSearchRequestTask =
                                                             * reverseGeocoding.search(options, reverseSearchCallback);
                                                             */
    }

    /**
     * Function for set already edited exchangeData
     **/
    private void setEditProducts() {
        Log.i(TAG, "setEditProducts: " + new Gson().toJson(itemMap));
        String htmlTitle = JoysaleApplication.stripHtml(itemMap.getItemTitle().trim());
        String htmlDescription = JoysaleApplication.stripHtml(itemMap.getItemDescription().trim());

        productDes.setText(htmlDescription);

        productName.setText(htmlTitle);
        price.setText(itemMap.getPrice());
        if (itemMap.getYoutubeLink() != null)
            edtYoutube.setText(itemMap.getYoutubeLink());
        if (Constants.GIVINGAWAY && itemMap.getPrice().equals("0")) {
            price.setText("");
            givingAwaySwitch.setChecked(true);
            if (offerLay.getVisibility() == View.VISIBLE) {
                offerLay.setVisibility(View.GONE);
            }
        } else if (!Constants.GIVINGAWAY && itemMap.getPrice().equals("0")) {
            if (GIVINGAWAY)
                givingLay.setVisibility(View.VISIBLE);
            else
                priceLay.setVisibility(View.VISIBLE);
            price.setText("0");
            givingAwaySwitch.setChecked(true);
            if (offerLay.getVisibility() == View.VISIBLE) {
                offerLay.setVisibility(View.GONE);
            }
        } else if (!Constants.GIVINGAWAY && !itemMap.getPrice().equals("0")) {
            givingLay.setVisibility(View.GONE);
            givingAwaySwitch.setChecked(false);
            if (offerLay.getVisibility() == View.GONE) {
                offerLay.setVisibility(View.VISIBLE);
            }
        } else {
            if (GIVINGAWAY) {
                if (givingLay.getVisibility() == View.GONE) {
                    givingLay.setVisibility(View.VISIBLE);
                }
            }
            givingAwaySwitch.setChecked(false);
            if (offerLay.getVisibility() == View.GONE) {
                offerLay.setVisibility(View.VISIBLE);
            }
        }
        txtCategory.setTextColor(getResources().getColor(R.color.primaryText));
        catArrow.setColorFilter(getResources().getColor(R.color.primaryText));
        categoryId = itemMap.getCategoryId();
        subCategoryId = itemMap.getSubcatId();
        childCategoryId = itemMap.getChildCatId();
        txtLocation.setText(itemMap.getLocation());
        txtLocation.setTextColor(getResources().getColor(R.color.primaryText));
        locArrow.setColorFilter(getResources().getColor(R.color.primaryText));
        location = itemMap.getLocation();

        if (itemMap.getSubcatName() != null && !itemMap.getSubcatName().equals("")) {
            setCategoryName(itemMap.getCategoryName(), itemMap.getSubcatName(), "");
        } else
            setCategoryName(itemMap.getCategoryName(), "", "");

        try {
            lat = Double.parseDouble(itemMap.getLatitude());
            lng = Double.parseDouble(itemMap.getLongitude());
        } catch (NullPointerException | NumberFormatException e) {
            e.printStackTrace();
            lat = 0;
            lng = 0;
        } catch (Exception e) {
            e.printStackTrace();
            lat = 0;
            lng = 0;
        }
        exchangeSwitch.setChecked(itemMap.getExchangeBuy().equals("1"));
        chatSwitch.setChecked(itemMap.getMakeOffer().equals("1"));
        buySwitch.setChecked(itemMap.getInstantBuy().equals("1"));
        if (deliveryTypeSpinner != null) {
            String deliveryType = itemMap.getProductDeliveryType();
            if (deliveryType != null && !deliveryType.trim().isEmpty()) {
                if (Constants.DELIVERY_TYPE_BUY_NOW_SHIPPING.equalsIgnoreCase(deliveryType.trim())) {
                    deliveryTypeSpinner.setSelection(1);
                    buySwitch.setChecked(true);
                    buynowLay.setVisibility(View.VISIBLE);
                } else {
                    deliveryTypeSpinner.setSelection(0);
                    buySwitch.setChecked(false);
                    buynowLay.setVisibility(View.GONE);
                }
            } else {
                // Backward compatibility:
                // instant_buy = "1" -> Buy Now / Shipping
                // instant_buy = "0" -> Local Meetup
                if ("1".equals(itemMap.getInstantBuy())) {
                    deliveryTypeSpinner.setSelection(1);
                    buySwitch.setChecked(true);
                    buynowLay.setVisibility(View.VISIBLE);
                } else {
                    deliveryTypeSpinner.setSelection(0);
                    buySwitch.setChecked(false);
                    buynowLay.setVisibility(View.GONE);
                }
            }
        }
        conditionId = itemMap.getItemConditionId() != null ? itemMap.getItemConditionId() : "";
        conditionName = itemMap.getItemCondition() != null ? itemMap.getItemCondition() : "";

        for (BeforeAddResponse.Category category : categoryList) {
            if (category.getCategoryId().equals(categoryId)) {
                Log.d(TAG, "setEditProducts: " + category.getFilters().size());
                tempFilterList.addAll(category.getFilters());
                for (BeforeAddResponse.Subcategory subcategory : category.getSubcategory()) {
                    if (from.equals(Constants.TAG_EDIT)) {
                        if (subcategory.getSubId().equals(subCategoryId)) {
                            Log.d(TAG, "setEditProducts: " + subcategory.getFilters().size());
                            tempFilterList.addAll(subcategory.getFilters());
                        }
                    } else {
                        tempFilterList.addAll(subcategory.getFilters());
                    }
                    if (subcategory.getChildCategory() != null) {
                        for (BeforeAddResponse.ChildCategory childCategory : subcategory.getChildCategory()) {
                            if (from.equals(Constants.TAG_EDIT)) {
                                if (childCategory.getChildId().equals(childCategoryId)) {
                                    tempFilterList.addAll(childCategory.getFilters());
                                }
                            } else {
                                tempFilterList.addAll(childCategory.getFilters());
                            }
                        }
                    }
                }
                break;
            }
        }

        filterList.clear();
        filterList.addAll(itemMap.getFilters());

        if (!from.equals(Constants.TAG_EDIT)) {
            ArrayList<BeforeAddResponse.Filters> tempList = new ArrayList<>();
            for (int i = 0; i < tempFilterList.size(); i++) {
                BeforeAddResponse.Filters categoryFilter = tempFilterList.get(i);
                for (BeforeAddResponse.Filters itemFilter : filterList) {
                    if (categoryFilter.getId().equals(itemFilter.getSelectedParentId())) {
                        boolean exists = false;
                        for (BeforeAddResponse.Filters temp : tempList) {
                            if (temp.getSelectedParentId().equals(itemFilter.getSelectedParentId())) {
                                exists = true;
                            }
                        }
                        if (!exists) {
                            itemFilter.setValues(categoryFilter.getValues());
                            itemFilter.setLabel(categoryFilter.getLabel());
                            itemFilter.setId(categoryFilter.getId());
                            if (!itemFilter.getType().equals(categoryFilter.getType())) {
                                if (categoryFilter.getType().equals("range")) {
                                    itemFilter.setMinValue(categoryFilter.getMinValue());
                                    itemFilter.setMaxValue(categoryFilter.getMaxValue());
                                    itemFilter.setSelectedRangeValue(getOldTypeValue(itemFilter));
                                } else if (categoryFilter.getType().equals("dropdown")) {
                                    String value = getOldTypeValue(itemFilter);
                                    System.out.println("//edit value " + value);
                                    for (BeforeAddResponse.Value valueList : categoryFilter.getValues()) {
                                        System.out.println("//edit valuelist : " + valueList.getName() + " " + value);
                                        if (valueList.getName().equals(value)) {
                                            itemFilter.setChildId(valueList.getId());
                                            itemFilter.setChildLabel(valueList.getName());
                                        }
                                    }
                                } else if (categoryFilter.getType().equals("multilevel")) {

                                }
                                itemFilter.setType(categoryFilter.getType());
                            }
                            tempList.add(itemFilter);
                        }
                        tempFilterList.remove(i);
                        i--;
                        break;
                    }
                }
            }

            if (tempFilterList.size() > 0)
                tempList.addAll(tempFilterList);

            filterList.clear();
            filterList.addAll(tempList);
        }

        /*
         * if (filterList.size() != tempFilterList.size()) {
         * ArrayList<BeforeAddResponse.Filters> tempList = new ArrayList<>();
         * 
         * if(filterList.size() > tempFilterList.size()) {
         * Log.d(TAG, "setEditProductsEditProdcut: "+"FirstOne");
         * for (int i = 0; i < filterList.size(); i++) {
         * int j;
         * for (j = 0; j < tempFilterList.size(); j++) {
         * if
         * (filterList.get(i).getSelectedParentId().equals(tempFilterList.get(j).getId()
         * )) {
         * break;
         * }
         * }
         * if (j != tempFilterList.size()) {
         * Log.v("remove", "remove=" + filterList.get(i).getSelectedParentId() +
         * "tempsize=" + tempList.size());
         * // tempList.remove(i);
         * tempList.add(itemMap.getFilters().get(i));
         * }
         * }
         * } else {
         * Log.d(TAG, "setEditProductsEditProdcut: "+"SecondOne");
         * tempList.addAll(itemMap.getFilters());
         * for (int i = 0; i < tempFilterList.size(); i++) {
         * int j;
         * for (j = 0; j < filterList.size(); j++) {
         * if
         * (tempFilterList.get(i).getId().equals(filterList.get(j).getSelectedParentId()
         * )) {
         * break;
         * }
         * }
         * if (j == filterList.size()) {
         * tempList.add(tempFilterList.get(i));
         * Log.v("remove", "remove=" + tempFilterList.get(i).getId());
         * }
         * }
         * }
         * filterList.clear();
         * filterList.addAll(tempList);
         * }
         */

        filterAdapter.notifyDataSetChanged();
        if (filterList.size() > 0) {
            filterRecycler.setVisibility(View.VISIBLE);
        } else {
            filterRecycler.setVisibility(View.GONE);
        }
        itemCondition.setText(conditionName);
        itemCondition.setTextColor(getResources().getColor(R.color.primaryText));
        condArrow.setColorFilter(getResources().getColor(R.color.primaryText));
        paypalId.setText(itemMap.getPaypalId());
        shippingFee.setText(itemMap.getShippingCost());
        currency.setSelection(getIndex(currency, itemMap.getCurrencyCode()));
    }

    private String getOldTypeValue(BeforeAddResponse.Filters itemFilter) {
        System.out.println("//edit old type : " + itemFilter.getType());
        if (itemFilter.getType().equals("dropdown")) {
            System.out.println("//edit value : " + itemFilter.getChildLabel());
            return itemFilter.getChildLabel();
        } else if (itemFilter.getType().equals("range")) {
            System.out.println("//edit value : " + itemFilter.getSelectedRangeValue());
            return itemFilter.getSelectedRangeValue();
        } else if (itemFilter.getType().equals("multilevel"))
            return itemFilter.getChildLabel();
        return null;
    }

    /**
     * For setting product condition depends on txtCategory
     **/
    private void setCategoryConditions() {
        bottomLay.setVisibility(View.GONE);
        // Delivery type stays visible whether or not a category is selected.
        instantLay.setVisibility(View.VISIBLE);
        buyLay.setVisibility(View.VISIBLE);
        buynowLay.setVisibility(buySwitch.isChecked() ? View.VISIBLE : View.GONE);
        if (categoryList.size() > 0) {
            for (BeforeAddResponse.Category category : categoryList) {
                if (category.getCategoryId().equals(categoryId)) {
                    if (category.getProductCondition().equals("disable") && category.getExchangeBuy().equals("disable")
                            && category.getMakeOffer().equals("disable")) {
                        bottomLay.setVisibility(View.GONE);
                        conditionLay.setVisibility(View.GONE);
                        exchangeLay.setVisibility(View.GONE);
                        offerLay.setVisibility(View.GONE);
                    } else {
                        bottomLay.setVisibility(View.VISIBLE);
                        conditionLay.setVisibility(
                                category.getProductCondition().equals("enable") ? View.VISIBLE : View.GONE);
                        if (givingAwaySwitch.isChecked()) {
                            offerLay.setVisibility(View.GONE);
                        } else {
                            offerLay.setVisibility(
                                    category.getMakeOffer().equals("disable") ? View.GONE : View.VISIBLE);
                        }
                        if (category.getExchangeBuy().equals("enable")) {
                            exchangeLay.setVisibility(Constants.EXCHANGE ? View.VISIBLE : View.GONE);
                        } else {
                            exchangeLay.setVisibility(View.GONE);
                        }
                    }

                    break;
                }
            }
        }
    }

    private int getIndex(Spinner spinner, String myString) {

        int index = 0;
        if (myString.contains("-")) {
            String[] cur = myString.split("-");
            myString = cur[1] + "-" + cur[0];
        }
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).equals(myString)) {
                index = i;
            }
        }
        Log.v("index spin=" + spinner.getCount(), "index=" + myString + "==" + index);
        return index;
    }

    /**
     * For set images to listview
     **/
    private void setImageAdapter() {
        try {
            if (itemMap.getPhotos() == null) {

            } else {
                images.clear();
                for (HomeItemResponse.Photo photo : itemMap.getPhotos()) {
                    Log.d(TAG, "setImageAdapter: " + photo.getItemUrlMain350());
                    String imageurl = "";
                    if (photo.getItemUrlMainOriginal() != null) {
                        imageurl = photo.getItemUrlMainOriginal();
                        photo.setType(Constants.KEY_URL);
                    } else if (photo.getType() != null) {
                        imageurl = photo.getImage();
                    }

                    photo.setImage(imageurl);
                    images.add(photo);
                }

                if (images.size() < 5) {
                    addPlusIcon();
                }
            }

            if (imagesAdapter == null) {
                imagesAdapter = new ImagesAdapter(AddProductDetail.this, images);
                imageList.setLayoutManager(
                        new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
                imageList.setAdapter(imagesAdapter);
            }
            imagesAdapter.notifyDataSetChanged();

        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void addPlusIcon() {
        HomeItemResponse.Photo photo = new HomeItemResponse().new Photo();
        photo.setType("add");
        for (HomeItemResponse.Photo image : images) {
            if (image.getType().equals("add")) {
                images.remove(image);
                break;
            }
        }
        images.add(photo);
    }

    /* Check Image is Already Removed or not */
    private boolean isImgRemoved(String fileName) {
        boolean isPresent = false;
        for (int i = 0; i < removeAry.size(); i++) {
            isPresent = removeAry.get(i).equals(fileName);
        }
        return isPresent;
    }

    /**
     * function for update the captured image
     **/
    public File saveBitmapToFile(Bitmap finalBitmap, String fileName) {

        Log.d(TAG, "saveBitmapToFile: " + finalBitmap + "fileName" + fileName);
        try {
            fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
            File sdCard = getExternalCacheDir();
            File dir = new File(sdCard.getAbsolutePath() + "/" + getString(R.string.app_name));
            dir.mkdirs();
            File newFile = new File(dir, fileName);
            FileOutputStream outputStream = new FileOutputStream(newFile);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            outputStream.flush();
            outputStream.close();
            return newFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void galleryAddPic(String file) {
        File f = new File(file);
        Uri contentUri = Uri.fromFile(f);
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri);
        sendBroadcast(mediaScanIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Constants.ACTION_EDIT_REQUEST_CODE:
                    itemMap = (HomeItemResponse.Item) data.getExtras().get("data");
                    setImageAdapter();
                    break;
                case Constants.ADD_IMAGES_REQUEST_CODE:
                    itemMap = (HomeItemResponse.Item) data.getExtras().get("data");
                    setImageAdapter();
                    break;
                case Constants.CATEGORY_REQUEST_CODE:
                    isCategoryChanged = true;
                    tempFilterList.clear();
                    filterList.clear();
                    filterAdapter.notifyDataSetChanged();
                    category = Constants.selectedCategory;
                    subCategory = Constants.selectedSubCategory;
                    childCategory = Constants.selectedChildCategory;
                    subCategoryId = null;
                    childCategoryId = null;
                    categoryId = category.getCategoryId();
                    if (subCategory != null) {
                        subCategoryId = subCategory.getSubId();
                    }

                    if (data.hasExtra(Constants.TAG_POSITION)) {
                        groupPosition = data.getStringExtra(Constants.TAG_POSITION);
                    }

                    if (childCategory != null) {
                        childCategoryId = childCategory.getChildId();
                    }

                    if (childCategory != null) {
                        setCategoryName(category.getCategoryName(), subCategory.getSubName(),
                                childCategory.getChildName());
                    } else if (subCategory != null) {
                        setCategoryName(category.getCategoryName(), subCategory.getSubName(), "");
                    } else {
                        setCategoryName(category.getCategoryName(), "", "");
                    }

                    txtCategory.setTextColor(getResources().getColor(R.color.primaryText));
                    catArrow.setColorFilter(getResources().getColor(R.color.primaryText));
                    setCategoryConditions();
                    filterList.addAll(category.getFilters());

                    if (subCategory != null) {
                        filterList.addAll(subCategory.getFilters());
                    }

                    if (childCategory != null) {
                        filterList.addAll(childCategory.getFilters());
                    }

                    if (filterList.size() > 0) {
                        filterRecycler.setVisibility(View.VISIBLE);
                    } else {
                        filterRecycler.setVisibility(View.GONE);
                    }
                    // Log.d(TAG, "onActivityResult: " + filterList.get(1).getMaxValue() +" Min"
                    // +filterList.get(1).getMinValue());
                    // Log.d(TAG, "onActivityResult: " + filterList.get(0).getMaxValue() +" Min"
                    // +filterList.get(0).getMinValue());
                    filterAdapter.notifyDataSetChanged();
                    break;
                case Constants.FILTER_REQUEST_CODE:

                    String type = data.getStringExtra(Constants.TAG_TYPE);
                    String parentId = data.getStringExtra(Constants.TAG_PARENT_ID);
                    String subParentId = data.getStringExtra(Constants.TAG_SUBPARENT_ID);
                    String childId = data.getStringExtra(Constants.TAG_CHILD_ID);
                    String parentLabel = data.getStringExtra(Constants.TAG_PARENT_LABEL);
                    String childLabel = data.getStringExtra(Constants.TAG_CHILD_LABEL);
                    if (type.equals(Constants.TAG_DROPDOWN)) {
                        filterList.get(lastClickedPosition).setSelectedParentId(parentId != null ? parentId : "");
                        filterList.get(lastClickedPosition).setChildId(childId != null ? childId : "");
                        filterList.get(lastClickedPosition).setChildLabel(childLabel != null ? childLabel : "");
                        filterAdapter.notifyItemChanged(lastClickedPosition);
                    } else if (type.equals(Constants.TAG_MULTILEVEL)) {
                        filterList.get(lastClickedPosition).setSelectedParentId(parentId != null ? parentId : "");
                        filterList.get(lastClickedPosition)
                                .setSelectedParentLabel(parentLabel != null ? parentLabel : "");
                        filterList.get(lastClickedPosition).setSubParentId(subParentId != null ? subParentId : "");
                        filterList.get(lastClickedPosition).setChildId(childId != null ? childId : "");
                        filterList.get(lastClickedPosition).setChildLabel(childLabel != null ? childLabel : "");
                        filterAdapter.notifyItemChanged(lastClickedPosition);
                    }
                    break;
                case Constants.LOCATION_REQUEST_CODE:
                    lat = data.getDoubleExtra(Constants.TAG_LAT, 0);
                    lng = data.getDoubleExtra(Constants.TAG_LON, 0);
                    location = data.getStringExtra(Constants.TAG_LOCATION);
                    txtLocation.setText(location);
                    txtLocation.setTextColor(getResources().getColor(R.color.primaryText));
                    locArrow.setColorFilter(getResources().getColor(R.color.primaryText));
                    break;
                case Constants.CONDITION_REQUEST_CODE:
                    conditionId = data.getStringExtra(Constants.TAG_CONDITION_ID);
                    conditionName = data.getStringExtra(Constants.TAG_CONDITION_NAME);
                    itemCondition.setText(conditionName);
                    itemCondition.setTextColor(getResources().getColor(R.color.primaryText));
                    condArrow.setColorFilter(getResources().getColor(R.color.primaryText));
                    break;
                default:
                    break;
            }

        }
    }

    private void setCategoryName(String categoryName, String subCategoryName, String childCategoryName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!TextUtils.isEmpty(childCategoryName)) {
                txtCategory.setText(
                        Html.fromHtml(categoryName + " &#8226; " + subCategoryName + " &#8226; " + childCategoryName,
                                Html.FROM_HTML_MODE_LEGACY));
            } else if (!TextUtils.isEmpty(subCategoryName))
                txtCategory.setText(
                        Html.fromHtml(categoryName + " &#8226; " + subCategoryName, Html.FROM_HTML_MODE_LEGACY));
            else
                txtCategory.setText(Html.fromHtml(categoryName, Html.FROM_HTML_MODE_LEGACY));
        } else {
            if (TextUtils.isEmpty(childCategoryName)) {
                txtCategory.setText(
                        Html.fromHtml(categoryName + " &#8226; " + subCategoryName + " &#8226; " + childCategoryName));
            } else if (!TextUtils.isEmpty(subCategoryName))
                txtCategory.setText(Html.fromHtml(categoryName + " &#8226; " + subCategoryName));
            else
                txtCategory.setText(Html.fromHtml(categoryName));
        }
    }

    public void addProductApi() {
        if (NetworkReceiver.isConnected()) {
            String name = "";
            String des = "";
            try {
                name = productName.getText().toString().trim();
                des = Html.toHtml(new SpannableString(productDes.getText().toString().trim()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            // Add your data
            if (from.equals(Constants.TAG_EDIT)) {
                map.put("post_flag", "edit");
                map.put(Constants.TAG_ITEM_ID, itemMap.getId());
                map.put(Constants.TAG_SOLD, "false");
                removeAry = new ArrayList<>();
                for (String imageName : originalImagesName) {
                    if (!uploadedImage.contains(imageName)) {
                        removeAry.add(imageName);
                    }
                }
            }
            map.put(Constants.TAG_PRODUCT_IMG, uploadedImage.toString().replaceAll("[\\[\\]]|(?<=,)\\s+", ""));
            map.put(Constants.TAG_REMOVE_IMG, removeAry.toString().replaceAll("[\\[\\]]|(?<=,)\\s+", ""));
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_ITEM_NAME, name);
            map.put(Constants.TAG_ITEMDES, des.trim());
            map.put(Constants.TAG_QUANTITY, "1");
            postPrice = postPrice.replaceAll("[^a-zA-Z0-9 .,]|(?<!\\d)[.,]|[.,](?!\\d)", "");
            map.put(Constants.TAG_PRICE, postPrice);
            map.put(Constants.TAG_SIZE, "");
            map.put(Constants.TAG_CATEGORY, categoryId);
            if (subCategoryId != null)
                map.put(Constants.TAG_SUBCATEGORY, subCategoryId);
            if (childCategoryId != null)
                map.put(Constants.TAG_CHILD_CATEGORY, childCategoryId);
            map.put(Constants.TAG_SHIPPING_DETAIL, "");
            map.put(Constants.TAG_SHIPPING_TIME, "");
            map.put(Constants.TAG_ADDRESS, txtLocation.getText().toString().trim());
            map.put(Constants.TAG_LAT, Double.toString(lat));
            map.put(Constants.TAG_LON, Double.toString(lng));
            map.put(Constants.TAG_CURRENCY, currencyid);
            map.put(Constants.TAG_PAYPALID, paypalId.getText().toString().trim());
            // map.put(Constants.TAG_COUNTRYID, "100");
            map.put(Constants.TAG_COUNTRYID, String.valueOf(mcountryId));
//            map.put(Constants.TAG_SHIPPING_COST, shippingFee.getText().toString().trim());
            if (exchangeLay.getVisibility() == View.VISIBLE) {
                if (exchangeSwitch.isChecked()) {
                    map.put(Constants.TAG_EXCHANGE_TO_BUY, "1");
                } else {
                    map.put(Constants.TAG_EXCHANGE_TO_BUY, "0");
                }
            } else {
                map.put(Constants.TAG_EXCHANGE_TO_BUY, "2");
            }
            if (givingAwaySwitch.isChecked()) {
                map.put(Constants.TAG_GIVING_AWAY, "yes");
                map.put(Constants.TAG_MAKE_OFFER, "2");
                map.put(Constants.TAG_INSTANT_BUY, "0");
            } else {
                map.put(Constants.TAG_GIVING_AWAY, "no");
                if (offerLay.getVisibility() == View.VISIBLE) {
                    if (chatSwitch.isChecked()) {
                        map.put(Constants.TAG_MAKE_OFFER, "1");
                    } else {
                        map.put(Constants.TAG_MAKE_OFFER, "0");
                    }
                } else {
                    map.put(Constants.TAG_MAKE_OFFER, "2");
                }
                if (buyLay.getVisibility() == View.VISIBLE) {
                    if (buySwitch.isChecked()) {
                        map.put(Constants.TAG_INSTANT_BUY, "1");
                    } else {
                        map.put(Constants.TAG_INSTANT_BUY, "0");
                    }
                } else {
                    map.put(Constants.TAG_INSTANT_BUY, "2");
                }
            }

            String selectedDeliveryType = (deliveryTypeSpinner != null && deliveryTypeSpinner.getSelectedItemPosition() == 1)
                    ? Constants.DELIVERY_TYPE_BUY_NOW_SHIPPING
                    : Constants.DELIVERY_TYPE_LOCAL_MEETUP;
            map.put(Constants.TAG_PRODUCT_DELIVERY_TYPE, selectedDeliveryType);

            if (conditionLay.getVisibility() == View.VISIBLE) {
                map.put(Constants.TAG_ITEM_CONDITION, conditionId);
            } else {
                map.put(Constants.TAG_ITEM_CONDITION, "0");
            }

            /* Filters Params */
            JSONArray filterArray = new JSONArray();
            for (BeforeAddResponse.Filters filters : filterList) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(Constants.TAG_TYPE, filters.getType());
                    jsonObject.put(Constants.TAG_PARENT_ID,
                            filters.getId() != null ? filters.getId() : filters.getSelectedParentId());
                    switch (filters.getType()) {
                        case Constants.TAG_DROPDOWN:
                            jsonObject.put(Constants.TAG_CHILD_ID, filters.getChildId());
                            break;
                        case Constants.TAG_RANGE:
                            jsonObject.put(Constants.TAG_VALUE, filters.getSelectedRangeValue());
                            break;
                        case Constants.TAG_MULTILEVEL:
                            jsonObject.put(Constants.TAG_SUBPARENT_ID, filters.getSubParentId());
                            jsonObject.put(Constants.TAG_CHILD_ID, filters.getChildId());
                            break;
                    }
                    filterArray.put(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            map.put(Constants.TAG_FILTERS, "" + filterArray);
            map.put(Constants.TAG_YOUTUBE_LINK, "" + edtYoutube.getText());
            if (itemMap != null && (from.equals(Constants.TAG_EDIT) || itemMap.isAffiliateItem())) {
                map.put("is_affiliate", itemMap.isAffiliateItem() ? "1" : "0");
                if (itemMap.getAffiliateUrl() != null) {
                    map.put("affiliate_url", itemMap.getAffiliateUrl());
                }
                if (itemMap.getPromoCode() != null) {
                    map.put("promo_code", itemMap.getPromoCode());
                }
                String discountPercent = itemMap.getEffectiveDiscountPercent();
                if (!discountPercent.isEmpty()) {
                    map.put("discount_percent", discountPercent);
                }
            }
            // Vivo Logcat masks lat/lng/url as **** — log keys renamed so request stays readable
            String reqLog = new Gson().toJson(map)
                    .replace("\"lat\"", "\"latitude\"")
                    .replace("\"lon\"", "\"longitude\"")
                    .replace("https://", "https_ ")
                    .replace("http://", "http_ ");
            Log.i(TAG, "addProduct req:  " + reqLog);
            Log.i(TAG, "addProduct api: addProduct | lat=" + lat + " | lon=" + lng);
            Call<AddProductResponse> call = apiInterface.addProduct(map);
            call.enqueue(new Callback<AddProductResponse>() {
                @Override
                public void onResponse(Call<AddProductResponse> call, retrofit2.Response<AddProductResponse> response) {
                    Log.i(TAG, "addProduct response:  " + new Gson().toJson(response.body()));
                    if (response.isSuccessful() && response.body().getStatus().equalsIgnoreCase("true")) {
                        AddProductResponse result = response.body();
                        productUrl = result.getProductUrl();
                        posteditemId = result.getItemId();
                        if (Constants.ADDON_AWS_SERVER) {
                            callAwsUpload(posteditemId);
                        }
                        uploadSuccessLay.setVisibility(View.VISIBLE);
                        imageLoadingLay.setVisibility(View.INVISIBLE);
                        if (result.getMessage().equalsIgnoreCase("Product waiting for admin approval")) {
                            successText.setText(getString(R.string.product_waiting_for_admin_approval));
                        } else {
                            successText.setText(getString(R.string.successfully_posted));
                            SearchAdvance.applyFilter = true;
                        }
                        // CameraActivity.images.clear();
                        images.clear();
                        if (result.getPromotionType().equalsIgnoreCase("Normal") && Constants.PROMOTION) {
                            if (from.equals(Constants.TAG_EDIT) && itemMap.getItemStatus().equalsIgnoreCase("sold")) {
                                promote.setVisibility(View.GONE);
                            } else {
                                promote.setVisibility(View.VISIBLE);
                            }
                        } else {
                            promote.setVisibility(View.GONE);
                        }
                        if (from.equals(Constants.TAG_EDIT)) {
                            DetailActivity.fromEdit = true;
                        }
                    } else if (response.body() != null && response.body().getMessage() != null
                            && response.body().getMessage().equalsIgnoreCase("Please Enter Valid description")) {
                        imageLoadingLay.setVisibility(View.GONE);
                        dialog.dismiss();
                        JoysaleApplication.dialog(AddProductDetail.this, getString(R.string.alert),
                                getString(R.string.your_product_not_valid_des));
                    } else if (response.body() != null && response.body().getMessage() != null
                            && response.body().getMessage().equalsIgnoreCase("Please Enter Valid ProductName")) {
                        imageLoadingLay.setVisibility(View.GONE);
                        dialog.dismiss();
                        JoysaleApplication.dialog(AddProductDetail.this, getString(R.string.alert),
                                getString(R.string.your_product_not_valid_product));
                    } else {
                        imageLoadingLay.setVisibility(View.GONE);
                        dialog.dismiss();
                        JoysaleApplication.dialog(AddProductDetail.this, getString(R.string.alert),
                                getString(R.string.your_product_not));
                    }
                }

                @Override
                public void onFailure(Call<AddProductResponse> call, Throwable t) {
                    Log.e(TAG, "addProductOnFailure: " + t.getMessage());
                    imageLoadingLay.setVisibility(View.GONE);
                    dialog.dismiss();
                    if (t.getMessage().equalsIgnoreCase("timeout")) {

                    } else {
                        JoysaleApplication.dialog(AddProductDetail.this, getString(R.string.alert),
                                getString(R.string.your_product_not));
                    }

                }
            });
        }
    }

    // Addons WebAPI
    private void callAwsUpload(String posteditemId) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
        map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
        map.put(Constants.TAG_ITEM_ID, posteditemId);
        map.put(Constants.TAG_PRODUCT_IMG, uploadedImage.toString().replaceAll("[\\[\\]]|(?<=,)\\s+", ""));
        Call<ReadMessageResponse> call = apiInterface.uploadAws(map);
        call.enqueue(new Callback<ReadMessageResponse>() {
            @Override
            public void onResponse(Call<ReadMessageResponse> call, Response<ReadMessageResponse> response) {
                // Log.d(TAG, "onResponse: " + response.body().getStatus());
            }

            @Override
            public void onFailure(Call<ReadMessageResponse> call, Throwable t) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(AddProductDetail.this,
                    WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AddProductDetail.this, new String[] { WRITE_EXTERNAL_STORAGE }, 102);
            }
        }
        Log.i(TAG, "addproductonResum: " + SplashActivity.beforeDecimal);
        if (SplashActivity.beforeDecimal == 0 && SplashActivity.afterDecimal == 0) {
            Log.i(TAG, "addproductonResume: " + SplashActivity.beforeDecimal);
            getAdminData();
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(AddProductDetail.this, isConnected);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
            int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: " + requestCode);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            int permissionLocation = ContextCompat.checkSelfPermission(AddProductDetail.this,
                    ACCESS_FINE_LOCATION);
            int permissionLocation1 = ContextCompat.checkSelfPermission(AddProductDetail.this,
                    ACCESS_COARSE_LOCATION);
            if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (permissionLocation == PackageManager.PERMISSION_GRANTED
                        || permissionLocation1 == PackageManager.PERMISSION_GRANTED) {

                    // Addon Mapbox
                    if (Constants.ADDON_MAP_BOX_ENABLED) {
                      /*  Intent j = new Intent(AddProductDetail.this, MainActivity.class);
                        j.putExtra(Constants.TAG_LAT, lat);
                        j.putExtra(Constants.TAG_LON, lng);
                        j.putExtra(Constants.TAG_LOCATION, location);
                        j.putExtra(Constants.TAG_FROM, "add");
                        startActivityForResult(j, Constants.LOCATION_REQUEST_CODE);*/
                    } else {
                        Intent j = new Intent(AddProductDetail.this, LocationActivity.class);
                        j.putExtra(Constants.TAG_FROM, Constants.TAG_ADD);
                        j.putExtra(Constants.TAG_LOCATION, location);
                        j.putExtra(Constants.TAG_LAT, lat);
                        j.putExtra(Constants.TAG_LON, lng);
                        j.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivityForResult(j, Constants.LOCATION_REQUEST_CODE);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.need_permission_to_access),
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, LOCATION_PERMISSION_CODE);
                }
            } else {
                if (permissionLocation == PackageManager.PERMISSION_GRANTED
                        || permissionLocation1 == PackageManager.PERMISSION_GRANTED) {

                    // Addon Mapbox
                    if (Constants.ADDON_MAP_BOX_ENABLED) {
               /*         Intent j = new Intent(AddProductDetail.this, MainActivity.class);
                        j.putExtra(Constants.TAG_LAT, lat);
                        j.putExtra(Constants.TAG_LON, lng);
                        j.putExtra(Constants.TAG_LOCATION, location);
                        j.putExtra(Constants.TAG_FROM, "add");
                        startActivityForResult(j, Constants.LOCATION_REQUEST_CODE);*/
                    } else {
                        Intent j = new Intent(AddProductDetail.this, LocationActivity.class);
                        j.putExtra(Constants.TAG_FROM, Constants.TAG_ADD);
                        j.putExtra(Constants.TAG_LOCATION, location);
                        j.putExtra(Constants.TAG_LAT, lat);
                        j.putExtra(Constants.TAG_LON, lng);
                        j.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivityForResult(j, Constants.LOCATION_REQUEST_CODE);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.need_permission_to_access),
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, LOCATION_PERMISSION_CODE);
                }
            }

        }
        if (requestCode == 103 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(AddProductDetail.this, getString(R.string.storage_permission_access), Toast.LENGTH_SHORT)
                    .show();
            // finish();
        } else {
            // Toast.makeText(AddProductDetail.this,
            // getString(R.string.need_permission_to_access), Toast.LENGTH_SHORT).show();
            // finish();
        }
    }

    private void reset() {
        conditionId = "";
        conditionName = "";
        categoryId = "";/*
                         * for (BeforeAddResponse.Country country : result.getCountry()) {
                         * String counId = country.getCountryId();
                         * if (!counId.equals("0")) {
                         * countryId.add(counId);
                         * countryName.add(country.getCountryName());
                         * countryCode.add(country.getCountryCode());
                         * }
                         * }
                         */
        subCategoryId = "";
        childCategoryId = null;
        location = "";
        lat = 0;
        lng = 0;
        images.clear();
    }

    private String getCountryId() {
        mcountryId = "";
        try {
            /*
             * Log.v(TAG, "from=" + from);
             * Log.d(TAG, "getCountryId: "+itemMap.getLocation().toString());
             * Log.d(TAG, "getCountryId: "+txtLocation.getText().toString());
             */
            if (from.equals(Constants.TAG_EDIT) && itemMap != null
                    && itemMap.getLocation().equalsIgnoreCase(txtLocation.getText().toString())) {

                if (itemMap.getInstantBuy().equalsIgnoreCase("1")) {
                    mcountryId = itemMap.getCountryId();
                } else {
                    if (instantLay.getVisibility() == View.VISIBLE && buySwitch.isChecked()) {
                        String countryname = txtLocation.getText().toString();
                        Log.v("location", "location=" + countryname);
                        if (countryname.contains(",")) {
                            countryname = countryname.substring(countryname.lastIndexOf(',') + 1).trim();
                        }
                        countryname = countryname.trim();

                        Log.v("countryname", "countryname=" + countryname);

                        if (countryname.equalsIgnoreCase("Russia")) {
                            mcountryId = "181";
                            return mcountryId;
                        }

                        if (countryId.size() > 0) {
                            int index = countryName.indexOf(countryname);
                            Log.v("index", "index=" + index);
                            Log.v("countryId", "countryId=" + countryId);
                            mcountryId = countryId.get(index);
                            Log.v("countryId", "countryId=" + mcountryId);

                        }

                    } else {
                        mcountryId = "0";
                    }
                }
            } else {
                Log.v(TAG, "Visibility=" + instantLay.getVisibility());
                if (instantLay.getVisibility() == View.VISIBLE && buySwitch.isChecked()) {
                    String countryname = txtLocation.getText().toString();
                    Log.v(TAG, "location=" + countryname);
                    if (countryname.contains(",")) {
                        countryname = countryname.substring(countryname.lastIndexOf(',') + 1).trim();
                    }
                    countryname = countryname.trim();

                    if (countryname.equalsIgnoreCase("Russia")) {
                        mcountryId = "181";
                        return mcountryId;
                    }

                    Log.v(TAG, "countryname=" + countryname);
                    Log.v(TAG, "countryName=" + countryName);
                    if (countryId.size() > 0) {
                        int index = countryName.indexOf(countryname);
                        Log.v(TAG, "index=" + index);
                        Log.v(TAG, "countryId=" + countryId);
                        mcountryId = countryId.get(index);
                    }
                } else {
                    mcountryId = "0";
                }
            }
        } catch (NullPointerException e) {
            mcountryId = "";
            e.printStackTrace();
        } catch (Exception e) {
            mcountryId = "";
            e.printStackTrace();
        }
        return mcountryId;
    }

    @Override
    public void onBackPressed() {
        // reset();
        if (from.equals("home") || from.equals("add")) {
            JoysaleApplication.dialogOkCancel(AddProductDetail.this, getString(R.string.discard),
                    getString(R.string.discard_post), new OnButtonClick() {
                        @Override
                        public void onOkClicked() {
                            finish();
                        }

                        @Override
                        public void onCancelClicked() {

                        }
                    });
        } else {
            JoysaleApplication.dialogOkCancel(AddProductDetail.this, getString(R.string.discard),
                    getString(R.string.discard_changes), new OnButtonClick() {
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

    private void getCountries() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_LANG_TYPE, Locale.ENGLISH.getLanguage());

            Call<BeforeAddResponse> call = apiInterface.getProductBeforeAdd(map);
            call.enqueue(new Callback<BeforeAddResponse>() {
                @Override
                public void onResponse(Call<BeforeAddResponse> call, retrofit2.Response<BeforeAddResponse> response) {
                    if (response.isSuccessful() && response.body().getStatus().equalsIgnoreCase("true")) {
                        BeforeAddResponse.Result result = response.body().getResult();
                        for (BeforeAddResponse.Country country : result.getCountry()) {
                            String counId = country.getCountryId();
                            if (!counId.equals("0")) {
                                countryId.add(counId);
                                countryName.add(country.getCountryName());
                                countryCode.add(country.getCountryCode());
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<BeforeAddResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void getCategories() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(AddProductDetail.this));

            Call<BeforeAddResponse> call = apiInterface.getProductBeforeAdd(map);
            call.enqueue(new Callback<BeforeAddResponse>() {
                @Override
                public void onResponse(Call<BeforeAddResponse> call, retrofit2.Response<BeforeAddResponse> response) {
                    if (response.isSuccessful() && response.body().getStatus().equalsIgnoreCase("true")) {
                        BeforeAddResponse.Result result = response.body().getResult();

                        if (categoryList.size() > 0) {
                            categoryList.clear();
                        }
                        categoryList.addAll(result.getCategory());

                        Log.d(TAG, "onResponseItem: " + categoryList);

                        if (currencyID.size() > 0) {
                            currencyID.clear();
                            currencySpin.clear();
                        }
                        for (BeforeAddResponse.Currency resultCurrency : result.getCurrency()) {
                            currencyID.add(resultCurrency.getId());
                            currencySpin.add(resultCurrency.getSymbol());
                        }

                        if (conditionAry.size() > 0) {
                            conditionAry.clear();
                        }
                        Log.d(TAG, "onResponse: " + result.getProductCondition().toString());
                        conditionAry.addAll(result.getProductCondition());

                        /*
                         * for (BeforeAddResponse.Country country : result.getCountry()) {
                         * String counId = country.getCountryId();
                         * if (!counId.equals("0")) {
                         * countryId.add(counId);
                         * countryName.add(country.getCountryName());
                         * countryCode.add(country.getCountryCode());
                         * }
                         * }
                         */

                        Constants.GIVINGAWAY = result.getGivingAway().equalsIgnoreCase("enable");
                        JoysaleApplication.adminEditor.putBoolean(Constants.PREF_GIVINGAWAY, Constants.GIVINGAWAY);
                        JoysaleApplication.adminEditor.commit();
                    }

                    setAdminCondition(currencyID, currencySpin);
                }

                @Override
                public void onFailure(Call<BeforeAddResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void setAdminCondition(ArrayList<String> currencyID, ArrayList<String> currencySpin) {
        currencyadapter = new ArrayAdapter<String>(AddProductDetail.this, R.layout.spinner_item, currencySpin);
        currencyadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currency.setAdapter(currencyadapter);

        currency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                // currencyid = currencyID.get(position);
                try {
                    ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.secondaryText));

                    String selectedCurrency = currencySpin.get(position);
                    if (selectedCurrency.contains("-")) {
                        String[] cur = selectedCurrency.split("-");
                        currencyid = cur[1] + "-" + cur[0];
                    } else {
                        currencyid = "";
                    }
                    Log.v("currencyid", "" + currencyid);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        if (from.equals(Constants.TAG_EDIT)) {
            setEditProducts();
            setImageAdapter();
        }
        setCategoryConditions();
        parentLay.setVisibility(View.VISIBLE);
        saveLay.setVisibility(View.VISIBLE);
        loadingView.setVisibility(View.GONE);

    }

    private boolean checkPermissions(String[] permissionList, AddProductDetail activity) {
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                JoysaleApplication.hideSoftKeyboard(AddProductDetail.this);
                onBackPressed();
                break;
            case R.id.cancelIcon:
                dialog.dismiss();
                finish();
                Intent in = new Intent(AddProductDetail.this, ProfileActivity.class);
                in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                in.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                startActivity(in);
                reset();
                break;
            case R.id.cancel:
                onBackPressed();
                break;
            case R.id.post:
                try {
                    postPrice = price.getText().toString().trim();
                    if (productName.getText().toString().trim().equals("")) {
                        Log.v(TAG, "cond1");
                        Toast.makeText(AddProductDetail.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT)
                                .show();
                    } else if (productDes.getText().toString().trim().equals("")) {
                        Log.v(TAG, "cond2");
                        Toast.makeText(AddProductDetail.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT)
                                .show();
                    } else if (priceLay.getVisibility() == View.VISIBLE
                            && (postPrice == null || postPrice.equals(""))) {
                        Log.v(TAG, "cond3");
                        Toast.makeText(AddProductDetail.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT)
                                .show();
                    } else if (priceLay.getVisibility() == View.VISIBLE && postPrice.equals("0")) {// == 0) {
                        Log.v(TAG, "cond4");
                        Toast.makeText(AddProductDetail.this, getString(R.string.price_should), Toast.LENGTH_SHORT)
                                .show();
                    } else if (txtCategory.getText().toString().equals(getString(R.string.select_your_category))) {
                        Log.v(TAG, "cond7");
                        Toast.makeText(AddProductDetail.this, getString(R.string.choose_category), Toast.LENGTH_SHORT)
                                .show();
                    } else if (!checkFilters()) {

                    } else if (txtLocation.getText().toString().equals(getString(R.string.set_your_location)) ||
                            txtLocation.getText().toString().equals("")) {
                        Log.v(TAG, "cond5");
                        Toast.makeText(AddProductDetail.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT)
                                .show();
                    } else if (getCountryId().equals("")) {
                        Log.v(TAG, "cond6");
                        Toast.makeText(AddProductDetail.this, getString(R.string.problem_location), Toast.LENGTH_SHORT)
                                .show();
                    } else if ((conditionId == null || conditionId.equals(""))
                            && conditionLay.getVisibility() == View.VISIBLE) {
                        Log.v(TAG, "cond8");
                        Toast.makeText(AddProductDetail.this, getString(R.string.choose_condition), Toast.LENGTH_SHORT)
                                .show();
                    } else if (!TextUtils.isEmpty(edtYoutube.getText()) && !isYoutubeUrl("" + edtYoutube.getText())) {
                        Toast.makeText(getApplicationContext(), R.string.enter_valid_youtube_link, Toast.LENGTH_SHORT)
                                .show();
                    }
                    /*
                     * else if (instantLay.getVisibility() == View.VISIBLE && buySwitch.isChecked()
                     * && paypalId.getText().toString().trim().length() == 0) {
                     * Log.v(TAG, "cond9");
                     * Toast.makeText(AddProductDetail.this, getString(R.string.please_fill_all),
                     * Toast.LENGTH_SHORT).show();
                     * } else if (instantLay.getVisibility() == View.VISIBLE &&
                     * buySwitch.isChecked() &&
                     * !paypalId.getText().toString().matches(emailPattern)) {
                     * Log.v(TAG, "cond10");
                     * Toast.makeText(AddProductDetail.this,
                     * getString(R.string.please_verify_paypalid), Toast.LENGTH_SHORT).show();
                     * }
                     */
                   /* else if (instantLay.getVisibility() == View.VISIBLE && buySwitch.isChecked()
                            *//*&& shippingFee.getText().toString().trim().length() == 0*//*) {
                        Log.v(TAG, "cond11");
                        Toast.makeText(AddProductDetail.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT)
                                .show();
                    }*/ else if (images.size() == 1 && images.get(0).getType().equals("add")) {
                        Log.v(TAG, "cond12");
                        Toast.makeText(AddProductDetail.this, getString(R.string.please_upload_image),
                                Toast.LENGTH_SHORT).show();
                    } else if (isValidPrice
                            && !isValidPrice(postPrice, SplashActivity.beforeDecimal, SplashActivity.afterDecimal)) {
                        Log.v(TAG, "cond13");
                        Toast.makeText(AddProductDetail.this, getString(R.string.reqd_valid_price), Toast.LENGTH_SHORT)
                                .show();
                    } else if (isValidPrice && shippingFee.getText().toString().length() > 0
                            && !isValidPrice(shippingFee.getText().toString(), SplashActivity.beforeDecimal,
                                    SplashActivity.afterDecimal)) {
                        Log.v(TAG, "cond14");
                        Toast.makeText(AddProductDetail.this, getString(R.string.reqd_valid_price), Toast.LENGTH_SHORT)
                                .show();
                    } else if (!isValidShipping(shippingFee.getText().toString())) {
                        Toast.makeText(AddProductDetail.this, getString(R.string.enter_valid_ship), Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        // Addon Location Personalization
                        if (Constants.ADDON_SINGLE_COUNTRY) {
                            if (LocationHelper.isUserCountry(this, txtLocation.getText().toString())) {
                                addProduct();
                            } else {
                                // Not from India
                                Toast.makeText(AddProductDetail.this,
                                        String.format(getString(R.string.india_location), "India"), Toast.LENGTH_SHORT)
                                        .show();
                            }
                        } else {
                            addProduct();
                        }
                    }

                } catch (NullPointerException e) {
                    e.printStackTrace();
                    Log.v(TAG, "NullPointerException: " + e.getMessage());
                } catch (NumberFormatException e) {
                    Log.v(TAG, "NumberFormatException: " + e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.promote:
                if (!posteditemId.equals("")) {
                    reset();
                    Intent u = new Intent(AddProductDetail.this, CreatePromote.class);
                    u.putExtra("itemId", posteditemId);
                    u.putExtra(Constants.TAG_FROM, Constants.TAG_ADD);
                    startActivity(u);
                    finish();
                } else {
                    Toast.makeText(AddProductDetail.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case R.id.conditionLay:
                Intent i = new Intent(AddProductDetail.this, ConditionActivity.class);
                i.putExtra(Constants.TAG_DATA, conditionAry);
                i.putExtra(Constants.TAG_FROM, Constants.TAG_ADD);
                i.putExtra(Constants.TAG_CONDITION_ID, conditionId);
                i.putExtra(Constants.TAG_CONDITION_NAME, conditionName);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityForResult(i, Constants.CONDITION_REQUEST_CODE);
                break;
            case R.id.locationLay:
                Log.d(TAG, "onClick: " + lat + "lng" + lng);
                if (!checkPermissions(Constants.LOCATION_PERMISSIONS, AddProductDetail.this)) {
                    ActivityCompat.requestPermissions(AddProductDetail.this, Constants.LOCATION_PERMISSIONS,
                            LOCATION_PERMISSION_CODE);
                } else {
                    // Addon Mapbox
                    if (Constants.ADDON_MAP_BOX_ENABLED) {
                       /* Intent j = new Intent(AddProductDetail.this, MainActivity.class);
                        j.putExtra(Constants.TAG_FROM, "add");
                        j.putExtra(Constants.TAG_LOCATION, location);
                        j.putExtra(Constants.TAG_LAT, lat);
                        j.putExtra(Constants.TAG_LON, lng);
                        startActivityForResult(j, Constants.LOCATION_REQUEST_CODE);*/
                    } else {
                        Intent j = new Intent(AddProductDetail.this, LocationActivity.class);
                        j.putExtra(Constants.TAG_FROM, Constants.TAG_ADD);
                        j.putExtra(Constants.TAG_LOCATION, location);
                        j.putExtra(Constants.TAG_LAT, lat);
                        j.putExtra(Constants.TAG_LON, lng);
                        j.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivityForResult(j, Constants.LOCATION_REQUEST_CODE);
                    }
                }

                break;
            case R.id.categoryLay:
                Intent categoryIntent = new Intent(AddProductDetail.this, SelectCategory.class);
                categoryIntent.putExtra(Constants.TAG_FROM, Constants.TAG_ADD);
                MyData.getInstance().setData(categoryList);
                if (category != null) {
                    Constants.selectedCategory = category;
                }
                if (subCategory != null) {
                    Constants.selectedSubCategory = subCategory;
                }
                if (categoryId != null && !TextUtils.isEmpty(categoryId.trim())) {
                    categoryIntent.putExtra(Constants.CATEGORYID, categoryId);
                    categoryIntent.putExtra(Constants.TAG_POSITION, groupPosition);
                }
                if (subCategoryId != null)
                    categoryIntent.putExtra(Constants.TAG_SUBCATEGORY_ID, subCategoryId);
                if (childCategoryId != null)
                    categoryIntent.putExtra(Constants.TAG_CHILD_CATEGORY_ID, childCategoryId);
                if (childCategory != null) {
                    Constants.selectedChildCategory = childCategory;
                }
                categoryIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityForResult(categoryIntent, Constants.CATEGORY_REQUEST_CODE);
                break;
        }
    }

    private void addProduct() {
        loadingProgress.setMax(count);
        dialog.show();
        count = 0;
        uploadedImage.clear();
        for (HomeItemResponse.Photo image : images) {
            if (!image.getType().equals(Constants.TAG_ADD) && !image.getType().equals(Constants.KEY_URL))
                count++;
        }

        if (count > 0) {
            loadingProgress.setMax(count);
            failedImageCount = 0;
            new UploadImageTask(images).execute();
        } else {
            alert_title.setText(getString(R.string.posting_list));
            loadingProgress.setVisibility(View.GONE);
            postProgress.setVisibility(View.VISIBLE);
            uploadStatus.setVisibility(View.GONE);
            for (HomeItemResponse.Photo image : images) {
                if (image.getType().equals(Constants.KEY_URL)) {
                    if (image.getItemImage() != null) {
                        uploadedImage.add(image.getItemImage());
                    } else {
                        String imageurl = "" + image.getImage();
                        imageurl = imageurl.substring(imageurl.lastIndexOf("/") + 1);
                        uploadedImage.add(imageurl);
                    }
                }
            }
            addProductApi();
            // new SendProducts().execute();
        }
    }

    private boolean checkFilters() {
        Log.d(TAG, "checkFilters: " + filterList.size());
        for (BeforeAddResponse.Filters filters : filterList) {
            if (filters.getType().equals(Constants.TAG_DROPDOWN) && filters.getChildId() == null) {
                // Toast.makeText(getApplicationContext(), getString(R.string.please_fill_all),
                // Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(),
                        String.format(getString(R.string.dropdown_error_description), filters.getLabel()),
                        Toast.LENGTH_SHORT).show();
                return false;
            } else if (filters.getType().equals(Constants.TAG_RANGE)) {
                if ((filters.getSelectedRangeValue() == null || filters.getSelectedRangeValue().equals(""))) {
                    Log.d(TAG, "checkFilters: " + "FirstOne");
                    Log.d(TAG, "checkFilters: " + filters.getSelectedRangeValue());
                    // Toast.makeText(getApplicationContext(), getString(R.string.please_fill_all),
                    // Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.enter) + " " + filters.getLabel() + " " + getString(R.string.between)
                                    + " " + filters.getMinValue() + " " + getString(R.string.and)
                                    + filters.getMaxValue(),
                            Toast.LENGTH_SHORT).show();
                    return false;
                } else if ((Double.parseDouble(filters.getSelectedRangeValue()) > Double
                        .parseDouble(filters.getMaxValue())) ||
                        (Double.parseDouble(filters.getSelectedRangeValue()) < Double
                                .parseDouble(filters.getMinValue()))) {
                    Log.d(TAG, "checkFilters: " + "SecondOne");
                    Log.d(TAG, "checkFilters: " + filters.getSelectedRangeValue());
                    // Toast.makeText(getApplicationContext(), getString(R.string.please_fill_all),
                    // Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.enter) + " " + filters.getLabel() + " " + getString(R.string.between)
                                    + " " + filters.getMinValue() + " " + getString(R.string.and)
                                    + filters.getMaxValue(),
                            Toast.LENGTH_SHORT).show();
                    return false;
                }
            } else if (filters.getType().equals(Constants.TAG_MULTILEVEL) && filters.getChildId() == null) {
                // Toast.makeText(getApplicationContext(), getString(R.string.please_fill_all),
                // Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(),
                        String.format(getString(R.string.multilevel_error_description), filters.getLabel()),
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    /**
     * Function for Check a Valid Price
     **/

    private boolean isValidPrice(String postPrice, long mDigitsBeforeZero, long mDigitsAfterZero) {
        try {
            String[] indexOfDecimal = postPrice.split("\\.");
            String beforeDecimal = indexOfDecimal[0].trim();
            String afterDecimal = indexOfDecimal[1].trim();
            beforeDecimal = beforeDecimal.replaceAll("[^\\d.]", "");
            afterDecimal = afterDecimal.replaceAll("[^\\d.]", "");
            return beforeDecimal.length() <= mDigitsBeforeZero && afterDecimal.length() <= mDigitsAfterZero;
        } catch (ArrayIndexOutOfBoundsException e) {
            return postPrice.length() <= mDigitsBeforeZero;
        } catch (NumberFormatException e) {
            Log.e(TAG, "isValidPrice: " + e.getMessage());
            return false;
        }
    }

    /**
     * Function for Check a Valid Shipping
     **/

    private boolean isValidShipping(String shipPrice) {
        boolean check = true;
        if (shipPrice.equals(".")) {
            check = false;
        }

        return check;
    }

    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }

    public void deleteImage() {
        for (HomeItemResponse.Photo photo : images) {
            if (photo.getType().equals("url")) {
                String imageurl = photo.getImage();
                imageurl = imageurl.substring(imageurl.lastIndexOf("/") + 1);
                removeAry.add(imageurl);
                Log.d("removeindex", images.indexOf(photo) + " ");
            }
            images.remove(photo);
            images.indexOf(photo);
        }
        addPlusIcon();
    }

    /**
     * class for restrict space and spl characters
     **/

    public static class addListenerOnTextChange implements TextWatcher {
        private final Context mContext;
        EditText mEdittextview;

        public addListenerOnTextChange(Context context, EditText edittextview) {
            super();
            this.mContext = context;
            this.mEdittextview = edittextview;
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mEdittextview.getText().length() > 0) {
                mEdittextview.setError(null);
            }

            String result = s.toString().replaceAll("  ", " ");
            String specialChar = s.toString().replaceAll("[^\\s\\w]*", "");
            // for numbers
            // specialChar = specialChar.replaceAll("[0-9]", "");

            Log.v("special char", "=" + specialChar);
            if (!s.toString().equals(result)) {
                mEdittextview.setText(result);
                mEdittextview.setSelection(result.length());
                // alert the user
            }
            if (!s.toString().equals(specialChar)) {
                mEdittextview.setText(specialChar);
                mEdittextview.setSelection(specialChar.length());
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
    }

    /**
     * Class for filter_icon the multiple spaces
     **/
    public static class addListenerForDes implements TextWatcher {
        private final Context mContext;
        EditText mEdittextview;

        public addListenerForDes(Context context, EditText edittextview) {
            super();
            this.mContext = context;
            this.mEdittextview = edittextview;
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mEdittextview.getText().length() > 0) {
                mEdittextview.setError(null);
            }

            String result = s.toString().replaceAll("  ", " ");

            if (!s.toString().equals(result)) {
                mEdittextview.setText(result);
                mEdittextview.setSelection(result.length());
                // alert the user
            }

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
    }

    public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder> {

        private final Context mContext;
        private final int imageWidth;
        private final int imageHeight;
        private ArrayList<HomeItemResponse.Photo> imgAry = new ArrayList<>();

        public ImagesAdapter(Context ctx2, ArrayList<HomeItemResponse.Photo> data) {
            mContext = ctx2;
            imgAry = data;
            imageWidth = JoysaleApplication.dpToPx(mContext, 60);
            imageHeight = JoysaleApplication.dpToPx(mContext, 60);
        }

        public void clear(ArrayList<HomeItemResponse.Photo> imgAry) {
            this.imgAry = imgAry;
        }

        @NonNull
        @Override
        public ImagesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.addproduct_image, parent, false);// layout
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImagesAdapter.ViewHolder holder, int position) {
            try {
                final HomeItemResponse.Photo photo = imgAry.get(position);
                if (photo.getType().equals("add")) {
                    holder.delete.setVisibility(View.GONE);
                    holder.singleImage.setImageResource(R.drawable.plus_sign);
                    holder.singleImage.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            images.remove(photo);
                            if (from.equals(Constants.TAG_EDIT)) {
                                Intent i = new Intent(AddProductDetail.this, CameraViewActivity.class);
                                i.putExtra(Constants.TAG_FROM, Constants.TAG_EDIT);
                                i.putExtra(Constants.TAG_DATA, itemMap);
                                i.putExtra(Constants.TAG_IMAGES, imgAry);
                                i.putExtra("isAddProduct", true);
                                if (Constants.ADDON_MAP_BOX_ENABLED) {
                                    i.putExtra(Constants.TAG_LOCATION, locationmapbox);
                                }
                                startActivityForResult(i, Constants.ACTION_EDIT_REQUEST_CODE);
                            } else {
                                Intent i = new Intent(AddProductDetail.this, CameraViewActivity.class);
                                i.putExtra(Constants.TAG_FROM, Constants.TAG_ADD);
                                i.putExtra(Constants.TAG_DATA, itemMap);
                                i.putExtra(Constants.TAG_IMAGES, imgAry);
                                i.putExtra("isAddProduct", true);
                                if (Constants.ADDON_MAP_BOX_ENABLED) {
                                    i.putExtra(Constants.TAG_LOCATION, locationmapbox);
                                }
                                startActivityForResult(i, Constants.ADD_IMAGES_REQUEST_CODE);
                            }
                        }
                    });
                } else if (photo.getType().equals(Constants.TAG_PATH)) {
                    holder.delete.setVisibility(View.VISIBLE);
                    Picasso.get().load("file://" + photo.getImage())
                            .resize(imageWidth, imageHeight)
                            .centerCrop().into(holder.singleImage);
                } else {
                    holder.delete.setVisibility(View.VISIBLE);
                    Picasso.get().load(photo.getImage())
                            .resize(imageWidth, imageHeight)
                            .centerCrop().into(holder.singleImage);
                }

                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (images.size() > 0) {
                            if (photo.getType().equals("url")) {
                                String imageurl = photo.getImage();
                                imageurl = imageurl.substring(imageurl.lastIndexOf("/") + 1);
                                removeAry.add(imageurl);
                                Log.d("removeindex", images.indexOf(photo) + " ");
                            }
                            images.remove(photo);
                            images.indexOf(photo);
                            addPlusIcon();
                        }
                        imagesAdapter.notifyDataSetChanged();
                    }
                });

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return imgAry.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView singleImage, delete;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                singleImage = itemView.findViewById(R.id.imageView);
                delete = itemView.findViewById(R.id.delete);
            }
        }
    }

    public class FilterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int VIEW_TYPE_RANGE = 1;
        private static final int VIEW_TYPE_DROPDOWN = 2;
        private static final int VIEW_TYPE_MULTILEVEL = 3;
        Context context;
        ArrayList<BeforeAddResponse.Filters> filterList = new ArrayList<>();

        public FilterAdapter(Context context, ArrayList<BeforeAddResponse.Filters> filterList) {
            this.context = context;
            this.filterList = filterList;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            if (viewType == VIEW_TYPE_RANGE) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_filter_range, parent, false);
                return new RangeViewHolder(view);
            } else if (viewType == VIEW_TYPE_DROPDOWN) {

                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_filter_dropdown, parent, false);
                return new DropdownViewHolder(view);
            } else {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_filter_multilevel, parent, false);
                return new MultilevelViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder,
                @SuppressLint("RecyclerView") int position) {
            final BeforeAddResponse.Filters filter = filterList.get(position);
            if (getItemViewType(position) == VIEW_TYPE_RANGE) {
                ((RangeViewHolder) holder).txtRangeLabel.setText(filter.getSelectedParentLabel() != null
                        ? filter.getSelectedParentLabel()
                        : filter.getLabel());
                Log.d(TAG, "onBindViewHolder: " + filter.getMinValue() + "Max" + filter.getMaxValue());
                if (filter.getSelectedRangeValue() != null) {
                    ((RangeViewHolder) holder).edtRangeValue.setText(filter.getSelectedRangeValue());
                } else {
                    ((RangeViewHolder) holder).edtRangeValue.setText("");
                    ((RangeViewHolder) holder).edtRangeValue
                            .setHint(filter.getMinValue() + " - " + filter.getMaxValue());
                    Log.d(TAG, "onBindViewHolder: " + ((RangeViewHolder) holder).edtRangeValue.getHint());

                }

                /*
                 * if (isRTL) {
                 * ((RangeViewHolder) holder).edtRangeValue.setGravity(Gravity.END);
                 * } else {
                 * ((RangeViewHolder) holder).edtRangeValue.setGravity(Gravity.START);
                 * }
                 * 
                 * ((RangeViewHolder)
                 * holder).edtRangeValue.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
                 */

                ((RangeViewHolder) holder).edtRangeValue.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (editable.length() > 0) {
                                        String temp = ("" + editable).replaceAll("[^\\d.]", "");
                                        if (Double.parseDouble(temp) < Double.parseDouble(filter.getMinValue()) ||
                                                Double.parseDouble(temp) > Double.parseDouble(filter.getMaxValue())) {
                                            filter.setSelectedRangeValue("");
                                        } else {
                                            filter.setSelectedRangeValue("" + temp);
                                        }
                                    } else {
                                        if (!isCategoryChanged)
                                            ((RangeViewHolder) holder).edtRangeValue
                                                    .setHint(filter.getMinValue() + " - " + filter.getMaxValue());
                                        filter.setSelectedRangeValue("");
                                    }
                                } catch (NumberFormatException e) {
                                    Log.i(TAG, "run: " + e.getMessage());
                                }
                            }
                        }, 500);
                    }
                });
                ((RangeViewHolder) holder).divider
                        .setVisibility(position != filterList.size() - 1 ? View.VISIBLE : View.GONE);
            } else if (getItemViewType(position) == VIEW_TYPE_DROPDOWN) {
                ((DropdownViewHolder) holder).txtFilterName.setText(filter.getSelectedParentLabel() != null
                        ? filter.getSelectedParentLabel()
                        : filter.getLabel());
                ((DropdownViewHolder) holder).txtSelectFilter
                        .setText(filter.getChildLabel() != null ? filter.getChildLabel() : "");
                ((DropdownViewHolder) holder).itemLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        lastClickedPosition = position;
                        if (from.equals(Constants.TAG_EDIT)) {
                            if (tempFilterList.size() > 0) {

                                for (BeforeAddResponse.Filters filters : tempFilterList) {
                                    if (filter.getSelectedParentId() != null
                                            && filter.getSelectedParentId().equals(filters.getId())) {
                                        filter.setValues(filters.getValues());
                                        break;
                                    }
                                }
                            }
                        }
                        Log.d(TAG, "onClick: " + filter.getValues());
                        if (filter.getValues() != null) {
                            Intent intent = new Intent(context, AddFilterActivity.class);
                            intent.putExtra(Constants.TAG_FROM, from);
                            intent.putExtra(Constants.TAG_DATA, filter);
                            Log.i(TAG, "onCreate: " + new Gson().toJson(filter));
                            intent.putExtra(Constants.TAG_TYPE, Constants.TAG_DROPDOWN);
                            intent.putExtra(Constants.TAG_FILTER_ID,
                                    filter.getChildId() != null ? filter.getChildId() : "");
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivityForResult(intent, Constants.FILTER_REQUEST_CODE);
                        }
                    }
                });
                if (isRTL) {
                    ((DropdownViewHolder) holder).btnNext.setRotation(180);
                } else {
                    ((DropdownViewHolder) holder).btnNext.setRotation(0);
                }
                ((DropdownViewHolder) holder).divider
                        .setVisibility(position != filterList.size() - 1 ? View.VISIBLE : View.GONE);
            } else if (getItemViewType(position) == VIEW_TYPE_MULTILEVEL) {
                ((MultilevelViewHolder) holder).txtFilterName.setText(filter.getLabel() != null
                        ? filter.getLabel()
                        : filter.getSelectedParentLabel());
                ((MultilevelViewHolder) holder).txtSelectFilter
                        .setText(filter.getChildLabel() != null ? filter.getChildLabel() : "");
                ((MultilevelViewHolder) holder).itemLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "onClickNEWEWWW: " + filter.getSelectedParentId());
                        Log.d(TAG, "onClickNEWEWWW: " + filter.getSelectedChildId());
                        lastClickedPosition = position;

                        if (from.equals(Constants.TAG_EDIT)) {
                            for (BeforeAddResponse.Filters filters : tempFilterList) {
                                if (filter.getSelectedParentId() != null
                                        && filter.getSelectedParentId().equals(filters.getId())) {
                                    for (BeforeAddResponse.Value value : filters.getValues()) {
                                        if (value.getParentId().equals(filter.getSelectedSubParentId())) {
                                            filter.setSubParentId(value.getParentId());
                                            break;
                                        }
                                    }
                                    filter.setValues(filters.getValues());
                                    filter.setLabel(filters.getLabel());
                                    filter.setSelectedParentLabel(
                                            filters.getSelectedParentLabel() != null ? filters.getSelectedParentLabel()
                                                    : "");
                                    filter.setChildLabel(filters.getChildLabel());
                                    break;
                                }
                            }
                        }

                        if (filter.getValues() != null) {
                            Intent intent = new Intent(context, AddFilterActivity.class);
                            intent.putExtra(Constants.TAG_FROM, from);
                            intent.putExtra(Constants.TAG_DATA, filter);
                            intent.putExtra(Constants.TAG_TYPE, Constants.TAG_MULTILEVEL);
                            intent.putExtra(Constants.TAG_PARENT_ID,
                                    filter.getSelectedParentId() != null ? filter.getSelectedParentId() : "");
                            intent.putExtra(Constants.TAG_SUBPARENT_ID,
                                    filter.getSubParentId() != null ? filter.getSubParentId() : "");
                            intent.putExtra(Constants.TAG_CHILD_ID,
                                    filter.getChildId() != null ? filter.getChildId() : "");
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivityForResult(intent, Constants.FILTER_REQUEST_CODE);
                        }
                    }
                });
                ((MultilevelViewHolder) holder).divider
                        .setVisibility(position != filterList.size() - 1 ? View.VISIBLE : View.GONE);
                if (isRTL) {
                    ((MultilevelViewHolder) holder).btnNext.setRotation(180);
                } else {
                    ((MultilevelViewHolder) holder).btnNext.setRotation(0);
                }
            }
        }

        @Override
        public int getItemCount() {
            return filterList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (filterList.get(position).getType().equals(Constants.TAG_RANGE)) {
                return VIEW_TYPE_RANGE;
            } else if (filterList.get(position).getType().equals(Constants.TAG_DROPDOWN)) {
                return VIEW_TYPE_DROPDOWN;
            } else {
                return VIEW_TYPE_MULTILEVEL;
            }
        }

        public class RangeViewHolder extends RecyclerView.ViewHolder {
            TextView txtRangeLabel;
            EditText edtRangeValue;
            LinearLayout itemLay;
            View divider;

            public RangeViewHolder(@NonNull View itemView) {
                super(itemView);
                txtRangeLabel = itemView.findViewById(R.id.txtRangeLabel);
                edtRangeValue = itemView.findViewById(R.id.edtRangeValue);
                itemLay = itemView.findViewById(R.id.itemLay);
                divider = itemView.findViewById(R.id.divider);
            }
        }

        public class DropdownViewHolder extends RecyclerView.ViewHolder {
            TextView txtFilterName, txtSelectFilter;
            ImageView btnNext;
            LinearLayout itemLay;
            View divider;

            public DropdownViewHolder(@NonNull View itemView) {
                super(itemView);
                txtFilterName = itemView.findViewById(R.id.txtFilterName);
                txtSelectFilter = itemView.findViewById(R.id.txtSelectFilter);
                btnNext = itemView.findViewById(R.id.btnNext);
                itemLay = itemView.findViewById(R.id.itemLay);
                divider = itemView.findViewById(R.id.divider);
            }
        }

        public class MultilevelViewHolder extends RecyclerView.ViewHolder {
            TextView txtFilterName, txtSelectFilter;
            ImageView btnNext;
            LinearLayout itemLay;
            View divider;

            public MultilevelViewHolder(@NonNull View itemView) {
                super(itemView);
                txtFilterName = itemView.findViewById(R.id.txtFilterName);
                txtSelectFilter = itemView.findViewById(R.id.txtSelectFilter);
                btnNext = itemView.findViewById(R.id.btnNext);
                itemLay = itemView.findViewById(R.id.itemLay);
                divider = itemView.findViewById(R.id.divider);
            }
        }
    }

    public static Bitmap createFlippedBitmap(Bitmap source) {
        Matrix matrix = new Matrix();
        matrix.postScale(-1, 1, source.getWidth() / 2f, source.getHeight() / 2f);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    /**
     * class for uploading images to server
     **/
    class UploadImageTask extends AsyncTask<String, Integer, Integer> {
        JSONObject jsonobject = null;
        String responseString;
        int uploadCount = 0;
        String status, message = "";
        ArrayList<HomeItemResponse.Photo> uploadImageList = new ArrayList<>();

        public UploadImageTask(ArrayList<HomeItemResponse.Photo> imageList) {
            uploadImageList = new ArrayList<>();
            for (HomeItemResponse.Photo photo : imageList) {
                if (!photo.getType().equals(Constants.KEY_URL) && !photo.getType().equals(Constants.TAG_ADD)) {
                    uploadImageList.add(photo);
                }
            }

            uploadCount = uploadImageList.size();
            loadingProgress.setMax(uploadCount);
        }

        @Override
        protected Integer doInBackground(String... imgpath) {
            for (int i = 0; i < uploadImageList.size(); i++) {
                publishProgress(Math.min(i, count));

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
                int maxBufferSize = 1 * 1024 * 1024;
                String urlString = Constants.API_UPLOAD_IMAGE;
                Bitmap bitmapImage = null;
                String exsistingFileName = uploadImageList.get(i).getImage();
                File file = new File(exsistingFileName);
                Log.d(TAG, "doInBackground: " + file.getAbsolutePath());
                File newFile = null;

                if (uploadImageList.get(i).getPathType().equals(Constants.TAG_GALLERY)) {
                    try {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        // bitmapImage = BitmapFactory.decodeFile(file.getAbsolutePath());
                        // int inSampleSize = JoysaleApplication.calculateInSampleSize(options,
                        // bitmapImage.getWidth(), bitmapImage.getHeight());
                        // Log.i(TAG, "doInBackground: " + inSampleSize);
                        // Get length of file in bytes
                        long fileSizeInBytes = file.length();
                        // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
                        long fileSizeInKB = fileSizeInBytes / 1024;
                        // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
                        long fileSizeInMB = fileSizeInKB / 1024;
                        Log.i(TAG, "originalSize: " + i + ": " + fileSizeInKB + " KB");
                        Log.i(TAG, "originalSize: " + i + ": " + fileSizeInMB + " MB");
                        if (fileSizeInMB > 2) {
                            options.inSampleSize = 4;
                            bitmapImage = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                        } else {
                            bitmapImage = BitmapFactory.decodeFile(file.getAbsolutePath());
                        }

                        Log.d(TAG, "doInBackground: " + bitmapImage);

                        bitmapImage = compressImage(file.getAbsolutePath());
                        newFile = saveBitmapToFile(bitmapImage, file.getName());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 4;
                    newFile = new File(uploadImageList.get(i).getImage());
                    bitmapImage = BitmapFactory.decodeFile(newFile.getAbsolutePath(), options);
                    ExifInterface ei = null;
                    try {
                        ei = new ExifInterface(file.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED);

                    Log.d(TAG, "doInBackgroundOrientation: " + orientation);

                    switch (orientation) {

                        case ExifInterface.ORIENTATION_ROTATE_90:
                            bitmapImage = rotateImage(bitmapImage, 90);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_180:
                            bitmapImage = rotateImage(bitmapImage, 180);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_270:
                            bitmapImage = rotateImage(bitmapImage, 270);
                            break;
                        case ExifInterface.ORIENTATION_TRANSVERSE:
                            bitmapImage = rotateImage(bitmapImage, 270);
                            bitmapImage = createFlippedBitmap(bitmapImage);
                            break;

                        case ExifInterface.ORIENTATION_NORMAL:
                            bitmapImage = rotateImage(bitmapImage, 0);
                            break;
                        default:
                            bitmapImage = bitmapImage;
                    }
                    newFile = saveBitmapToFile(bitmapImage, file.getName());
                }

                Log.d(TAG, "doInBackground: " + newFile);

                if (newFile != null) {
                    // Get length of file in bytes
                    long fileSizeInBytes = newFile.length();
                    // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
                    long fileSizeInKB = fileSizeInBytes / 1024;
                    // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
                    long fileSizeInMB = fileSizeInKB / 1024;
                    Log.i(TAG, "compressedSize: " + i + ": " + fileSizeInKB + " KB");
                    Log.e(TAG, "compressedSize: " + i + ": " + fileSizeInMB + " MB");

                    try {
                        refreshGallery(newFile);
                        FileInputStream fileInputStream = new FileInputStream(newFile);
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
                        dos.writeBytes("Content-Disposition: form-data;name=\"type\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes("item");
                        dos.writeBytes(lineEnd);

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"images\";filename=\""
                                + exsistingFileName + "\"" + lineEnd);
                        dos.writeBytes(lineEnd);
                        Log.e("MediaPlayer", "Headers are written");

                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        buffer = new byte[bufferSize];
                        // int sentBytes=0;
                        // Read file
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                        while (bytesRead > 0) {
                            try {
                                dos.write(buffer, 0, bufferSize);
                            } catch (OutOfMemoryError e) {
                                e.printStackTrace();
                                Log.e(TAG, "doInBackground: " + e.getMessage());
                            }
                            // sentBytes += bufferSize;
                            // publishProgress(Math.min(i, count), (int)(sentBytes * 100 / bytesAvailable));
                            bytesAvailable = fileInputStream.available();
                            bufferSize = Math.min(bytesAvailable, maxBufferSize);
                            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                            Log.v(TAG, "bytesRead" + bytesRead);
                        }
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                        // Responses from the server (code and message)
                        int serverResponseCode = conn.getResponseCode();
                        String serverResponseMessage = conn.getResponseMessage();
                        dos.flush();
                        fileInputStream.close();
                        System.out.println("Server Response Code " + " " + serverResponseCode);
                        System.out.println("Server Response Message " + serverResponseMessage);

                        conn.getInputStream();
                        // for android InputStream is = connection.getInputStream();
                        InputStream is = conn.getInputStream();

                        int ch;
                        StringBuilder b = new StringBuilder();
                        while ((ch = is.read()) != -1) {
                            b.append((char) ch);
                        }

                        responseString = b.toString();
                        dos.close();
                        dos = null;
                        Log.i(TAG, "doInBackgroundResponse: " + responseString);

                    } catch (IOException ex) {
                        Log.e(TAG, "IOException: " + ex.getMessage(), ex);
                    }

                    try {
                        jsonobject = new JSONObject(responseString);
                        status = jsonobject.getString("status");
                        if (jsonobject.has("message")) {
                            message = jsonobject.getString("message");
                        }
                        if (status.equals("true")) {
                            JSONObject image = jsonobject.getJSONObject("Image");
                            message = image.getString("Message");
                            uploadedImage.add(image.getString("Name"));
                        } else if (status.equals("false")) {
                            break;
                        }

                    } catch (JSONException e) {
                        status = "false";
                        Log.e(TAG, "doInBackground: " + e.getMessage());
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        status = "false";
                        Log.e(TAG, "doInBackground: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    status = "Error";
                    Log.e(TAG, "doInBackground: " + "Error");
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            loadingProgress.setProgress(0);

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Log.v(TAG, "values[0]: " + values[0]);
            loadingProgress.setProgress(values[0]);
            uploadStatus.setText(
                    values[0] + " " + getString(R.string.of) + " " + count + getString(R.string.image_uploaded));
        }

        @Override
        protected void onPostExecute(Integer unused) {
            if (status.equals("true")) {
                if (message.equalsIgnoreCase("Invalid image")) {
                    dialog.dismiss();
                    JoysaleApplication.uploadAlertDialog(AddProductDetail.this, getString(R.string.alert),
                            getString(R.string.image_cannot_moderator));
                } else {
                    loadingProgress.setProgress(count);
                    uploadStatus.setText(
                            count + " " + getString(R.string.of) + " " + count + getString(R.string.image_uploaded));
                    loadingProgress.setVisibility(View.GONE);
                    uploadStatus.setVisibility(View.GONE);
                    alert_title.setText(getString(R.string.posting_list));
                    postProgress.setVisibility(View.VISIBLE);
                    if (from.equals(Constants.TAG_EDIT) && uploadedImage.size() > 0) {
                        ArrayList<String> tempAry = new ArrayList<>(uploadedImage);
                        uploadedImage = new ArrayList<>();
                        int index = 0;
                        // Log.i(TAG, "onPostExecute: " + new Gson().toJson(images));
                        // Log.i(TAG, "onPostExecute: " + new Gson().toJson(tempAry));
                        for (HomeItemResponse.Photo image : images) {
                            if (image.getType().equals(Constants.KEY_URL)) {
                                uploadedImage.add(image.getItemImage());
                            } else {
                                if (tempAry.size() > index) {
                                    String imageUrl = tempAry.get(index);
                                    uploadedImage.add(imageUrl);
                                    index++;
                                }
                            }
                        }
                    }
                    addProductApi();
                }
            } else if (status.equalsIgnoreCase("false")) {
                if (message.equalsIgnoreCase("Image cannot be uploaded")) {
                    dialog.dismiss();

                    JoysaleApplication.uploadAlertDialog(AddProductDetail.this, getString(R.string.alert),
                            getString(R.string.image_cannot_moderator));

                }
            } else if (status.equalsIgnoreCase("Error")) {
                dialog.dismiss();
                JoysaleApplication.uploadAlertDialog(AddProductDetail.this, getString(R.string.alert),
                        getString(R.string.select_your_imageName));

            }
        }
    }

    public Bitmap compressImage(String imagePath) {
        Bitmap scaledBitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(imagePath, options);
        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;
        float imgRatio = (float) actualWidth / (float) actualHeight;
        float maxRatio = maxWidth / maxHeight;
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;
            }
        }
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];
        try {
            bmp = BitmapFactory.decodeFile(imagePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.RGB_565);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }
        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);
        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2,
                new Paint(Paint.FILTER_BITMAP_FLAG));
        if (bmp != null) {
            bmp.recycle();
        }
        androidx.exifinterface.media.ExifInterface exif;
        try {
            exif = new androidx.exifinterface.media.ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION, 0);
            Log.i(TAG, "compressImageorientation: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(),
                    matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return scaledBitmap;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }
        return inSampleSize;
    }

    private void getAdminData() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(AddProductDetail.this));

            Call<AdminDataResponse> call = apiInterface.getAdminData(map);
            call.enqueue(new Callback<AdminDataResponse>() {
                @Override
                public void onResponse(Call<AdminDataResponse> call, retrofit2.Response<AdminDataResponse> response) {
                    if (response.isSuccessful()) {
                        AdminDataResponse adminDataResponse = response.body();
                        if (adminDataResponse.getStatus().equalsIgnoreCase("true")) {
                            Log.d(TAG, "onResponse: " + adminDataResponse.getResult().toString());
                            AdminDataResponse.Result result = adminDataResponse.getResult();
                            try {
                                // Log.d(TAG, "onResponseAdmin: "+result.getCategory().size());
                                if (result.getPriceRange() != null) {
                                    SplashActivity.beforeDecimal = result.getPriceRange().getBeforeDecimal();
                                    SplashActivity.afterDecimal = result.getPriceRange().getAfterDecimal();

                                    if (isRTL) {
                                        isValidPrice = true;
                                        price.setFilters(
                                                new InputFilter[] { new JoysaleApplication.DecimalDigitsInputFilter(
                                                        SplashActivity.beforeDecimal, SplashActivity.afterDecimal) });
                                        shippingFee.setFilters(
                                                new InputFilter[] { new JoysaleApplication.DecimalDigitsInputFilter(
                                                        SplashActivity.beforeDecimal, SplashActivity.beforeDecimal) });
                                    } else {
                                        price.setFilters(
                                                new InputFilter[] { new JoysaleApplication.DecimalDigitsInputFilter(
                                                        SplashActivity.beforeDecimal, SplashActivity.afterDecimal) });
                                        shippingFee.setFilters(
                                                new InputFilter[] { new JoysaleApplication.DecimalDigitsInputFilter(
                                                        SplashActivity.beforeDecimal, SplashActivity.beforeDecimal) });
                                    }
                                }

                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<AdminDataResponse> call, Throwable t) {
                    t.printStackTrace();
                    call.cancel();
                }
            });
        }
    }

}
