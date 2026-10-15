package com.ni.addons;

import android.graphics.Insets;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowMetrics;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.ni.SnatchItUp.BuildConfig;
import com.ni.utils.Constants;

public class BannerAdUtils {
    static AdView adView = null;
    /** Live banner unit. Google sample test banner only for local DEBUG builds. */
    public static final String LIVE_BANNER_ID = "ca-app-pub-9552829837499366/4470536152";
    public static final String GOOGLE_TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111";
    private static final String TAG = "BannerAdUtils";

    public static void loadBannerAd(FragmentActivity activity, AdView adView) {
        if (Constants.TAG_GOOGLEADS_STATUS.equalsIgnoreCase("Enable")) {
            MobileAds.initialize(activity, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
                }
            });

            AdRequest adRequest = new AdRequest.Builder().build();
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                }

                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    Log.e(activity.getLocalClassName(), "onAdFailedToLoad: " + loadAdError.getCode());
                    Log.e(activity.getLocalClassName(), "onAdFailedToLoad: " + loadAdError.getMessage());
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    Log.e(activity.getLocalClassName(), "onAdLoaded: ");
                }

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                }
            });
            adView.loadAd(adRequest);

        }
    }

    public static void loadBannerAd(FragmentActivity activity, FrameLayout adLay) {
        if (Constants.TAG_GOOGLEADS_STATUS.equalsIgnoreCase("Enable") && Constants.ADDON_BANNER_AD) {
            MobileAds.initialize(activity, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
                }
            });
            adView = new AdView(activity);
            adView.setAdUnitId(getBannerAdUnitId());
            adLay.removeAllViews();
            adLay.addView(adView);
            adView.setAdSize(getAdSize(activity));
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                }

                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    Log.e(activity.getLocalClassName(), "onAdFailedToLoad: " + loadAdError.getCode());
                    Log.e(activity.getLocalClassName(), "onAdFailedToLoad: " + loadAdError.getMessage());
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                }

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                }
            });
            // Initiate a generic request to load it with an ad
            /*List<String> testDevices = new ArrayList<>();
            testDevices.add("8024116C4033A1FFE31F4B53022EE848");
            testDevices.add("2F8ED1189BACAB56382F18AF1C810828");
            RequestConfiguration requestConfiguration
                    = new RequestConfiguration.Builder()
                    .setTestDeviceIds(testDevices)
                    .build();
            MobileAds.setRequestConfiguration(requestConfiguration);*/

            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            adView.loadAd(adRequest);
        }
    }

    public static void onResume(AdView adView) {
        if (adView != null) {
            adView.resume();
        }
    }

    public static void onPause(AdView adView) {
        if (adView != null) {
            adView.pause();
        }
    }

    public static void onDestroy(AdView adView) {
        if (adView != null) {
            adView.destroy();
        }
    }

    public static void onResume() {
        if (adView != null) {
            adView.resume();
        }
    }

    public static void onPause() {
        if (adView != null) {
            adView.pause();
        }
    }

    public static void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
    }

    public static boolean isBannerAdEnabled() {
        if (!Constants.ADDON_BANNER_AD || TextUtils.isEmpty(getBannerAdUnitId())) {
            return false;
        }
        String status = Constants.TAG_GOOGLEADS_STATUS;
        if (TextUtils.isEmpty(status)) {
            return false;
        }
        status = status.trim();
        if (status.equalsIgnoreCase("disable")
                || status.equalsIgnoreCase("disabled")
                || status.equalsIgnoreCase("false")
                || status.equalsIgnoreCase("0")
                || status.equalsIgnoreCase("off")
                || status.equalsIgnoreCase("no")) {
            return false;
        }
        return true;
    }

    public static String getBannerAdUnitId() {
        if (BuildConfig.DEBUG) {
            return GOOGLE_TEST_BANNER_ID;
        }
        String id = Constants.ADMOB_BANNER_ID;
        if (isValidAdUnitId(id)) {
            return id.trim();
        }
        return LIVE_BANNER_ID;
    }

    public static boolean isValidAdUnitId(String id) {
        return !TextUtils.isEmpty(id) && id.contains("ca-app-pub-") && id.contains("/");
    }

    public static void applyAdminBannerId(String bannerId) {
        // Keep live banner unless admin sends a real ad unit (…/…), not an app id (…~…).
        if (isValidAdUnitId(bannerId)) {
            Constants.ADMOB_BANNER_ID = bannerId.trim();
        }
    }

    public static void loadListBannerAd(final FragmentActivity activity, final FrameLayout adContainer) {
        if (activity == null || adContainer == null || !isBannerAdEnabled()) {
            Log.e(TAG, "skip list ad. status=" + Constants.TAG_GOOGLEADS_STATUS
                    + " unit=" + Constants.ADMOB_BANNER_ID);
            if (adContainer != null) {
                adContainer.setVisibility(View.GONE);
            }
            return;
        }

        AdView existingAd = findChildAdView(adContainer);
        if (existingAd != null && isAdViewReady(existingAd)) {
            adContainer.setVisibility(View.VISIBLE);
            existingAd.setVisibility(View.VISIBLE);
            if (Boolean.TRUE.equals(existingAd.getTag()) || "loading".equals(existingAd.getTag())) {
                return;
            }
        }

        final AdView adView = existingAd != null && isAdViewReady(existingAd)
                ? existingAd
                : createListAdView(activity, adContainer);
        if (adView == null || !isAdViewReady(adView)) {
            Log.e(TAG, "skip list ad: ad size or unit id missing");
            adContainer.setVisibility(View.GONE);
            return;
        }

        adContainer.setVisibility(View.VISIBLE);
        adView.setVisibility(View.VISIBLE);
        adView.setTag("loading");
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.e(TAG, "list ad failed: " + loadAdError.getCode()
                        + " " + loadAdError.getMessage());
                adView.setTag(null);
                adContainer.setVisibility(View.GONE);
            }

            @Override
            public void onAdLoaded() {
                adView.setTag(Boolean.TRUE);
                adContainer.setVisibility(View.VISIBLE);
                adView.setVisibility(View.VISIBLE);
                Log.d(TAG, "list ad loaded");
            }
        });

        try {
            adView.loadAd(new AdRequest.Builder().build());
        } catch (Exception e) {
            Log.e(TAG, "list ad load error", e);
            MobileAds.initialize(activity, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
                    if (activity.isFinishing() || activity.isDestroyed()) {
                        return;
                    }
                    if (!isAdViewReady(adView)) {
                        Log.e(TAG, "skip loadAd: ad size or unit id not set");
                        return;
                    }
                    adView.loadAd(new AdRequest.Builder().build());
                }
            });
        }
    }

    private static AdView createListAdView(FragmentActivity activity, FrameLayout adContainer) {
        destroyListAd(adContainer);
        AdView adView = new AdView(activity);
        adView.setAdSize(AdSize.MEDIUM_RECTANGLE);
        adView.setAdUnitId(getBannerAdUnitId());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);
        adContainer.addView(adView, params);
        return adView;
    }

    private static AdView findChildAdView(FrameLayout adContainer) {
        if (adContainer.getChildCount() > 0 && adContainer.getChildAt(0) instanceof AdView) {
            return (AdView) adContainer.getChildAt(0);
        }
        return null;
    }

    private static boolean isAdViewReady(AdView adView) {
        return adView != null
                && adView.getAdSize() != null
                && !TextUtils.isEmpty(adView.getAdUnitId());
    }

    public static void destroyListAd(FrameLayout adLay) {
        if (adLay == null) {
            return;
        }
        for (int i = 0; i < adLay.getChildCount(); i++) {
            View child = adLay.getChildAt(i);
            if (child instanceof AdView) {
                ((AdView) child).destroy();
            }
        }
        adLay.removeAllViews();
    }

    public static AdSize getListAdSize(FragmentActivity activity) {
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        int adWidth = (int) (displayMetrics.widthPixels / displayMetrics.density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, Math.max(adWidth, 320));
    }

    public static AdSize getAdSize(FragmentActivity activity) {
        float widthPixels;
        float density;
        int adWidth;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = activity.getWindowManager().getCurrentWindowMetrics();
            Insets insets = windowMetrics.getWindowInsets()
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
            widthPixels = windowMetrics.getBounds().width() - insets.left - insets.right;
            Display display = activity.getDisplay();
            DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
            display.getRealMetrics(displayMetrics);
            density = displayMetrics.densityDpi;
        } else {
            Display display = activity.getWindowManager().getDefaultDisplay();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            display.getMetrics(displayMetrics);
            widthPixels = displayMetrics.widthPixels;
            density = displayMetrics.density;
        }
        adWidth = (int) (widthPixels / density);
        /*Log.d(TAG, "getAdWidth: " + widthPixels);
        Log.d(TAG, "getAdWidth: " + density);
        Log.d(TAG, "getAdWidth: " + adWidth);*/
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth);
    }
}
