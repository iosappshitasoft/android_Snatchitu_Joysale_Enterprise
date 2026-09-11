package com.ni.addons.buynow;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.ni.helper.LocaleManager;
import com.ni.helper.NetworkReceiver;
import com.ni.SnatchItUp.BaseActivity;
import com.ni.SnatchItUp.JoysaleApplication;
import com.ni.SnatchItUp.R;
import com.ni.model.AddAddressResponse;
import com.ni.model.BeforeAddResponse;
import com.ni.model.ShippingAddressRes;
import com.ni.utils.ApiClient;
import com.ni.utils.ApiInterface;
import com.ni.utils.AppUtils;
import com.ni.utils.Constants;
import com.ni.utils.GetSet;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by hitasoft on 13/9/16.
 * <p>
 * This class is to add the shipping addresses.
 */

public class AddAddress extends BaseActivity implements View.OnClickListener {

    /**
     * Declare Layout Elements
     **/
    ImageView back;
    TextView title, save, country;
    EditText addressTitle, name, address, addressLine, city, state, zipcode, phone;
    AVLoadingIndicatorView progress;
    LinearLayout mainLay;
    ProgressDialog dialog;

    /**
     * Declare Variables
     **/
    static final String TAG = "AddAddress";
    String from, to, addTitle, addName, addAddress, addAddressLine, addCity, addState, addCountry,
            addCountryId, addZipCode, addPhone;

