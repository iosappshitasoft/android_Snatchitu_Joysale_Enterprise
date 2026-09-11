package com.ni.addons.productlimit;

import static com.ni.SnatchItUp.SplashActivity.adminPaymentType;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;



import com.google.gson.Gson;
import com.ni.external.ExpandableHeightListView;
import com.ni.helper.LocaleManager;
import com.ni.helper.NetworkReceiver;
import com.ni.SnatchItUp.BaseActivity;
import com.ni.SnatchItUp.DetailActivity;
import com.ni.SnatchItUp.JoysaleApplication;
import com.ni.SnatchItUp.R;
import com.ni.utils.ApiClient;
import com.ni.utils.ApiInterface;
import com.ni.utils.AppUtils;
import com.ni.utils.Constants;
import com.ni.utils.GetSet;

import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;


public class ProductLimitation extends BaseActivity implements View.OnClickListener {

    static final String TAG = ProductLimitation.class.getSimpleName();
    TextView title, promote;
    ImageView backBtn;

    ExpandableHeightListView promoteList;
    public Boolean isSelected = false;

    private Context mContext;

    String selectedId = "", selectedPrice, subscriptionId, subscriptionPrice;

    ApiInterface apiInterface;

    private static final int DROP_IN_REQUEST = 100;

    private NumberFormat numberFormat;

    String listcount, currencySymbol;

    private PromoteAdapters adapter;

    private String from, urgent = "", currencyCode = "", adPrice = "";

    private boolean isRTL = false;
    private ArrayList<SubscriptionsItem> promotionList = new ArrayList<SubscriptionsItem>();
    private ArrayList<SubscriptionsItem> promoteItems = new ArrayList<SubscriptionsItem>();
    private ActivityResultLauncher<Intent> stripeResult;
    private String nonce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_limitation);
        mContext = this;
        isRTL = LocaleManager.isRTL(this);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        title = (TextView) findViewById(R.id.title);
        backBtn = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);
        promoteList = (ExpandableHeightListView) findViewById(R.id.promoteList);
        promote = (TextView) findViewById(R.id.promote);

        promote.setOnClickListener(this);
        backBtn.setOnClickListener(this);

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        title.setText(getString(R.string.chooseplan));

        numberFormat = NumberFormat.getInstance(new Locale("en", "US"));
        numberFormat.setMaximumFractionDigits(2);
        loadPromotion();
        setAdapter();
