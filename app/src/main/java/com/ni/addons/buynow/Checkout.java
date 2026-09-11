package com.ni.addons.buynow;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;


import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ni.helper.LocaleManager;
import com.ni.helper.NetworkReceiver;
import com.ni.SnatchItUp.BaseActivity;
import com.ni.SnatchItUp.JoysaleApplication;
import com.ni.SnatchItUp.R;
import com.ni.SnatchItUp.UpdateCardActivity;
import com.ni.model.CheckPromoResponse;
import com.ni.model.HomeItemResponse;
import com.ni.model.ShippingAddressRes;
import com.ni.utils.ApiClient;
import com.ni.utils.ApiInterface;
import com.ni.utils.Constants;
import com.ni.utils.DiscountUtils;
import com.ni.utils.GetSet;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by hitasoft on 13/9/16.
 * <p>
 * This class is to Checkout a Product.
 */

public class Checkout extends BaseActivity implements View.OnClickListener {

    private static final String TAG = Checkout.class.getSimpleName();
    /**
     * Declare Layout Elements
     **/
    ImageView backbtn, itemImage;
    TextView title, itemPrice, itemTitle, sellerName, txtChange, price, shipping, total, paynow,
            nickName, fullName, addressLine, city, country, promoCodeTitle, promoApplyBtn,
            promoMessage, discountAmountView;
    EditText promoCodeInput;
    LinearLayout mainLay, promoCodeLay, discountLay;
    View discountDivider;
    AVLoadingIndicatorView progress;
    ProgressDialog dialog;

    /**
     * Declare Variables
     **/
    static final int DROP_IN_REQUEST = 100;
    String clientToken = "", currencySymbol = "", currencyCode = "", apiTotal = "", shippingId = "",
            offerId = "", offerCurrency = null, offerCurrencyCode = null, offerPrice = null, offerStatus = null,
            offerTotal = null, formattedOfferTotal = null, formattedOfferPrice = null, formattedShippingPrice = null;
    String appliedPromoCode = "";
    double appliedDiscountAmount = 0;
    double baseOrderTotal = 0;