    ShippingAddressRes.Result datas = new ShippingAddressRes().new Result();
    InputFilter filterWithoutSpace, filterWithSpace;
    //for categoryList
    ArrayList<String> countryIdList = new ArrayList<String>(), countryName = new ArrayList<String>();
    ApiInterface apiInterface;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_address);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        /** Initializing Layout Elements **/
        back = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);
        save = (TextView) findViewById(R.id.save);
        addressTitle = (EditText) findViewById(R.id.addressTitle);
        name = (EditText) findViewById(R.id.name);
        address = (EditText) findViewById(R.id.address);
        addressLine = (EditText) findViewById(R.id.addressLine);
        city = (EditText) findViewById(R.id.city);
        state = (EditText) findViewById(R.id.state);
        country = (TextView) findViewById(R.id.country);
        zipcode = (EditText) findViewById(R.id.zipcode);
        phone = (EditText) findViewById(R.id.phone);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
        mainLay = (LinearLayout) findViewById(R.id.mainLay);

        dialog = new ProgressDialog(AddAddress.this, R.style.AppCompatAlertDialogStyle);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = new ProgressBar(this).getIndeterminateDrawable().mutate();
            drawable.setColorFilter(ContextCompat.getColor(this, R.color.progressColor),
                    PorterDuff.Mode.SRC_IN);
            dialog.setIndeterminateDrawable(drawable);
        }

        from = getIntent().getExtras().getString(Constants.TAG_FROM);
        to = getIntent().getExtras().getString("to");
        Log.d(TAG, "onCreate: " + from);
        Log.d(TAG, "onCreate: " + to);
        getCountry();

        back.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);
        mainLay.setVisibility(View.GONE);
        save.setVisibility(View.GONE);

        JoysaleApplication.setupUI(AddAddress.this, mainLay);

        filterWithoutSpace = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isLetterOrDigit(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };

        filterWithSpace = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isLetter(source.charAt(i)) && !Character.isSpaceChar(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };

        addressTitle.setFilters(new InputFilter[]{filterWithSpace, new InputFilter.LengthFilter(20)});
        name.setFilters(new InputFilter[]{filterWithSpace, new InputFilter.LengthFilter(30)});
        city.setFilters(new InputFilter[]{filterWithSpace, new InputFilter.LengthFilter(40)});
        state.setFilters(new InputFilter[]{filterWithSpace, new InputFilter.LengthFilter(40)});
        zipcode.setFilters(new InputFilter[]{filterWithoutSpace, new InputFilter.LengthFilter(20)});

        /** Setting Listeners for layout elements **/
        back.setOnClickListener(this);
        save.setOnClickListener(this);
        country.setOnClickListener(this);

        //for categoryList
      /*  if (adminCountryName.size() > 0) {
            progress.setVisibility(View.GONE);
            mainLay.setVisibility(View.VISIBLE);
            save.setVisibility(View.VISIBLE);
            getCountryList(adminCountryIdList, adminCountryName);
        } else {*/
        //    }


        if (to.equals(Constants.TAG_EDIT)) {
            title.setText(getString(R.string.edit_address));
            datas = (ShippingAddressRes.Result) getIntent().getExtras().get("data");
            setData();
            if (from.equals(Constants.TAG_CHECKOUT)) {
                country.setOnClickListener(this);
            } else {
                country.setOnClickListener(this);
            }
        } else {
            if (from.equals(Constants.TAG_CHECKOUT)) {
                country.setOnClickListener(this);
            } else {
                country.setOnClickListener(this);
            }
            title.setText(getString(R.string.add_address));
        }

        if (LocaleManager.isRTL(AddAddress.this)) {
            addressTitle.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            name.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            address.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            addressLine.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            city.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            state.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            zipcode.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            phone.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            country.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        } else {
            addressTitle.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            name.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            address.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            addressLine.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            city.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            state.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            zipcode.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            phone.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            country.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        JoysaleApplication.hideSoftKeyboard(AddAddress.this);
    }

    private void setData() {
        addressTitle.setText(datas.getNickName());
        name.setText(datas.getFullName());
        address.setText(datas.getAddress1());
        addressLine.setText(datas.getAddress2());
        city.setText(datas.getCity());
        state.setText(datas.getState());
        zipcode.setText(datas.getZipCode() != null ? datas.getZipCode() : datas.getZipcode());
        phone.setText(datas.getPhoneNo());
    }

    // To get Country from Api
    private void getCountry() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(AddAddress.this));
            Log.d(TAG, "getCountry: " + map);
            Call<BeforeAddResponse> call = apiInterface.getProductBeforeAdd(map);
            call.enqueue(new Callback<BeforeAddResponse>() {
                @Override
                public void onResponse(Call<BeforeAddResponse> call, retrofit2.Response<BeforeAddResponse> response) {

                    if (response.isSuccessful() && response.body().getStatus().equalsIgnoreCase("true")) {
                        BeforeAddResponse.Result result = response.body().getResult();
                        Log.d(TAG, "getCountry: " + response.body().toString());

                       /* if(adminConditionList.size()>0){
                            adminCountryIdList.clear();
                            adminCountryName.clear();
                        }*/
                        for (BeforeAddResponse.Country country : result.getCountry()) {
                            if (!country.getCountryId().equals("0")) {
                                countryIdList.add(country.getCountryId());
                                countryName.add(country.getCountryName());
                            }
                        }
                    }
                    Log.d(TAG, "getCountry: " + countryIdList);
                    Log.d(TAG, "getCountry: " + countryName);


                    progress.setVisibility(View.GONE);
                    mainLay.setVisibility(View.VISIBLE);
                    save.setVisibility(View.VISIBLE);

                    getCountryList(countryIdList, countryName);
                }

                @Override
                public void onFailure(Call<BeforeAddResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void getCountryList(ArrayList<String> countryIdList, ArrayList<String> countryName) {
        if (to.equals(Constants.TAG_EDIT)) {
            for (int i = 0; i < countryIdList.size(); i++) {
                if (datas.getCountrycode().equals(countryIdList.get(i))) {
                    addCountryId = countryIdList.get(i);
                    addCountry = countryName.get(i);
                    country.setText(addCountry);
                    break;
                }
            }
        } else {
            if (from.equals(Constants.TAG_CHECKOUT)) {
                String countryId = getIntent().getStringExtra(Constants.TAG_COUNTRYID);
                Log.v(TAG, "countryId=" + countryId);
                for (int i = 0; i < countryIdList.size(); i++) {
                    if (countryIdList.get(i).equals(countryId)) {
                        addCountryId = countryIdList.get(i);
                        addCountry = countryName.get(i);
                        country.setText(addCountry);
                        break;
                    }
                }
            } else if (from.equals("profile") || to.equals("primary")) {

                if (countryName.size() > 0) {
                    addCountryId = countryIdList.get(0);
                    addCountry = countryName.get(0);
                    country.setText(addCountry);
                }


            }
        }
    }


    //To add shipping address
    private void addAddress() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_NICK_NAME, addTitle);
            map.put(Constants.TAG_FULL_NAME, addName);
            map.put(Constants.TAG_ADDRESS1, addAddress);
            map.put(Constants.TAG_ADDRESS2, addAddressLine);
            map.put(Constants.TAG_CITY, addCity);
            map.put(Constants.TAG_STATE, addState);
            map.put(Constants.TAG_ZIP_CODE, addZipCode);
            map.put(Constants.TAG_COUNTRY_NAME, addCountry);
            map.put(Constants.TAG_COUNTRYID, addCountryId);
            map.put(Constants.TAG_PHONE_NO, addPhone);

            if (to.equals("edit")) {
                map.put(Constants.TAG_SHIPPING_ID, datas.getShippingId());
                map.put(Constants.TAG_DEFAULT, datas.getDefaultShipping());
            } else {
                map.put(Constants.TAG_SHIPPING_ID, "0");
                map.put(Constants.TAG_DEFAULT, "0");
            }


            Call<AddAddressResponse> call = apiInterface.addShippingAddress(map);
            call.enqueue(new Callback<AddAddressResponse>() {
                @Override
                public void onResponse(Call<AddAddressResponse> call, retrofit2.Response<AddAddressResponse> response) {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    try {
                        if (response.isSuccessful()) {
                            String status = response.body().getStatus();
                            if (status.equalsIgnoreCase("true")) {
                                AddAddressResponse.Result result = response.body().getResult();
                                if (to.equals("primary"))
                                    setDefaultAddress(result);
                                else
                                    setAddress(result);
                            } else if (response.body().getStatus().equalsIgnoreCase(Constants.TAG_FALSE_CODE)) {
                                JoysaleApplication.disabledialog(AddAddress.this, "" + response.body().getMessage(), GetSet.getUserId());
                            } else {
                                Toast.makeText(AddAddress.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(AddAddress.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<AddAddressResponse> call, Throwable t) {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    Toast.makeText(AddAddress.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setAddress(AddAddressResponse.Result result) {
        ShippingAddressRes.Result shippingResult = new ShippingAddressRes().new Result();
        shippingResult.setShippingId(result.getShippingid());
        shippingResult.setNickName(result.getNickname());
        shippingResult.setFullName(result.getName());
        shippingResult.setCountryName(result.getCountry());
        shippingResult.setState(result.getState());
        shippingResult.setAddress1(result.getAddress1());
        shippingResult.setAddress2(result.getAddress2());
        shippingResult.setCity(result.getCity());
        shippingResult.setZipCode(result.getZipcode());
        shippingResult.setPhoneNo(result.getPhone());
        shippingResult.setCountrycode(result.getCountrycode());
        if (result.getDefaultshipping() != null) {
            shippingResult.setDefaultShipping(result.getDefaultshipping());
        }

        switch (from) {
            case "profile": {
                finish();
                Intent i = new Intent(AddAddress.this, Addresses.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                i.putExtra(Constants.TAG_FROM, from);
                startActivity(i);
                break;
            }
            case Constants.TAG_CHECKOUT:
                if (to.equals("edit")) {
                    Intent i = new Intent();
                    setResult(RESULT_OK, i);
                    finish();
                } else {
                    Intent i = new Intent();
                    i.putExtra(Constants.TAG_SHIPPING_DATA, shippingResult);
                    setResult(RESULT_OK, i);
                    finish();
                }
                break;
        }
    }

    private void setDefaultAddress(AddAddressResponse.Result result) {
        if (NetworkReceiver.isConnected()) {
            dialog.show();
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_SHIPPING_ID, result.getShippingid());

            Call<HashMap<String, String>> call = apiInterface.setDefaultShipping(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                    result.setDefaultshipping("1");
                    setAddress(result);
                    if (dialog.isShowing()) dialog.dismiss();
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    if (dialog.isShowing()) dialog.dismiss();
                }
            });
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(AddAddress.this, isConnected);
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent();
        setResult(RESULT_CANCELED, i);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                onBackPressed();
                break;
            case R.id.save:
                addTitle = addressTitle.getText().toString().trim();
                addName = name.getText().toString().trim();
                addAddress = address.getText().toString().trim();
                addAddressLine = addressLine.getText().toString().trim();
                addCity = city.getText().toString().trim();
                addState = state.getText().toString().trim();
                addCountry = country.getText().toString().trim();
                addZipCode = zipcode.getText().toString().trim();
                addPhone = phone.getText().toString().trim();

                if (addTitle.length() == 0 || addName.length() == 0 || addAddress.length() == 0 || addCity.length() == 0
                        || addState.length() == 0 || addCountry.length() == 0 || addPhone.length() == 0) {
                    Toast.makeText(AddAddress.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT).show();
                } else if (addName.length() < 3 || addName.length() > 30) {
                    Toast.makeText(AddAddress.this, getString(R.string.fullnameishort), Toast.LENGTH_SHORT).show();
                } else if (addAddress.length() < 3) {
                    Toast.makeText(AddAddress.this, getString(R.string.addrsisshort), Toast.LENGTH_SHORT).show();
                } else if (addCity.length() < 2) {
                    Toast.makeText(AddAddress.this, getString(R.string.cityisshort), Toast.LENGTH_SHORT).show();
                } else if (addState.length() < 2) {
                    Toast.makeText(AddAddress.this, getString(R.string.stateisshort), Toast.LENGTH_SHORT).show();
                } else {
                    dialog.show();
                    addAddress();
                }
                break;
            case R.id.country:
                ArrayAdapter<String> countryadapter = new ArrayAdapter<String>(AddAddress.this,
                        android.R.layout.simple_list_item_1, countryName);
                final Dialog dialog = new Dialog(AddAddress.this);
                dialog.setContentView(R.layout.share);
                RelativeLayout main = (RelativeLayout) dialog.findViewById(R.id.main);
                final ListView lv = (ListView) dialog.findViewById(R.id.lv);
                lv.setAdapter(countryadapter);
                main.setBackground(null);
                dialog.setCancelable(true);
                dialog.setTitle(getString(R.string.select_country));
                dialog.show();

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        addCountryId = countryIdList.get(position);
                        addCountry = countryName.get(position);
                        country.setText(addCountry);
                        dialog.dismiss();
                    }
                });
                break;
        }
    }
}
