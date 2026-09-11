package com.ni.SnatchItUp;

import static com.ni.SnatchItUp.SplashActivity.adminPaymentType;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.ni.addons.productlimit.SubscriptionViewActivity;
import com.ni.helper.LocaleManager;
import com.ni.helper.NetworkReceiver;
import com.ni.model.AdminDataResponse;
import com.ni.utils.ApiClient;
import com.ni.utils.ApiInterface;
import com.ni.utils.AppUtils;
import com.ni.utils.Constants;
import com.ni.utils.GetSet;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateCardActivity extends AppCompatActivity {
    private static final String TAG = UpdateCardActivity.class.getSimpleName();
    private PaymentSheet paymentSheet;
    private String paymentIntentClientSecret;
    private String customerId;
    private String ephemeralKeySecret;
    private String paymentNonce;
    HashMap<String, String> requestMap = new HashMap<>();
    private String from, stripePublishableKey, transaction_id, promotionPrice;
    private Button buyButton;
    ApiInterface apiInterface;
    ProgressDialog progressDialog;
    String languageCode;
    private View paymentLoadingLay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.google_pay_activity);
        buyButton = findViewById(R.id.google_pay_button);
        paymentLoadingLay = findViewById(R.id.paymentLoadingLay);
        buyButton.setEnabled(false);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        languageCode = LocaleManager.getLanguageCode(this);
        from = getIntent().getStringExtra(Constants.TAG_FROM);
        stripePublishableKey = SplashActivity.stripePublicKey;
        Object requestData = getIntent().getExtras() != null
                ? getIntent().getExtras().get(Constants.TAG_REQUEST_DATA)
                : null;
        if (requestData instanceof HashMap) {
            //noinspection unchecked
            requestMap = (HashMap<String, String>) requestData;
        } else {
            requestMap = new HashMap<>();
        }

        if (from.equalsIgnoreCase(Constants.TAG_AD) || from.equalsIgnoreCase(Constants.TAG_URGENT)
                || from.equalsIgnoreCase(Constants.TAG_PRODUCT_SUBSCRIPTION)) {
            promotionPrice = getIntent().getStringExtra(Constants.TAG_PRICE);
        }
        Log.e("stripekey", "" + stripePublishableKey);
        if (stripePublishableKey != null && !stripePublishableKey.isEmpty()) {
            PaymentConfiguration.init(this, stripePublishableKey);
        } else {
            getAdminData();
        }

        paymentSheet = new PaymentSheet(this, result -> {
            onPaymentSheetResult(result);
        });
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presentPaymentSheet();
            }
        });
        initProgress();
        fetchInitData();
    }

    private void getAdminData() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_LANG_TYPE, languageCode);
            Log.e(TAG,"Admin data : "+map);
            Call<AdminDataResponse> call = apiInterface.getAdminData(map);
            call.enqueue(new Callback<AdminDataResponse>() {
                @Override
                public void onResponse(Call<AdminDataResponse> call, retrofit2.Response<AdminDataResponse> response) {
                    if (response.isSuccessful()) {
                        Log.e(TAG,"Admin res : " + new Gson().toJson(response.body()));
                        AdminDataResponse adminDataResponse = response.body();
                        if (adminDataResponse.getStatus().equalsIgnoreCase("true")) {
                            AdminDataResponse.Result result = adminDataResponse.getResult();
                            try {
                                    if (result.getStripePublicKey() != null && !TextUtils.isEmpty(result.getStripePublicKey())) {
                                        SplashActivity.stripePublicKey = result.getStripePublicKey();
                                        PaymentConfiguration.init(UpdateCardActivity.this, result.getStripePublicKey());
                                    }
                            } catch (NullPointerException e) {
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
    public void initProgress() {
        progressDialog = new ProgressDialog(UpdateCardActivity.this, R.style.AppCompatAlertDialogStyle);
        progressDialog.setMessage(getString(R.string.pleasewait));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = new ProgressBar(this).getIndeterminateDrawable().mutate();
            drawable.setColorFilter(ContextCompat.getColor(this, R.color.progressColor),
                    PorterDuff.Mode.SRC_IN);
            progressDialog.setIndeterminateDrawable(drawable);
        }
    }

    private void fetchInitData() {
        if (NetworkReceiver.isConnected()) {
            progressDialog.show();
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            String amount = "";
            if (from.equalsIgnoreCase(Constants.TAG_BUYNOW))
                amount = requestMap.get(Constants.ORDER_TOTAL);
            else if (from.equalsIgnoreCase(Constants.TAG_BANNER))
                amount = requestMap.get(Constants.TAG_PRICE);
            else if (from.equalsIgnoreCase(Constants.TAG_AD) || from.equalsIgnoreCase(Constants.TAG_URGENT)
                    || from.equalsIgnoreCase(Constants.TAG_PRODUCT_SUBSCRIPTION)) {
                amount = promotionPrice;
            }
            if (TextUtils.isEmpty(amount)) {
                progressDialog.dismiss();
                Toast.makeText(this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            map.put("amount", amount.replaceAll(",", "").trim());
            String currency = requestMap.get(Constants.TAG_CURRENCY_CODE);
            if (!TextUtils.isEmpty(currency) && currency.contains("-")) {
                currency = currency.split("-")[1];
            }
            if (TextUtils.isEmpty(currency)) {
                currency = "INR";
            }
            map.put("currency", currency);

            Log.i(TAG, "paymentsheet: " + new Gson().toJson(map));

            Call<HashMap<String, String>> call = apiInterface.balanceSheet(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    if (response.isSuccessful()) {
                        progressDialog.dismiss();
                        Log.e("response", "" + response.body().toString());
                        final String status = response.body().get(Constants.TAG_STATUS);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (status.equalsIgnoreCase("true")) {
                                    try {
                                        JSONObject responseJson = new JSONObject(response.body());
                                        paymentIntentClientSecret = responseJson.optString("paymentIntent");
                                        customerId = responseJson.optString("customer");
                                        ephemeralKeySecret = responseJson.optString("ephemeralKey");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Toast.makeText(UpdateCardActivity.this,
                                                getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    if (paymentIntentClientSecret != null) {
                                        if (!paymentIntentClientSecret.equalsIgnoreCase("")) {
                                            String[] arr = paymentIntentClientSecret.split("_secret");
                                            paymentNonce = arr[0];
                                        }
                                    }

                                    buyButton.setEnabled(true);
                                    //   Log.e("valuee",paymentNonce+" paymentIntet "+paymentIntentClientSecret+" "+customerId+" "+ephemeralKeySecret);
                                    presentPaymentSheet();
                                } else if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase("false")) {
                                    showDialog(getString(R.string.failed), getString(R.string.your_Subscription_was_activated_not_successfully));
                                }
                            }
                        });
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    Log.e("UpdatedCardActivity", "payNowError: " + t.getMessage());
                    progressDialog.dismiss();
                    call.cancel();
                }
            });
        }
    }


    private void presentPaymentSheet() {

        paymentSheet.presentWithPaymentIntent(
                paymentIntentClientSecret,
                new PaymentSheet.Configuration(
                        "SnatchitUp, Inc.",
                        new PaymentSheet.CustomerConfiguration(
                                customerId,
                                ephemeralKeySecret
                        ),
                        new PaymentSheet.GooglePayConfiguration(
                                PaymentSheet.GooglePayConfiguration.Environment.Production,
                                "IN")
                )
        );

    }

    private void onPaymentSheetResult(
            final PaymentSheetResult paymentSheetResult
    ) {
        if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Toast.makeText(
                    this,
                    "Payment Canceled",
                    Toast.LENGTH_LONG
            ).show();
            showPaymentLoading(false);
            finish();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            Toast.makeText(
                    this,
                    "Payment Failed. See logcat for details.",
                    Toast.LENGTH_LONG
            ).show();
            showPaymentLoading(false);

            Log.e("App", "Got error: ", ((PaymentSheetResult.Failed) paymentSheetResult).getError());
            finish();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Toast.makeText(
                    this,
                    "Payment Complete",
                    Toast.LENGTH_SHORT
            ).show();

            showPaymentLoading(true);
            showProgress();

            switch (from) {
                case Constants.TAG_BUYNOW:
                    payNow(paymentNonce);
                    break;
                case Constants.TAG_AD:
                    payForPromotion(paymentNonce);
                    break;
                case Constants.TAG_URGENT:
                    payForPromotion(paymentNonce);
                    break;
                case Constants.TAG_BANNER:
                    addBanner(paymentNonce);
                    break;

                //Addon ProductLimitation
                case Constants.TAG_PRODUCT_SUBSCRIPTION:
                    payForSubscription(paymentNonce);
                    break;
            }
        }
    }

    private void showPaymentLoading(boolean show) {
        if (paymentLoadingLay != null) {
            paymentLoadingLay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void payNow(final String payNonce) {

        if (NetworkReceiver.isConnected()) {
            if (requestMap == null) {
                requestMap = new HashMap<>();
            }
            // Retrofit @FieldMap rejects null values
            HashMap<String, String> safeMap = new HashMap<>();
            for (Map.Entry<String, String> entry : requestMap.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    safeMap.put(entry.getKey(), entry.getValue());
                }
            }
            safeMap.put(Constants.TAG_NONCE, payNonce != null ? payNonce : "");
            safeMap.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(getApplicationContext()));
            requestMap = safeMap;

            Log.i(TAG, "payNow: " + new Gson().toJson(requestMap));
            Call<HashMap<String, String>> call = apiInterface.buyNowPayment(requestMap);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        final String status = response.body().get(Constants.TAG_STATUS);
                        final String message = response.body().get(Constants.TAG_MESSAGE);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (status != null && status.equalsIgnoreCase("true")) {
                                    dismissProgress();
                                    showPaymentLoading(false);
                                    setResult(RESULT_OK);
                                    finish();
                                } else {
                                    showPaymentLoading(false);
                                    dismissProgress();
                                    String errorMsg = !TextUtils.isEmpty(message)
                                            ? message
                                            : getString(R.string.somethingwrong);
                                    JoysaleApplication.dialog(UpdateCardActivity.this,
                                            getResources().getString(R.string.alert), errorMsg);
                                }
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showPaymentLoading(false);
                                dismissProgress();
                                JoysaleApplication.dialog(UpdateCardActivity.this,
                                        getResources().getString(R.string.alert),
                                        getString(R.string.somethingwrong));
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    showPaymentLoading(false);
                    dismissProgress();
                    Log.e(TAG, "payNowError: " + t.getMessage());
                    JoysaleApplication.dialog(UpdateCardActivity.this,
                            getResources().getString(R.string.alert),
                            getString(R.string.somethingwrong));
                    call.cancel();
                }
            });
        } else {
            showPaymentLoading(false);
            dismissProgress();
        }
    }


    private void payForPromotion(final String payNonce) {
        if (NetworkReceiver.isConnected()) {
            requestMap.put(Constants.TAG_PAY_NONCE, payNonce);
            requestMap.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(getApplicationContext()));

            Call<HashMap<String, String>> call = apiInterface.payForPromotion(requestMap);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    dismissProgress();
                    if (response.isSuccessful() && response.body().get(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                        DetailActivity.fromEdit = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showDialog(getString(R.string.success), getString(R.string.your_promotion_was_activated_successfully));
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                JoysaleApplication.dialog(UpdateCardActivity.this, getResources().getString(R.string.alert), getString(R.string.somethingwrong));
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {

                }
            });
        } else {
            dismissProgress();
        }
    }


    private void addBanner(String nonce) {
        requestMap.put(Constants.TAG_NONCE, nonce);
        Call<HashMap<String, String>> call = apiInterface.addBanner(requestMap);
        Log.i(TAG, "addBanner: " + new Gson().toJson(requestMap));
        call.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                dismissProgress();
                if (response.isSuccessful() && response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.banner_added_description), Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {

            }
        });

    }