    HomeItemResponse.Item data = new HomeItemResponse().new Item();
    ShippingAddressRes.Result addressResult = new ShippingAddressRes().new Result();
    ApiInterface apiInterface;
    private NumberFormat numberFormat;
    String[] stripeCurrency = {"BIF", "CLP", "DJF", "GNF", "JPY", "KMF", "KRW", "MGA", "PYG", "RWF", "UGX", "VND"
            , "VUV", "XAF", "XOF", "XPF"};
    String stripePrice = "", stripeShippingCost = "", stripeGrandTotal = "";
    String[] stringCurrency;
    String stringCurrencyCode, stringCurrenencySymbol;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkout);
        numberFormat = NumberFormat.getInstance(new Locale("en", "US"));
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        backbtn = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);
        itemImage = (ImageView) findViewById(R.id.itemImage);
        itemPrice = (TextView) findViewById(R.id.itemPrice);
        itemTitle = (TextView) findViewById(R.id.itemTitle);
        sellerName = (TextView) findViewById(R.id.sellerName);
        txtChange = (TextView) findViewById(R.id.change);
        price = (TextView) findViewById(R.id.price);
        shipping = (TextView) findViewById(R.id.shipping);
        total = (TextView) findViewById(R.id.total);
        paynow = (TextView) findViewById(R.id.paynow);
        mainLay = (LinearLayout) findViewById(R.id.mainLay);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
        nickName = (TextView) findViewById(R.id.nickName);
        fullName = (TextView) findViewById(R.id.fullName);
        addressLine = (TextView) findViewById(R.id.addressLine);
        city = (TextView) findViewById(R.id.city);
        country = (TextView) findViewById(R.id.country);
        promoCodeLay = (LinearLayout) findViewById(R.id.promoCodeLay);
        promoCodeTitle = (TextView) findViewById(R.id.promoCodeTitle);
        promoCodeInput = (EditText) findViewById(R.id.promoCodeInput);
        promoApplyBtn = (TextView) findViewById(R.id.promoApplyBtn);
        promoMessage = (TextView) findViewById(R.id.promoMessage);
        discountLay = (LinearLayout) findViewById(R.id.discountLay);
        discountAmountView = (TextView) findViewById(R.id.discountAmount);
        discountDivider = findViewById(R.id.discountDivider);
        DiscountUtils.applyPromoCodeTheme(this, promoCodeLay, findViewById(R.id.promoCodeInputLay),
                promoCodeTitle, null, promoCodeInput);

        dialog = new ProgressDialog(Checkout.this, R.style.AppCompatAlertDialogStyle);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = new ProgressBar(this).getIndeterminateDrawable().mutate();
            drawable.setColorFilter(ContextCompat.getColor(this, R.color.progressColor),
                    PorterDuff.Mode.SRC_IN);
            dialog.setIndeterminateDrawable(drawable);
        }

        data = (HomeItemResponse.Item) getIntent().getExtras().get(Constants.TAG_ITEM_DATA);
        addressResult = (ShippingAddressRes.Result) getIntent().getExtras().get(Constants.TAG_SHIPPING_DATA);
        if (getIntent().hasExtra(Constants.TAG_OFFER_ID)) {
            offerId = getIntent().getStringExtra(Constants.TAG_OFFER_ID);
            offerStatus = getIntent().getStringExtra(Constants.TAG_OFFER_STATUS);
            offerPrice = getIntent().getStringExtra(Constants.TAG_OFFER_PRICE);
            offerTotal = getIntent().getStringExtra(Constants.TAG_OFFER_TOTAL);
            formattedOfferPrice = getIntent().getStringExtra(Constants.TAG_FORMATTED_OFFER_PRICE);
            formattedOfferTotal = getIntent().getStringExtra(Constants.TAG_FORMATTED_OFFER_TOTAL);
            formattedShippingPrice = getIntent().getStringExtra(Constants.TAG_FORMATTED_SHIPPING_PRICE);
            offerCurrency = getIntent().getStringExtra(Constants.TAG_OFFER_CURRENCY);
            offerCurrencyCode = getIntent().getStringExtra(Constants.TAG_OFFER_CURRENCY_CODE);
        }

        stringCurrency = data.getCurrencyCode().split("-");
        stringCurrencyCode = stringCurrency[1];
        stringCurrenencySymbol = stringCurrency[0];

        for (String s : stripeCurrency) {
            if (stringCurrencyCode.equalsIgnoreCase(s)) {
                stripePrice = String.valueOf(Math.round(Double.parseDouble(data.getPrice())));
                stripeShippingCost = String.valueOf(Math.round(Double.parseDouble(data.getShippingCost())));
            }
        }

        backbtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);
        mainLay.setVisibility(View.GONE);
        paynow.setVisibility(View.GONE);

        title.setText(getString(R.string.checkout));

        backbtn.setOnClickListener(this);
        paynow.setOnClickListener(this);
        txtChange.setOnClickListener(this);
        if (promoApplyBtn != null) {
            promoApplyBtn.setOnClickListener(this);
        }
        if (promoCodeInput != null) {
            promoCodeInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!TextUtils.isEmpty(appliedPromoCode)
                            && !appliedPromoCode.equalsIgnoreCase(s.toString().trim())) {
                        clearAppliedPromo();
                    }
                    if (promoMessage != null) {
                        promoMessage.setVisibility(View.GONE);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        setData();
        setAddressData(addressResult);
        progress.setVisibility(View.GONE);
        mainLay.setVisibility(View.VISIBLE);
        paynow.setVisibility(View.VISIBLE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
         if (requestCode == Constants.ADDRESS_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null && data.hasExtra(Constants.TAG_HAS_CHANGES)) {
                    if (data.getBooleanExtra(Constants.TAG_HAS_CHANGES, false)) {
                        if (data.getStringExtra(Constants.TAG_SHIPPING_ID) != null) {
                            shippingId = data.getStringExtra(Constants.TAG_SHIPPING_ID);
                        }
                        getAddress();
                    }
                }
            }
        } else if (requestCode == Constants.PAYMENT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(Checkout.this, getString(R.string.ordered_successfully), Toast.LENGTH_SHORT).show();
                Intent i = new Intent(Checkout.this, MySalesnOrder.class);
                i.putExtra(Constants.TAG_FROM, Constants.TAG_BUYNOW);
                startActivity(i);
                finish();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @SuppressLint("SetTextI18n")
    private void setData() {
        try {
            Picasso.get().load(data.getPhotos().get(0).getItemUrlMainOriginal()).into(itemImage);
            itemTitle.setText(JoysaleApplication.stripHtml(data.getItemTitle()));

            if (!TextUtils.isEmpty(offerId) && !("" + offerId).equals("0")) {
                currencySymbol = offerCurrency;
                currencyCode = offerCurrencyCode;
            } else if (data.getCurrencyCode().contains("-")) {
                String currency[] = data.getCurrencyCode().split("-");
                currencySymbol = currency[0];
                currencyCode = currency[1];
            } else {
                currencyCode = data.getCurrencyCode();
            }

            if (LocaleManager.isRTL(Checkout.this)) {
                itemTitle.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            } else {
                itemTitle.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            }
            sellerName.setText(data.getSellerName());

            //Get Price and Shipping Price Separately
            if (!TextUtils.isEmpty(offerId) && !("" + offerId).equals("0")) {
                apiTotal = numberFormat.format(Double.parseDouble(offerTotal));
                baseOrderTotal = Double.parseDouble(offerTotal);
                if (stripePrice.equalsIgnoreCase("")) {
                    itemPrice.setText(formattedOfferPrice);
                    price.setText(formattedOfferPrice);
                } else {
                    itemPrice.setText(stringCurrenencySymbol + " " + stripePrice);
                    price.setText(stringCurrenencySymbol + " " + stripePrice);
                }

                if (stripeShippingCost.equalsIgnoreCase(""))
                    shipping.setText(formattedShippingPrice);
                else
                    shipping.setText(stringCurrenencySymbol + " " + stripeShippingCost);
                if (!stripePrice.equalsIgnoreCase("") && !stripeShippingCost.equalsIgnoreCase(""))
                    stripeGrandTotal = String.valueOf(sum(stripePrice, stripeShippingCost));

                if (stripeGrandTotal.equalsIgnoreCase(""))
                    total.setText(formattedOfferTotal);
                else {
                    total.setText(stringCurrenencySymbol + " " + String.valueOf(sum(stripePrice, stripeShippingCost)));
                    stripeGrandTotal = String.valueOf(sum(stripePrice, stripeShippingCost));
                }
            } else {
                apiTotal = numberFormat.format(Double.parseDouble(data.getTotalPrice()));
                baseOrderTotal = Double.parseDouble(data.getTotalPrice());

                if (stripePrice.equalsIgnoreCase("")) {
                    itemPrice.setText(data.getFormattedPrice());
                    Log.d(TAG, "setData: "+data.getFormattedPrice());
                    price.setText(data.getFormattedPrice());
                } else {
                    itemPrice.setText(stringCurrenencySymbol + " " + stripePrice);
                    price.setText(stringCurrenencySymbol + " " + stripePrice);
                }
                if (stripeShippingCost.equalsIgnoreCase(""))
                    shipping.setText(data.getFormattedShippingCost());
                else
                    shipping.setText(stringCurrenencySymbol + " " + stripeShippingCost);

                if (!stripePrice.equalsIgnoreCase("") && !stripeShippingCost.equalsIgnoreCase(""))
                    stripeGrandTotal = String.valueOf(sum(stripePrice, stripeShippingCost));

                if (stripeGrandTotal.equalsIgnoreCase(""))
                    total.setText(data.getFormattedTotalPrice());
                else {

                    total.setText(stringCurrenencySymbol + " " + String.valueOf(sum(stripePrice, stripeShippingCost)));


                }
            }
            if (!stripeGrandTotal.equalsIgnoreCase("")) {
                baseOrderTotal = Double.parseDouble(stripeGrandTotal);
            }
            refreshTotalWithDiscount();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int sum(String x, String y) {
        int a = Integer.parseInt(x);
        int b = Integer.parseInt(y);
        int sum = a + b;
        return sum;
    }

    private void setAddressData(ShippingAddressRes.Result addressResult) {
        if (addressResult != null) {
            nickName.setVisibility(View.VISIBLE);
            fullName.setVisibility(View.VISIBLE);
            addressLine.setVisibility(View.VISIBLE);
            city.setVisibility(View.VISIBLE);
            country.setVisibility(View.VISIBLE);
            nickName.setText(addressResult.getNickName() != null ? addressResult.getNickName() : addressResult.getNickname());
            fullName.setText(addressResult.getFullName() != null ? addressResult.getFullName() : addressResult.getName());
            addressLine.setText(addressResult.getAddress1() + " , " + addressResult.getAddress2());
            city.setText(addressResult.getCity() + " , " + (addressResult.getZipCode() != null ? addressResult.getZipCode() : addressResult.getZipcode()));
            StringBuilder tempCountry = new StringBuilder();
            tempCountry.append(addressResult.getCountryName() != null ? addressResult.getCountryName() : addressResult.getCountry());
            tempCountry.append("\n");
            tempCountry.append(addressResult.getPhoneNo() != null ? addressResult.getPhoneNo() : addressResult.getPhone());
            country.setText("" + tempCountry);
            shippingId = addressResult.getShippingId() != null ? addressResult.getShippingId() : addressResult.getShippingid();
        }
    }

    private void showDialog(String title, String message) {
        final AlertDialog alertDialog = new AlertDialog.Builder(Checkout.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(Checkout.this, MySalesnOrder.class);
                i.putExtra(Constants.TAG_FROM, "buynow");
                startActivity(i);
                finish();
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }




    /**
     * send paid item details to server
     **/



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                finish();
                break;
            case R.id.paynow:
                apiTotal = apiTotal.replaceAll(",", "");
                if (addressResult == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.add_shipping_address_desc), Toast.LENGTH_SHORT).show();
                } else {
                    payStripe();
                }
                break;
            case R.id.change:
                Intent l = new Intent(Checkout.this, Addresses.class);
                l.putExtra(Constants.TAG_FROM, Constants.TAG_CHECKOUT);
                l.putExtra(Constants.TAG_SHIPPING_ID, shippingId);
                l.putExtra(Constants.TAG_ITEM_ID, data.getId());
                l.putExtra(Constants.TAG_COUNTRYID, data.getCountryId());
                Log.d(TAG, "getid: "+data.getCountryId());
                // l.putExtra(Constants.TAG_COUNTRYID, "100");
                startActivityForResult(l, Constants.ADDRESS_REQUEST_CODE);
                break;
            case R.id.promoApplyBtn:
                applyPromoCode();
                break;
        }
    }

    private void applyPromoCode() {
        String code = promoCodeInput != null ? promoCodeInput.getText().toString().trim() : "";
        if (TextUtils.isEmpty(code)) {
            showPromoMessage(getString(R.string.enter_promo_code), true);
            return;
        }
        if (!NetworkReceiver.isConnected()) {
            JoysaleApplication.networkError(Checkout.this, false);
            return;
        }
        hideKeyboard();
        showLoading();

        Map<String, String> map = new HashMap<>();
        map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
        map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
        map.put("promocode", code);
        map.put("product_id", data.getId());
        String qty = "1";
        if (data.getQuantity() != null && !data.getQuantity().trim().isEmpty()) {
            qty = data.getQuantity().trim();
        }
        map.put("qty", qty);

        Log.d(TAG, "checkPromo request: " + new com.google.gson.Gson().toJson(map));
        Call<CheckPromoResponse> call = apiInterface.checkPromo(map);
        call.enqueue(new Callback<CheckPromoResponse>() {
            @Override
            public void onResponse(Call<CheckPromoResponse> call, retrofit2.Response<CheckPromoResponse> response) {
                hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    CheckPromoResponse body = response.body();
                    String status = body.getStatus() != null ? body.getStatus() : "false";
                    if (status.equalsIgnoreCase("true")) {
                        appliedPromoCode = !TextUtils.isEmpty(body.getPromoCode()) ? body.getPromoCode() : code;
                        try {
                            appliedDiscountAmount = Double.parseDouble(
                                    body.getDiscountAmount() != null ? body.getDiscountAmount().trim() : "0");
                        } catch (NumberFormatException e) {
                            appliedDiscountAmount = 0;
                        }
                        if (promoCodeInput != null) {
                            promoCodeInput.setText(appliedPromoCode);
                        }
                        refreshTotalWithDiscount();
                        showPromoMessage(getString(R.string.promo_applied), false);
                        Toast.makeText(Checkout.this, getString(R.string.promo_applied), Toast.LENGTH_SHORT).show();
                    } else {
                        clearAppliedPromo();
                        String message = !TextUtils.isEmpty(body.getMessage())
                                ? body.getMessage()
                                : getString(R.string.somethingwrong);
                        showPromoMessage(message, true);
                        Toast.makeText(Checkout.this, message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    clearAppliedPromo();
                    showPromoMessage(getString(R.string.somethingwrong), true);
                }
            }

            @Override
            public void onFailure(Call<CheckPromoResponse> call, Throwable t) {
                hideLoading();
                clearAppliedPromo();
                showPromoMessage(getString(R.string.somethingwrong), true);
                call.cancel();
            }
        });
    }

    private void clearAppliedPromo() {
        appliedPromoCode = "";
        appliedDiscountAmount = 0;
        if (discountLay != null) {
            discountLay.setVisibility(View.GONE);
        }
        if (discountDivider != null) {
            discountDivider.setVisibility(View.GONE);
        }
        refreshTotalWithDiscount();
    }

    private void refreshTotalWithDiscount() {
        double finalTotal = Math.max(0, baseOrderTotal - appliedDiscountAmount);
        NumberFormat priceFormat = NumberFormat.getInstance(Locale.US);
        priceFormat.setGroupingUsed(false);
        if (Math.abs(finalTotal - Math.rint(finalTotal)) < 0.0001d) {
            priceFormat.setMaximumFractionDigits(0);
        } else {
            priceFormat.setMaximumFractionDigits(2);
            priceFormat.setMinimumFractionDigits(2);
        }
        String symbol = !TextUtils.isEmpty(currencySymbol) ? currencySymbol
                : (!TextUtils.isEmpty(stringCurrenencySymbol) ? stringCurrenencySymbol : "$");
        apiTotal = priceFormat.format(finalTotal);
        if (total != null) {
            total.setText(symbol + " " + apiTotal);
        }
        if (appliedDiscountAmount > 0) {
            NumberFormat discountFormat = NumberFormat.getInstance(Locale.US);
            discountFormat.setGroupingUsed(false);
            if (Math.abs(appliedDiscountAmount - Math.rint(appliedDiscountAmount)) < 0.0001d) {
                discountFormat.setMaximumFractionDigits(0);
            } else {
                discountFormat.setMaximumFractionDigits(2);
                discountFormat.setMinimumFractionDigits(2);
            }
            if (discountLay != null) {
                discountLay.setVisibility(View.VISIBLE);
            }
            if (discountDivider != null) {
                discountDivider.setVisibility(View.VISIBLE);
            }
            if (discountAmountView != null) {
                discountAmountView.setText("-" + symbol + " " + discountFormat.format(appliedDiscountAmount));
            }
        } else {
            if (discountLay != null) {
                discountLay.setVisibility(View.GONE);
            }
            if (discountDivider != null) {
                discountDivider.setVisibility(View.GONE);
            }
        }
    }

    private void showPromoMessage(String message, boolean isError) {
        if (promoMessage == null) {
            return;
        }
        promoMessage.setVisibility(View.VISIBLE);
        promoMessage.setText(message);
        promoMessage.setTextColor(isError ? Color.parseColor("#FF5252") : Color.parseColor("#00D632"));
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void openPaymentDialog() {
        BottomSheetDialog paymentDialog;
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_dialog_payment, null);
        paymentDialog = new BottomSheetDialog(this, R.style.Bottom_FilterDialog); // Style here
        paymentDialog.setContentView(sheetView);
        FrameLayout btnPayPal = paymentDialog.findViewById(R.id.btnPayPal);
        FrameLayout btnStripe = paymentDialog.findViewById(R.id.btnStripe);

        btnPayPal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paymentDialog.dismiss();
            }
        });

        btnStripe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                payStripe();
                paymentDialog.dismiss();
            }
        });

        paymentDialog.show();
    }

    private void openPayPalDialog() {

        /*GooglePaymentRequest googlePaymentRequest = new GooglePaymentRequest()
                .transactionInfo(TransactionInfo.newBuilder()
                        .setTotalPrice(apiTotal)
                        .setCurrencyCode(currencySymbol)
                        .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                        .build())
                .emailRequired(false);

        DropInRequest dropInRequest = new DropInRequest()
                .tokenizationKey(clientToken)
                .amount(apiTotal)
                .googlePaymentRequest(googlePaymentRequest);
        startActivityForResult(dropInRequest.getIntent(Checkout.this), DROP_IN_REQUEST);*/


    }

    /*
        public static int math(float f) {
            int c = (int) ((f) + 0.5f);
            float n = f + 0.5f;
            return (n - c) % 2 == 0 ? (int) f : c;
        }
    */
    private void payStripe() {

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
        map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
        map.put(Constants.TAG_USERID, GetSet.getUserId());
        map.put(Constants.TAG_MERCHANT_ID, data.getSellerId());
        map.put(Constants.TAG_SHIPPING_ID, TextUtils.isEmpty(shippingId) ? "" : shippingId);
        map.put(Constants.TAG_ITEM_ID, data.getId());
        if (stripePrice.equalsIgnoreCase(""))
            map.put(Constants.TAG_ITEM_PRICE, data.getPrice());
        else
            map.put(Constants.TAG_ITEM_PRICE, stripePrice);

        if (stripeShippingCost.equalsIgnoreCase(""))
            map.put(Constants.SHIPPING_FEE, data.getShippingCost() != null ? data.getShippingCost() : "0");
        else
            map.put(Constants.SHIPPING_FEE, stripeShippingCost);

        // Keep Stripe charge amount and buynowPayment order_total identical
        String orderTotalToPay;
        if (!TextUtils.isEmpty(appliedPromoCode) && appliedDiscountAmount > 0) {
            orderTotalToPay = apiTotal;
        } else if (!stripeGrandTotal.equalsIgnoreCase("")) {
            orderTotalToPay = stripeGrandTotal;
        } else {
            orderTotalToPay = apiTotal;
        }
        orderTotalToPay = orderTotalToPay.replaceAll(",", "").trim();
        map.put(Constants.ORDER_TOTAL, orderTotalToPay);

        map.put(Constants.TAG_CURRENCY_CODE, currencyCode);
        map.put(Constants.TAG_OFFER_ID, TextUtils.isEmpty(offerId) ? "0" : offerId);
        map.put(Constants.TAG_PAYMENT_TYPE, Constants.TAG_STRIPE);
        if (!TextUtils.isEmpty(appliedPromoCode) && appliedDiscountAmount > 0) {
            String discountAmount = formatAmount(appliedDiscountAmount);
            map.put(Constants.TAG_PROMO_CODE, appliedPromoCode);
            map.put("promocode", appliedPromoCode);
            map.put("discount_amount", discountAmount);
            map.put(Constants.TAG_DISCOUNT_PERCENTAGE,
                    data.getDiscountPercentage() != null ? data.getDiscountPercentage().trim() : "");
        }
        Log.d(TAG, "payStripe request: " + new com.google.gson.Gson().toJson(map));

        Intent intent = new Intent(this, UpdateCardActivity.class);
        intent.putExtra(Constants.TAG_FROM, Constants.TAG_BUYNOW);
        intent.putExtra(Constants.TAG_REQUEST_DATA, map);
        startActivityForResult(intent, Constants.PAYMENT_REQUEST_CODE);
    }

    private String formatAmount(double amount) {
        NumberFormat priceFormat = NumberFormat.getInstance(Locale.US);
        priceFormat.setGroupingUsed(false);
        if (Math.abs(amount - Math.rint(amount)) < 0.0001d) {
            priceFormat.setMaximumFractionDigits(0);
        } else {
            priceFormat.setMaximumFractionDigits(2);
            priceFormat.setMinimumFractionDigits(2);
        }
        return priceFormat.format(amount);
    }

    private void getAddress() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_ITEM_ID, data.getId());
            Call<ShippingAddressRes> call = apiInterface.getShippingAddress(map);
            call.enqueue(new Callback<ShippingAddressRes>() {
                @Override
                public void onResponse(Call<ShippingAddressRes> call, retrofit2.Response<ShippingAddressRes> response) {
                    if (response.isSuccessful()) {
                        String status = "" + response.body().getStatus();
                        if (status.equalsIgnoreCase("true")) {
                            if (response.body().getResult().size() == 0) {
                                addressResult = null;
                                nickName.setVisibility(View.GONE);
                                fullName.setVisibility(View.GONE);
                                addressLine.setVisibility(View.GONE);
                                city.setVisibility(View.GONE);
                                country.setVisibility(View.GONE);
                            } else {
                                txtChange.setText(getString(R.string.change));
                                for (ShippingAddressRes.Result result : response.body().getResult()) {
                                    if (shippingId != null) {
                                        if (result.getShippingId() != null && result.getShippingId().equals(shippingId)) {
                                            addressResult = result;
                                            setAddressData(addressResult);
                                            break;
                                        } else if (result.getShippingid() != null && result.getShippingid().equals(shippingId)) {
                                            addressResult = result;
                                            setAddressData(addressResult);
                                            break;
                                        }
                                    }
                                }
                            }

                        } else if (response.body().getStatus().equalsIgnoreCase(Constants.TAG_FALSE_CODE)) {
                            JoysaleApplication.disabledialog(Checkout.this, "" + response.body().getMessage(), GetSet.getUserId());
                        } else if (status.equalsIgnoreCase("error")) {
                            JoysaleApplication.disabledialog(Checkout.this, response.body().getMessage(), GetSet.getUserId());
                        } else if (status.equalsIgnoreCase("false")) {
                            addressResult = null;
                            nickName.setVisibility(View.GONE);
                            fullName.setVisibility(View.GONE);
                            addressLine.setVisibility(View.GONE);
                            city.setVisibility(View.GONE);
                            country.setVisibility(View.GONE);
                            txtChange.setText(getString(R.string.add));
                        }
                    }
                }

                @Override
                public void onFailure(Call<ShippingAddressRes> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void showLoading() {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    private void hideLoading() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(Checkout.this, isConnected);
    }
}