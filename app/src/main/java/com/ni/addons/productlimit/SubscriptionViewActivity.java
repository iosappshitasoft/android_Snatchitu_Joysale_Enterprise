package com.ni.addons.productlimit;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.ni.helper.LocaleManager;
import com.ni.helper.NetworkReceiver;
import com.ni.SnatchItUp.BaseActivity;
import com.ni.SnatchItUp.FragmentMainActivity;
import com.ni.SnatchItUp.JoysaleApplication;
import com.ni.SnatchItUp.R;
import com.ni.SnatchItUp.UrgentPromotion;
import com.ni.utils.ApiClient;
import com.ni.utils.ApiInterface;
import com.ni.utils.Constants;
import com.ni.utils.GetSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class SubscriptionViewActivity extends BaseActivity implements View.OnClickListener {


    static final String TAG = "UrgentPromotion";
    ImageView backbtn;
    TextView title, plantxt, textViewCuurrentSubscription, textViewAvailablePost, statustxt, promote, subscripeView;

    ProgressDialog progressDialog;

    RelativeLayout viewpage;

    LinearLayout nullLay;

    String paidamount, status, totalpost, transtxt, from, currencySymbol = "", currencyCode = "", clientToken = "";
    ApiInterface apiInterface;
    SubscriptionResponse data = new SubscriptionResponse();
    UrgentPromotion.UrgentAdapter adapter;
    ArrayList<SubscriptionResponse> urgentAry = new ArrayList<>();
    private boolean isRTL = false;
    private ArrayList<SubscriptionResponse> promoteItems = new ArrayList<SubscriptionResponse>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_view);
        isRTL = LocaleManager.isRTL(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        title = (TextView) findViewById(R.id.title);
        plantxt = (TextView) findViewById(R.id.plantxt);
        statustxt = (TextView) findViewById(R.id.statustxt);
        backbtn = (ImageView) findViewById(R.id.backbtn);
        textViewAvailablePost = findViewById(R.id.availablePostText);
        textViewCuurrentSubscription = findViewById(R.id.currentSubscriptionText);
        promote = (TextView) findViewById(R.id.promote);
        subscripeView = (TextView) findViewById(R.id.subscripeView);
        backbtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        viewpage = (RelativeLayout) findViewById(R.id.viewpage);

        nullLay = (LinearLayout) findViewById(R.id.nullLay);

        progressDialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
        if (getIntent().getExtras() != null) {
            from = getIntent().getStringExtra("From");
        }

        progressDialog.setMessage(getString(R.string.pleasewait));
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = new ProgressBar(this).getIndeterminateDrawable().mutate();
            drawable.setColorFilter(ContextCompat.getColor(this, R.color.progressColor),
                    PorterDuff.Mode.SRC_IN);
            progressDialog.setIndeterminateDrawable(drawable);
        }

        subscripeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SubscriptionViewActivity.this, ProductLimitation.class);
                startActivity(intent);
            }
        });

        promote.setOnClickListener(this);
        backbtn.setOnClickListener(this);
        getUrgentList();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(SubscriptionViewActivity.this, isConnected);

    }


    private void getUrgentList() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());

            progressDialog.show();

            Log.v("TAG", "getsubcriprion" + map);
            Call<SubscriptionResponse> call = apiInterface.getMySubscription(map);
            call.enqueue(new Callback<SubscriptionResponse>() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onResponse(Call<SubscriptionResponse> call, retrofit2.Response<SubscriptionResponse> response) {

                    if (progressDialog.isShowing())
                        progressDialog.dismiss();

                    if (response.isSuccessful() && response.body().getStatus().equalsIgnoreCase("true")) {
                        if (response.body().getResult().get(0).getSubscriptionPlan().equals("") || response.body().getResult().get(0).getAvailablePosts() == 0) {
                            subscripeView.setVisibility(View.VISIBLE);
                        } else {
                            subscripeView.setVisibility(View.GONE);
                        }
                        viewpage.setVisibility(View.VISIBLE);

                        if (response.body().getResult().get(0).getAvailablePosts() == 0) {
                            statustxt.setText(getString(R.string.expired));
                            statustxt.setTextColor(getColor(R.color.red_color));
                        } else {
                            if (response.body().getResult().get(0).getCurrentStatus().equalsIgnoreCase("live")) {
                                statustxt.setText(getString(R.string.live));
                            } else if (response.body().getResult().get(0).getCurrentStatus().equalsIgnoreCase("pending")) {
                                statustxt.setText(getString(R.string.pending));
                            }
                        }

                        if (response.body().getResult().get(0).getCurrentStatus().equalsIgnoreCase("Expired")) {
                            statustxt.setTextColor(getColor(R.color.red_color));
                        }
                        textViewAvailablePost.setText(String.valueOf(response.body().getResult().get(0).getAvailablePosts()));
                        if (response.body().getResult().get(0).getSubscriptionPlan().equalsIgnoreCase("")) {
                            if (response.body().getResult().get(0).getAvailablePosts() == 0) {
                                textViewCuurrentSubscription.setText(R.string.noplans_sub);
                                statustxt.setText(getString(R.string.pending));
                            } else {
                                textViewCuurrentSubscription.setText(R.string.freepost_sub);
                            }


                        } else
                            textViewCuurrentSubscription.setText(String.valueOf(response.body().getResult().get(0).getSubscriptionPlan()));

                    } else {
                        viewpage.setVisibility(View.GONE);
                        nullLay.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onFailure(Call<SubscriptionResponse> call, Throwable t) {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    call.cancel();
                    Log.v("Response", t.toString());


                }
            });
        } else {

            Toast.makeText(SubscriptionViewActivity.this, "Please Connect Internet", Toast.LENGTH_SHORT).show();

        }
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.promote:
                Intent intent = new Intent(SubscriptionViewActivity.this, ProductLimitation.class);
                startActivity(intent);
                break;
            case R.id.backbtn:
                if (from.equalsIgnoreCase("payment")) {
                    Intent inten = new Intent(SubscriptionViewActivity.this, FragmentMainActivity.class);
                    startActivity(inten);
                } else {
                    finish();
                }
                break;

        }

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (from != null) {
            if (from.equalsIgnoreCase("payment")) {
                Intent intent = new Intent(SubscriptionViewActivity.this, FragmentMainActivity.class);
                startActivity(intent);
            } else {
                finish();
            }
        } else {
            finish();
        }
    }

}
