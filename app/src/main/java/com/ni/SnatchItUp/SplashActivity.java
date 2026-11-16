package com.ni.SnatchItUp;
/****************
 *
 * @author 'Hitasoft Technologies'
 *
 * Description:
 * This class is used for displaying splash screen
 *
 * Revision History:
 * Version 1.0 - Initial Version
 *
 *****************/

import static com.ni.SnatchItUp.FragmentMainActivity.categoryAry;
import static com.ni.utils.Constants.PREF_ADMIN_VALUE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.Toast;

import com.ni.addons.BannerAdUtils;
import com.ni.addons.InterstitialAdUtils;
import com.ni.helper.ImageStorage;
import com.ni.helper.LocaleManager;
import com.ni.helper.NetworkReceiver;
import com.ni.helper.SharedPrefManager;
import com.ni.model.AdminDataResponse;
import com.ni.utils.ApiClient;
import com.ni.utils.ApiInterface;
import com.ni.utils.Constants;

import java.util.HashMap;
import java.util.Map;

import io.socket.client.Socket;
import retrofit2.Call;
import retrofit2.Callback;

public class SplashActivity extends BaseActivity {

    /**
     * Declare variables
     **/
    private static final String TAG = SplashActivity.class.getSimpleName();
    private static final int SPLASH_TIME_OUT = 0;
    public static SharedPreferences pref;
    public static Editor editor;
    public static long beforeDecimal = 8, afterDecimal = 2;
    public static String adminCurrencyCode = null, adminPaymentType = null;
    public static String stripePublicKey = null;
    String languageCode;
    ApiInterface apiInterface;
    private ImageStorage imageStorage;
    private boolean doubleBackToExit;
    private String distanceType;
    private Socket mSocket;
    SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        setContentView(R.layout.splash_screen);
        pref = getApplicationContext().getSharedPreferences("JoysalePref", MODE_PRIVATE);
        editor = pref.edit();
        sharedPrefManager = SharedPrefManager.getInstance(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        imageStorage = new ImageStorage(this);
        JoysaleApplication.adminPref = getApplicationContext().getSharedPreferences("JoysaleAdminPref",
                MODE_PRIVATE);
        JoysaleApplication.adminEditor = JoysaleApplication.adminPref.edit();
        imageStorage.deleteCacheDir();
        languageCode = LocaleManager.getLanguageCode(this);
        JoysaleApplication.getInstance().setConnectivityListener(this);
    }

    /**
     * Function for get the admin default exchangeData
     **/

