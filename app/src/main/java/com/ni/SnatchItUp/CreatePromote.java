package com.ni.SnatchItUp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.ni.helper.NetworkReceiver;
import com.ni.model.PromotionResponse;
import com.ni.utils.ApiClient;
import com.ni.utils.ApiInterface;
import com.ni.utils.AppUtils;
import com.ni.utils.Constants;
import com.ni.utils.GetSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by hitasoft  on 24/6/16.
 * <p>
 * This class is for Create a Promotion
 */

public class CreatePromote extends BaseActivity implements View.OnClickListener {

    /**
     * Declare Layout Elements
     **/
    public static TabLayout slidingTabLayout;
    ViewPager mViewPager;
    TextView title;
    ImageView backBtn;

    /**
     * Declare Variables
     **/
    private static final String TAG = CreatePromote.class.getSimpleName();
    private ArrayList<PromotionResponse.OtherPromotion> promoteItems = new ArrayList<>();
    static String itemId = "";
    ApiInterface apiInterface;
    private String from;
    PromoteAd promoteAd;
    PromoteUrgent promoteUrgent;
    String fragmentString;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.promote);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        backBtn = (ImageView) findViewById(R.id.backbtn);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        slidingTabLayout = (TabLayout) findViewById(R.id.slideTab);
        title = (TextView) findViewById(R.id.title);

        itemId = getIntent().getExtras().getString("itemId");
        if (getIntent().hasExtra(Constants.TAG_FROM)) {
            from = getIntent().getStringExtra(Constants.TAG_FROM);
        }
        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        title.setText(getString(R.string.create_promotion));
        fragmentString = Constants.TAG_PROMOTEURGENT;
        progressDialog.show();

        loadPromotion();

        backBtn.setOnClickListener(this);
        slidingTabLayout.addTab(slidingTabLayout.newTab().setText(R.string.urgent));
        slidingTabLayout.addTab(slidingTabLayout.newTab().setText(R.string.ad));
        slidingTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        slidingTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    if (progressDialog != null && !progressDialog.isShowing()) {
                        progressDialog.show();
                    }
                    fragmentString = Constants.TAG_PROMOTEURGENT;
                    loadPromotion();
                } else if (tab.getPosition() == 1) {
                    if (progressDialog != null && !progressDialog.isShowing()) {
                        progressDialog.show();
                    }
                    fragmentString = Constants.TAG_PROMOTEAD;
                    if (promoteItems != null) {
                        promoteItems.clear();
                    }
                    loadPromotion();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

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
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(CreatePromote.this, isConnected);
    }



    /**
     * Function for get promotion exchangeData form admin
     **/

    private void loadPromotion() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(getApplicationContext()));

            Call<PromotionResponse> call = apiInterface.getPromotion(map);
            call.enqueue(new Callback<PromotionResponse>() {
                @SuppressLint("ResourceType")
                @Override
                public void onResponse(Call<PromotionResponse> call, retrofit2.Response<PromotionResponse> response) {
                    if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                    }
                    if (response.isSuccessful() && response.body().getStatus().equalsIgnoreCase("true")) {
                        PromotionResponse.Result result = response.body().getResult();
                            Bundle bundle = new Bundle();
                            bundle.putString(Constants.TAG_CURRENCY_CODE, result.getCurrencyCode().trim());
                            bundle.putString(Constants.TAG_CURRENCY_SYMBOL, result.getCurrencySymbol().trim());
                            bundle.putString(Constants.TAG_TYPE, result.getUrgent());
                            bundle.putString(Constants.TAG_PRICE, result.getUrgent());
                            bundle.putString(Constants.TAG_URGENTPRICE, result.getFormattedUrgentPrice());
                            bundle.putString("urgentduration", result.getUrgentDays());
                            Log.d(TAG, "onResponse: " + result.getFormattedUrgentPrice());
                            Log.d(TAG, "onResponse: " + result.getUrgent());
                            bundle.putString(Constants.TAG_CURRENCY_POSITION, result.getCurrencyPosition().trim());
                            bundle.putString(Constants.TAG_CURRENCY_MODE, result.getCurrencyMode().trim());
                            promoteItems.addAll(result.getOtherPromotions());
                            bundle.putSerializable(Constants.TAG_ITEMS, promoteItems);
                            if (fragmentString != null && fragmentString.equalsIgnoreCase(Constants.TAG_PROMOTEURGENT)) {
                                promoteUrgent = (PromoteUrgent) PromoteUrgent.newInstance(bundle);
                                changeFragment(promoteUrgent, PromoteUrgent.class.getSimpleName());

                            } else if (fragmentString != null && fragmentString.equalsIgnoreCase(Constants.TAG_PROMOTEAD)) {
                                Bundle adBundle = bundle;
                                adBundle.putSerializable(Constants.TAG_ITEMS, promoteItems);
                                promoteAd = (PromoteAd) PromoteAd.newInstance(bundle);
                                changeFragment(promoteAd, PromoteAd.class.getSimpleName());
                            }
                            Log.d(TAG, "onResponse: " + new Gson().toJson(promoteItems));

                        }
                    }
                //}

                @Override
                public void onFailure(Call<PromotionResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }


    /**
     * On Click Event
     **/
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.backbtn) {
            onBackPressed();
        }
    }

    @Override
    public void onBackPressed() {
        if (("" + from).equals(Constants.TAG_DETAIL) || (("" + from).equals(Constants.TAG_ADD))) {
            finish();
        } else {
            finish();
            Intent in = new Intent(this, ProfileActivity.class);
            in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            in.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
            startActivity(in);

        }
    }

    public void changeFragment(Fragment fragment, String tagFragmentName) {
        FragmentManager mFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        Fragment currentFragment = mFragmentManager.getPrimaryNavigationFragment();
        if (currentFragment != null) {
            fragmentTransaction.hide(currentFragment);
        }

        Fragment fragmentTemp = mFragmentManager.findFragmentByTag(tagFragmentName);
        if (fragmentTemp == null) {
            fragmentTemp = fragment;
            fragmentTransaction.add(R.id.fragment_placeHolder, fragmentTemp, tagFragmentName);
        } else {
            fragmentTransaction.show(fragmentTemp);
        }

        fragmentTransaction.setPrimaryNavigationFragment(fragmentTemp);
        fragmentTransaction.setReorderingAllowed(true);
        fragmentTransaction.commitNowAllowingStateLoss();
    }


}
