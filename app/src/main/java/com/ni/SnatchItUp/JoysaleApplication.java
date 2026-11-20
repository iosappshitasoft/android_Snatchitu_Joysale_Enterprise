package com.ni.SnatchItUp;

import static com.ni.utils.Constants.isCallPopup;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.Html;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.multidex.MultiDex;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.firebase.FirebaseApp;
import com.ni.addons.call.CallActivity;
import com.ni.addons.call.CallNotificationService;
import com.ni.helper.LocaleManager;
import com.ni.helper.NetworkReceiver;
import com.ni.helper.callback.OnButtonClick;
import com.ni.helper.worker.AppWorkManager;
import com.ni.utils.Constants;
import com.ni.utils.GetSet;

//import com.mapbox.android.core.location.LocationEngine;
//import com.mapbox.navigation.base.options.NavigationOptions;
//import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.stripe.android.PaymentConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Created by hitasoft
 * <p>
 * This class is to Provide a common methods for whole application.
 */
public class JoysaleApplication extends Application implements DefaultLifecycleObserver {
    static final String TAG = JoysaleApplication.class.getSimpleName();
    /**
     * Declare Variables
     **/
    public static SharedPreferences adminPref;
    public static SharedPreferences.Editor adminEditor;
    private static JoysaleApplication mInstance;
    private static Dialog dialog;
    private RequestQueue mRequestQueue;
    private static Activity mCurrentActivity = null;
    private Intent callNotificationService;
    public static boolean onAppForegrounded = false;
    // Addon Mapbox
//    public static LocationEngine locationEngine;
    private PeriodicWorkRequest periodicWorkRequest;
    private static final String PERIODIC_WORK_MANAGER = "PERIODIC_WORK_MANAGER";

    /**
     * for avoiding emoji typing in keyboard
     **/

