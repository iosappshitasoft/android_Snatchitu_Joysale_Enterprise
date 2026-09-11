package com.ni.SnatchItUp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import com.ni.helper.LocaleManager;
import com.ni.utils.ApiClient;
import com.ni.utils.ApiInterface;
import com.ni.utils.Constants;
import com.ni.utils.GetSet;
import com.wang.avi.AVLoadingIndicatorView;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by hitasoft on 24/6/16.
 * <p>
 * This class is for Promote an Urgent Type Promotion.
 */

public class PromoteUrgent extends Fragment implements View.OnClickListener {

    private static final String TAG = PromoteUrgent.class.getSimpleName();
    /**
     * Declare Layout Elements
     **/
    private RelativeLayout ad, main;
    private AVLoadingIndicatorView progress;
    private ScrollView scrollView;
    ImageView promote, tick1, tick2, tick3, tick4;
    private TextView tagText, adText1, adText2, adText3, adText4, productType, adText, pay, txtUrgentPrice,txtUrgentDuration;
    View tagView;
    ProgressDialog dialog;
    private String nonce;
    /**
     * Declare Variables
     **/
    private Context mContext;
    public static final int DROP_IN_REQUEST = 100;
    ApiInterface apiInterface;
    private String currencyCode = "", currencySymbol = "", urgentPrice = "", currencyMode, currencyPosition, formattedurgentprice,urgentduration;
    private NumberFormat numberFormat;
    private boolean isRTL = false;

    public PromoteUrgent() {
    }

    public static Fragment newInstance(Bundle bundle) {
        PromoteUrgent promoteUrgent = new PromoteUrgent();
        promoteUrgent.setArguments(bundle);
        return promoteUrgent;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        Bundle bundle = getArguments();
        Log.d(TAG, "onAttach: " + formattedurgentprice);
        currencyCode = bundle.getString(Constants.TAG_CURRENCY_CODE);
        currencySymbol = bundle.getString(Constants.TAG_CURRENCY_SYMBOL);
        urgentPrice = bundle.getString(Constants.TAG_PRICE);
        formattedurgentprice = bundle.getString(Constants.TAG_URGENTPRICE);
        urgentduration = bundle.getString("urgentduration");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.create_promote, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        if (mContext == null) mContext = getActivity();
        if (getActivity() != null) {
            isRTL = LocaleManager.isRTL(getActivity());
        }
        promote = (ImageView) view.findViewById(R.id.imageView);
        ad = (RelativeLayout) view.findViewById(R.id.promotead);
        pay = (TextView) view.findViewById(R.id.promote);
        scrollView = (ScrollView) view.findViewById(R.id.scrollView);
        progress = (AVLoadingIndicatorView) view.findViewById(R.id.progress);
        main = (RelativeLayout) view.findViewById(R.id.main);
        adText = (TextView) view.findViewById(R.id.adText);
        txtUrgentPrice = view.findViewById(R.id.txtUrgentPrice);
        txtUrgentDuration = view.findViewById(R.id.txtUrgentDuration);
        tick1 = (ImageView) view.findViewById(R.id.tick1);
        tick2 = (ImageView) view.findViewById(R.id.tick2);
        tick3 = (ImageView) view.findViewById(R.id.tick3);
        tick4 = (ImageView) view.findViewById(R.id.tick4);
        tagText = (TextView) view.findViewById(R.id.tagText);
        adText1 = (TextView) view.findViewById(R.id.adText1);
        adText2 = (TextView) view.findViewById(R.id.adText2);
        adText3 = (TextView) view.findViewById(R.id.adText3);
        adText4 = (TextView) view.findViewById(R.id.adText4);
        tagView = (View) view.findViewById(R.id.tagView);
        productType = (TextView) view.findViewById(R.id.productType);
        numberFormat = NumberFormat.getInstance(new Locale("en", "US"));
        numberFormat.setMaximumFractionDigits(2);
        dialog = new ProgressDialog(getActivity(), R.style.AppCompatAlertDialogStyle);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        pay.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);
        main.setVisibility(View.VISIBLE);
        setPrice(formattedurgentprice);
        dialog.setCanceledOnTouchOutside(false);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = new ProgressBar(getActivity()).getIndeterminateDrawable().mutate();
            drawable.setColorFilter(ContextCompat.getColor(getActivity(), R.color.progressColor),
                    PorterDuff.Mode.SRC_IN);
            dialog.setIndeterminateDrawable(drawable);
        }

        pay.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
        main.setVisibility(View.VISIBLE);
        ad.setVisibility(View.GONE);
        promote.setImageResource(R.drawable.promote_bg);

        tick1.setColorFilter(getResources().getColor(R.color.red));
        tick2.setColorFilter(getResources().getColor(R.color.red));
        tick3.setColorFilter(getResources().getColor(R.color.red));
        tick4.setColorFilter(getResources().getColor(R.color.red));

        pay.setText(getString(R.string.pay_and_highlight));
        tagText.setText(getString(R.string.urgent_tag_features));
        adText1.setText(getString(R.string.urgent_feature_list1));
        adText2.setText(getString(R.string.urgent_feature_list2));
        adText3.setText(getString(R.string.urgent_feature_list3));
        adText4.setText(getString(R.string.urgent_feature_list4));
        productType.setText(getString(R.string.urgent));
        tagText.setTextColor(getResources().getColor(R.color.red));
        tagView.setBackgroundColor(getResources().getColor(R.color.red));
        productType.setBackground(getResources().getDrawable(R.drawable.urgentbg));

        pay.setOnClickListener(this);

    }

    private void setPrice(String formattedUrgentPrice) {
        Locale locale = new Locale("en", "US");
        if (!TextUtils.isEmpty(formattedUrgentPrice)) {
            Log.i(TAG, "setPrice: " + formattedUrgentPrice);
            txtUrgentPrice.setText(formattedUrgentPrice);
            txtUrgentDuration.setText(urgentduration+" Days");
        }
    }


    /**
     * Function for send paid promotion details to server
     **/








    private void showProgress() {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    private void dismissProgress() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    /**
     * Function for Onclick Event
     **/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.promote:
                JoysaleApplication.preventMultipleClick(v);
                    payStripe();
                break;
        }
    }


    private void payStripe() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
        map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
        map.put(Constants.TAG_USERID, GetSet.getUserId());
        map.put(Constants.TAG_ITEM_ID, CreatePromote.itemId);
        map.put(Constants.TAG_PROMOTION_ID, "0");
        map.put(Constants.TAG_CURRENCY_CODE, currencyCode);
        map.put(Constants.TAG_PAYMENT_TYPE, Constants.TAG_STRIPE);
