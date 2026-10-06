package com.ni.SnatchItUp;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.POST_NOTIFICATIONS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.ni.utils.Constants.ITEM_LIMIT;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.gson.Gson;
import com.ni.addons.BannerAdUtils;
import com.ni.addons.DialogOverLayPermission;

import com.ni.addons.buynow.MySalesnOrder;
import com.ni.addons.call.appRTC.util.AppRTCUtils;

import com.ni.addons.productlimit.PostDetailsResponse;
import com.ni.addons.productlimit.ProductLimitation;
import com.ni.addons.productlimit.SubscriptionViewActivity;
import com.ni.camerax.CameraViewActivity;
import com.ni.external.AutoScrollViewPager;
import com.ni.external.BadgeView;
import com.ni.external.GridRecyclerOnScrollListener;
import com.ni.external.RecyclerItemClickListener;
import com.ni.external.TimeAgo;
import com.ni.external.pagerindicator.ScrollingPagerIndicator;
import com.ni.helper.ItemAdapter;
import com.ni.helper.LocaleManager;
import com.ni.helper.Model;
import com.ni.helper.NetworkReceiver;
import com.ni.helper.PermissionsUtils;
import com.ni.helper.callback.OkCallback;
import com.ni.helper.callback.OkayCancelCallback;
import com.ni.helper.worker.GetLocationWorker;
//import com.ni.mapbox.MainActivity;
import com.ni.model.AdminDataResponse;
import com.ni.model.AffiliateItemsResponse;
import com.ni.model.BeforeAddResponse;
import com.ni.model.GetCountResponse;
import com.ni.model.HomeItemResponse;
import com.ni.model.ProfileResponse;
import com.ni.utils.ApiClient;
import com.ni.utils.ApiInterface;
import com.ni.utils.AppUtils;
import com.ni.utils.Constants;
import com.ni.utils.DiscountUtils;
import com.ni.utils.GetSet;
import com.ni.utils.PhotoUtils;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by hitasoft.
 * <p>
 * This class is for User Home Page.
 */
