package com.ni.SnatchItUp;

import static com.ni.SnatchItUp.FragmentMainActivity.categoryAry;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ni.addons.BannerAdUtils;
import com.ni.addons.InterstitialAdUtils;
import com.ni.addons.LanguageData;
import com.ni.addons.chattranslate.GetLanguageWorker;
import com.ni.helper.LocaleManager;
import com.ni.helper.NetworkReceiver;
import com.ni.helper.SharedPrefManager;
import com.ni.model.AdminDataResponse;
import com.ni.utils.ApiClient;
import com.ni.utils.ApiInterface;
import com.ni.utils.Constants;
import com.ni.utils.GetSet;
import com.wang.avi.AVLoadingIndicatorView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by hitasoft on 21/7/16.
 * <p>
 * This class is for Select a App Language.
 */

public class LanguageActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = LanguageActivity.class.getSimpleName();
    /**
     * Declare Layout Elements
     **/
    RecyclerView listView;
    TextView categoryName, title;
    ImageView backbtn;
    ProgressDialog dialog;
    LinearLayout nullLay;

    private String from;
    List<String> langAry = new ArrayList<>();
    List<LanguageData> languageList = new ArrayList<>();
    SharedPrefManager sharedPreferences;
    AVLoadingIndicatorView indicatorView;

    /**
     * Declare Variables
     **/
    String[] languages, langCode;
    String selectedLang = Constants.LANGUAGE;
    LanguageAdapter languageAdapter;
    ApiInterface apiInterface;
    private String deviceName = null, deviceModel = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_condition);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        backbtn = (ImageView) findViewById(R.id.backbtn);
        listView = findViewById(R.id.listView);
        categoryName = (TextView) findViewById(R.id.categoryName);
        title = (TextView) findViewById(R.id.title);
        indicatorView = findViewById(R.id.progress);
        nullLay = findViewById(R.id.nullLay);

        if (getIntent().hasExtra(Constants.TAG_FROM)) {
            from = getIntent().getStringExtra(Constants.TAG_FROM);
        }
        sharedPreferences = SharedPrefManager.getInstance(this);
        SharedPrefManager.initPref(this);
        title.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);
        categoryName.setVisibility(View.GONE);

        title.setText(getString(R.string.language));

        Constants.pref = getApplicationContext().getSharedPreferences("JoysalePref", MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();

        backbtn.setOnClickListener(this);

        selectedLang = Constants.pref.getString(Constants.PREF_LANGUAGE, Constants.LANGUAGE);
        deviceName = JoysaleApplication.getDeviceName();
        deviceModel = JoysaleApplication.getDeviceModel();
        dialog = new ProgressDialog(LanguageActivity.this, R.style.AppCompatAlertDialogStyle);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = new ProgressBar(this).getIndeterminateDrawable().mutate();
            drawable.setColorFilter(ContextCompat.getColor(this, R.color.progressColor),
                    PorterDuff.Mode.SRC_IN);
            dialog.setIndeterminateDrawable(drawable);
        }

        languageAdapter = new LanguageAdapter(LanguageActivity.this, languageList);
        listView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        listView.setAdapter(languageAdapter);

        if (from != null && from.equals("Chat") && Constants.ADDON_TRANSLATE_ENABLED) {
            indicatorView.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            LanguageData data = new LanguageData();
            String lang = getString(R.string.none);
            String lan = "";
            data.setLanguage(lang);
            data.setLanguageCode(lan);
            languageList.add(data);
            startTranslateLanguageWork();
        } else if (from != null && from.equalsIgnoreCase("EditProfile")) {
            languages = getResources().getStringArray(R.array.languages);
            langCode = getResources().getStringArray(R.array.languageCode);
            for (int i = 0; i < languages.length; i++) {
                LanguageData data = new LanguageData();
                data.setLanguage(languages[i]);
                data.setLanguageCode(langCode[i]);
                languageList.add(data);
            }
            languageAdapter.notifyDataSetChanged();
        }
    }

    //Addon Chat Translate
    private void startTranslateLanguageWork() {
        OneTimeWorkRequest locationWorkRequest = new OneTimeWorkRequest.Builder(GetLanguageWorker.class)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .build();
        WorkManager workManager = WorkManager.getInstance(this);
        workManager.enqueue(locationWorkRequest);
        workManager.getWorkInfoByIdLiveData(locationWorkRequest.getId()).observe(this, workInfo -> {
            if (workInfo.getState().isFinished()) {
                if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                    String languageJson = workInfo.getOutputData().getString(Constants.TAG_DATA);
                    Log.d(TAG, "startTranslateLanguageWork: " + languageJson);
                    Type userListType = new TypeToken<ArrayList<LanguageData>>() {
                    }.getType();
                    languageList.addAll(new Gson().fromJson(languageJson, userListType));
                    if (languageList.size() > 0) {
                        indicatorView.setVisibility(View.GONE);
                        listView.setVisibility(View.VISIBLE);
                        languageAdapter.notifyDataSetChanged();
                    } else {
                        indicatorView.setVisibility(View.GONE);
                        listView.setVisibility(View.GONE);
                        nullLay.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    /**
     * function for change the selected language
     **/

    public void setLocale(String langCode) {
        LocaleManager.setNewLocale(this, langCode, true);
        dialog.show();
        getAdminData(langCode);
    }


    /**
     * For register push notification
     **/

    private void addDeviceId() {
        if (NetworkReceiver.isConnected()) {
            final String token = SharedPrefManager.getInstance(this).getDeviceToken();
            Map<String, String> map = new HashMap<String, String>();
            String[] languages = getResources().getStringArray(R.array.languages);
            String[] langCode = getResources().getStringArray(R.array.languageCode);
            String selectedLang = Constants.pref.getString(Constants.PREF_LANGUAGE, Constants.LANGUAGE);
            int index = Arrays.asList(languages).indexOf(selectedLang);
            final String languageCode = Arrays.asList(langCode).get(index);
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_DEVICE_ID, Constants.ANDROID_ID);
            map.put(Constants.TAG_FCM_USERID, GetSet.getUserId());
            map.put(Constants.TAG_DEVICE_TYPE, "1");
            map.put(Constants.TAG_DEVICE_MODE, "1");
            map.put(Constants.TAG_LANG_TYPE, languageCode);
            map.put(Constants.TAG_DEVICE_TOKEN, token);
            map.put(Constants.TAG_DEVICE_NAME, deviceName != null ? deviceName : "");
            map.put(Constants.TAG_DEVICE_MODEL, deviceModel != null ? deviceModel : "");
            map.put(Constants.TAG_DEVICE_OS, "" + Build.VERSION.SDK_INT);
            map.put(Constants.TAG_VOIP_TOKEN, "");

            Call<ResponseBody> call = apiInterface.addDeviceId(map);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    call.cancel();
                }
            });

        }
    }

    /**
     * Function for get the admin default exchangeData
     **/

    private void getAdminData(final String languageCode) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_LANG_TYPE, languageCode);
            Log.d(TAG, "getAdminData: " + map);
            Call<AdminDataResponse> call = apiInterface.getAdminData(map);
            call.enqueue(new Callback<AdminDataResponse>() {
                @Override
                public void onResponse(Call<AdminDataResponse> call, retrofit2.Response<AdminDataResponse> response) {
                    AdminDataResponse adminDataResponse = response.body();
                    try {
                        if (adminDataResponse.getStatus().equalsIgnoreCase("true")) {
                            AdminDataResponse.Result result = adminDataResponse.getResult();
                            FragmentMainActivity.homeBanner = result.getBanner();
                            JoysaleApplication.adminEditor.putString(Constants.PREF_DISTANCE_TYPE, result.getDistanceType());
                            JoysaleApplication.adminEditor.commit();

                            Constants.BUYNOW = result.getBuyNow().equalsIgnoreCase("enable");
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_BUYNOW, Constants.BUYNOW);
                            JoysaleApplication.adminEditor.commit();

                            Constants.EXCHANGE = result.getExchange().equalsIgnoreCase("enable");
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_EXCHANGE, Constants.EXCHANGE);
                            JoysaleApplication.adminEditor.commit();

                            if (result.getmSocketUrl() != null)
                                Constants.SOCKET_URL = result.getmSocketUrl();
                            if (result.getGoogleAdsType() != null)
                                Constants.TAG_GOOGLEADS_STATUS = result.getGoogleAdsType().trim();

                            if (result.getApprtcUrl() != null) {
                                Constants.APPRTC_URL = result.getApprtcUrl();
                            }

                            InterstitialAdUtils.applyAdminInterstitialId(result.getInterstitial_ad_key());

                            BannerAdUtils.applyAdminBannerId(result.getGoogle_ads_android());

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
                                FragmentMainActivity.bannerAry.addAll(result.getBannerData());
                            }

                           /* if (categoryAry != null) {
                                categoryAry.clear();
                                categoryAry.addAll(result.getCategory());
                            }*/

                            if (categoryAry != null) {
                                categoryAry.clear();
                                categoryAry.addAll(result.getCategory());
                              //  categoryAry.remove(0);
                            }

                            if (ChatActivity.templatMsgAry != null) {
                                ChatActivity.templatMsgAry.clear();
                                ChatActivity.templatMsgAry.addAll(result.getChatTemplate());
                            }
                            if (result.getPriceRange() != null) {
                                SplashActivity.beforeDecimal = result.getPriceRange().getBeforeDecimal();
                                SplashActivity.afterDecimal = result.getPriceRange().getAfterDecimal();
                            }

                            if (result.getStripePublicKey() != null && !TextUtils.isEmpty(result.getStripePublicKey())) {
                                SplashActivity.stripePublicKey = result.getStripePublicKey();
                            }

                            Constants.EMAILVERIFICATION = result.getEmailVerification().equalsIgnoreCase("enable");
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_EMAILVERIFICATION, Constants.EMAILVERIFICATION);
                            JoysaleApplication.adminEditor.commit();

                            dialog.dismiss();
                            SearchAdvance.applyFilter = false;
                            SearchAdvance.sortBy = "1";
                            FragmentMainActivity.homeItemList.clear();
                            Intent refresh = new Intent(LanguageActivity.this, FragmentMainActivity.class);
                            refresh.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(refresh);

                        }
                    } catch (Exception e) {
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

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(LanguageActivity.this, isConnected);
    }

    /**
     * Function for OnClick Event
     **/
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                finish();
                break;
        }
    }

    /**
     * Adapter for set the language to list
     **/

    public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.ViewHolder> {

        String[] lang;
        ViewHolder holder = null;
        private Context mContext;
        List<LanguageData> languageList;


        public LanguageAdapter(LanguageActivity ctx, List<LanguageData> languageList) {
            mContext = ctx;
            this.languageList = languageList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.item_row_selection, parent, false);//layout
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            int holderPosition = holder.getAbsoluteAdapterPosition();
            try {
                if (LocaleManager.isRTL(mContext)) {
                    holder.name.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
                } else {
                    holder.name.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                }
                holder.name.setText(languageList.get(holderPosition).getLanguage());
                if (from.equalsIgnoreCase("Chat")) {
                    if (SharedPrefManager.getString(SharedPrefManager.CHAT_LANGUAGE, "").equalsIgnoreCase(languageList.get(holderPosition).getLanguageCode())) {
                        holder.tick.setVisibility(View.VISIBLE);
                        holder.name.setTextColor(getResources().getColor(R.color.primary));
                    } else {
                        holder.tick.setVisibility(View.INVISIBLE);
                        holder.name.setTextColor(getResources().getColor(R.color.primaryText));
                    }
                } else {
                    if (languageList.get(holderPosition).getLanguage().equals(selectedLang)) {
                        holder.tick.setVisibility(View.VISIBLE);
                        holder.name.setTextColor(getResources().getColor(R.color.primary));
                    } else {
                        holder.tick.setVisibility(View.INVISIBLE);
                        holder.name.setTextColor(getResources().getColor(R.color.primaryText));
                    }
                }

                holder.mainLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        holder.tick.setVisibility(View.VISIBLE);
                        holder.name.setTextColor(getResources().getColor(R.color.colorPrimary));
                        if (from.equalsIgnoreCase("Chat")) {
                            setTranslateLanguage(languageList.get(holderPosition).getLanguageCode());
                            SharedPrefManager.putString(SharedPrefManager.DEFAULT_CHAT_LANGUAGE, languageList.get(holderPosition).getLanguageCode());
                        } else {
                            selectedLang = languageList.get(holderPosition).getLanguage();
                            languageAdapter.notifyDataSetChanged();
                            Constants.editor.putString(Constants.PREF_LANGUAGE, selectedLang);
                            Constants.editor.commit();
                            addDeviceId();
                            setLocale(languageList.get(holderPosition).getLanguageCode());
                        }
                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return languageList.size();

        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView tick;
            TextView name;
            LinearLayout mainLay;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.name);
                tick = (ImageView) itemView.findViewById(R.id.tick);
                mainLay = itemView.findViewById(R.id.mainLay);
            }
        }
    }

    private void setTranslateLanguage(String languageCode) {
        languageAdapter.notifyDataSetChanged();
        sharedPreferences.putString(SharedPrefManager.CHAT_LANGUAGE, languageCode);
        setResult(Constants.TAG_DEFAULT_REQUEST_CODE);
        finish();
    }

}