/*
        Intent intent = new Intent(getActivity(), AddCardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(Constants.TAG_REQUEST_DATA, map);
        intent.putExtra(Constants.TAG_FROM, Constants.TAG_URGENT);
        startActivityForResult(intent, Constants.PAYMENT_REQUEST_CODE); */

        Intent intent = new Intent(getActivity(), UpdateCardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(Constants.TAG_REQUEST_DATA, map);
        intent.putExtra(Constants.TAG_PRICE, urgentPrice.trim());
        intent.putExtra(Constants.TAG_FROM, Constants.TAG_URGENT);
        startActivityForResult(intent, Constants.PAYMENT_REQUEST_CODE);
        Log.d(TAG, "payStripe: " + urgentPrice.replaceAll(",", ""));
    }







    private String getFormattedPrice(String currencyCode, String currencySymbol, String currencyMode, String currencyPosition, String urgentPrice) {
        if (currencyMode.equals(Constants.TAG_SYMBOL)) {
            if (("" + currencyPosition).equals("postfix")) {
                if (LocaleManager.isRTL(getActivity())) {
//                    String temp = String.format(Locale.ENGLISH, getString(R.string.currency_format), urgentPrice, currencySymbol);
                    String temp = urgentPrice + currencySymbol;
                    Log.i(TAG, "getFormattedPrice:1 " + temp);
                    return temp;
                } else {
                    String temp = String.format(getString(R.string.currency_format), urgentPrice, currencySymbol);
                    Log.i(TAG, "getFormattedPrice:2 " + temp);
                    return temp;
                }
            } else {
                if (LocaleManager.isRTL(getActivity())) {
                    String temp = String.format(getString(R.string.currency_format), currencySymbol, urgentPrice);
                    Log.i(TAG, "getFormattedPrice:3 " + temp);
                    return temp;
                } else {
                    String temp = String.format(getString(R.string.currency_format), urgentPrice, currencySymbol);
                    Log.i(TAG, "getFormattedPrice:4 " + temp);
                    return temp;
                }
            }
        } else {
            if (("" + currencyPosition).equals("postfix")) {
                if (LocaleManager.isRTL(getActivity())) {
                    String temp = String.format(getString(R.string.currency_format), currencyCode, urgentPrice);
                    return temp;
                } else {
                    String temp = String.format(getString(R.string.currency_format), urgentPrice, currencyCode);
                    return temp;
                }
            } else {
                if (LocaleManager.isRTL(getActivity())) {
                    String temp = String.format(getString(R.string.currency_format), urgentPrice, currencyCode);
                    return temp;
                } else {
                    String temp = String.format(getString(R.string.currency_format), currencyCode, urgentPrice);
                    return temp;
                }
            }
        }
    }




}
