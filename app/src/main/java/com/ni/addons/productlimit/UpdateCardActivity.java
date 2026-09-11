package com.ni.addons.productlimit;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.ni.helper.NetworkReceiver;
import com.ni.SnatchItUp.DetailActivity;
import com.ni.SnatchItUp.JoysaleApplication;
import com.ni.SnatchItUp.R;
import com.ni.SnatchItUp.SplashActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.google_pay_activity);
        buyButton = findViewById(R.id.google_pay_button);
        buyButton.setEnabled(false);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        from = getIntent().getStringExtra(Constants.TAG_FROM);
        stripePublishableKey = SplashActivity.stripePublicKey;
        requestMap = (HashMap<String, String>) getIntent().getExtras().get(Constants.TAG_REQUEST_DATA);

        if (from.equalsIgnoreCase(Constants.TAG_AD) || from.equalsIgnoreCase(Constants.TAG_URGENT)
                || from.equalsIgnoreCase(Constants.TAG_PRODUCT_SUBSCRIPTION)) {
            promotionPrice = getIntent().getStringExtra(Constants.TAG_PRICE);
        }
        Log.e("stripekey", "" + stripePublishableKey);
        PaymentConfiguration.init(this, stripePublishableKey);

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
            if (from.equalsIgnoreCase(Constants.TAG_BUYNOW))
                map.put("amount", requestMap.get(Constants.ORDER_TOTAL));
            else if (from.equalsIgnoreCase(Constants.TAG_BANNER))
                map.put("amount", requestMap.get(Constants.TAG_PRICE));
            else if (from.equalsIgnoreCase(Constants.TAG_AD) || from.equalsIgnoreCase(Constants.TAG_URGENT)
                    || from.equalsIgnoreCase(Constants.TAG_PRODUCT_SUBSCRIPTION)) {
                map.put("amount", promotionPrice);
            }
            //map.put("currency","INR");
            map.put("currency", requestMap.get(Constants.TAG_CURRENCY_CODE));

            Log.i(TAG, "fetchInitData: " + new Gson().toJson(map));

            Call<HashMap<String, String>> call = apiInterface.balanceSheet(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                    if (response.isSuccessful()) {
                        progressDialog.dismiss();
                        Log.e("response", "" + response.body().toString());
                        final String status = response.body().get(Constants.TAG_STATUS);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (status.equalsIgnoreCase("true")) {
                                    JSONObject responseJson = new JSONObject(response.body());
                                    paymentIntentClientSecret = responseJson.optString("paymentIntent");
                                    customerId = responseJson.optString("customer");
                                    ephemeralKeySecret = responseJson.optString("ephemeralKey");

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
                    Log.e(TAG, "payNowError: " + t.getMessage());
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

    private void presentGpayPaymentSheet() {/*
        final PaymentSheet.GooglePayConfiguration googlePayConfiguration =
                new PaymentSheet.GooglePayConfiguration(
                        PaymentSheet.GooglePayConfiguration.Environment.Test,
                        "US"
                );
        PaymentSheet.Configuration configuration = new PaymentSheet.Configuration(
                "Example, Inc."
        );
        configuration.setGooglePay(googlePayConfiguration);
        paymentSheet.presentWithPaymentIntent(
                paymentIntentClientSecret,
                configuration
        );
    */
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
            finish();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            Toast.makeText(
                    this,
                    "Payment Failed. See logcat for details.",
                    Toast.LENGTH_LONG
            ).show();


            Log.e("App", "Got error: ", ((PaymentSheetResult.Failed) paymentSheetResult).getError());
        } else if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Toast.makeText(
                    this,
                    "Payment Complete",
                    Toast.LENGTH_LONG
            ).show();

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
                //Addons ProductLimitation
                case Constants.TAG_PRODUCT_SUBSCRIPTION:
                    payForSubscription(paymentNonce);
                    break;
            }
        }
    }

    private void payNow(final String payNonce) {

        if (NetworkReceiver.isConnected()) {
            requestMap.put(Constants.TAG_NONCE, payNonce);
            requestMap.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(getApplicationContext()));
            Log.i(TAG, "payNow: " + new Gson().toJson(requestMap));
            Call<HashMap<String, String>> call = apiInterface.buyNowPayment(requestMap);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                    if (response.isSuccessful()) {
                        final String status = response.body().get(Constants.TAG_STATUS);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (status.equalsIgnoreCase("true")) {
                                    dismissProgress();
                                    showDialog(getString(R.string.success), getString(R.string.ordered_successfully));
                                } else {
                                    dismissProgress();
                                    JoysaleApplication.dialog(UpdateCardActivity.this, getResources().getString(R.string.alert), getString(R.string.somethingwrong));
                                }
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    dismissProgress();
                    Log.e(TAG, "payNowError: " + t.getMessage());
                    call.cancel();
                }
            });
        } else {
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
                public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
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


    //Addons ProductLimitation
    private void payForSubscription(final String payNounce) {
        if (NetworkReceiver.isConnected()) {
            requestMap.put(Constants.TAG_PAY_NONCE, payNounce);
            requestMap.put(Constants.TAG_PAYMENT_TYPE, SplashActivity.adminPaymentType);

            Call<HashMap<String, String>> call = apiInterface.subscriptionPayment(requestMap);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    progressDialog.dismiss();
                    if (response.isSuccessful()) {
                        if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                            DetailActivity.fromEdit = true;

                            runOnUiThread(new Runnable() {

                                @SuppressWarnings("unchecked")
                                @Override
                                public void run() {
                                    showDialog(getString(R.string.success), getString(R.string.your_Subscription_was_activated_successfully));

                                }
                            });
                        } else if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase("false")) {
                            showDialog(getString(R.string.failed), getString(R.string.your_Subscription_was_activated_not_successfully));
                        } else {
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
                }
            });
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
                    setResult(RESULT_OK);
                    alertDialog.dismiss();
                    finish();
                } else if (from.equals(Constants.TAG_URGENT)) {
                    setResult(RESULT_OK);
                    alertDialog.dismiss();
                    finish();

                }
                //Addons ProductLimitation
                else if (Constants.ADDON_PRODUCT_LIMIT && from.equalsIgnoreCase(Constants.TAG_PRODUCT_SUBSCRIPTION)) {
                    Intent in = new Intent(UpdateCardActivity.this, SubscriptionViewActivity.class);
                    in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    in.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                    in.putExtra("From", "Payment");
                    startActivity(in);
                    alertDialog.dismiss();
                    finish();
                }

                alertDialog.dismiss();
            }
        });
        alertDialog.show();
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
}