    public static InputFilter EMOJI_FILTER = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int index = start; index < end; index++) {

                int type = Character.getType(source.charAt(index));

                if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL) {
                    Toast.makeText(getApplicationContext2(),
                            getApplicationContext2().getResources().getString(R.string.emoji_message),
                            Toast.LENGTH_LONG).show();
                    return "";
                }
            }
            return null;
        }
    };

    private DefaultLifecycleObserver lifecycleObserver = new DefaultLifecycleObserver() {
        @Override
        public void onCreate(@NonNull LifecycleOwner owner) {
            DefaultLifecycleObserver.super.onCreate(owner);
        }

        @Override
        public void onStart(@NonNull LifecycleOwner owner) {
            DefaultLifecycleObserver.super.onStart(owner);
        }

        @Override
        public void onResume(@NonNull LifecycleOwner owner) {
            onAppForegrounded = true;
            if (NetworkReceiver.isConnected() && GetSet.getUserId() != null) {
                if (GetSet.isLogged()) {
                    // Addon Call
                    if (CallActivity.isInCall && callNotificationService != null) {
                        stopService(callNotificationService);
                        callNotificationService = null;
                    }
                }
            }
        }

        @Override
        public void onPause(@NonNull LifecycleOwner owner) {
            onAppForegrounded = false;
            DefaultLifecycleObserver.super.onPause(owner);
        }

        @Override
        public void onStop(@NonNull LifecycleOwner owner) {
            Log.d(TAG, "onAppBackgrounded: ");
            onAppForegrounded = false;
            if (GetSet.isLogged()) {
                // Addon Call
                if (CallActivity.isInCall) {
                    callNotificationService = new Intent(getBaseContext(), CallNotificationService.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(callNotificationService);
                    } else {
                        startService(callNotificationService);
                    }
                }
            }
        }

        @Override
        public void onDestroy(@NonNull LifecycleOwner owner) {
            Log.d(TAG, "onDestroy: ");
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Firebase
        // FirebaseApp.initializeApp(this);
        FirebaseApp firebaseApp = FirebaseApp.initializeApp(this);

        Log.d("FIREBASE_INIT", "Firebase app: " + firebaseApp);
        Log.d("FIREBASE_INIT", "Firebase apps count: " + FirebaseApp.getApps(this).size());
        // Set default theme to Dark
        if (getSharedPreferences("JoysalePref", MODE_PRIVATE).getString("app_theme", "Dark").equals("Dark")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        mInstance = this;
        isCallPopup = false;
        startWorkManager();
        long maxSize = 50 * 1024 * 1024; // 50 MB


        // Addon Mapbox
        if (Constants.ADDON_MAP_BOX_ENABLED) {
       /*     MapboxNavigationApp.setup(
                    new NavigationOptions.Builder(this)
                            .accessToken(getString(R.string.mapbox_access_token))
                            .build());*/
            /*
             * MapboxSearchSdk.initialize(
             * JoysaleApplication.getInstance(),
             * getString(R.string.mapbox_access_token),
             * locationEngine = LocationEngineProvider.getBestLocationEngine(this)
             * );
             */
            // locationEngine = LocationEngineProvider.getBestLocationEngine(this);
        }
        Picasso picasso = new Picasso.Builder(this).downloader(new OkHttp3Downloader(getCacheDir(), maxSize)).build();
        Picasso.setSingletonInstance(picasso);
        PaymentConfiguration.init(
                getApplicationContext(),
                "pk_test_TYooMQauvdEDq54NiTphI7jx");

        ProcessLifecycleOwner.get().getLifecycle().addObserver(lifecycleObserver);
        Constants.ANDROID_ID = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);

        adminPref = getApplicationContext().getSharedPreferences("JoysaleAdminPref",
                MODE_PRIVATE);
        adminEditor = adminPref.edit();

        if (adminPref.getBoolean(Constants.TAG_BUYNOW, Constants.BUYNOW)) {
            Constants.BUYNOW = true;
        } else {
            Constants.BUYNOW = false;
        }

        if (adminPref.getBoolean("exchange", Constants.EXCHANGE)) {
            Constants.EXCHANGE = true;
        } else {
            Constants.EXCHANGE = false;
        }

        if (adminPref.getBoolean("promotion", Constants.PROMOTION)) {
            Constants.PROMOTION = false;
        } else {
            Constants.PROMOTION = false;
        }

        Constants.PAIDBANNER = adminPref.getBoolean(Constants.PREF_PAIDBANNER, Constants.PAIDBANNER);
        Constants.SITEMAINTAIN = adminPref.getBoolean(Constants.PREF_SITEMAINTAIN, Constants.SITEMAINTAIN);

        Constants.pref = getApplicationContext().getSharedPreferences("JoysalePref",
                MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();

        // setLanguage(this);

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
        }


    }

    public void startWorkManager() {
        WorkManager workManager = WorkManager.getInstance(this);
        workManager.cancelAllWorkByTag(PERIODIC_WORK_MANAGER);
        periodicWorkRequest = new PeriodicWorkRequest.Builder(AppWorkManager.class, 15, TimeUnit.MINUTES)
                .addTag(PERIODIC_WORK_MANAGER)
                .build();
        workManager.enqueue(periodicWorkRequest);
    }

    // Addon MapBox
  /*  public static LocationEngine getLocationEngine() {
        return locationEngine;
    }*/

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleManager.setLocale(this);
    }

    public static void preventMultipleClick(final View view) {
        view.setEnabled(false);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setEnabled(true);
            }
        }, 1000);
    }

    public static void setLanguage(Context context) {
        String[] languages, langCode;
        languages = context.getResources().getStringArray(R.array.languages);
        langCode = context.getResources().getStringArray(R.array.languageCode);
        Constants.pref = context.getSharedPreferences("JoysalePref",
                MODE_PRIVATE);
        String selectedLang = Constants.pref.getString(Constants.PREF_LANGUAGE, Constants.LANGUAGE);

        int index = Arrays.asList(languages).indexOf(selectedLang);
        String languageCode = Arrays.asList(langCode).get(index);
        Log.v(TAG, "languageCode=" + languageCode);

        Locale myLocale = new Locale(languageCode);
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.setLocale(myLocale);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(conf);
        } else {
            res.updateConfiguration(conf, dm);
        }
    }

    public static Locale getLocale(Resources res) {
        Configuration config = res.getConfiguration();
        return Build.VERSION.SDK_INT >= 24 ? config.getLocales().get(0) : config.locale;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
        MultiDex.install(this);
    }

    /**
     * Function to Show Network Error
     **/
    public static void networkError(final Activity activity, boolean isConnected) {
        dismissNetworkDialog();

        if (isConnected || activity == null || activity.isFinishing()
                || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed())) {
            return;
        }

        try {
            dialog = new Dialog(activity, R.style.PostDialog);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setContentView(R.layout.network_dialog);
            dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setCancelable(true);

            TextView ok = (TextView) dialog.findViewById(R.id.alert_button);
            TextView cancel = (TextView) dialog.findViewById(R.id.alert_cancel);
            ok.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismissNetworkDialog();
                    try {
                        Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                        activity.startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            cancel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismissNetworkDialog();
                }
            });

            dialog.show();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (WindowManager.BadTokenException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Safely dismiss the static network dialog (may belong to a finished Activity). */
    private static void dismissNetworkDialog() {
        if (dialog == null) {
            return;
        }
        try {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        } catch (IllegalArgumentException | WindowManager.BadTokenException e) {
            // Dialog window already detached (e.g. SplashActivity finished)
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dialog = null;
        }
    }

    public static synchronized JoysaleApplication getInstance() {
        return mInstance;
    }

    public static String loadJSONFromAsset(Context context, String name) {
        String json = null;
        try {

            InputStream is = context.getResources().getAssets().open(name);

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
     * Function to check Network availability
     **/

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();

            if (info != null) {
                for (NetworkInfo networkInfo : info) {
                    Log.i(TAG, "isNetworkAvailable: " + networkInfo.getState().toString());
                    if (networkInfo.getState() == NetworkInfo.State.CONNECTED
                            || networkInfo.getState() == NetworkInfo.State.CONNECTING) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * for closing the keyboard while touch outside
     **/

    public static void setupUI(Context context, View view) {
        final Activity act = (Activity) context;
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(act);
                    return false;
                }

            });
        }

        // If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(act, innerView);
            }
        }
    }

    /**
     * Function Hide a keyboard
     **/

    public static void hideSoftKeyboard(Activity context) {
        try {
            if (context == null) {
                return;
            }
            View focused = context.getCurrentFocus();
            if (focused == null) {
                return;
            }
            InputMethodManager inputMethodManager = (InputMethodManager) context
                    .getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(focused.getWindowToken(), 0);
            }
        } catch (Exception npe) {
            npe.printStackTrace();
        }

    }

    /**
     * Function for Common Dialog
     **/

    public static void dialog(Context ctx, String title, String content) {
        final Dialog dialog = new Dialog(ctx, R.style.AlertDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) ctx).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        dialog.getWindow().setLayout(displayMetrics.widthPixels * 90 / 100, LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        TextView alertTitle = (TextView) dialog.findViewById(R.id.alert_title);
        TextView alertMsg = (TextView) dialog.findViewById(R.id.alert_msg);
        TextView alertOk = (TextView) dialog.findViewById(R.id.alert_button);

        alertTitle.setText(title);
        if (!content.equalsIgnoreCase("call")) {
            alertMsg.setText(content);
        } else {
            alertMsg.setText(title);
        }

        alertOk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (content.equalsIgnoreCase(((Activity) ctx).getString(R.string.user_not_registered_yet))) {

                } else if (content.equalsIgnoreCase("call")) {
                    ctx.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + ctx.getPackageName())));
                    Toast.makeText(ctx, R.string.read_phone_state_permission_forandroid12, Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();

            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }

    }

    // addons audioVideo
    public static Context getApplicationContext2() {
        return mInstance;
    }

    public static void uploadAlertDialog(Context ctx, String title, String content) {
        final Dialog dialog = new Dialog(ctx, R.style.AlertDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) ctx).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        dialog.getWindow().setLayout(displayMetrics.widthPixels * 90 / 100, LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);

        TextView alertTitle = (TextView) dialog.findViewById(R.id.alert_title);
        TextView alertMsg = (TextView) dialog.findViewById(R.id.alert_msg);
        TextView alertOk = (TextView) dialog.findViewById(R.id.alert_button);

        alertTitle.setText(title);
        alertMsg.setText(content);

        alertOk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }

    }

    public static void dialogOkCancel(Context ctx, String title, String content, OnButtonClick onButtonClick) {
        final Dialog dialog = new Dialog(ctx, R.style.AlertDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) ctx).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        dialog.getWindow().setLayout(displayMetrics.widthPixels * 90 / 100, LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        TextView alertTitle = (TextView) dialog.findViewById(R.id.alert_title);
        TextView alertMsg = (TextView) dialog.findViewById(R.id.alert_msg);
        TextView alertOk = (TextView) dialog.findViewById(R.id.alert_button);
        TextView alertCancel = (TextView) dialog.findViewById(R.id.cancel_button);

        alertCancel.setVisibility(View.VISIBLE);
        alertTitle.setText(title);
        alertMsg.setText(content);

        alertOk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                onButtonClick.onOkClicked();
            }
        });

        alertCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                onButtonClick.onCancelClicked();
            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }

    }

    /**
     * Function to Check RTL
     **/

    /**
     * Function to Disable Dialog for Application
     **/

    public static void disabledialog(final Context ctx, final String content, String userId) {
        ((Activity) ctx).runOnUiThread(new Runnable() {

            @Override
            public void run() {
                final Dialog dialog = new Dialog(ctx, R.style.AlertDialog);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.setContentView(R.layout.default_dialog);
                DisplayMetrics displayMetrics = new DisplayMetrics();
                ((Activity) ctx).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                dialog.getWindow().setLayout(displayMetrics.widthPixels * 90 / 100, LayoutParams.WRAP_CONTENT);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);

                TextView alertMsg = (TextView) dialog.findViewById(R.id.alert_msg);
                TextView alertOk = (TextView) dialog.findViewById(R.id.alert_button);

                alertMsg.setText(content);

                alertOk.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        try {
                            if (userId.equals(GetSet.getUserId())) {
                                Constants.editor.clear();
                                Constants.editor.commit();
                                GetSet.reset();
                                FragmentMainActivity.homeItemList.clear();
                                if (FragmentMainActivity.itemAdapter != null) {
                                    FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                                }
                                WelcomeActivity.fromSignout = true;
                                dialog.dismiss();
                                ((Activity) ctx).finish();
                                Intent intent = new Intent(ctx, WelcomeActivity.class);
                                ((Activity) ctx).startActivity(intent);
                            } else {
                                dialog.dismiss();
                                ((Activity) ctx).finish();
                            }

                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                if (!dialog.isShowing()) {
                    dialog.show();
                }

            }
        });

    }

    /**
     * to display the text without html content
     **/

    public static String stripHtml(String html) {
        Spanned spanned;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            spanned = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            spanned = Html.fromHtml(html);
        }
        return spanned.toString().trim();
    }

    public static String format(double number) {
        if (number < 1000) {
            int c = (int) number;
            return Integer.toString(c);
        } else {
            String[] suffix = new String[] { "", "k", "m", "b", "t" };
            int MAX_LENGTH = 4;
            String r = new DecimalFormat("##0E0").format(number);
            r = r.replaceAll("E[0-9]", suffix[Character.getNumericValue(r.charAt(r.length() - 1)) / 3]);
            while (r.length() > MAX_LENGTH || r.matches("[0-9]+\\.[a-z]")) {
                r = r.substring(0, r.length() - 2) + r.substring(r.length() - 1);
            }
            return r;
        }
    }

    /**
     * Function to Resize Bitmap
     **/
    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    /*
     * public static Bitmap getResizedBitmap(File file) {
     * BitmapFactory.Options options = new BitmapFactory.Options();
     * options.inJustDecodeBounds = true;
     * int imageHeight = options.outHeight;
     * int imageWidth = options.outWidth;
     * // Calculate inSampleSize
     * options.inSampleSize = calculateInSampleSize(options, imageWidth,
     * imageHeight);
     * // Decode bitmap with inSampleSize set
     * options.inJustDecodeBounds = false;
     * return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
     * }
     */

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        Log.i(TAG, "calculateInSampleSize: " + reqWidth);
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * To convert the given dp value to pixel
     **/

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    /**
     * To convert the given pixel to dp value
     **/

    public static float pxToDp(Context context, float px) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    /**
     * To convert timestamp to Date
     **/

    public static String getDate(long timeStamp) {
        try {
            DateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.US);
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "xx";
        }
    }

    /**
     * To convert timestamp to Date
     **/

    public static String getDate(Context context, long timeStamp) {
        try {
            DateFormat sdf = new SimpleDateFormat("MMM d, yyyy", new Locale("en"));
            Date netDate = (new Date(timeStamp * 1000));
            return sdf.format(netDate);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    /**
     * To convert timestamp to Date and give a required format
     **/

    public static String getDate(long timeStamp, String format) {
        try {
            DateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "xx";
        }
    }

    public static String getShippingDate(long timeStamp) {
        try {
            DateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());
            DateFormat sdf = new SimpleDateFormat("dd-yyyy", Locale.ENGLISH);
            Date netDate = (new Date(timeStamp));
            return monthFormat.format(netDate) + "-" + sdf.format(netDate);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "xx";
        }
    }

    /**
     * Functions for Volley Lib Access
     **/

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        req.setRetryPolicy(new DefaultRetryPolicy(20000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        req.setRetryPolicy(new DefaultRetryPolicy(20000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public void setConnectivityListener(NetworkReceiver.ConnectivityReceiverListener listener) {
        NetworkReceiver.connectivityReceiverListener = listener;
    }

    /**
     * Class for Price Decimal Filter
     **/

    public static class DecimalDigitsInputFilter implements InputFilter {
        long mDigitsBeforeZero, mDigitsAfterZero;
        Pattern mPattern;

        public DecimalDigitsInputFilter(long digitsBeforeZero, long digitsAfterZero) {
            this.mDigitsBeforeZero = (digitsBeforeZero);
            this.mDigitsAfterZero = (digitsAfterZero);
            mPattern = Pattern.compile("-?[0-9]{0," + (mDigitsBeforeZero) + "}+((\\.[0-9]{0," + (mDigitsAfterZero)
                    + "})?)||(\\.)?");
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            String replacement = source.subSequence(start, end).toString();
            String newVal = dest.subSequence(0, dstart).toString() + replacement
                    + dest.subSequence(dend, dest.length()).toString();

            Matcher matcher = mPattern.matcher(newVal);

            if (matcher.matches()) {
                return null;
            }
            if (TextUtils.isEmpty(source)) {
                return dest.subSequence(dstart, dend);
            } else {
                return "";
            }
        }
    }

    public static InputFilter USERNAME_FILTER = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            boolean keepOriginal = true;
            StringBuilder sb = new StringBuilder(end - start);
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (isCharAllowed(c)) // put your condition here
                    sb.append(c);
                else
                    keepOriginal = false;
            }
            if (keepOriginal)
                return null;
            else {
                if (source instanceof Spanned) {
                    SpannableString sp = new SpannableString(sb);
                    TextUtils.copySpansFrom((Spanned) source, start, sb.length(), null, sp, 0);
                    return sp;
                } else {
                    return sb;
                }
            }
        }

        private boolean isCharAllowed(char c) {
            return Character.isLetterOrDigit(c);
        }
    };

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    /**
     * Returns the consumer friendly device name
     */
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        return manufacturer;
    }

    public static String getDeviceModel() {
        String model = Build.MODEL;
        return model;
    }

    /**
     * Returns the consumer friendly device name & model
     */
    public static String getDeviceNameModel() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        return manufacturer + " " + model;
    }

    public static String getFormattedPrice(Context context, String currencySymbol, String currencyCode,
            String currencyMode, String currencyPosition, String price) {
        String formattedPrice;
        if (currencyMode.equals("symbol")) {
            if (currencyPosition.equals("prefix")) {
                if (LocaleManager.isRTL(context)) {
                    formattedPrice = price + " " + currencySymbol;
                } else {
                    formattedPrice = currencySymbol + " " + price;
                }
            } else {
                if (LocaleManager.isRTL(context)) {
                    formattedPrice = currencySymbol + " " + price;
                } else {
                    formattedPrice = price + " " + currencySymbol;
                }
            }
        } else {
            if (currencyPosition.equals("prefix")) {
                if (LocaleManager.isRTL(context)) {
                    formattedPrice = price + " " + currencyCode;
                } else {
                    formattedPrice = currencyCode + " " + price;
                }
            } else {
                if (LocaleManager.isRTL(context)) {
                    formattedPrice = currencyCode + " " + price;
                } else {
                    formattedPrice = price + " " + currencyCode;
                }
            }
        }
        return formattedPrice;
    }

    public static Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    public static void setCurrentActivity(AppCompatActivity activity) {
        mCurrentActivity = activity;
    }

    public static void openDeleteDialog(String message, String ok) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getCurrentActivity(),
                com.google.android.material.R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        builder.setMessage(message);
        builder.setPositiveButton(ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        Typeface typeface = null;
        /*
         * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
         * typeface = getResources().getFont(R.font.font_regular);
         * } else {
         * typeface = ResourcesCompat.getFont(this, R.font.font_regular);
         * }
         */
        TextView textView = dialog.findViewById(android.R.id.message);
        textView.setTypeface(typeface);

        Button btn1 = dialog.findViewById(android.R.id.button1);
        btn1.setTypeface(typeface);

        Button btn2 = dialog.findViewById(android.R.id.button2);
        btn2.setTypeface(typeface);

    }

}
