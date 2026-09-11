package com.ni.addons;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.ni.utils.Constants;


public class InterstitialAdUtils {

    private static final String TAG = InterstitialAdUtils.class.getSimpleName();
    public static InterstitialAd mInterstitialAd;


    public static void loadInterstitialAd(FragmentActivity activity, Bundle bundle) {
        if (Constants.ADDON_INTERSTITIAL_AD && Constants.TAG_GOOGLEADS_STATUS.equalsIgnoreCase("Enable")) {
            MobileAds.initialize(activity.getApplicationContext(), new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                }
            });
            AdRequest adRequest = new AdRequest.Builder().build();

            InterstitialAd.load(activity.getApplicationContext(), "ca-app-pub-9552829837499366/6507115513", adRequest,
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            // The mInterstitialAd reference will be null until
                            // an ad is loaded.
                            mInterstitialAd = interstitialAd;
                            Log.i(TAG, "onAdLoaded");
                            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                @Override
                                public void onAdClicked() {
                                    super.onAdClicked();
                                }

                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    mInterstitialAd = null;
                                    onAdClosed(activity, bundle);
                                }

                                @Override
                                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                    super.onAdFailedToShowFullScreenContent(adError);
                                    Log.e(TAG, "onAdFailedToShowFullScreenContent: " + adError.getMessage());
                                    mInterstitialAd = null;
                                }

                                @Override
                                public void onAdImpression() {
                                    super.onAdImpression();
                                }

                                @Override
                                public void onAdShowedFullScreenContent() {
                                    super.onAdShowedFullScreenContent();
                                    mInterstitialAd = null;
                                }
                            });
                            if (mInterstitialAd != null) {
                                mInterstitialAd.show(activity);
                            }
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error
                            Log.d(TAG, loadAdError.toString());
                            mInterstitialAd = null;
                        }
                    });
        }
    }

    private static void onAdClosed(FragmentActivity activity, Bundle bundle) {

    }
}