//        initResultLauncher();

        promoteList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        promoteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedId = promotionList.get(position).getId();
                selectedPrice = promotionList.get(position).getPrice();
                subscriptionPrice = promotionList.get(position).getSubscriptionPrice();
                Log.d(TAG, "onItemClick: " + subscriptionPrice);
                adapter.notifyDataSetChanged();
            }
        });


    }

    private void initResultLauncher() {
        stripeResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                Log.d(TAG, "onActivityResult: " + result.getResultCode());
                Log.d(TAG, "onActivityResult: " + result.getData());
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.d(TAG, "onActivityResult: " + new Gson().toJson(result.getData()));
                }
            }
        });
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(ProductLimitation.this, isConnected);

    }


    private void loadPromotion() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(getApplicationContext()));
            Call<SubResponseData> call = apiInterface.getSubscription(map);
            call.enqueue(new Callback<SubResponseData>() {
                @Override
                public void onResponse(Call<SubResponseData> call, retrofit2.Response<SubResponseData> response) {

                    if (response.isSuccessful() && response.body().getStatus().equalsIgnoreCase("true")) {
                        promotionList.clear();
                        Result result = response.body().getResult();

                        currencySymbol = result.getCurrencySymbol().trim();
                        currencyCode = result.getCurrencyCode().trim();
                        promoteItems.addAll(result.getSubscriptions());
                        setData(currencyCode, currencySymbol, urgent, promoteItems);

                    }
                }

                @Override
                public void onFailure(Call<SubResponseData> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "resultCode=" + resultCode);
        if (requestCode == DROP_IN_REQUEST) {
            if (resultCode == RESULT_OK) {
                /*DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                String paymentMethodNonce = result.getPaymentMethodNonce().getNonce();
                Log.v(TAG, "paymentMethodNonce: " + paymentMethodNonce);
                payForPromotion(paymentMethodNonce);*/

            } else if (resultCode != RESULT_CANCELED) {
                dismissProgress();
                JoysaleApplication.dialog(ProductLimitation.this, getString(R.string.alert), getString(R.string.payment_error));
            } else {
                dismissProgress();
                Toast.makeText(ProductLimitation.this, getString(R.string.payment_cancelled), Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Function for send paid promotion details to server
     **/
    private void payForPromotion(final String payNonce) {
        if (NetworkReceiver.isConnected()) {
            showProgress();
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.SUBCRIPTION_ID, selectedId);
            map.put(Constants.TAG_CURRENCY_CODE, currencyCode);
            map.put(Constants.TAG_PAY_NONCE, payNonce);
            map.put(Constants.TAG_PAYMENT_TYPE, adminPaymentType);

            Log.v(TAG, "payForPromotion: " + new JSONObject(map));

            Call<HashMap<String, String>> call = apiInterface.subscriptionPayment(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    dismissProgress();
                    if (response.isSuccessful()) {
                        if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase("true")) {

                            //    Toast.makeText(ProductLimitation.this,"sucess", Toast.LENGTH_SHORT).show();
                            DetailActivity.fromEdit = true;

                            runOnUiThread(new Runnable() {

                                @SuppressWarnings("unchecked")
                                @Override
                                public void run() {
                                    showDialog(ProductLimitation.this, getString(R.string.success), getString(R.string.your_Subscription_was_activated_successfully));

                                }
                            });
                        } else if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase("false")) {
                            showDialog(ProductLimitation.this, getString(R.string.failed), getString(R.string.your_Subscription_was_activated_not_successfully));


                        } else {
                            runOnUiThread(new Runnable() {

                                @SuppressWarnings("unchecked")
                                @Override
                                public void run() {
                                    JoysaleApplication.dialog(ProductLimitation.this, getString(R.string.alert), getString(R.string.somethingwrong));

                                }
                            });
                        }
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    dismissProgress();
                }
            })
            ;
        } else {
            dismissProgress();
        }
    }


    private void showDialog(final Context context, String title, String message) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((Activity) context).finish();
                alertDialog.dismiss();
                Intent in = new Intent(ProductLimitation.this, SubscriptionViewActivity.class);
                in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                in.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                in.putExtra("From", "Payment");
                startActivity(in);

            }
        });

        alertDialog.show();
    }


    public void setData(String currencyCode, String currencySymbol, String adPrice, ArrayList<SubscriptionsItem> promoteItems) {
        this.currencyCode = currencyCode;
        this.currencySymbol = currencySymbol;
        this.adPrice = adPrice;
        promotionList.clear();
        promotionList.addAll(promoteItems);
        adapter.notifyDataSetChanged();
    }


    private void setAdapter() {
        if (adapter == null) {
            adapter = new PromoteAdapters(this, promotionList);
            promoteList.setAdapter(adapter);
            promoteList.setExpanded(true);
        } else {
            adapter.notifyDataSetChanged();
        }
    }







    /**
     * Adapter for Ad Promote
     **/

    public class PromoteAdapters extends BaseAdapter {
        ArrayList<SubscriptionsItem> data;
        ViewHolder holder = null;
        Context mContext;

        PromoteAdapters(Context context, ArrayList<SubscriptionsItem> Items) {
            mContext = context;
            data = Items;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.create_promote_post,
                        parent, false);
                holder = new ViewHolder();

                holder.promotionName = (TextView) convertView.findViewById(R.id.promotionName);
                holder.promotionPrice = (TextView) convertView.findViewById(R.id.promotionPrice);
                holder.promotionDays = (TextView) convertView.findViewById(R.id.promotionDays);
                holder.viewLine = (View) convertView.findViewById(R.id.view);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            try {
                if (selectedId.equals(data.get(position).getId())) {
                    isSelected = true;
                    holder.viewLine.setVisibility(View.VISIBLE);
                    holder.promotionPrice.setTextColor(getResources().getColor(R.color.primary));
                } else {
                    holder.viewLine.setVisibility(View.INVISIBLE);
                    holder.promotionPrice.setTextColor(getResources().getColor(R.color.primaryText));
                }

                holder.promotionName.setText(data.get(position).getName());
                holder.promotionDays.setText(numberFormat.format(Integer.parseInt(data.get(position).getListcount())) + " " + getString(R.string.postSub));
                holder.promotionPrice.setText(data.get(position).getPrice());
                // subscriptionId = numberFormat.format(Double.parseDouble(promotionList.get(position).getId()));

                subscriptionId = data.get(position).getId();

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }

        class ViewHolder {
            TextView promotionName, promotionPrice, promotionDays;
            View viewLine;
        }
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.promote:
                JoysaleApplication.preventMultipleClick(promote);
                if (isSelected) {
                        payStripe();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.select_plan), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.backbtn:
                finish();
                break;
        }


    }

    private void payStripe() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
        map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
        map.put(Constants.TAG_USERID, GetSet.getUserId());
        map.put(Constants.SUBCRIPTION_ID, selectedId);
        map.put(Constants.TAG_CURRENCY_CODE, currencyCode);
        Log.d(TAG, "payStripe: "+map);
        Intent intent = new Intent(ProductLimitation.this, UpdateCardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(Constants.TAG_REQUEST_DATA, map);
        String[] number = selectedPrice.split(" ");
        intent.putExtra(Constants.TAG_PRICE, subscriptionPrice);
        intent.putExtra(Constants.TAG_FROM, Constants.TAG_PRODUCT_SUBSCRIPTION);
        startActivity(intent);
    }

}