public class FragmentMainActivity extends BaseActivity
        implements OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    /**
     * Declare Variables
     **/
    final static int REQUEST_CHECK_SETTINGS_GPS = 0x1, REQUEST_ID_MULTIPLE_PERMISSIONS = 0x2,
            REQUEST_CAMERA_PERMISSION = 0x3;
    private static final String TAG = FragmentMainActivity.class.getSimpleName();
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private static final int ALPHA_ANIMATIONS_DURATION = 1000, LOCATION_VISIBILITY_DURATION = 3000;
    // TextView login, userid, ratingCount, locationTxt;
    // addons
    public static int currentPage;
    public static TextView login, userid, ratingCount, locationTxt;
    public static ItemViewAdapter itemAdapter;
    public static GridRecyclerOnScrollListener mScrollListener;
    public static String chatCount = "", homeBanner = "";
    public static List<HomeItemResponse.Item> homeItemList = new ArrayList<>();
    public static List<AdminDataResponse.BannerData> bannerAry = new ArrayList<>();
    public static List<BeforeAddResponse.Category> categoryAry = new ArrayList<>();
    public static List<BeforeAddResponse.Subcategory> subcategoryList = new ArrayList<>();
    // for addon audio and video call
    public static boolean isInVideoCall;
    public static boolean resumeHome = false, resumeMessage = false;
    static TextView username;
    static ImageView userImage;
    public ListView listView;
    public ItemAdapter adapter;
    public FilterAdapter filterAdapter;
    public ArrayList<HashMap<String, String>> filterAry = new ArrayList<>();
    ImageView titleImage, menu_btn, filter_btn, search_btn, notifybtn, filterBadge;
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    LinearLayout profheader, proflogin, nullLay, left_drawer;
    RelativeLayout locationLay, headerLay, reviewLay;
    FrameLayout filterLay;
    Toolbar toolbar;
    SwipeRefreshLayout swipeLayout;
    AVLoadingIndicatorView progress;
    View filterView;
    BadgeView notifyBadge;
    RatingBar ratingBar;
    ExtendedFloatingActionButton btnAddStuff;
    Dialog inviteDialog = null;
    ApiInterface apiInterface;
    BannerPagerAdapter bannerPagerAdapter;
    NpaGridLayoutManager itemManager;
    RecyclerView categoryRecycler, filterRecycler, itemRecycler;
    List<String> idList = new ArrayList<>();
    Location mCurrentLocation;
    LocationManager locationManager;
    Button btnAdvertise;
    AdminDataResponse.BannerData bannerData = new AdminDataResponse().new BannerData();
    int mDrawerPosition = 0;
    boolean pulldown = false, mDrawerItemClicked = false;
    private int affiliatePhotoRequestId = 0;
    String languageCode;
    // Addon ProductLimitation
    ArrayList<PostDetailsResponse.Result> urgentAry = new ArrayList<>();
    PostDetailsResponse.Result data = new PostDetailsResponse().new Result();

    DialogOverLayPermission dialogOverLayPermission;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    SharedPreferences sharedPrefs;
    SharedPreferences.Editor editor1;
    Boolean isSettingsIn = false;
    Boolean isAutoStartIn = false;
    /**
     * Declare Layout Elements
     **/
    private Boolean isItemClicked = false;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private boolean mRequestingLocationUpdates = true;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean isGPSOn, locationLayVisible = true;
    private BeforeAddResponse.Category category;
    private BeforeAddResponse.Subcategory subCategory;
    private BeforeAddResponse.ChildCategory childCategory;
    private String selectedCategoryId = "", selectedSubCategoryId = "", selectedChildCatId = "",
            selectedCategoryName = "",
            selectedSubCategoryName = "", selectedChildCatName = "", postedWithin = "", postedTxt = "", location = "",
            selectedCondition = "", selectedConditionId = "", searchQuery = "";
    private double lat, lng;
    private String currencySymbol = "", currencyCode = "";
    private ArrayList<BeforeAddResponse.Filters> filterList = new ArrayList<>();
    private Handler locationHandler = new Handler();
    private Runnable locationRunnable = new Runnable() {
        @Override
        public void run() {
            if (locationLayVisible) {
                startAlphaAnimation(locationLay, ALPHA_ANIMATIONS_DURATION, GONE);
                locationLayVisible = false;
            }
        }
    };
    private int priceMin, priceMax, zeroMax = 1, screenHalf, bannerHeight;
    private String searchType, distance;
    private boolean isRTL = false;
    private NumberFormat numberFormat;
    private HashMap<String, ArrayList<String>> childIdMap = new HashMap<>(), childLabelMap = new HashMap<>();
    private String groupPosition;

    public static void startAlphaAnimation(View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }

    public ActivityResultLauncher<String[]> notificationPermission;
    private ActivityResultLauncher<String[]> phonepermissionresult;
    public String from = "";
    private ActivityResultLauncher<String[]> locations;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_frame);
        isRTL = LocaleManager.isRTL(this);
        preferences = getSharedPreferences("SavedPref", MODE_PRIVATE);
        editor = preferences.edit();
        sharedPrefs = getSharedPreferences("MyPref", 0);
        editor1 = sharedPrefs.edit();
        numberFormat = NumberFormat.getInstance(new Locale("en", "US"));
        numberFormat.setMaximumFractionDigits(2);
        searchType = "all";
        Constants.selectedCategory = null;
        Constants.selectedSubCategory = null;
        Constants.selectedChildCategory = null;
        // For Getting Side Menu itemList
        Model.LoadModel(FragmentMainActivity.this);
        Log.d(TAG,
                "onCreate: " + Constants.pref.getString(Constants.TAG_ACCESS_TOKENS, Constants.TAG_ACCESS_TOKENS_PREF));
        final String[] ids = new String[Model.items.size()];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = Integer.toString(i + 1);
        }

        initlauncher();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermission.launch(PermissionsUtils.NOTIFICATION_PERMISSION);
            }
            /*
             * if (ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) !=
             * PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
             * ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
             * Log.d(TAG, "onCreate: "+"locations");
             * locations.launch(PermissionsUtils.LOCATIONS);
             * 
             * }
             */
            if (Constants.ADDON_CALL_ENABLED) {
                if (!isPermissionsGranted(AppRTCUtils.MANDATORY_PERMISSIONSFORANDROID12)) {
                    phonepermissionresult.launch(AppRTCUtils.MANDATORY_PERMISSIONSFORANDROID12);
                }
            }
        }
        if (SDK_INT >= Build.VERSION_CODES.S) {
            if (!isPermissionsGranted(AppRTCUtils.MANDATORY_PERMISSIONSFORANDROID12)) {
                phonepermissionresult.launch(PermissionsUtils.READ_PHONE_STATEPERMISSION);
            }
            if (ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this,
                            ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                locations.launch(PermissionsUtils.LOCATIONS);

            }
        }
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Elements initialisation
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        menu_btn = (ImageView) findViewById(R.id.menubtn);
        filter_btn = (ImageView) findViewById(R.id.homefilterbtn);
        search_btn = (ImageView) findViewById(R.id.searchbtn);
        listView = findViewById(R.id.list);
        profheader = (LinearLayout) findViewById(R.id.profile_header);
        proflogin = (LinearLayout) findViewById(R.id.profile_login);
        headerLay = (RelativeLayout) findViewById(R.id.header);
        login = (TextView) findViewById(R.id.login);
        username = (TextView) findViewById(R.id.userName);
        userid = (TextView) findViewById(R.id.userId);
        userImage = (ImageView) findViewById(R.id.userImage);
        titleImage = (ImageView) findViewById(R.id.titleimg);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
        nullLay = (LinearLayout) findViewById(R.id.nullLay);
        btnAddStuff = findViewById(R.id.btnAddStuff);
        locationLay = (RelativeLayout) findViewById(R.id.locationLay);
        locationTxt = (TextView) findViewById(R.id.locationTxt);
        filterRecycler = findViewById(R.id.filterRecycler);
        filterView = (View) findViewById(R.id.filterView);
        notifybtn = (ImageView) findViewById(R.id.notifybtn);
        // Addon BuyNow
          reviewLay = (RelativeLayout) findViewById(R.id.reviewLay);
          ratingBar = (RatingBar) findViewById(R.id.ratingBar);
          ratingCount = (TextView) findViewById(R.id.ratingCount);

        left_drawer = (LinearLayout) findViewById(R.id.left_drawer);
        itemRecycler = (RecyclerView) findViewById(R.id.recyclerView);
        btnAdvertise = (Button) findViewById(R.id.btnAdvertise);
        filterLay = findViewById(R.id.filterLay);
        filterBadge = findViewById(R.id.filterBadge);

        notifyBadge = new BadgeView(FragmentMainActivity.this, notifybtn);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        swipeLayout.setProgressViewOffset(false, 0, JoysaleApplication.dpToPx(FragmentMainActivity.this, 70));

        // Elements Visibility
        filter_btn.setVisibility(View.VISIBLE);
        search_btn.setVisibility(View.VISIBLE);
        titleImage.setVisibility(View.VISIBLE);
        nullLay.setVisibility(GONE);
        progress.setVisibility(GONE);

        bannerPagerAdapter = new BannerPagerAdapter(this, bannerAry);
        // Adapter for side menu
        adapter = new ItemAdapter(FragmentMainActivity.this, R.layout.menu_list_item, ids);
        // Log.d(TAG, "onCreateId: "+ids.length);
        listView.setAdapter(adapter);

        toggle = new ActionBarDrawerToggle(this, drawer, null, R.string.open, R.string.close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (GetSet.isLogged()) {
                    getCountDetails();
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (mDrawerItemClicked) {
                    mDrawerItemClicked = false;
                    setLocationView();
                    openActivity(Model.GetbyId(Integer.parseInt(ids[mDrawerPosition])).name);
                }
            }
        };

        Constants.filpref = getApplicationContext().getSharedPreferences("FilterPref", MODE_PRIVATE);
        Constants.fileditor = Constants.filpref.edit();

        // Addon Mapbox
        /*
         * if (Constants.ADDON_MAP_BOX_ENABLED) {
         * MainActivity mainActivity = new MainActivity();
         * MainActivity.location = Constants.filpref.getString("location",
         * getString(R.string.world_wide));
         * mainActivity.lat =
         * Double.parseDouble(Constants.filpref.getString(Constants.TAG_LAT, "0"));
         * mainActivity.lng =
         * Double.parseDouble(Constants.filpref.getString(Constants.TAG_LON, "0"));
         * mainActivity.locationRemoved =
         * Constants.filpref.getBoolean("locationRemoved", false);
         * }
         */
        location = Constants.filpref.getString(Constants.TAG_LOCATION, getResources().getString(R.string.world_wide));
        if (Constants.filpref.getBoolean(Constants.TAG_LOCATION_REMOVED, false)) {
            location = getString(R.string.world_wide);
        }
        lat = Double.parseDouble(Constants.filpref.getString(Constants.TAG_LAT, "0"));
        lng = Double.parseDouble(Constants.filpref.getString(Constants.TAG_LON, "0"));
        // To get FilterArray
        getFilterAry();
        setFilterAdapter();
        initPermissionDialog();

        drawer.addDrawerListener(toggle);
        drawer.post(new Runnable() {
            @Override
            public void run() {
                toggle.syncState();
            }
        });

        screenHalf = (displayMetrics.widthPixels * 50 / 100) - JoysaleApplication.dpToPx(this, 30);

        // To set Grid Layout manager
        itemRecycler.setHasFixedSize(true);
        itemRecycler.setItemViewCacheSize(24);
        itemRecycler.setItemAnimator(null);
        itemRecycler.getRecycledViewPool().setMaxRecycledViews(ItemViewAdapter.TYPE_ITEM, 24);
        itemManager = new NpaGridLayoutManager(FragmentMainActivity.this, 2, GridLayoutManager.VERTICAL, false);
        itemManager.setInitialPrefetchItemCount(8);
        itemRecycler.setLayoutManager(itemManager);

        // To initialize the adapter
        itemAdapter = new ItemViewAdapter(FragmentMainActivity.this, homeItemList);
        itemRecycler.setAdapter(itemAdapter);

        GridLayoutManager.SpanSizeLookup spanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (itemAdapter.getItemViewType(position) == ItemViewAdapter.TYPE_ITEM) ? 1 : 2;
            }
        };
        spanSizeLookup.setSpanIndexCacheEnabled(true);
        itemManager.setSpanSizeLookup(spanSizeLookup);

        if (bannerAry.size() > 0) {
            locationHandler.postDelayed(locationRunnable, LOCATION_VISIBILITY_DURATION);
        }

        mScrollListener = new GridRecyclerOnScrollListener(itemManager) {
            @Override
            public void onLoadMore(int current_page) {
                if (!swipeLayout.isRefreshing()) {
                    initializeHomeUI();
                    loadHomeItemList(current_page * ITEM_LIMIT);
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                    int lastItem = layoutManager.findLastCompletelyVisibleItemPosition();
                    if (bannerAry.size() == 0 && lastItem <= 4) {
                        locationLay.setAlpha(1f);
                        locationLay.setVisibility(VISIBLE);
                    } else if (homeItemList.size() == 0) {
                        setLocationView();
                        locationLay.setVisibility(VISIBLE);
                    } else if (lastItem > 4 || (bannerAry.size() > 0 && lastItem > 2)) {
                        if (!locationLayVisible) {
                            setLocationView();
                            startAlphaAnimation(locationLay, ALPHA_ANIMATIONS_DURATION, VISIBLE);
                            locationLayVisible = true;
                        }
                        locationHandler.postDelayed(locationRunnable, LOCATION_VISIBILITY_DURATION);
                    } else if (bannerAry.size() == 0) {
                        locationLay.setAlpha(1f);
                        locationLay.setVisibility(VISIBLE);
                    }
                }
            }
        };

        itemRecycler.addOnScrollListener(mScrollListener);

        float scale = (float) displayMetrics.widthPixels / Constants.HOME_BANNER_WIDTH;
        bannerHeight = (int) Math.round(Constants.HOME_BANNER_HEIGHT * scale);

        // Elements Listener
        listView.setOnItemClickListener(new DrawerItemClickListener());
        login.setOnClickListener(this);
        filter_btn.setOnClickListener(this);
        filterLay.setOnClickListener(this);
        search_btn.setOnClickListener(this);
        menu_btn.setOnClickListener(this);
        swipeLayout.setOnRefreshListener(this);
        locationLay.setOnClickListener(this);
        login.setOnClickListener(this);
        btnAddStuff.setOnClickListener(this);
        headerLay.setOnClickListener(this);
        notifybtn.setOnClickListener(this);
        btnAdvertise.setOnClickListener(this);
        languageCode = LocaleManager.getLanguageCode(this);

        // For Set Login & Logout State
        Constants.pref = getApplicationContext().getSharedPreferences("JoysalePref",
                MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();
        if (Constants.pref.getBoolean("isLogged", false)) {
            GetSet.setLogged(true);
            GetSet.setUserId(Constants.pref.getString(Constants.TAG_USER_ID, null));
            GetSet.setUserName(Constants.pref.getString(Constants.TAG_USERNAME, null));
            GetSet.setEmail(Constants.pref.getString(Constants.TAG_EMAIL, null));
            GetSet.setPassword(Constants.pref.getString(Constants.TAG_PASSWORD, null));
            GetSet.setFullName(Constants.pref.getString(Constants.TAG_FULL_NAME, null));
            GetSet.setImageUrl(Constants.pref.getString(Constants.TAG_PHOTO, null));
            GetSet.setRating(Constants.pref.getString(Constants.TAG_RATING, "0"));
            GetSet.setRatingUserCount(Constants.pref.getString(Constants.TAG_RATING_USER_COUNT, "0"));
            profheader.setVisibility(View.VISIBLE);
            proflogin.setVisibility(GONE);
            btnAdvertise.setVisibility(Constants.PAIDBANNER ? View.VISIBLE : GONE);

        } else {
            profheader.setVisibility(GONE);
            proflogin.setVisibility(View.VISIBLE);
        }

        setNavigationUI();

        // Addon BuyNow
        /*
         * LayerDrawable stars = (LayerDrawable)
         * ratingBar.getProgressDrawable().getCurrent();
         * stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.starColor
         * ), PorterDuff.Mode.SRC_ATOP);
         * stars.getDrawable(0).setColorFilter(getResources().getColor(R.color.white),
         * PorterDuff.Mode.SRC_ATOP);
         * stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.starColor
         * ), PorterDuff.Mode.SRC_ATOP);
         * swipeLayout.setColorSchemeColors(getResources().getColor(R.color.
         * progressColor));
         */

        // REQUEST LOCATION
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);
        if (!isGPSOn)
            turnGPSOn();
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
        if (SDK_INT < Build.VERSION_CODES.S) {
            if (!checkPermissions(Constants.LOCATION_PERMISSIONS, FragmentMainActivity.this)) {
                ActivityCompat.requestPermissions(FragmentMainActivity.this, Constants.LOCATION_PERMISSIONS,
                        Constants.LOCATION_PERMISSION_CODE);
            }
        }

        // To set Location
        setLocation();

        // To get Home data from Api
        loadData();
    }

    public String getLatLongByURL(String requestURL) {
        URL url;
        String response = "";
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                response = "";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * Set a Navigation View UI
     **/

    private void requestOverLayPermission() {
        // Check if Android M or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Show alert dialog to the user saying a separate permission is needed
            // Launch the settings activity if the user prefers
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, Constants.OVERLAY_REQUEST_CODE);
        }
    }

    private void initPermissionDialog() {
        dialogOverLayPermission = new DialogOverLayPermission();
        dialogOverLayPermission.setContext(this);
        dialogOverLayPermission.setCancelable(false);
        dialogOverLayPermission.setCallBack(new OkCallback() {
            @Override
            public void onOkClicked(Object o) {
                boolean isAllow = (boolean) o;
                if (isAllow) {
                    requestOverLayPermission();
                }
                dialogOverLayPermission.dismissAllowingStateLoss();
            }
        });
    }

    // Load Home page

    private void setNavigationUI() {
        if (GetSet.isLogged()) {
            profheader.setVisibility(View.VISIBLE);
            proflogin.setVisibility(GONE);
            username.setText(GetSet.getFullName());
            // userid.setText(GetSet.getUserName());
            if (GetSet.getImageUrl() != null && !GetSet.getImageUrl().equals("")) {
                Picasso.get()
                        .load(GetSet.getImageUrl())
                        .centerCrop()
                        .fit()
                        .placeholder(R.drawable.appicon)
                        .error(R.drawable.appicon)
                        .into(userImage);
            }
        } else {
            profheader.setVisibility(GONE);
            proflogin.setVisibility(View.VISIBLE);
        }

          if (Constants.BUYNOW) {
          reviewLay.setVisibility(View.VISIBLE);
          userid.setVisibility(GONE);

          if (GetSet.getRating() == null || TextUtils.isEmpty(GetSet.getRating())) {
          ratingBar.setRating(0);
          } else {
          ratingBar.setRating(Float.parseFloat(GetSet.getRating()));
          }

          if (GetSet.getRatingUserCount() == null ||
          TextUtils.isEmpty(GetSet.getRatingUserCount())) {
          ratingCount.setText("(0)");
          } else {
          ratingCount.setText("(" + GetSet.getRatingUserCount() + ")");
          }
          } else {
        // reviewLay.setVisibility(GONE);
        userid.setVisibility(View.VISIBLE);
         }
    }

    private void loadData() {
        if (homeItemList.size() == 0) {
            mScrollListener.resetpagecount();
            initializeHomeUI();
            loadHomeItemList(0);
        } else if (SearchAdvance.applyFilter) {
            SearchAdvance.applyFilter = false;
            swipeRefresh();
            mScrollListener.resetpagecount();
            pulldown = true;
            initializeHomeUI();
            loadHomeItemList(0);
        }
    }

    private void swipeRefresh() {

        swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                if (filterAry.size() > 0) {
                    idList = new ArrayList<>();
                    // searchType = null;
                } else if (filterAry.size() == 0) {
                    idList = new ArrayList<>();
                    searchType = "all";
                }

                swipeLayout.setRefreshing(true);
            }
        });
    }

    // addons

    /**
     * function for get the location from gps
     **/

    private void setLocation() {
        Log.d(TAG, "setLocation: " + "eeeeeee");
        if (Constants.filpref.getBoolean(Constants.TAG_LOCATION_REMOVED, false)) {
            locationTxt.setText(getString(R.string.world_wide));
        } else if (!location.equals(getString(R.string.world_wide))) {
            locationTxt.setText(location);
        } else {
            if (mCurrentLocation != null) {
                Log.d(TAG, "setLocation: " + lat + lng);
                lat = mCurrentLocation.getLatitude();
                lng = mCurrentLocation.getLongitude();
                refreshLocation(lat, lng);
            }
        }
    }

    /**
     * function for get the location from gps
     *
     * @param lat
     * @param lng
     */

    private void refreshLocation(double lat, double lng) {
        mScrollListener.resetpagecount();
        pulldown = true;

        Data.Builder builder = new Data.Builder();
        builder.putDouble(Constants.TAG_LAT, this.lat);
        builder.putDouble(Constants.TAG_LON, this.lng);
        builder.putString(Constants.TAG_FROM, "");
        builder.putString(Constants.TAG_COUNTRY_CODE, "");

        OneTimeWorkRequest locationWorkRequest = new OneTimeWorkRequest.Builder(GetLocationWorker.class)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .setInputData(builder.build())
                .build();
        WorkManager workManager = WorkManager.getInstance(FragmentMainActivity.this);
        workManager.enqueue(locationWorkRequest);
        workManager.getWorkInfoByIdLiveData(locationWorkRequest.getId()).observe(FragmentMainActivity.this,
                workInfo -> {
                    if (workInfo.getState().isFinished()) {
                        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            location = workInfo.getOutputData().getString(Constants.TAG_LOCATION);
                            locationTxt.setText(location);

                            Constants.fileditor.putString(Constants.TAG_LOCATION, location);
                            Constants.fileditor.commit();
                        }
                    }
                });
    }

    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.d(TAG, "onLocationResult: " + !Constants.filpref.getBoolean(Constants.TAG_LOCATION_REMOVED, false));
                if (!Constants.filpref.getBoolean(Constants.TAG_LOCATION_REMOVED, false)
                        && location.equals(getString(R.string.world_wide))) {
                    Log.d(TAG, "onLocationResult: " + "eeee");
                    mCurrentLocation = locationResult.getLastLocation();
                    Constants.fileditor.putString(Constants.TAG_LAT, String.valueOf(mCurrentLocation.getLatitude()));
                    Constants.fileditor.putString(Constants.TAG_LON, String.valueOf(mCurrentLocation.getLongitude()));

                    if (mCurrentLocation != null) {
                        lat = mCurrentLocation.getLatitude();
                        lng = mCurrentLocation.getLongitude();
                    }
                }
                mRequestingLocationUpdates = true;
                setLocation();
                Log.d(TAG, "onLocationResult: ");
                stopLocationUpdates();
                if (JoysaleApplication.isNetworkAvailable(FragmentMainActivity.this)) {
                    FragmentMainActivity.startAlphaAnimation(locationLay, 1000, View.VISIBLE);
                    locationLayVisible = true;
                    locationHandler.postDelayed(locationRunnable, LOCATION_VISIBILITY_DURATION);
                    initializeHomeUI();
                    loadHomeItemList(0);
                }
            }
        };
    }

    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are
        // available, or
        // you may receive them slower than requested. You may also receive updates
        // faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact,
        // and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Uses a
     * {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to
     * build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is
     * used for checking
     * if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * function for get the applied filters to Ary
     **/
    private void getFilterAry() {

        if (distance != null && !distance.equals("") && Integer.parseInt(distance) > 0) {
            String distanceType = JoysaleApplication.adminPref.getString(Constants.PREF_DISTANCE_TYPE, "km");
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Constants.TAG_TYPE, "distance");
            if (distanceType.equalsIgnoreCase("km")) {
                map.put(Constants.TAG_NAME,
                        getString(R.string.within) + " " + distance + " " + getString(R.string.kilometers));
            } else {
                map.put(Constants.TAG_NAME,
                        getString(R.string.within) + " " + distance + " " + getString(R.string.miles));
            }
            filterAry.add(map);
            // searchType = null;
        }

        if (!postedWithin.equals("")) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Constants.TAG_TYPE, "postedWithin");
            map.put(Constants.TAG_NAME, postedTxt);
            filterAry.add(map);
        }

        Log.d(TAG, "getFilterAry: " + zeroMax + "min" + priceMin + "max" + priceMax);
        if (zeroMax == 0 && priceMin == 0 && priceMax == 0) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Constants.TAG_TYPE, Constants.TAG_GIVING_AWAY);
            map.put(Constants.TAG_NAME, "Giving Away");
            /*
             * map.put(Constants.TAG_PRICE,"0-0");
             */
            filterAry.add(map);
        } else if (priceMin != 0 || priceMax != 0) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Constants.TAG_TYPE, Constants.TAG_PRICE);
            map.put(Constants.TAG_NAME, String.valueOf(priceMin) + "-" + String.valueOf(priceMax));
            filterAry.add(map);
        }

        if (!SearchAdvance.sortBy.equals("1")) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Constants.TAG_TYPE, "sortBy");
            map.put(Constants.TAG_NAME, SearchAdvance.sortTxt);
            filterAry.add(map);
        }

        if (SearchAdvance.sortBy.equalsIgnoreCase("5")) {
            searchType = null;
        }
        if (!searchQuery.equals("")) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Constants.TAG_TYPE, Constants.TAG_SEARCH);
            map.put(Constants.TAG_NAME, searchQuery);
            filterAry.add(map);
        }

        if (!TextUtils.isEmpty(selectedConditionId)) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Constants.TAG_TYPE, Constants.TAG_PRODUCT_CONDITION);
            map.put(Constants.TAG_CONDITION_ID, selectedConditionId);
            map.put(Constants.TAG_CONDITION_NAME, selectedCondition);
            filterAry.add(map);
        }

        for (BeforeAddResponse.Filters filters : filterList) {
            HashMap<String, String> map = new HashMap<String, String>();
            switch (filters.getType()) {
                case Constants.TAG_DROPDOWN:
                    if (filters.getSelectedChildId() != null && filters.getSelectedChildId().size() > 0) {
                        ArrayList<String> idList = filters.getSelectedChildId();
                        ArrayList<String> labelList = filters.getSelectedChildLabel();
                        for (int i = 0; i < idList.size(); i++) {
                            map = new HashMap<String, String>();
                            map.put(Constants.TAG_TYPE, Constants.TAG_DROPDOWN);
                            map.put(Constants.TAG_ID, idList.get(i));
                            map.put(Constants.TAG_NAME, labelList.get(i));
                            filterAry.add(map);
                        }
                    }
                    break;
                case Constants.TAG_RANGE:
                    if (Integer.parseInt(filters.getSelectedMinValue()) != Integer.parseInt(filters.getMinValue()) ||
                            Integer.parseInt(filters.getSelectedMaxValue()) != Integer
                                    .parseInt(filters.getMaxValue())) {
                        map = new HashMap<String, String>();
                        map.put(Constants.TAG_TYPE, Constants.TAG_RANGE);
                        map.put(Constants.TAG_ID, filters.getSelectedParentId());
                        map.put(Constants.TAG_NAME, filters.getLabel() + ": " + filters.getSelectedMinValue() + " - "
                                + filters.getSelectedMaxValue());
                        filterAry.add(map);
                    }
                    break;
                case Constants.TAG_MULTILEVEL:
                    if (filters.getSelectedChildId() != null && filters.getSelectedChildId().size() > 0) {
                        for (int i = 0; i < filters.getSelectedChildId().size(); i++) {
                            map = new HashMap<String, String>();
                            map.put(Constants.TAG_ID, filters.getSelectedChildId().get(i));
                            map.put(Constants.TAG_NAME, filters.getSelectedChildLabel().get(i));
                            map.put(Constants.TAG_TYPE, Constants.TAG_MULTILEVEL);
                            filterAry.add(map);
                        }
                    }
                    break;
            }
        }

        Log.i(TAG, "getFilterAry: " + new Gson().toJson(filterAry));
    }

    // To initialize the adapter
    private void setFilterAdapter() {
        if (filterAdapter == null) {
            filterAdapter = new FilterAdapter(FragmentMainActivity.this, filterAry);
            filterRecycler.setLayoutManager(
                    new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
            filterRecycler.setAdapter(filterAdapter);
        }
        filterAdapter.notifyDataSetChanged();

        if (filterAry.size() == 0) {
            filterRecycler.setVisibility(View.GONE);
            filterView.setVisibility(View.GONE);
        } else {
            filterRecycler.setVisibility(View.VISIBLE);
            filterView.setVisibility(View.VISIBLE);
        }
    }

    private RecyclerItemClickListener categoryItemClick(Context context, RecyclerView recyclerView) {

        RecyclerItemClickListener recyclerItemClickListener = new RecyclerItemClickListener(context, recyclerView,
                new RecyclerItemClickListener.OnItemClickListener() {

                    @Override
                    public void onItemClick(View view, int position) {
                        setLocationView();
                        Intent intent = new Intent(getApplicationContext(), SubCategoryActivity.class);
                        if (categoryAry.get(position).getSubcategory().size() > 0) {
                            intent.putExtra(Constants.TAG_FROM, Constants.TAG_HOME);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            intent.putExtra(Constants.CATEGORYID, categoryAry.get(position).getCategoryId());
                            intent.putExtra(Constants.TAG_CATEGORYNAME, categoryAry.get(position).getCategoryName());
                            intent.putExtra(Constants.TAG_SUBCATEGORY_ID, selectedSubCategoryId);
                            intent.putExtra(Constants.TAG_CHILD_CATEGORY_ID, selectedChildCatId);
                            if (groupPosition != null && !TextUtils.isEmpty(groupPosition)
                                    && !groupPosition.equals("null")
                                    && categoryAry.get(position).getCategoryId().equals(selectedCategoryId)) {
                                intent.putExtra(Constants.TAG_POSITION, groupPosition);
                            }

                            // MyData.getInstance().setSelectedSubCategoryList(categoryAry.get(position).getSubcategory());
                            Constants.selectedSubCategoryList = categoryAry.get(position).getSubcategory();
                            if (childCategory != null) {
                                Constants.selectedChildCategory = childCategory;
                            }
                            startActivityForResult(intent, Constants.CATEGORY_REQUEST_CODE);
                        } else {
                            filterList.clear();
                            selectedCategoryId = categoryAry.get(position).getCategoryId();
                            selectedCategoryName = categoryAry.get(position).getCategoryName();
                            selectedSubCategoryId = null;
                            selectedChildCatId = null;
                            selectedSubCategoryName = null;
                            selectedChildCatName = null;
                            groupPosition = null;
                            isItemClicked = true;
                            getProductsByFilters();
                        }
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }
                });

        return recyclerItemClickListener;
    }

    private void setLocationView() {
        locationHandler.removeCallbacks(locationRunnable);
    }

    private void enableDisableSwipeRefresh(boolean enabled) {
        if (enabled) {
            swipeLayout.setEnabled(true);
        } else {
            swipeLayout.setEnabled(false);
        }
    }

    /**
     * function for open the corresponding activity from sliding menu
     **/

    public void openActivity(String from) {
        if (from.equals(getString(R.string.sell_your_stuff))) {
            if (GetSet.isLogged()) {

                if (Constants.ADDON_PRODUCT_LIMIT)
                    getMyPostDetails();
                else {
                    if (SDK_INT >= Build.VERSION_CODES.Q) {
                        if (ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                                CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(FragmentMainActivity.this, new String[] { CAMERA },
                                    REQUEST_CAMERA_PERMISSION);
                        } else {
                            setLocationView();

                            if (Constants.ADDON_PRODUCT_LIMIT) {
                                getMyPostDetails();
                            } else {
                                Intent m = new Intent(FragmentMainActivity.this, CameraViewActivity.class);
                                m.putExtra(Constants.TAG_FROM, "home");
                                if (Constants.ADDON_MAP_BOX_ENABLED) {
                                    m.putExtra(Constants.TAG_LOCATION, location);
                                }
                                startActivity(m);
                            }
                        }
                    } else {
                        if (ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                                CAMERA) != PackageManager.PERMISSION_GRANTED
                                && ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                                        WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(FragmentMainActivity.this,
                                    new String[] { CAMERA, WRITE_EXTERNAL_STORAGE }, REQUEST_CAMERA_PERMISSION);
                        } else if (ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                                CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(FragmentMainActivity.this, new String[] { CAMERA },
                                    REQUEST_CAMERA_PERMISSION);
                        } else if (ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                                WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(FragmentMainActivity.this,
                                    new String[] { WRITE_EXTERNAL_STORAGE }, REQUEST_CAMERA_PERMISSION);
                        } else {

                            if (Constants.ADDON_PRODUCT_LIMIT) {
                                getMyPostDetails();
                            } else {
                                Intent m = new Intent(FragmentMainActivity.this, CameraViewActivity.class);
                                m.putExtra(Constants.TAG_FROM, "home");
                                if (Constants.ADDON_MAP_BOX_ENABLED) {
                                    m.putExtra(Constants.TAG_LOCATION, location);
                                }
                                startActivity(m);
                            }
                            setLocationView();
                        }
                    }
                }
            } else {
                Intent i = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
            }
        } else if (from.equalsIgnoreCase(getString(R.string.chats))) {
            if (GetSet.isLogged()) {
                Intent i = new Intent(FragmentMainActivity.this, MessageActivity.class);
                startActivity(i);
            } else {
                Intent i = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
            }
        } else if (from.equals(getString(R.string.categories))) {
            Intent c = new Intent(FragmentMainActivity.this, CategoryActivity.class);
            c.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            c.putExtra(Constants.TAG_FROM, Constants.TAG_HOME);
            if (selectedCategoryId != null) {
                c.putExtra(Constants.CATEGORYID, selectedCategoryId);
                c.putExtra(Constants.TAG_CATEGORYNAME, selectedCategoryName);
            }

            Constants.selectedSubCategory = subCategory;
            Constants.selectedChildCategory = childCategory;
            c.putExtra(Constants.TAG_SUBCATEGORY_ID, selectedSubCategoryId);
            c.putExtra(Constants.TAG_CHILD_CATEGORY_ID, selectedChildCatId);

            c.putExtra(Constants.TAG_POSITION, groupPosition);
            startActivityForResult(c, Constants.CATEGORY_REQUEST_CODE);
        } else if (from.equals(getString(R.string.myprofile))) {
            if (GetSet.isLogged()) {
                Intent i = new Intent(FragmentMainActivity.this, ProfileActivity.class);
                i.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                startActivity(i);
            } else {
                Intent i = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
            }
        } else if (from.equals(getString(R.string.myorders_sales))) {
            // Addon BuyNow
            if (GetSet.isLogged()) {
                Intent i = new Intent(FragmentMainActivity.this, MySalesnOrder.class);
                startActivity(i);
            } else {
                Intent i = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
            }
        } else if (from.equals(getString(R.string.myexchange))) {
            if (GetSet.isLogged()) {
                Intent i = new Intent(FragmentMainActivity.this, ExchangeActivity.class);
                startActivity(i);
            } else {
                Intent i = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
            }
        } else if (from.equals(getString(R.string.my_promotions))) {
            if (GetSet.isLogged()) {
                Intent i = new Intent(FragmentMainActivity.this, MyPromotions.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
            } else {
                Intent i = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
            }
        } else if (Constants.ADDON_PRODUCT_LIMIT && from.equals(getString(R.string.mypost_subs))) {
            // Addon ProductLimitation
            if (GetSet.isLogged()) {
                Intent i = new Intent(FragmentMainActivity.this, SubscriptionViewActivity.class);
                i.putExtra("From", "Main");
                startActivity(i);
            } else {
                Intent i = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                startActivity(i);
            }

        } else if (from.equals(getString(R.string.invite_friends))) {
            inviteDialog();
        } else if (from.equals(getString(R.string.theme))) {
            showThemeDialog();
        } else if (from.equals(getString(R.string.help))) {
            Intent Hl = new Intent(FragmentMainActivity.this, Help.class);
            startActivity(Hl);
        }
    }



    public void inviteDialog() {
        inviteDialog = new Dialog(FragmentMainActivity.this);
        inviteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        inviteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        inviteDialog.setContentView(R.layout.invite_dialog);

        inviteDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        Window window = inviteDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        // wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        inviteDialog.setCancelable(true);
        inviteDialog.setCanceledOnTouchOutside(false);

        RelativeLayout fblay, whatsapplay, emaillay;
        fblay = (RelativeLayout) inviteDialog.findViewById(R.id.fbLay);
        whatsapplay = (RelativeLayout) inviteDialog.findViewById(R.id.whatsaplay);
        emaillay = (RelativeLayout) inviteDialog.findViewById(R.id.emaillay);

        final String inviteContent = getString(R.string.invite_content) + " "
                + "https://play.google.com/store/apps/details?id=" +
                getApplicationContext().getPackageName();

        fblay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean installed = appInstalledOrNot("com.facebook.orca");
                if (installed) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, inviteContent);
                    sendIntent.setType("text/plain");
                    sendIntent.setPackage("com.facebook.orca");
                    startActivity(sendIntent);
                    inviteDialog.dismiss();
                    inviteDialog.cancel();
                } else {
                    Toast.makeText(FragmentMainActivity.this,
                            String.format(getString(R.string.app_not_installed), "Facebook Messenger"),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        whatsapplay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean installed = appInstalledOrNot("com.whatsapp");
                if (installed) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, inviteContent);
                    sendIntent.setType("text/plain");
                    sendIntent.setPackage("com.whatsapp");
                    startActivity(sendIntent);
                    inviteDialog.dismiss();
                    inviteDialog.cancel();
                } else {
                    Toast.makeText(FragmentMainActivity.this,
                            String.format(getString(R.string.app_not_installed), "WhatsApp"), Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        emaillay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean installed = appInstalledOrNot("com.google.android.gm");
                if (installed) {
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.setType("text/html");
                    sendIntent.setPackage("com.google.android.gm");
                    sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {});
                    sendIntent.putExtra(Intent.EXTRA_SUBJECT,
                            getString(R.string.app_name) + "!!! " + getString(R.string.invite_subject));
                    sendIntent.putExtra(Intent.EXTRA_TEXT, inviteContent);
                    sendIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(sendIntent);
                    inviteDialog.dismiss();
                    inviteDialog.cancel();
                } else {
                    Toast.makeText(FragmentMainActivity.this,
                            String.format(getString(R.string.app_not_installed), "Gmail"), Toast.LENGTH_SHORT).show();
                }
            }
        });
        if (!inviteDialog.isShowing()) {
            inviteDialog.show();
        }
    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            // moveTaskToBack(true);
                            FragmentMainActivity.this.finishAffinity();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
            builder.setMessage(getResources().getString(R.string.reallyExit))
                    .setPositiveButton(getResources().getString(R.string.exit),
                            dialogClickListener)
                    .setNegativeButton(getResources().getString(R.string.keep),
                            dialogClickListener);
            AlertDialog dialog = builder.create();
            dialog.show();
            Typeface typeface = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                typeface = getResources().getFont(R.font.font_regular);
            } else {
                typeface = ResourcesCompat.getFont(this, R.font.font_regular);
            }
            TextView textView = dialog.findViewById(android.R.id.message);
            textView.setTypeface(typeface);

            Button btn1 = dialog.findViewById(android.R.id.button1);
            btn1.setTypeface(typeface);

            Button btn2 = dialog.findViewById(android.R.id.button2);
            btn2.setTypeface(typeface);
        } else {
            super.onBackPressed();
        }
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

    private void startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates: ");
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            // noinspection MissingPermission
                            if (checkSelfPermission(ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                    && checkSelfPermission(
                                            ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                // Activity#requestPermissions
                                // here to request the missing permissions, and then overriding
                                // public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                // int[] grantResults)
                                // to handle the case where the user grants the permission. See the
                                // documentation
                                // for Activity#requestPermissions for more details.
                                return;
                            }
                        }
                        fusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            int statusCode = ((ApiException) e).getStatusCode();
                            switch (statusCode) {
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    Log.e(TAG, "Location settings are inadequate, and cannot be " +
                                            "fixed here. Fix in Settings.");
                            }
                        } else {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.");
            return;
        }

        // It is a good practice to remove location requests when the activity is in a
        // paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        if (fusedLocationClient != null && mLocationCallback != null) {
            fusedLocationClient.removeLocationUpdates(mLocationCallback)
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mRequestingLocationUpdates = false;
                        }
                    });
        }
    }

    @Override
    public void onRefresh() {
        if (!pulldown) {
            if (filterAry.size() > 0) {
                idList = new ArrayList<>();
                // searchType = null;
            } else {
                idList = new ArrayList<>();
                searchType = "all";
            }
            if (JoysaleApplication.isNetworkAvailable(FragmentMainActivity.this)) {
                // setLocation();
                pulldown = true;
                mScrollListener.resetpagecount();
                initializeHomeUI();
                getAdminDatas();
                loadHomeItemList(0);

                if (GetSet.isLogged()) {
                    getCountDetails();
                }

            } else {
                swipeLayout.setRefreshing(false);
            }
        } else {
            swipeLayout.setRefreshing(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS_GPS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        isGPSOn = true;
                        mRequestingLocationUpdates = true;
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        if (locationTxt.getText().toString().equalsIgnoreCase(getString(R.string.world_wide)))
                            locationTxt.setText(getString(R.string.world_wide));
                        break;
                }
                break;
            case Constants.CATEGORY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    filterList.clear();
                    filterBadge.setVisibility(VISIBLE);
                    searchType = null;
                    selectedSubCategoryId = null;
                    selectedSubCategoryName = null;
                    selectedChildCatId = null;
                    selectedChildCatName = null;
                    groupPosition = null;
                    idList = new ArrayList<>();
                    selectedCategoryId = data.getStringExtra(Constants.TAG_CATEGORYID);
                    selectedCategoryName = data.getStringExtra(Constants.TAG_CATEGORYNAME);
                    subCategory = Constants.selectedSubCategory;
                    Constants.selectedSubCategory = null;
                    if (subCategory != null) {
                        selectedSubCategoryId = subCategory.getSubId();
                        selectedSubCategoryName = subCategory.getSubName();
                    }
                    childCategory = Constants.selectedChildCategory;
                    Constants.selectedChildCategory = null;
                    if (childCategory != null) {
                        selectedChildCatId = childCategory.getChildId();
                        selectedChildCatName = childCategory.getChildName();
                    }
                    if (data.hasExtra(Constants.TAG_POSITION))
                        groupPosition = data.getStringExtra(Constants.TAG_POSITION);
                    getProductsByFilters();
                }
                break;
            case Constants.FILTER_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    // searchType = null;
                    idList = new ArrayList<>();
                    priceMin = data.getIntExtra(Constants.TAG_PRICE_MIN, 0);
                    priceMax = data.getIntExtra(Constants.TAG_PRICE_MAX, 0);
                    zeroMax = data.getIntExtra(Constants.TAG_ZERO_MAX, 1);
                    selectedConditionId = data.getStringExtra(Constants.TAG_CONDITION_ID);
                    selectedCondition = data.getStringExtra(Constants.TAG_CONDITION_NAME);
                    if (data.hasExtra(Constants.TAG_POSITION))
                        groupPosition = data.getStringExtra(Constants.TAG_POSITION);
                    setLocationView();
                    startAlphaAnimation(locationLay, 500, VISIBLE);
                    lat = data.getDoubleExtra(Constants.TAG_LAT, 0);
                    lng = data.getDoubleExtra(Constants.TAG_LON, 0);
                    location = data.getStringExtra(Constants.TAG_LOCATION);
                    if (location != null) {
                        if (location.equals(getString(R.string.world_wide))) {
                            removeDistanceFilter();
                        }
                        locationTxt.setText(data.getStringExtra(Constants.TAG_LOCATION));
                        searchType = "all";

                    }
                    mRequestingLocationUpdates = true;
                    stopLocationUpdates();
                    locationHandler.postDelayed(locationRunnable, LOCATION_VISIBILITY_DURATION);
                    if (data.getBooleanExtra(Constants.TAG_FILTERS, false)) {
                        distance = data.getStringExtra(Constants.TAG_DISTANCE);
                        JoysaleApplication.adminEditor.putString(Constants.PREF_ADMIN_DISTANCE, distance);
                        JoysaleApplication.adminEditor.commit();

                        selectedCategoryId = data.getStringExtra(Constants.TAG_CATEGORYID);
                        selectedSubCategoryId = data.getStringExtra(Constants.TAG_SUBCATEGORY_ID);
                        selectedCategoryName = data.getStringExtra(Constants.TAG_CATEGORYNAME);
                        selectedSubCategoryName = data.getStringExtra(Constants.TAG_SUBCATEGORYNAME);
                        selectedChildCatId = data.getStringExtra(Constants.TAG_CHILD_CATEGORY_ID);
                        selectedChildCatName = data.getStringExtra(Constants.TAG_CHILD_CATEGORY_NAME);
                        postedWithin = data.getStringExtra(Constants.TAG_POSTED_WITHIN);
                        postedTxt = data.getStringExtra(Constants.TAG_POSTED_TEXT);
                        filterList = (ArrayList<BeforeAddResponse.Filters>) data
                                .getSerializableExtra(Constants.TAG_DATA);
                        childIdMap = (HashMap<String, ArrayList<String>>) data
                                .getSerializableExtra(Constants.TAG_CHILD_ID);
                        childLabelMap = (HashMap<String, ArrayList<String>>) data
                                .getSerializableExtra(Constants.TAG_CHILD_LABEL);

                        if (distance != null) {
                            if (!distance.equalsIgnoreCase("0")) {
                                searchType = "all";
                            }
                        }
                        if (!selectedCategoryId.equalsIgnoreCase("") || !postedWithin.equalsIgnoreCase("")
                                || priceMin != 0 || priceMax != 0) {
                            searchType = null;
                        }

                        getProductsByFilters();
                    } else {
                        filterBadge.setVisibility(GONE);
                        selectedCategoryId = "";
                        selectedSubCategoryId = "";
                        selectedCategoryName = "";
                        selectedSubCategoryName = "";
                        selectedChildCatId = "";
                        selectedChildCatName = "";
                        selectedCondition = "";
                        selectedConditionId = "";
                        postedWithin = "";
                        searchType = "all";
                        distance = "";
                        filterList = new ArrayList<>();
                        filterAry.clear();
                        filterAdapter.notifyDataSetChanged();
                        filterRecycler.setVisibility(GONE);
                        filterView.setVisibility(GONE);
                        homeItemList.clear();
                        swipeRefresh();
                        mScrollListener.resetpagecount();
                        pulldown = true;
                        JoysaleApplication.adminEditor.putString(Constants.PREF_ADMIN_DISTANCE, distance);
                        JoysaleApplication.adminEditor.commit();
                        if (JoysaleApplication.isNetworkAvailable(FragmentMainActivity.this)) {
                            initializeHomeUI();
                            loadHomeItemList(0);
                        }
                    }
                }
                break;
            case Constants.LOCATION_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    if (Constants.ADDON_MAP_BOX_ENABLED) {
                        /*
                         * Constants.fileditor.putString(Constants.TAG_LAT, String.valueOf(lat));
                         * Constants.fileditor.putString(Constants.TAG_LON, String.valueOf(lng));
                         * Constants.fileditor.putBoolean("locationRemoved", true);
                         * Constants.fileditor.putString(Constants.TAG_LOCATION, location);
                         * Constants.fileditor.commit();
                         */

                        Constants.isLocationChanged = true;
                        FragmentMainActivity.currentPage = 0;
                        if (FragmentMainActivity.mScrollListener != null) {
                            FragmentMainActivity.mScrollListener.resetpagecount();
                        }
                        FragmentMainActivity.homeItemList.clear();
                        if (FragmentMainActivity.itemAdapter != null) {
                            FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                        }
                    }
                    searchType = null;
                    idList = new ArrayList<>();
                    mRequestingLocationUpdates = true;
                    stopLocationUpdates();
                    homeItemList.clear();
                    lat = data.getDoubleExtra(Constants.TAG_LAT, 0);
                    Log.d(TAG, "onActivityResult: " + data.getDoubleExtra(Constants.TAG_LAT, 0));
                    Log.d(TAG, "onActivityResult: " + lng);
                    lng = data.getDoubleExtra(Constants.TAG_LON, 0);
                    location = data.getStringExtra(Constants.TAG_LOCATION);
                    Log.d(TAG, "onClickMap: " + location);
                    if (Constants.ADDON_MAP_BOX_ENABLED) {
                        Constants.fileditor.putString(Constants.TAG_LAT, String.valueOf(lat));
                        Constants.fileditor.putString(Constants.TAG_LON, String.valueOf(lng));
                        Constants.fileditor.putBoolean("locationRemoved", true);
                        Constants.fileditor.putString(Constants.TAG_LOCATION, location);
                        Constants.fileditor.commit();
                    }

                    if (location != null) {
                        if (location.equals(getString(R.string.world_wide))) {
                            removeDistanceFilter();
                        }
                        locationTxt.setText(data.getStringExtra(Constants.TAG_LOCATION));
                    }
                    setLocationView();
                    startAlphaAnimation(locationLay, 500, VISIBLE);
                    locationHandler.postDelayed(locationRunnable, LOCATION_VISIBILITY_DURATION);
                    if (mScrollListener != null) {
                        mScrollListener.resetpagecount();
                    }
                    homeItemList.clear();
                    if (itemAdapter != null) {
                        itemAdapter.notifyDataSetChanged();
                    }
                    if (lat != 0) {
                        filterBadge.setVisibility(VISIBLE);
                    } else if (filterAry.size() == 0) {
                        filterBadge.setVisibility(GONE);
                    }
                    if (JoysaleApplication.isNetworkAvailable(FragmentMainActivity.this)) {
                        initializeHomeUI();
                        searchType = "all";
                        loadHomeItemList(0);
                    }
                }
                break;
            case Constants.SEARCH_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    searchType = null;
                    idList = new ArrayList<>();
                    SearchAdvance.applyFilter = true;
                    searchQuery = data.getStringExtra(Constants.TAG_SEARCH_KEY);
                    getProductsByFilters();
                }
                break;

            case Constants.LOCATION_PERMISSION_CODE:
                int permissionLocation = ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                        ACCESS_FINE_LOCATION);
                if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                    if (isGPSOn) {
                        startLocationUpdates();
                    } else {
                        turnGPSOn();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.ask_for_locations), Toast.LENGTH_SHORT)
                            .show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, Constants.LOCATION_PERMISSION_CODE);
                }
                break;
            case REQUEST_CAMERA_PERMISSION:
                int permissionCamera = ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                        CAMERA);
                int permissionStorage = ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                        WRITE_EXTERNAL_STORAGE);

                if (SDK_INT >= Build.VERSION_CODES.Q) {
                    if (permissionCamera == PackageManager.PERMISSION_GRANTED) {
                        /* Clear previously added images */
                        setLocationView();
                        // Addons ProductLimitation
                        /*
                         * if (Constants.ADDON_PRODUCT_LIMIT) {
                         * getMyPostDetails();
                         * }
                         */
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.need_permission_to_access),
                                Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, 100);
                    }
                } else {
                    if (permissionCamera == PackageManager.PERMISSION_GRANTED &&
                            permissionStorage == PackageManager.PERMISSION_GRANTED) {
                        /* Clear previously added images */
                        setLocationView();
                        // Addons ProductLimitation
                        /*
                         * if (Constants.ADDON_PRODUCT_LIMIT) {
                         * getMyPostDetails();
                         * }
                         */
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.need_permission_to_access),
                                Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, 100);
                    }
                }
                break;
        }
    }

    private void removeDistanceFilter() {
        String distanceType = JoysaleApplication.adminPref.getString(Constants.PREF_DISTANCE_TYPE, "km");
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Constants.TAG_TYPE, "distance");
        if (distanceType.equalsIgnoreCase("km")) {
            map.put(Constants.TAG_NAME,
                    getString(R.string.within) + " "
                            + ((distance != null && !TextUtils.isEmpty(distance)) ? distance : "0") + " "
                            + getString(R.string.kilometers));
        } else {
            map.put(Constants.TAG_NAME,
                    getString(R.string.within) + " "
                            + ((distance != null && !TextUtils.isEmpty(distance)) ? distance : "0") + " "
                            + getString(R.string.miles));
        }
        filterAry.remove(map);
        filterAdapter.notifyDataSetChanged();
        if (filterAry.size() == 0) {
            filterView.setVisibility(GONE);
            filterRecycler.setVisibility(GONE);
        }
        distance = "" + 0;
    }

    private void removeConditionFilter() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Constants.TAG_TYPE, Constants.TAG_PRODUCT_CONDITION);
        map.put(Constants.TAG_CONDITION_ID, selectedConditionId);
        map.put(Constants.TAG_CONDITION_NAME, selectedCondition);
        filterAry.remove(map);
        selectedCondition = "";
        selectedConditionId = "";
    }

    private void getProductsByFilters() {
        filterAry.clear();
        if (selectedCategoryId != null && !selectedCategoryId.equals("")) {
            filterAry.add(getSelectedCategory());
        }
        getFilterAry();
        setFilterAdapter();
        swipeRefresh();
        if (filterAry.size() > 0) {
            filterBadge.setVisibility(VISIBLE);
        }
        mScrollListener.resetpagecount();
        pulldown = true;
        if (JoysaleApplication.isNetworkAvailable(FragmentMainActivity.this)) {
            initializeHomeUI();
            homeItemList.clear();
            loadHomeItemList(0);
        }
    }

    public HashMap<String, String> getSelectedCategory() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Constants.TAG_TYPE, Constants.TAG_CATEGORY);
        map.put(Constants.CATEGORYID, selectedCategoryId);
        map.put(Constants.TAG_SUBCATEGORY_ID, selectedSubCategoryId);
        map.put(Constants.TAG_CATEGORYNAME, selectedCategoryName);
        map.put(Constants.TAG_SUBCATEGORYNAME, selectedSubCategoryName);
        map.put(Constants.TAG_CHILD_CATEGORY_ID, selectedChildCatId);
        map.put(Constants.TAG_CHILD_CATEGORY_NAME, selectedChildCatName);
        return map;
    }

    /**
     * Function for get the user profile information
     **/
    private void getProfileInformation() {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            hashMap.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            hashMap.put(Constants.TAG_USERID, GetSet.getUserId());

            Call<ProfileResponse> call = apiInterface.getProfileInformation(hashMap);
            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(Call<ProfileResponse> call, retrofit2.Response<ProfileResponse> response) {
                    if (response.isSuccessful()) {
                        ProfileResponse profile = response.body();
                        if (profile.getStatus().equals(Constants.TAG_TRUE)) {
                            ProfileResponse.Result result = profile.getResult();
                            if (!(result == null)) {
                                if (result.getUserId().equalsIgnoreCase(GetSet.getUserId())) {
                                    Constants.pref = getApplicationContext().getSharedPreferences("JoysalePref",
                                            MODE_PRIVATE);
                                    Constants.editor = Constants.pref.edit();
                                    Constants.editor.putString(Constants.TAG_PHOTO, result.getUserImg());
                                    Constants.editor.putString(Constants.TAG_USERNAME, result.getUserName());
                                    Constants.editor.putString(Constants.TAG_FULL_NAME, result.getFullName());
                                    Constants.editor.putString(Constants.TAG_RATING, result.getRating());
                                    Constants.editor.putString(Constants.TAG_RATING_USER_COUNT,
                                            result.getRatingUserCount());
                                    Constants.editor.commit();

                                    GetSet.setImageUrl(Constants.pref.getString(Constants.TAG_PHOTO, null));
                                    GetSet.setUserName(Constants.pref.getString(Constants.TAG_USERNAME, null));
                                    GetSet.setFullName(Constants.pref.getString(Constants.TAG_FULL_NAME, null));
                                    GetSet.setRating(Constants.pref.getString(Constants.TAG_RATING, null));
                                    GetSet.setRatingUserCount(
                                            Constants.pref.getString(Constants.TAG_RATING_USER_COUNT, null));
                                    GetSet.setStripePrivateKey(result.getStripeDetails().getStripePrivateKey());
                                    GetSet.setStripePublicKey(result.getStripeDetails().getStripePublicKey());

                                    setNavigationUI();
                                }
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

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
        // viewPager.stopAutoScroll();
    }

    private boolean checkOverLayPermission() {
        // Android M Or Over
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean isOverLayEnabled = Settings.canDrawOverlays(this);
            if (isOverLayEnabled) {
                editor.putBoolean(Constants.POP_UP_WINDOW_PERMISSION, false).apply();
            }
            return isOverLayEnabled;
        }

        return true;
    }

    private void openPermissionDialog() {
        try {
            if (preferences.getBoolean(Constants.POP_UP_WINDOW_PERMISSION, true)) {
                if (dialogOverLayPermission != null && !dialogOverLayPermission.isAdded()) {
                    dialogOverLayPermission.show(getSupportFragmentManager(), "PermissionDialog");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        if (Constants.ADDON_CALL_ENABLED && checkOverLayPermission()) {
            if (Build.BRAND.equalsIgnoreCase("TECNO")) {
                enableProtectesApps();
            }
        } else {
            if (Build.BRAND.equalsIgnoreCase("TECNO")) {
                enableProtectesApps();
            }
        }

        if (isSettingsIn) {
            if (Build.BRAND.equalsIgnoreCase("TECNO")) {
                enableAutoStart();
            }

        }

        if (Constants.ADDON_CALL_ENABLED && !Constants.isCallPopup) {
            if (preferences.getString(Constants.POP_UP_WINDOW_PERMISSION_ASK_AGAIN, "false")
                    .equalsIgnoreCase("false")) {
                if (!checkOverLayPermission())
                    openPermissionDialog();
            }
        }

        if (GetSet.isLogged()) {
            getCountDetails();
            getProfileInformation();
        }
        isGPSOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!isGPSOn)
            turnGPSOn();
        itemAdapter.notifyDataSetChanged();
        if (EditProfile.deleteaccount) {
            openDeleteDialog();
        }

        super.onResume();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void enableProtectesApps() {
        Log.d(TAG, "enableAutoStart: " + Build.BRAND);
        sharedPrefs = getSharedPreferences("MyPref", 0);
        editor1 = sharedPrefs.edit();
        isSettingsIn = sharedPrefs.getBoolean("isSettingsIn", false);
        if (!isSettingsIn) {

            AlertDialog.Builder builder = new AlertDialog.Builder(FragmentMainActivity.this);
            ViewGroup viewGroup = findViewById(android.R.id.content);
            View dialogView = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.dialog_overlay_permission_call, viewGroup, false);
            builder.setView(dialogView);
            AlertDialog alertDialog = builder.create();
            alertDialog.getWindow()
                    .setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorTransparent)));
            Button allowButton = dialogView.findViewById(R.id.btnAllow);
            Button denyButton = dialogView.findViewById(R.id.btnDeny);
            allowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editor1.putBoolean("isSettingsIn", true);
                    editor1.apply();
                    // Addons AudioVideo Call
                    isSettingsIn = true;
                    Intent intent = new Intent();
                    // com.cyin.himgr.widget.activity.MainSettingGpActivity
                    intent.setClassName("com.transsion.phonemaster",
                            "com.cyin.himgr.widget.activity.MainSettingGpActivity");
                    startActivity(intent);
                    alertDialog.dismiss();
                }
            });
            denyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    if (Constants.ADDON_CALL_ENABLED && !checkOverLayPermission()) {
                        openPermissionDialog();
                    }

                }
            });

            alertDialog.show();

        }
    }

    private void enableAutoStart() {
        Log.d(TAG, "enableAutoStart: " + Build.BRAND);
        sharedPrefs = getSharedPreferences("MyPref", 0);
        ;
        editor1 = sharedPrefs.edit();
        isAutoStartIn = sharedPrefs.getBoolean("isAutoStartIn", false);
        if (!isAutoStartIn) {

            AlertDialog.Builder builder = new AlertDialog.Builder(FragmentMainActivity.this);
            ViewGroup viewGroup = findViewById(android.R.id.content);
            View dialogView = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.dialog_overlay_permission_autostart, viewGroup, false);
            builder.setView(dialogView);
            AlertDialog alertDialog = builder.create();
            alertDialog.getWindow()
                    .setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorTransparent)));
            Button allowButton = dialogView.findViewById(R.id.btnAllow);
            Button denyButton = dialogView.findViewById(R.id.btnDeny);
            allowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editor1.putBoolean("isAutoStartIn", true);
                    editor1.apply();
                    Intent intent = new Intent();
                    // com.cyin.himgr.widget.activity.MainSettingGpActivity
                    intent.setClassName("com.transsion.phonemaster",
                            "com.cyin.himgr.widget.activity.MainActivity");
                    startActivity(intent);
                    alertDialog.dismiss();
                }
            });
            denyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    if (Constants.ADDON_CALL_ENABLED && !checkOverLayPermission()) {
                        openPermissionDialog();
                    }
                }
            });

            alertDialog.show();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
            int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.v(TAG, "requestCode=" + requestCode);
        if (requestCode == Constants.LOCATION_PERMISSION_CODE) {
            int permissionLocation = ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                    ACCESS_FINE_LOCATION);
            int permissionLocation1 = ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                    ACCESS_COARSE_LOCATION);
            if (permissionLocation == PackageManager.PERMISSION_GRANTED
                    || permissionLocation1 == PackageManager.PERMISSION_GRANTED) {
                if (isGPSOn) {
                    startLocationUpdates();
                } else {
                    turnGPSOn();
                }
            } else {
                // ActivityCompat.requestPermissions(FragmentMainActivity.this,
                // Constants.LOCATION_PERMISSIONS, Constants.LOCATION_PERMISSION_CODE);
                /*
                 * Toast.makeText(getApplicationContext(),
                 * getString(R.string.ask_for_locations), Toast.LENGTH_SHORT).show();
                 * Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                 * Uri uri = Uri.fromParts("package", getPackageName(), null);
                 * intent.setData(uri);
                 * startActivityForResult(intent, Constants.LOCATION_PERMISSION_CODE);
                 */
            }
        } else if (requestCode == REQUEST_CAMERA_PERMISSION) {
            int permissionCamera = ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                    CAMERA);
            int permissionStorage = 0;
            if (SDK_INT >= Build.VERSION_CODES.Q) {
                permissionStorage = PackageManager.PERMISSION_GRANTED;
            } else {
                permissionStorage = ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                        WRITE_EXTERNAL_STORAGE);
            }

            // andorid 10
            if (SDK_INT >= Build.VERSION_CODES.Q) {
                if (permissionCamera == PackageManager.PERMISSION_GRANTED) {
                    /* Clear previously added images */
                    setLocationView();
                    if (Constants.ADDON_PRODUCT_LIMIT) {
                        getMyPostDetails();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.need_permission_to_access),
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, 100);
                }
            } else {
                if (permissionCamera == PackageManager.PERMISSION_GRANTED &&
                        permissionStorage == PackageManager.PERMISSION_GRANTED) {
                    /* Clear previously added images */
                    setLocationView();
                    if (Constants.ADDON_PRODUCT_LIMIT) {
                        getMyPostDetails();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.need_permission_to_access),
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, 100);
                }
            }

        }
    }

    // Load home page Api
    private void loadHomeItemList(final int pageCount) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
        map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
        map.put(Constants.TAG_TYPE, "search");
        map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(FragmentMainActivity.this));

        map.put(Constants.TAG_LAT, lat != 0 ? ("" + lat) : "");
        map.put(Constants.TAG_LON, lng != 0 ? ("" + lng) : "");

        distance = JoysaleApplication.adminPref.getString(Constants.PREF_ADMIN_DISTANCE, "0");
        if (distance != null && !TextUtils.isEmpty(distance)) {
            map.put(Constants.TAG_DISTANCE, "" + distance);
            Log.d(TAG, "loadHomeItemList: " + distance);
            String distanceType = JoysaleApplication.adminPref.getString(Constants.PREF_DISTANCE_TYPE, "km");
            map.put(Constants.TAG_DISTANCE_TYPE, distanceType);
        }

        if (!SearchAdvance.sortBy.equals("")) {
            map.put(Constants.TAG_SORTING_ID, SearchAdvance.sortBy);
        }
        if (!postedWithin.equals("") && !postedWithin.equals("all")) {
            map.put(Constants.TAG_POSTED_WITHIN, postedWithin);
        }
        if (!searchQuery.equals("")) {
            map.put(Constants.TAG_SEARCH_KEY, searchQuery);
        }

        if (selectedCategoryId != null && !selectedCategoryId.equals("")) {
            map.put(Constants.TAG_CATEGORYID, selectedCategoryId);
        }

        if (selectedSubCategoryId != null && !selectedSubCategoryId.equals("")) {
            map.put(Constants.TAG_SUBCATEGORY_ID, selectedSubCategoryId);
        }

        if (selectedChildCatId != null && !selectedChildCatId.equals("")) {
            map.put(Constants.TAG_CHILD_CATEGORY_ID, selectedChildCatId);
        }

        if ((priceMin != 0 || priceMax != 0) || zeroMax == 0) {
            map.put("price", String.valueOf(priceMin) + "-" + String.valueOf(priceMax));
        }

        map.put(Constants.TAG_OFFSET, String.valueOf(pageCount));
        map.put(Constants.TAG_LIMIT, String.valueOf(ITEM_LIMIT));
        if (GetSet.isLogged()) {
            map.put(Constants.TAG_USERID, GetSet.getUserId());
        }

        if (pageCount != 0) {
            if (idList.size() > 0) {
                map.put("ads", "" + idList);
            }
        }

        /* Filters Params */
        JSONArray dropDownArray = new JSONArray(), multiLevelArray = new JSONArray(), rangeArray = new JSONArray();
        JSONObject range = new JSONObject();
        ArrayList<Integer> dropDownList = new ArrayList<>();
        ArrayList<Integer> multiLevelList = new ArrayList<>();
        for (BeforeAddResponse.Filters filters : filterList) {
            try {
                switch (filters.getType()) {
                    case Constants.TAG_DROPDOWN:
                        if (filters.getSelectedChildId() != null && filters.getSelectedChildId().size() > 0) {
                            for (String id : filters.getSelectedChildId()) {
                                dropDownList.add(Integer.valueOf(id));
                            }
                        }
                        break;
                    case Constants.TAG_RANGE:
                        // if (filters.getPriceChanged()) {
                        range = new JSONObject();
                        range.put(Constants.TAG_ID, filters.getId());
                        range.put(Constants.TAG_MIN_VALUE, filters.getSelectedMinValue());
                        range.put(Constants.TAG_MAX_VALUE, filters.getSelectedMaxValue());
                        rangeArray.put(range);
                        // }
                        break;
                    case Constants.TAG_MULTILEVEL:
                        if (filters.getSelectedChildId() != null && filters.getSelectedChildId().size() > 0) {
                            for (String id : filters.getSelectedChildId()) {
                                multiLevelList.add(Integer.valueOf(id));
                            }
                        }
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (filterList.size() > 0) {
            JSONObject filterObject = new JSONObject();
            try {
                filterObject.put(Constants.TAG_RANGE, rangeArray);
                filterObject.put(Constants.TAG_DROPDOWN, new JSONArray(dropDownList));
                filterObject.put(Constants.TAG_MULTILEVEL, new JSONArray(multiLevelList));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            map.put(Constants.TAG_FILTERS, "" + filterObject);
        }

        /*
         * if (!TextUtils.isEmpty(selectedCondition)) {
         * map.put(Constants.TAG_PRODUCT_CONDITION, selectedCondition);
         * }
         */
        if (!TextUtils.isEmpty(selectedConditionId)) {
            map.put(Constants.TAG_PRODUCT_CONDITION, selectedConditionId);
        }

        if (!TextUtils.isEmpty(searchType)) {
            map.put(Constants.TAG_SEARCH_TYPE, searchType);
        }

        if (SearchAdvance.sortBy.equalsIgnoreCase("3") || SearchAdvance.sortBy.equalsIgnoreCase("5")) {
            map.put(Constants.TAG_SEARCH_TYPE, "");

        } else if (SearchAdvance.sortBy.equalsIgnoreCase("4") || SearchAdvance.sortBy.equalsIgnoreCase("2")) {
            map.put(Constants.TAG_SEARCH_TYPE, "");
        }

        if (map.get("price") != null) {
            if (map.get("price").equalsIgnoreCase("0-0"))
                map.put(Constants.TAG_SEARCH_TYPE, "");
        }

        if (!searchQuery.equals(""))
            map.put(Constants.TAG_SEARCH_TYPE, "");

        if (isItemClicked) {
            map.put(Constants.TAG_SEARCH_TYPE, "");
            isItemClicked = false;
        }

        if (lat != 0 && lng != 0 && distance.equalsIgnoreCase("0"))
            map.put(Constants.TAG_DISTANCE, "" + "1");
        Log.i(TAG, "getHomeItems: " + new JSONObject(map));
        Call<HomeItemResponse> call = apiInterface.getHomeItems(map);
        call.enqueue(new Callback<HomeItemResponse>() {
            @Override
            public void onResponse(Call<HomeItemResponse> call, retrofit2.Response<HomeItemResponse> response) {
                if (response.isSuccessful()) {
                    Log.d("HOME_API", "Response_Body: " + new Gson().toJson(response.body()));

                    if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        idList = new ArrayList<>();
                        // if(priceMin !=0 && priceMax !=0)
                        idList.addAll(response.body().getAdsList());

                        if (mScrollListener != null && response.body().getResult().getItems().size() >= ITEM_LIMIT) {
                            mScrollListener.setLoading(false);
                        }
                        int previousAdapterCount = itemAdapter != null ? itemAdapter.getItemCount() : 0;
                        List<HomeItemResponse.Item> newItems = response.body().getResult().getItems();
                        homeItemList.addAll(newItems);
                        notifyHomeItemsInserted(previousAdapterCount);
                        enrichAffiliatePhotos();
                    } else if (response.body().getStatus().equalsIgnoreCase("error")) {
                        JoysaleApplication.disabledialog(FragmentMainActivity.this, "" + response.body().getMessage(),
                                GetSet.getUserId());
                    }
                }
                if (pulldown) {
                    pulldown = false;
                }
                swipeLayout.setRefreshing(false);
                progress.setVisibility(GONE);

                runOnUiThread(new Runnable() {
                    public void run() {
                        if (homeItemList.size() == 0) {
                            locationHandler.removeCallbacks(locationRunnable);
                            locationLay.setVisibility(VISIBLE);
                            nullLay.setVisibility(View.VISIBLE);
                            itemRecycler.setVisibility(GONE);
                            locationLay.setVisibility(VISIBLE);
                        } else {
                            nullLay.setVisibility(View.GONE);
                            itemRecycler.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<HomeItemResponse> call, Throwable t) {
                Log.e(TAG, "getHomeItems: " + t.getMessage());
                call.cancel();
                swipeLayout.setRefreshing(false);
                progress.setVisibility(GONE);
                if (mScrollListener != null) {
                    mScrollListener.setLoading(false);
                }
            }
        });
    }

    private void notifyHomeItemsInserted(int previousAdapterCount) {
        if (itemAdapter == null) {
            return;
        }
        int newAdapterCount = itemAdapter.getItemCount();
        if (previousAdapterCount <= 1 || newAdapterCount <= previousAdapterCount) {
            itemAdapter.notifyDataSetChanged();
        } else {
            itemAdapter.notifyItemRangeInserted(previousAdapterCount,
                    newAdapterCount - previousAdapterCount);
        }
        if (previousAdapterCount <= 1 && homeBanner.equalsIgnoreCase("enable") && bannerPagerAdapter != null) {
            bannerPagerAdapter.notifyDataSetChanged();
        }
    }

    private void enrichAffiliatePhotos() {
        if (homeItemList == null || homeItemList.isEmpty()) {
            return;
        }
        HashSet<String> pendingIds = new HashSet<>();
        for (HomeItemResponse.Item item : homeItemList) {
            if (item != null && item.isAffiliateItem() && item.getId() != null
                    && PhotoUtils.getFirstListThumbUrl(item) == null) {
                pendingIds.add(item.getId());
            }
        }
        if (pendingIds.isEmpty()) {
            return;
        }
        final int requestId = ++affiliatePhotoRequestId;
        fetchAffiliatePhotosForHome(0, requestId, pendingIds);
    }

    private void fetchAffiliatePhotosForHome(final int offset, final int requestId,
                                             final HashSet<String> pendingIds) {
        if (apiInterface == null || isFinishing() || pendingIds == null || pendingIds.isEmpty()) {
            return;
        }
        Map<String, String> map = new HashMap<String, String>();
        map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
        map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
        map.put(Constants.TAG_LIMIT, String.valueOf(ITEM_LIMIT));
        map.put(Constants.TAG_OFFSET, String.valueOf(offset));

        Call<AffiliateItemsResponse> call = apiInterface.getAffiliateItems(map);
        call.enqueue(new Callback<AffiliateItemsResponse>() {
            @Override
            public void onResponse(Call<AffiliateItemsResponse> call, retrofit2.Response<AffiliateItemsResponse> response) {
                if (requestId != affiliatePhotoRequestId || isFinishing()) {
                    return;
                }
                try {
                    if (!response.isSuccessful() || response.body() == null
                            || !Constants.TAG_TRUE.equalsIgnoreCase(response.body().getStatus())
                            || response.body().getResult() == null) {
                        return;
                    }
                    List<AffiliateItemsResponse.AffiliateItem> result = response.body().getResult();
                    if (result.isEmpty()) {
                        return;
                    }
                    ArrayList<Integer> changedIndexes = new ArrayList<>();
                    for (AffiliateItemsResponse.AffiliateItem affiliateItem : result) {
                        if (affiliateItem == null || affiliateItem.getItemId() == null
                                || !pendingIds.contains(affiliateItem.getItemId())) {
                            continue;
                        }
                        for (int i = 0; i < homeItemList.size(); i++) {
                            HomeItemResponse.Item homeItem = homeItemList.get(i);
                            if (homeItem != null && affiliateItem.getItemId().equals(homeItem.getId())) {
                                PhotoUtils.applyAffiliatePhotos(homeItem, affiliateItem.getPhotos());
                                changedIndexes.add(i);
                            }
                        }
                        pendingIds.remove(affiliateItem.getItemId());
                    }
                    if (!changedIndexes.isEmpty() && itemAdapter != null) {
                        itemAdapter.notifyItemIndexesChanged(changedIndexes);
                    }
                    if (!pendingIds.isEmpty() && result.size() >= ITEM_LIMIT && offset < 200) {
                        fetchAffiliatePhotosForHome(offset + ITEM_LIMIT, requestId, pendingIds);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "fetchAffiliatePhotosForHome error", e);
                }
            }

            @Override
            public void onFailure(Call<AffiliateItemsResponse> call, Throwable t) {
                Log.e(TAG, "fetchAffiliatePhotosForHome failure: "
                        + (t != null ? t.getMessage() : "unknown"), t);
            }
        });
    }

    // home items
    private void initializeHomeUI() {
        if (mScrollListener != null) {
            mScrollListener.setLoading(true);
        }
        nullLay.setVisibility(View.GONE);
        if (pulldown) {
            affiliatePhotoRequestId++;
            homeItemList.clear();
            itemAdapter.notifyDataSetChanged();
            progress.setVisibility(GONE);
            swipeRefresh();
        } else if (homeItemList.size() > 0) {
            progress.setVisibility(GONE);
        } else {
            progress.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Function for getting notification and chat badge count
     **/
    private void getCountDetails() {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            hashMap.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            hashMap.put(Constants.TAG_USERID, GetSet.getUserId());

            Call<GetCountResponse> call = apiInterface.getCountDetails(hashMap);
            call.enqueue(new Callback<GetCountResponse>() {
                @Override
                public void onResponse(Call<GetCountResponse> call, retrofit2.Response<GetCountResponse> response) {
                    if (response.isSuccessful() && response.body().getStatus() != null
                            && response.body().getStatus().equalsIgnoreCase("true")) {
                        JSONObject result = null;
                        String notificationCount = "" + response.body().getResult().getNotificationCount();
                        chatCount = "" + response.body().getResult().getChatCount();

                        if (!notificationCount.equals("0") && !notificationCount.equals("")) {
                            notifyBadge.setText(notificationCount);
                            notifyBadge.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
                            notifyBadge.setBadgeMargin(7);
                            notifyBadge.setTextSize(12);
                            notifyBadge.setGravity(Gravity.CENTER);
                            notifyBadge.show();
                        } else {
                            notifyBadge.hide();
                        }

                        if (!chatCount.equals("")) {
                            adapter.notifyDataSetChanged();
                        }
                    }
                }

                @Override
                public void onFailure(Call<GetCountResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void removeDropDownFilter(String filterId, String label) {
        for (BeforeAddResponse.Filters filters : filterList) {
            if (filters.getType().equals(Constants.TAG_DROPDOWN)) {
                if (filters.getSelectedChildId() != null && filters.getSelectedChildId().contains(filterId)) {
                    filters.getSelectedChildId().remove(filterId);
                    filters.getSelectedChildLabel().remove(label);
                    break;
                }
            }
        }
    }

    private void removeRangeFilter(String filterId) {
        for (BeforeAddResponse.Filters filters : filterList) {
            if (filterId.equals(filters.getSelectedParentId())) {
                filters.setSelectedMinValue(filters.getMinValue());
                filters.setSelectedMaxValue(filters.getMaxValue());
                break;
            }
        }
    }

    private void removeMultiLevelFilter(HashMap<String, String> data) {
        // Log.i(TAG, "removeMultiLevelFilter: " + new Gson().toJson(data));
        String filterId = data.get(Constants.TAG_ID);
        String label = data.get(Constants.TAG_NAME);
        String type = data.get(Constants.TAG_TYPE);
        String subParentId = data.get(Constants.TAG_SUBPARENT_ID);
        Log.i(TAG, "removeMultiLevelData: " + new Gson().toJson(data));
        String tempParentId = null;
        String tempChildId = null;
        String tempChildLabel = null;

        ArrayList<String> subIdList = new ArrayList<>();
        for (BeforeAddResponse.Filters filters : filterList) {
            if (filters.getType().equals(Constants.TAG_MULTILEVEL)) {
                for (BeforeAddResponse.Value value : filters.getValues()) {
                    for (BeforeAddResponse.ParentValue parentValue : value.getParentValues()) {
                        if (parentValue.getChildId().equals(filterId)) {
                            filters.getSelectedChildId().remove(filterId);
                            filters.getSelectedChildLabel().remove(label);
                            tempParentId = value.getParentId();
                            tempChildId = parentValue.getChildId();
                            tempChildLabel = parentValue.getChildName();
                            break;
                        }
                    }
                }
            }
        }

        if (tempParentId != null) {
            childIdMap.get(tempParentId).remove(tempChildId);
            childLabelMap.get(tempParentId).remove(tempChildLabel);
        }
    }

    private void removeAllCategoryFilters() {
        Iterator<HashMap<String, String>> iterator = filterAry.iterator();
        while (iterator.hasNext()) {
            HashMap<String, String> hashMap = iterator.next();
            if (hashMap.get(Constants.TAG_TYPE).equals(Constants.TAG_RANGE) ||
                    hashMap.get(Constants.TAG_TYPE).equals(Constants.TAG_DROPDOWN) ||
                    hashMap.get(Constants.TAG_TYPE).equals(Constants.TAG_MULTILEVEL)) {
                iterator.remove();
            }
        }
    }

    public String stringListToString(ArrayList<String> arrayList) {
        return ("" + arrayList).replace("[", "").replace("]", "").replaceAll(", ", ",");
    }

    /**
     * Function for OnClick Event
     **/
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                drawer.closeDrawers();
                setLocationView();
                Intent i = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                startActivity(i);
                break;
            case R.id.homefilterbtn:
            case R.id.filterLay:
                setLocationView();
                Intent filter = new Intent(FragmentMainActivity.this, SearchAdvance.class);
                filter.putExtra(Constants.TAG_POSTED_WITHIN, postedWithin);
                filter.putExtra(Constants.TAG_LOCATION, "" + locationTxt.getText());
                filter.putExtra(Constants.TAG_LAT, lat);
                filter.putExtra(Constants.TAG_LON, lng);
                filter.putExtra(Constants.TAG_PRICE_MIN, priceMin);
                filter.putExtra(Constants.TAG_PRICE_MAX, priceMax);
                filter.putExtra(Constants.TAG_ZERO_MAX, zeroMax);
                filter.putExtra(Constants.TAG_DISTANCE, distance);
                filter.putExtra(Constants.TAG_CONDITION_ID, selectedConditionId);
                filter.putExtra(Constants.TAG_CONDITION_NAME, selectedCondition);
                filter.putExtra(Constants.TAG_POSITION, groupPosition);
                if (selectedCategoryId != null && !selectedCategoryId.equals("")) {
                    filter.putExtra(Constants.TAG_CATEGORYID, selectedCategoryId);
                    filter.putExtra(Constants.TAG_CATEGORYNAME, selectedCategoryName);
                    filter.putExtra(Constants.TAG_SUBCATEGORY_ID, selectedSubCategoryId);
                    filter.putExtra(Constants.TAG_SUBCATEGORYNAME, selectedSubCategoryName);
                    filter.putExtra(Constants.TAG_CHILD_CATEGORY_ID, selectedChildCatId);
                    filter.putExtra(Constants.TAG_CHILD_CATEGORY_NAME, selectedChildCatName);
                    if (filterList.size() > 0) {
                        filter.putExtra(Constants.TAG_DATA, filterList);
                    }
                }
                filter.putExtra(Constants.TAG_CHILD_ID, childIdMap);
                filter.putExtra(Constants.TAG_CHILD_LABEL, childLabelMap);
                startActivityForResult(filter, Constants.FILTER_REQUEST_CODE);
                break;
            case R.id.menubtn:
                drawer.openDrawer(Gravity.LEFT);
                break;

            case R.id.locationLay: {
                Log.d(TAG,
                        "onClick: "
                                + (ContextCompat.checkSelfPermission(this,
                                        ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                                + (ContextCompat.checkSelfPermission(this,
                                        ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED));
                if ((ContextCompat.checkSelfPermission(this,
                        ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                        || (ContextCompat.checkSelfPermission(this,
                                ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                    // Addon Mapbox
                    if (Constants.ADDON_MAP_BOX_ENABLED) {
                   /*     Intent mapbox = new Intent(FragmentMainActivity.this, MainActivity.class);
                        mapbox.putExtra(Constants.TAG_FROM, "home");
                        mapbox.putExtra(Constants.TAG_LOCATION, location);
                        mapbox.putExtra(Constants.TAG_LAT, lat);
                        mapbox.putExtra(Constants.TAG_LON, lng);
                        Log.d(TAG, "onClick: " + lat + lng);
                        startActivityForResult(mapbox, Constants.LOCATION_REQUEST_CODE);*/
                    } else {
                        setLocationView();
                        Intent k = new Intent(FragmentMainActivity.this, LocationActivity.class);
                        k.putExtra(Constants.TAG_FROM, Constants.TAG_HOME);
                        k.putExtra(Constants.TAG_LAT, lat);
                        k.putExtra(Constants.TAG_LON, lng);
                        k.putExtra(Constants.TAG_LOCATION, location);
                        startActivityForResult(k, Constants.LOCATION_REQUEST_CODE);
                    }
                } else {
                    Log.d(TAG, "onClick: " + "locations");
                    locations.launch(PermissionsUtils.LOCATIONS);
                    // startActivityForResult(intent, LOCATION_PERMISSION_CODE);
                }
            }
                break;
            case R.id.searchbtn:
                setLocationView();
                Intent search = new Intent(FragmentMainActivity.this, SearchActivity.class);
                search.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityForResult(search, Constants.SEARCH_REQUEST_CODE);
                break;

            case R.id.btnAddStuff:

                if (GetSet.isLogged()) {
                    if (SDK_INT >= Build.VERSION_CODES.Q) {
                        if (ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                                CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(FragmentMainActivity.this, new String[] { CAMERA },
                                    REQUEST_CAMERA_PERMISSION);
                        } else {
                            setLocationView();

                            if (Constants.ADDON_PRODUCT_LIMIT) {
                                getMyPostDetails();
                            } else {
                                Intent m = new Intent(FragmentMainActivity.this, CameraViewActivity.class);
                                m.putExtra(Constants.TAG_FROM, "home");
                                if (Constants.ADDON_MAP_BOX_ENABLED) {
                                    m.putExtra(Constants.TAG_FROM, "home");
                                }
                                startActivity(m);
                            }

                        }
                    } else {
                        if (ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                                CAMERA) != PackageManager.PERMISSION_GRANTED
                                && ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                                        WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(FragmentMainActivity.this,
                                    new String[] { CAMERA, WRITE_EXTERNAL_STORAGE }, REQUEST_CAMERA_PERMISSION);
                        } else if (ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                                CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(FragmentMainActivity.this, new String[] { CAMERA },
                                    REQUEST_CAMERA_PERMISSION);
                        } else if (ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                                WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(FragmentMainActivity.this,
                                    new String[] { WRITE_EXTERNAL_STORAGE }, REQUEST_CAMERA_PERMISSION);
                        } else {
                            if (Constants.ADDON_PRODUCT_LIMIT) {
                                getMyPostDetails();
                            } else {
                                Intent m = new Intent(FragmentMainActivity.this, CameraViewActivity.class);
                                m.putExtra(Constants.TAG_FROM, "home");
                                if (Constants.ADDON_MAP_BOX_ENABLED) {
                                    m.putExtra(Constants.TAG_LOCATION, location);
                                }
                                startActivity(m);
                            }
                            setLocationView();
                        }
                    }
                } else {
                    setLocationView();
                    Intent m = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                    m.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(m);
                }
                break;
            case R.id.header:
                drawer.closeDrawers();
                setLocationView();
                if (GetSet.isLogged()) {
                    Intent n = new Intent(FragmentMainActivity.this, ProfileActivity.class);
                    n.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                    startActivity(n);
                } else {
                    Intent n = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                    startActivity(n);
                }
                break;
            case R.id.notifybtn:
                setLocationView();
                if (GetSet.isLogged()) {
                    Intent o = new Intent(FragmentMainActivity.this, Notification.class);
                    startActivity(o);
                } else {
                    Intent n = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                    startActivity(n);
                }
                break;
            case R.id.btnAdvertise:
                setLocationView();
                if (GetSet.isLogged()) {
                    Intent o = new Intent(FragmentMainActivity.this, AdvertiseActivity.class);
                    o.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(o);
                } else {
                    Intent n = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                    n.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(n);
                }
                break;
        }
    }

    public void turnGPSOn() {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isGPSOn = true;
        } else {
            mSettingsClient
                    .checkLocationSettings(mLocationSettingsRequest)
                    .addOnSuccessListener(FragmentMainActivity.this, new OnSuccessListener<LocationSettingsResponse>() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public void onSuccess(LocationSettingsResponse locationSettingsResponse) {// GPS is already
                                                                                                  // enable, callback
                                                                                                  // GPS status through
                                                                                                  // listener
                            isGPSOn = true;
                        }
                    })
                    .addOnFailureListener(FragmentMainActivity.this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            int statusCode = 0;
                            if (e instanceof IllegalArgumentException) {
                                e.printStackTrace();
                            } else {
                                statusCode = ((ApiException) e).getStatusCode();
                                switch (statusCode) {
                                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                        try {
                                            // Show the dialog by calling startResolutionForResult(), and check the
                                            // result in onActivityResult().
                                            ResolvableApiException rae = (ResolvableApiException) e;
                                            rae.startResolutionForResult(FragmentMainActivity.this,
                                                    REQUEST_CHECK_SETTINGS_GPS);
                                        } catch (IntentSender.SendIntentException sie) {
                                            Log.e(TAG, "PendingIntent unable to execute request.");
                                        }
                                        break;
                                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                        String errorMessage = "Location settings are inadequate, and cannot be " +
                                                "fixed here. Fix in Settings.";
                                        Log.e(TAG, errorMessage);
                                        Toast.makeText(FragmentMainActivity.this, errorMessage, Toast.LENGTH_LONG)
                                                .show();
                                }
                            }
                        }
                    });
        }

    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(FragmentMainActivity.this, isConnected);
    }

    // Addon ProductLimitation
    private void getMyPostDetails() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            progressDialog.show();
            Call<PostDetailsResponse> call = apiInterface.getMyPostDetails(map);
            call.enqueue(new Callback<PostDetailsResponse>() {
                @Override
                public void onResponse(Call<PostDetailsResponse> call,
                        retrofit2.Response<PostDetailsResponse> response) {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();

                    if (response.isSuccessful() && response.body().getStatus().equalsIgnoreCase("true")) {
                        urgentAry.clear();
                        urgentAry.addAll(response.body().getResult());
                        data = urgentAry.get(0);

                        if (data.getSubscription_enable() == 1) {
                            Intent intent = new Intent(FragmentMainActivity.this, ProductLimitation.class);
                            startActivity(intent);
                        } else {
                            Intent m = new Intent(FragmentMainActivity.this, CameraViewActivity.class);
                            m.putExtra(Constants.TAG_FROM, "home");
                            startActivity(m);
                        }
                    }
                }

                @Override
                public void onFailure(Call<PostDetailsResponse> call, Throwable t) {
                    t.printStackTrace();
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    call.cancel();
                }
            });
        } else {
            Toast.makeText(FragmentMainActivity.this, "Please Connect Internet", Toast.LENGTH_SHORT).show();

        }
    }

    // for banner purpose
    private void getAdminDatas() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_LANG_TYPE, languageCode);

            Call<AdminDataResponse> call = apiInterface.getAdminData(map);
            call.enqueue(new Callback<AdminDataResponse>() {
                @Override
                public void onResponse(Call<AdminDataResponse> call, retrofit2.Response<AdminDataResponse> response) {
                    AdminDataResponse adminDataResponse = response.body();
                    try {
                        if (adminDataResponse.getStatus().equalsIgnoreCase("true")) {
                            AdminDataResponse.Result result = adminDataResponse.getResult();
                            FragmentMainActivity.homeBanner = result.getBanner();
                            JoysaleApplication.adminEditor.putString(Constants.PREF_DISTANCE_TYPE,
                                    result.getDistanceType());
                            JoysaleApplication.adminEditor.commit();

                            Constants.BUYNOW = result.getBuyNow().equalsIgnoreCase("enable");
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_BUYNOW, Constants.BUYNOW);
                            JoysaleApplication.adminEditor.commit();

                            Constants.EXCHANGE = result.getExchange().equalsIgnoreCase("enable");
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_EXCHANGE, Constants.EXCHANGE);
                            JoysaleApplication.adminEditor.commit();

                            Constants.PROMOTION = result.getPromotion().equalsIgnoreCase("enable");
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_PROMOTION, Constants.PROMOTION);
                            JoysaleApplication.adminEditor.commit();

                            // Dynamic changes
                            if (result.getmSocketUrl() != null)
                                Constants.SOCKET_URL = result.getmSocketUrl();

                            if (result.getGoogleAdsType() != null)
                                Constants.TAG_GOOGLEADS_STATUS = result.getGoogleAdsType().trim();

                            if (Constants.ADDON_INTERSTITIAL_AD && result.getInterstitial_ad_key() != null)
                                Constants.INTERSTITIAL_AD_ID = result.getInterstitial_ad_key();

                            if (Constants.ADDON_CALL_ENABLED && result.getApprtcUrl() != null)
                                Constants.APPRTC_URL = result.getApprtcUrl();

                            if (Constants.ADDON_BANNER_AD) {
                                BannerAdUtils.applyAdminBannerId(result.getGoogle_ads_android());
                            }

                            Constants.GIVINGAWAY = result.getGivingAway().equalsIgnoreCase("enable");
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_GIVINGAWAY, Constants.GIVINGAWAY);
                            JoysaleApplication.adminEditor.commit();

                            Constants.PAIDBANNER = result.getPaidBanner().equalsIgnoreCase("enable");
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_PAIDBANNER, Constants.PAIDBANNER);
                            JoysaleApplication.adminEditor.commit();

                            Constants.SITEMAINTAIN = result.getPaidBanner().equalsIgnoreCase("enable");
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_SITEMAINTAIN,
                                    Constants.SITEMAINTAIN);
                            JoysaleApplication.adminEditor.commit();

                            if (FragmentMainActivity.bannerAry != null) {
                                FragmentMainActivity.bannerAry.clear();
                                bannerAry.addAll(result.getBannerData());
                            }

                            if (ChatActivity.templatMsgAry != null) {
                                ChatActivity.templatMsgAry.clear();
                                ChatActivity.templatMsgAry.addAll(result.getChatTemplate());
                            }
                            if (result.getPriceRange() != null) {
                                SplashActivity.beforeDecimal = result.getPriceRange().getBeforeDecimal();
                                SplashActivity.afterDecimal = result.getPriceRange().getAfterDecimal();
                            }

                            if (result.getStripePublicKey() != null
                                    && !TextUtils.isEmpty(result.getStripePublicKey())) {
                                SplashActivity.stripePublicKey = result.getStripePublicKey();
                            }

                            /*
                             * Constants.EMAILVERIFICATION =
                             * result.getEmailVerification().equalsIgnoreCase("enable");
                             * JoysaleApplication.adminEditor.putBoolean(Constants.PREF_EMAILVERIFICATION,
                             * Constants.EMAILVERIFICATION);
                             * JoysaleApplication.adminEditor.commit();
                             */

                        }
                        if (itemAdapter != null) {
                            itemAdapter.notifyDataSetChanged();
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<AdminDataResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private static class NpaGridLayoutManager extends GridLayoutManager {
        public NpaGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        public NpaGridLayoutManager(Context context, int spanCount) {
            super(context, spanCount);
        }

        public NpaGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
            super(context, spanCount, orientation, reverseLayout);
        }

        /**
         * Disable predictive animations. There is a bug in RecyclerView which causes
         * views that
         * are being reloaded to pull invalid ViewHolders from the internal recycler
         * stack if the
         * adapter size has decreased since the ViewHolder was recycled.
         */
        @Override
        public boolean supportsPredictiveItemAnimations() {
            return false;
        }
    }

    /**
     * Adapter for Home Items
     **/
    public class ItemViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public static final int TYPE_HEADER = 0;
        public static final int TYPE_ITEM = 1;
        public static final int TYPE_AD = 2;
        private static final int HOME_GRID_SPAN = 2;
        private static final int AD_AFTER_ROWS = 5;
        private static final int ITEMS_PER_AD = HOME_GRID_SPAN * AD_AFTER_ROWS;
        List<HomeItemResponse.Item> itemList;
        Context mContext;
        TimeAgo timeAgo;
        private final int pad10;
        private final int pad5;
        private final android.graphics.drawable.Drawable soldBg;
        private final android.graphics.drawable.Drawable adBg;
        private final android.graphics.drawable.Drawable urgentBg;

        public ItemViewAdapter(Context ctx, List<HomeItemResponse.Item> itemList) {
            this.itemList = itemList;
            this.mContext = ctx;
            timeAgo = new TimeAgo(mContext);
            pad10 = JoysaleApplication.dpToPx(mContext, 10);
            pad5 = JoysaleApplication.dpToPx(mContext, 5);
            soldBg = ContextCompat.getDrawable(mContext, R.drawable.soldbg);
            adBg = ContextCompat.getDrawable(mContext, R.drawable.adbg);
            urgentBg = ContextCompat.getDrawable(mContext, R.drawable.urgentbg);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_ITEM) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.home_list_items, parent, false);
                return new MyViewHolder(itemView);
            } else if (viewType == TYPE_HEADER) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.home_banner, parent, false);
                return new HeaderView(itemView);
            } else if (viewType == TYPE_AD) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.home_list_ad, parent, false);
                return new AdViewHolder(itemView);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int holderPosition) {
            int position = viewHolder.getAbsoluteAdapterPosition();
            if (position == RecyclerView.NO_POSITION) {
                return;
            }
            if (viewHolder instanceof AdViewHolder) {
                BannerAdUtils.loadListBannerAd(FragmentMainActivity.this,
                        ((AdViewHolder) viewHolder).adContainer);
                return;
            }
            if (viewHolder instanceof MyViewHolder) {

                MyViewHolder holder = (MyViewHolder) viewHolder;
                final int itemIndex = getItemIndex(position);
                if (itemIndex < 0 || itemIndex >= itemList.size()) {
                    return;
                }

                if (itemIndex % 2 == 0) {
                    holder.mainLay.setPadding(pad10, pad10, pad5, 0);
                } else {
                    holder.mainLay.setPadding(pad5, pad10, pad10, 0);
                }

                final HomeItemResponse.Item homeItem = itemList.get(itemIndex);
                holder.boundItem = homeItem;
                holder.boundIndex = itemIndex;

                try {
                    PhotoUtils.loadListImage(holder.singleImage,
                            PhotoUtils.getFirstListThumbUrl(homeItem),
                            screenHalf, R.drawable.applogo, FragmentMainActivity.this);
                } catch (Exception e) {
                    holder.singleImage.setImageResource(R.drawable.applogo);
                    e.printStackTrace();
                }

                String title = homeItem.getItemTitle();
                if (title == null) {
                    holder.itemName.setText("");
                } else if (title.indexOf('<') >= 0) {
                    holder.itemName.setText(JoysaleApplication.stripHtml(title));
                } else {
                    holder.itemName.setText(title.trim());
                }

                DiscountUtils.bindListingDiscount(mContext, homeItem, holder.itemPrice, holder.originalPrice, holder.discountBadge);

                DiscountUtils.bindAffiliateLabel(mContext, homeItem, holder.affiliateLabel, holder.priceLay);


                holder.location.setText(homeItem.getLocation());

                if (homeItem.getItemStatus() != null && homeItem.getItemStatus().equalsIgnoreCase("sold")) {
                    holder.productType.setVisibility(View.VISIBLE);
                    holder.productType.setText(getString(R.string.sold));
                    holder.productType.setBackground(soldBg);
                } else if (Constants.PROMOTION && homeItem.getPromotionType() != null) {
                    if (homeItem.getPromotionType().equalsIgnoreCase("Ad")) {
                        holder.productType.setVisibility(View.VISIBLE);
                        holder.productType.setText(getString(R.string.ad));
                        holder.productType.setBackground(adBg);
                    } else if (homeItem.getPromotionType().equalsIgnoreCase("Urgent")) {
                        holder.productType.setVisibility(View.VISIBLE);
                        holder.productType.setText(getString(R.string.urgent));
                        holder.productType.setBackground(urgentBg);
                    } else {
                        holder.productType.setVisibility(GONE);
                    }
                } else {
                    holder.productType.setVisibility(GONE);
                }

                try {
                    long timestamp = 0;
                    String time = homeItem.getPostedTime();
                    if (time != null) {
                        timestamp = Long.parseLong(time) * 1000;
                    }
                    holder.postedTime.setText(timeAgo.timeAgo(timestamp));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (viewHolder instanceof HeaderView) {
                HeaderView holder = (HeaderView) viewHolder;
                if ((homeBanner.equalsIgnoreCase("disable") || bannerAry.size() == 0)) {
                    holder.viewPager.setVisibility(GONE);
                    holder.pageIndicator.setVisibility(GONE);
                } else {
                    holder.viewPager.setVisibility(VISIBLE);
                    holder.pageIndicator.setVisibility(VISIBLE);
                }
            }

        }

        @Override
        public int getItemCount() {
            return 1 + itemList.size() + getAdCount();
        }

        @Override
        public int getItemViewType(int position) {
            if (isPositionHeader(position)) {
                return TYPE_HEADER;
            }
            if (isAdPosition(position)) {
                return TYPE_AD;
            }
            return TYPE_ITEM;
        }

        private boolean isPositionHeader(int position) {
            return position == 0;
        }

        private boolean isHomeListAdEnabled() {
            return BannerAdUtils.isBannerAdEnabled();
        }

        private int getAdCount() {
            if (!isHomeListAdEnabled()) {
                return 0;
            }
            return itemList.size() / ITEMS_PER_AD;
        }

        private boolean isAdPosition(int position) {
            if (!isHomeListAdEnabled() || position <= 0) {
                return false;
            }
            int dataPos = position - 1;
            return (dataPos + 1) % (ITEMS_PER_AD + 1) == 0;
        }

        private int getItemIndex(int position) {
            if (!isHomeListAdEnabled()) {
                return position - 1;
            }
            int dataPos = position - 1;
            int adsBefore = dataPos / (ITEMS_PER_AD + 1);
            return dataPos - adsBefore;
        }

        @Override
        public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
            super.onViewRecycled(holder);
            if (holder instanceof MyViewHolder) {
                ImageView imageView = ((MyViewHolder) holder).singleImage;
                Picasso.get().cancelRequest(imageView);
                imageView.setTag(R.id.list_image_url_tag, null);
            }
        }

        public void notifyItemIndexesChanged(List<Integer> itemIndexes) {
            if (itemIndexes == null || itemIndexes.isEmpty()) {
                return;
            }
            for (Integer itemIndex : itemIndexes) {
                if (itemIndex == null || itemIndex < 0 || itemIndex >= itemList.size()) {
                    continue;
                }
                int adapterPosition = getAdapterPositionForItemIndex(itemIndex);
                if (adapterPosition >= 0 && adapterPosition < getItemCount()) {
                    notifyItemChanged(adapterPosition);
                }
            }
        }

        private int getAdapterPositionForItemIndex(int itemIndex) {
            if (!isHomeListAdEnabled()) {
                return itemIndex + 1;
            }
            return 1 + itemIndex + (itemIndex / ITEMS_PER_AD);
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
            ImageView singleImage;
            TextView itemPrice, originalPrice, itemName, location, postedTime, productType, discountBadge, affiliateLabel;
            RelativeLayout imageLay;
            LinearLayout mainLay, priceLay;
            HomeItemResponse.Item boundItem;
            int boundIndex;

            public MyViewHolder(View view) {
                super(view);
                singleImage = (ImageView) view.findViewById(R.id.singleImage);
                itemPrice = (TextView) view.findViewById(R.id.priceText);
                originalPrice = (TextView) view.findViewById(R.id.originalPrice);
                discountBadge = (TextView) view.findViewById(R.id.discountBadge);
                affiliateLabel = (TextView) view.findViewById(R.id.affiliateLabel);
                priceLay = (LinearLayout) view.findViewById(R.id.priceLay);
                itemName = (TextView) view.findViewById(R.id.itemName);
                productType = (TextView) view.findViewById(R.id.productType);
                location = (TextView) view.findViewById(R.id.location);
                postedTime = (TextView) view.findViewById(R.id.postedTime);
                imageLay = (RelativeLayout) view.findViewById(R.id.imageLay);
                mainLay = (LinearLayout) view.findViewById(R.id.mainLay);

                singleImage.getLayoutParams().height = screenHalf;
                imageLay.getLayoutParams().height = screenHalf;
                singleImage.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                if (boundItem == null) {
                    return;
                }
                setLocationView();
                Intent i = new Intent(FragmentMainActivity.this, DetailActivity.class);
                i.putExtra(Constants.TAG_DATA, boundItem);
                i.putExtra(Constants.TAG_POSITION, boundIndex);
                i.putExtra(Constants.TAG_FROM, Constants.TAG_HOME);
                startActivity(i);
            }
        }

        public class AdViewHolder extends RecyclerView.ViewHolder {
            FrameLayout adContainer;

            public AdViewHolder(View itemView) {
                super(itemView);
                adContainer = itemView.findViewById(R.id.homeListAdContainer);
            }
        }

        public class HeaderView extends RecyclerView.ViewHolder {

            AutoScrollViewPager viewPager;
            ScrollingPagerIndicator pageIndicator;
            RelativeLayout adContainer;

            public HeaderView(View itemView) {
                super(itemView);

                viewPager = (AutoScrollViewPager) itemView.findViewById(R.id.view_pager);
                // adContainer = itemView.findViewById(R.id.adView);
                pageIndicator = itemView.findViewById(R.id.indicator);
                categoryRecycler = (RecyclerView) itemView.findViewById(R.id.categoryView);
                showheaderContent();
            }

            public void showheaderContent() {

                Log.d(TAG, "showheaderContent: " + "bannercall");
                if (homeBanner.equalsIgnoreCase("enable") && bannerAry.size() > 0) {
                    viewPager.setVisibility(View.VISIBLE);
                    viewPager.stopAutoScroll();
                    pageIndicator.reattach();
                } else {
                    viewPager.setVisibility(View.GONE);
                    pageIndicator.setVisibility(View.INVISIBLE);
                    ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) categoryRecycler
                            .getLayoutParams();
                    marginLayoutParams.setMargins(0, JoysaleApplication.dpToPx(getApplicationContext(), 30), 0, 0);
                    categoryRecycler.setLayoutParams(marginLayoutParams);
                }

                viewPager.getLayoutParams().height = bannerHeight;
                viewPager.setAdapter(bannerPagerAdapter);
                pageIndicator.attachToPager(viewPager);
                // bannerPagerAdapter.notifyDataSetChanged();

                viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                    @Override
                    public void onPageScrollStateChanged(int state) {
                        // enableDisableSwipeRefresh(state == ViewPager.SCROLL_STATE_IDLE);
                    }

                    @Override
                    public void onPageScrolled(int arg0, float arg1, int arg2) {

                    }

                    @Override
                    public void onPageSelected(int position) {

                    }
                });

                viewPager.startAutoScroll(1000);

                final float MILLISECONDS_PER_INCH = 500f; /* Large amount = slow speed */
                LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(itemRecycler.getContext()) {

                    @Override
                    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                        return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
                    }
                };

                if (categoryAry.size() > 0) {
                    categoryRecycler.setVisibility(View.VISIBLE);
                } else {
                    categoryRecycler.setVisibility(View.GONE);
                }
                LinearLayoutManager layoutManager = new LinearLayoutManager(FragmentMainActivity.this,
                        LinearLayoutManager.HORIZONTAL, false);
                categoryRecycler.setLayoutManager(layoutManager);
                // categoryRecycler.setLayoutManager(new
                // SpeedyLinearLayoutManager(getApplicationContext(),
                // SpeedyLinearLayoutManager.HORIZONTAL, false));
                categoryRecycler.post(new Runnable() {
                    @Override
                    public void run() {
                        categoryRecycler.setAdapter(new RecyclerViewAdapter(categoryAry, FragmentMainActivity.this));

                    }
                });

                categoryRecycler.setHasFixedSize(true);
                categoryRecycler.setNestedScrollingEnabled(false);
                categoryRecycler.addOnItemTouchListener(categoryItemClick(FragmentMainActivity.this, categoryRecycler));

            }
        }
    }

    /**
     * adapter for showing the applied filters
     * adapter for showing the applied filters
     **/
    public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.ViewHolder> {

        ArrayList<HashMap<String, String>> datas = new ArrayList<>();
        Context mContext;

        public FilterAdapter(Context ctx, ArrayList<HashMap<String, String>> data) {
            mContext = ctx;
            datas = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View convertView = inflater.inflate(R.layout.home_filter_item, parent, false);// layout
            return new ViewHolder(convertView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

            Log.d(TAG, "onBindViewHolder: " + datas);
            try {
                final HashMap<String, String> data = datas.get(position);
                switch (data.get(Constants.TAG_TYPE)) {
                    case Constants.TAG_CATEGORY:
                        if (data.containsKey(Constants.TAG_CATEGORYNAME)
                                && data.containsKey(Constants.TAG_CHILD_CATEGORY_NAME)
                                && data.get(Constants.TAG_CHILD_CATEGORY_NAME) != null
                                && !data.get(Constants.TAG_CHILD_CATEGORY_NAME).equals("")) {
                            holder.name.setText(data.get(Constants.TAG_CHILD_CATEGORY_NAME));
                        } else if (data.containsKey(Constants.TAG_CATEGORYNAME)
                                && data.containsKey(Constants.TAG_SUBCATEGORYNAME)
                                && data.get(Constants.TAG_SUBCATEGORYNAME) != null
                                && !data.get(Constants.TAG_SUBCATEGORYNAME).equals("")) {
                            holder.name.setText(data.get(Constants.TAG_SUBCATEGORYNAME));
                        } else if (data.containsKey(Constants.TAG_CATEGORYNAME)) {
                            holder.name.setText(data.get(Constants.TAG_CATEGORYNAME));
                        }
                        break;
                    case Constants.TAG_PRODUCT_CONDITION:
                        holder.name.setText(data.get(Constants.TAG_CONDITION_NAME));
                        break;
                    default:
                        holder.name.setText(data.get(Constants.TAG_NAME));
                        break;
                }

                holder.crossIcon.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (datas.get(position).get(Constants.TAG_TYPE)) {
                            case "category":
                                selectedCategoryId = "";
                                selectedSubCategoryId = "";
                                selectedCategoryName = "";
                                selectedSubCategoryName = "";
                                selectedChildCatId = "";
                                selectedChildCatName = "";
                                groupPosition = null;
                                childCategory = null;
                                subCategory = null;
                                removeConditionFilter();
                                filterList.clear();
                                removeAllCategoryFilters();
                                break;
                            case "distance":
                                distance = "" + 100;
                                JoysaleApplication.adminEditor.putString(Constants.PREF_ADMIN_DISTANCE, distance);
                                JoysaleApplication.adminEditor.commit();
                                break;
                            case "postedWithin":
                                postedWithin = "";
                                break;
                            case "sortBy":
                                SearchAdvance.sortBy = "1";
                                break;
                            case "search":
                                searchQuery = "";
                                break;
                            case "price":
                                priceMin = 0;
                                priceMax = 0;
                                break;
                            case Constants.TAG_GIVING_AWAY:
                                zeroMax = 1;
                                priceMin = 0;
                                priceMax = 0;
                                break;
                            case Constants.TAG_PRODUCT_CONDITION:
                                selectedCondition = "";
                                selectedConditionId = "";
                                break;
                            case Constants.TAG_DROPDOWN:
                                removeDropDownFilter(data.get(Constants.TAG_ID), data.get(Constants.TAG_NAME));
                                break;
                            case Constants.TAG_RANGE:
                                removeRangeFilter(data.get(Constants.TAG_ID));
                                break;
                            case Constants.TAG_MULTILEVEL:
                                removeMultiLevelFilter(data);
                                break;
                        }
                        filterAry.remove(position);
                        notifyDataSetChanged();
                        if (filterAry.size() == 0) {
                            searchType = "all";
                            idList = new ArrayList<>();
                            filterRecycler.setVisibility(GONE);
                            filterView.setVisibility(GONE);
                            if (lat == 0) {
                                filterBadge.setVisibility(GONE);
                            }
                        }
                        swipeRefresh();
                        mScrollListener.resetpagecount();
                        pulldown = true;
                        if (JoysaleApplication.isNetworkAvailable(FragmentMainActivity.this)) {
                            initializeHomeUI();
                            loadHomeItemList(0);
                        }
                    }
                });

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView crossIcon;
            TextView name;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.name);
                crossIcon = itemView.findViewById(R.id.cross_icon);
            }
        }
    }

    /**
     * Adapter for Category
     **/
    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

        List<BeforeAddResponse.Category> itemList = new ArrayList<>();
        Context context;

        public RecyclerViewAdapter(List<BeforeAddResponse.Category> itemList, Context context) {
            this.itemList = itemList;
            this.context = context;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.category_single_view, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final BeforeAddResponse.Category category = itemList.get(position);
            Picasso.get()
                    .load(category.getCategoryImg())
                    .error(R.drawable.appicon)
                    .placeholder(R.drawable.appicon)
                    .into(holder.singleImage);
            holder.singleTitle.setText(category.getCategoryName());
        }

        @Override
        public int getItemCount() {
            return itemList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView singleImage;
            TextView singleTitle;
            LinearLayout singleLayout;

            public MyViewHolder(View view) {
                super(view);
                singleLayout = (LinearLayout) view.findViewById(R.id.singleLayout);
                singleImage = (ImageView) view.findViewById(R.id.singleImage);
                singleTitle = (TextView) view.findViewById(R.id.singleTitle);

                /* Set the txtCategory last item partially visible */
                Display display = getWindowManager().getDefaultDisplay();
                // display size in pixels
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                width = width / 10;
                width = width + width;
                int height = size.y;
                LinearLayout.LayoutParams oldParams = new LinearLayout.LayoutParams(width,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                oldParams.setMargins(JoysaleApplication.dpToPx(getApplicationContext(), 5),
                        JoysaleApplication.dpToPx(getApplicationContext(), 10), 0, 0);
                singleLayout.setLayoutParams(oldParams);
            }
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            drawer.closeDrawers();
            mDrawerItemClicked = true;
            mDrawerPosition = position;
        }
    }

    /**
     * Adapter for showing banner image
     **/
    class BannerPagerAdapter extends PagerAdapter {
        Context context;
        LayoutInflater inflater;
        List<AdminDataResponse.BannerData> data;

        public BannerPagerAdapter(Context act, List<AdminDataResponse.BannerData> newary) {
            this.data = newary;
            this.context = act;
        }

        public int getCount() {
            return data.size();
        }

        public Object instantiateItem(ViewGroup collection, final int position) {
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View itemView = inflater.inflate(R.layout.banner_image,
                    collection, false);

            ImageView image = (ImageView) itemView.findViewById(R.id.image);
            ProgressBar progressBar = itemView.findViewById(R.id.progress);
            progressBar.getIndeterminateDrawable().setColorFilter(
                    context.getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY);
            String img = data.get(position).getBannerImage();
            Log.d(TAG, "instantiateItem: " + data.get(position).getBannerImage());
            if (img != null && !TextUtils.isEmpty(img)) {
                Picasso.get()
                        .load(img)
                        .error(R.drawable.applogo)
                        .into(image, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                progressBar.setVisibility(GONE);
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e(TAG, "onError: " + e.getMessage());
                            }
                        });
            }

            image.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Patterns.WEB_URL.matcher(data.get(position).getBannerURL()).matches()) {
                        try {
                            Intent b = new Intent(Intent.ACTION_VIEW, Uri.parse(data.get(position).getBannerURL()));
                            startActivity(b);
                            if (inviteDialog != null) {
                                inviteDialog.cancel();
                            }
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(FragmentMainActivity.this, getString(R.string.url_invalid), Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            });

            ((ViewPager) collection).addView(itemView, 0);
            return itemView;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            int index = data.indexOf(object);

            if (index == -1)
                return POSITION_NONE;
            else
                return index;
        }

        @Override
        public void destroyItem(ViewGroup arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView((View) arg2);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == ((View) arg1);

        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }

    private void openDeleteDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(
                FragmentMainActivity.this, com.google.android.material.R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        builder.setMessage(getString(R.string.accountdelete_succesfully));
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditProfile.deleteaccount = false;
                dialog.cancel();
                Intent intent = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                startActivity(intent);
            }
        });
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
        Typeface typeface = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeface = getResources().getFont(R.font.font_regular);
        } else {
            typeface = ResourcesCompat.getFont(this, R.font.font_regular);
        }
        TextView textView = dialog.findViewById(android.R.id.message);
        textView.setTypeface(typeface);

        Button btn1 = dialog.findViewById(android.R.id.button1);
        btn1.setTypeface(typeface);

        Button btn2 = dialog.findViewById(android.R.id.button2);
        btn2.setTypeface(typeface);

    }

    public void initlauncher() {
        locations = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                new ActivityResultCallback<Map<String, Boolean>>() {
                    @Override
                    public void onActivityResult(Map<String, Boolean> result) {
                        boolean granted = true;
                        Log.d(TAG, "onActivityResult: " + result);

                        for (Map.Entry<String, Boolean> x : result.entrySet()) {
                            if (!x.getValue()) {
                                if (ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                                        ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                        && ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                                                ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    granted = false;
                                } else {
                                    granted = true;
                                }
                            }
                            Log.d(TAG, "locations: " + granted);
                        }

                        if (granted) {
                            createLocationCallback();
                            int permissionLocation = ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                                    ACCESS_FINE_LOCATION);
                            /* if (permissionLocation == PackageManager.PERMISSION_GRANTED) { */
                            if (isGPSOn) {
                                startLocationUpdates();
                            } else {
                                turnGPSOn();
                            }
                            /* } */

                        } else {
                            boolean neverAsked = false;
                            if (ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                                    ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                    || ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                                            ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                createLocationCallback();
                            }
                            Log.d(TAG, "requestPermissions: ");
                            for (Map.Entry<String, Boolean> x : result.entrySet()) {
                                if (!x.getValue()) {
                                    if (ActivityCompat.shouldShowRequestPermissionRationale(FragmentMainActivity.this,
                                            x.getKey())) {
                                        locations.launch(result.keySet().toArray(new String[result.size()]));
                                    } else {
                                        if (ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                                                ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                                && ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                                                        ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                            PermissionsUtils.openPermissionDialog(FragmentMainActivity.this,
                                                    new OkayCancelCallback() {
                                                        @Override
                                                        public void onOkayClicked(Object o) {
                                                            Intent intent = new Intent(
                                                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                                    Uri.fromParts("package", BuildConfig.APPLICATION_ID,
                                                                            null));
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                            startActivity(intent);
                                                        }

                                                        @Override
                                                        public void onCancelClicked(Object o) {

                                                        }
                                                    }, FragmentMainActivity.this.getString(R.string.ask_for_locations));
                                        }
                                        break;
                                    }
                                }
                            }
                        }

                    }
                });

        notificationPermission = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                new ActivityResultCallback<Map<String, Boolean>>() {
                    @Override
                    public void onActivityResult(Map<String, Boolean> result) {
                        boolean granted = true;
                        Log.d(TAG, "onActivityResult: " + result);
                        for (Map.Entry<String, Boolean> x : result.entrySet()) {
                            if (ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                                    POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                if (ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                                        ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                        && ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                                                ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    granted = false;
                                } else if (ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                                        POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                    granted = false;
                                } else {
                                    granted = true;
                                }
                            } else if (ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                                    POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                                if (ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                                        ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                        && ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                                                ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    granted = false;
                                } else if (ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                                        POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                    granted = false;
                                } else {
                                    granted = true;
                                }
                            }
                        }

                        if (granted) {

                        } else {
                            boolean neverAsked = false;
                            Log.d(TAG, "requestPermissions: ");
                            for (Map.Entry<String, Boolean> x : result.entrySet()) {
                                if (!x.getValue()) {
                                    if (ActivityCompat.shouldShowRequestPermissionRationale(FragmentMainActivity.this,
                                            x.getKey())) {
                                        notificationPermission
                                                .launch(result.keySet().toArray(new String[result.size()]));
                                    } else {
                                        openPermissionDialogs(FragmentMainActivity.this, new OkayCancelCallback() {
                                            @Override
                                            public void onOkayClicked(Object o) {
                                                Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                        Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(i);
                                            }

                                            @Override
                                            public void onCancelClicked(Object o) {
                                                Toast.makeText(FragmentMainActivity.this,
                                                        getResources().getString(
                                                                R.string.notification_permission_location_dialog),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }, getResources().getString(R.string.notification_permission_location_dialog));

                                    }
                                    break;
                                }
                            }
                        }

                    }
                });
        phonepermissionresult = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                new ActivityResultCallback<Map<String, Boolean>>() {
                    @Override
                    public void onActivityResult(Map<String, Boolean> result) {
                        boolean granted = true;
                        Log.d(TAG, "onActivityResult: " + result);
                        for (Map.Entry<String, Boolean> x : result.entrySet()) {
                            if (!x.getValue())
                                granted = false;
                        }

                        if (granted) {

                        } else {
                            boolean neverAsked = false;
                            Log.d(TAG, "requestPermissions: ");
                            for (Map.Entry<String, Boolean> x : result.entrySet()) {
                                if (!x.getValue()) {
                                    if (ActivityCompat.shouldShowRequestPermissionRationale(FragmentMainActivity.this,
                                            x.getKey())) {
                                        phonepermissionresult
                                                .launch(result.keySet().toArray(new String[result.size()]));
                                    } else {

                                    }
                                    break;
                                }
                            }
                        }

                    }
                });

    }

    public static void openPermissionDialogs(Context mContext, OkayCancelCallback callback, String description) {
        new androidx.appcompat.app.AlertDialog.Builder(mContext)
                .setMessage(description)
                .setPositiveButton(mContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
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

    public boolean isPermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    protected boolean checkNotificationPermisson() {
        return ContextCompat.checkSelfPermission(this,
                POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    }

    private void showThemeDialog() {
        final String[] themes = { getString(R.string.dark), getString(R.string.light) };
        String currentTheme = Constants.pref.getString("app_theme", getString(R.string.dark));
        int checkedItem = currentTheme.equals(getString(R.string.dark)) ? 0 : 1;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_theme);
        builder.setSingleChoiceItems(themes, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedTheme = themes[which];
                Constants.editor.putString("app_theme", selectedTheme);
                Constants.editor.commit();

                if (selectedTheme.equals(getString(R.string.dark))) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.cancel), null);
        builder.create().show();
    }

}