//Addon ProductLimitation
    private void payForSubscription(final String payNounce) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            requestMap.put(Constants.TAG_PAY_NONCE, payNounce);
            requestMap.put(Constants.TAG_PAYMENT_TYPE,adminPaymentType);
            Log.v(TAG,"payforPromotionSubscription"+requestMap);

            Call<HashMap<String, String>> call = apiInterface.subscriptionPayment(requestMap);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    Log.v(TAG,"payforPromotionSubscription"+"state1");
                    progressDialog.dismiss();
                    if (response.isSuccessful()) {
                        if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                            Log.v(TAG,"payforPromotionSubscription"+"state2");

                            Toast.makeText(UpdateCardActivity.this,"sucess", Toast.LENGTH_SHORT).show();
                            DetailActivity.fromEdit = true;

                            runOnUiThread(new Runnable() {

                                @SuppressWarnings("unchecked")
                                @Override
                                public void run() {
                                    showDialog( getString(R.string.success), getString(R.string.your_Subscription_was_activated_successfully));

                                }
                            });
                        }else if(response.body().get(Constants.TAG_STATUS).equalsIgnoreCase("false")){
                            showDialog( getString(R.string.failed), getString(R.string.your_Subscription_was_activated_not_successfully));
                            Log.v(TAG,"payforPromotionSubscription"+"state3");

                        } else {
                            Log.v(TAG,"payforPromotionSubscription"+"state4");
                            runOnUiThread(new Runnable() {

                                @SuppressWarnings("unchecked")
                                @Override
                                public void run() {
                                    JoysaleApplication.dialog(UpdateCardActivity.this, getString(R.string.alert), getString(R.string.somethingwrong));

                                }
                            });
                        }
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    progressDialog.dismiss();
                    Log.v(TAG,"payforPromotionSubscription"+"state5"+t.toString());
                }
            });
        } else {
        }
    }


    private void showDialog(String title, String message) {
        final AlertDialog alertDialog = new AlertDialog.Builder(UpdateCardActivity.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (from.equals(Constants.TAG_BUYNOW)) {
                    setResult(RESULT_OK);
                    finish();
                } else if (from.equals(Constants.TAG_AD)) {
                    finish();
                    setResult(RESULT_OK);
                    alertDialog.dismiss();
                    Intent in = new Intent(getApplicationContext(), ProfileActivity.class);
                    in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    in.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                    in.putExtra(Constants.TAG_FROM, "payment");
                    startActivity(in);
                    alertDialog.dismiss();
                } else if (from.equals(Constants.TAG_URGENT)) {
                    finish();
                    setResult(RESULT_OK);
                    alertDialog.dismiss();
                    Intent in = new Intent(getApplicationContext(), ProfileActivity.class);
                    in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    in.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                    in.putExtra(Constants.TAG_FROM, "payment");
                    startActivity(in);
                    alertDialog.dismiss();
                }
                //Addon ProductLimitation
                else if(from.equalsIgnoreCase(Constants.TAG_PRODUCT_SUBSCRIPTION)){
                    setResult(RESULT_OK);
                    Intent in = new Intent(UpdateCardActivity.this, SubscriptionViewActivity.class);
                    in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    in.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                    in.putExtra("From","Payment");
                    startActivity(in);
                    alertDialog.dismiss();
                    finish();
                }

                alertDialog.dismiss();
            }
        });

        alertDialog.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.primary));
    }

    public void showProgress() {
        if (progressDialog != null && !progressDialog.isShowing())
            progressDialog.show();
    }

    public void dismissProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    public void showDialog(final Context context, String title, String message) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((Activity) context).finish();
                Intent in = new Intent(getApplicationContext(), ProfileActivity.class);
                in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                in.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                startActivity(in);
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }
}