    private void getAdminData() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_LANG_TYPE, languageCode);

            Call<AdminDataResponse> call = apiInterface.getAdminData(map);
            call.enqueue(new Callback<AdminDataResponse>() {
                @Override
                public void onResponse(Call<AdminDataResponse> call, retrofit2.Response<AdminDataResponse> response) {
                    if (response.isSuccessful()) {
                        AdminDataResponse adminDataResponse = response.body();
                        if (adminDataResponse.getStatus().equalsIgnoreCase("true")) {
                            AdminDataResponse.Result result = adminDataResponse.getResult();
                            if (result.getSiteMaintenance().equals("enable")) {
                                Intent intent = new Intent(getApplicationContext(), SiteMaintainActivity.class);
                                intent.putExtra(Constants.TAG_DATA, result.getSiteMaintenanceText());
                                startActivity(intent);
                                finish();
                            } else {
                                try {

                                    if (result.getCategory() != null) {
                                        categoryAry.clear();
                                    }
                                    categoryAry.addAll(result.getCategory());

                                    FragmentMainActivity.homeBanner = result.getBanner();
                                    JoysaleApplication.adminEditor.putString(Constants.PREF_DISTANCE_TYPE, result.getDistanceType());
                                    JoysaleApplication.adminEditor.commit();

                                    Constants.BUYNOW = result.getBuyNow().equalsIgnoreCase("enable");
                                    JoysaleApplication.adminEditor.putBoolean(Constants.PREF_BUYNOW, Constants.BUYNOW);
                                    JoysaleApplication.adminEditor.commit();

                                    //Dynamic changes
                                    if (result.getmSocketUrl() != null)
                                        Constants.SOCKET_URL = result.getmSocketUrl();

                                    if (result.getGoogleAdsType() != null)
                                        Constants.TAG_GOOGLEADS_STATUS = result.getGoogleAdsType().trim();

                                    if (Constants.ADDON_INTERSTITIAL_AD) {
                                        InterstitialAdUtils.applyAdminInterstitialId(result.getInterstitial_ad_key());
                                    }

                                    if (Constants.ADDON_CALL_ENABLED && result.getApprtcUrl() != null)
                                        Constants.APPRTC_URL = result.getApprtcUrl();

                                    if (Constants.ADDON_BANNER_AD) {
                                        BannerAdUtils.applyAdminBannerId(result.getGoogle_ads_android());
                                    }

                                    Constants.EXCHANGE = result.getExchange().equalsIgnoreCase("enable");
                                    JoysaleApplication.adminEditor.putBoolean(Constants.PREF_EXCHANGE, Constants.EXCHANGE);
                                    JoysaleApplication.adminEditor.commit();

                                    Constants.PROMOTION = result.getPromotion().equalsIgnoreCase("enable");
                                    JoysaleApplication.adminEditor.putBoolean(Constants.PREF_PROMOTION, Constants.PROMOTION);
                                    JoysaleApplication.adminEditor.commit();

                                    Constants.GIVINGAWAY = result.getGivingAway().equalsIgnoreCase("enable");
                                    JoysaleApplication.adminEditor.putBoolean(Constants.PREF_GIVINGAWAY, Constants.GIVINGAWAY);
                                    JoysaleApplication.adminEditor.commit();

                                    Constants.PAIDBANNER = result.getPaidBanner().equalsIgnoreCase("enable");
                                    JoysaleApplication.adminEditor.putBoolean(Constants.PREF_PAIDBANNER, Constants.PAIDBANNER);
                                    JoysaleApplication.adminEditor.commit();

                                    Constants.SITEMAINTAIN = result.getPaidBanner().equalsIgnoreCase("enable");
                                    JoysaleApplication.adminEditor.putBoolean(Constants.PREF_SITEMAINTAIN, Constants.SITEMAINTAIN);
                                    JoysaleApplication.adminEditor.commit();

                                    if (FragmentMainActivity.bannerAry != null) {
                                        FragmentMainActivity.bannerAry.clear();
                                    }
                                    FragmentMainActivity.bannerAry.addAll(result.getBannerData());
                                    result.setBannerData(result.getBannerData());


                                    if (result.getPriceRange() != null) {
                                        beforeDecimal = result.getPriceRange().getBeforeDecimal();
                                        afterDecimal = result.getPriceRange().getAfterDecimal();
                                    }

                                    if (result.getAdminCurrencyCode() != null)
                                        adminCurrencyCode = result.getAdminCurrencyCode();

                                    if (result.getAdminPaymentType() != null)
                                        adminPaymentType = result.getAdminPaymentType();

                                    if (result.getStripePublicKey() != null && !TextUtils.isEmpty(result.getStripePublicKey())) {
                                        stripePublicKey = result.getStripePublicKey();
                                    }

                                    if (JoysaleApplication.adminPref.getString(Constants.PREF_ADMIN_DISTANCE, null) == null) {
                                        JoysaleApplication.adminEditor.putString(Constants.PREF_ADMIN_DISTANCE, response.body().getResult().getDistance());
                                        JoysaleApplication.adminEditor.commit();
                                    } else if (response.body().getResult().getDistance() != null) {
                                        JoysaleApplication.adminEditor.putString(Constants.PREF_ADMIN_DISTANCE, response.body().getResult().getDistance());
                                        JoysaleApplication.adminEditor.commit();
                                    }

                                    if (JoysaleApplication.adminPref.getString(PREF_ADMIN_VALUE, null) == null) {
                                        JoysaleApplication.adminEditor.putString(Constants.PREF_ADMIN_VALUE, "true");
                                        JoysaleApplication.adminEditor.commit();
                                    }

                                    if (ChatActivity.templatMsgAry != null) {
                                        ChatActivity.templatMsgAry.clear();
                                    }
                                    ChatActivity.templatMsgAry.addAll(result.getChatTemplate());


                                    Constants.EMAILVERIFICATION = result.getEmailVerification().equalsIgnoreCase("enable");
                                    JoysaleApplication.adminEditor.putBoolean(Constants.PREF_EMAILVERIFICATION, Constants.EMAILVERIFICATION);
                                    JoysaleApplication.adminEditor.commit();
                                    moveToHome();
                                } catch (NullPointerException e) {
                                    e.printStackTrace();
                                }
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

    public void moveToHome() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(SplashActivity.this,
                        FragmentMainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                finish();

                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        }, SPLASH_TIME_OUT);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExit) {
            super.onBackPressed();
        }
        this.doubleBackToExit = true;
        Toast.makeText(this, getString(R.string.double_back_exit), Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExit = false;
            }
        }, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (isConnected) {
            getAdminData();
        } else {
            JoysaleApplication.networkError(SplashActivity.this, isConnected);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Constants.ADDON_LOGIN_VIDEO) {
            /*ImageView splashImage = findViewById(R.id.splashImage);
            splashImage.setVisibility(View.GONE);
            VideoAddon.initVideo(findViewById(R.id.textureView), this);*/
        }
    }
}
