package com.ni.addons.buynow;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
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
import com.ni.model.ShippingAddressRes;
import com.ni.utils.ApiClient;
import com.ni.utils.ApiInterface;
import com.ni.utils.Constants;
import com.ni.utils.GetSet;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by hitasoft on 13/9/16.
 * <p>
 * This class is to Display all shipping addresses.
 */

public class Addresses extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = Addresses.class.getSimpleName();
    /**
     * Declare Layout Elements
     **/
    ImageView back;
    TextView title, addAddress, continueAddress;
    ListView addressList;
    LinearLayout nullLay;
    Display display;
    AVLoadingIndicatorView progress;
    ProgressDialog dialog;
    AddressAdapter addressAdapter;
    /**
     * Declare Variables
     **/
    String from = "", addressId = null, to = null, countryId = null,shippingAddress = null;
    ApiInterface apiInterface;
    public ArrayList<ShippingAddressRes.Result> addresses = new ArrayList<>();
    private DisplayMetrics displayMetrics;
    private boolean hasChanges = false;
    private String itemId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.address_layout);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        /** Initializing Layout Elements **/
        back = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);
        addAddress = (TextView) findViewById(R.id.addAddress);
        continueAddress = (TextView) findViewById(R.id.continueAddress);
        addressList = (ListView) findViewById(R.id.addressList);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
        nullLay = (LinearLayout) findViewById(R.id.nullLay);
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        /** Get Value from Intent **/
        from = getIntent().getExtras().getString(Constants.TAG_FROM);
        if (getIntent().hasExtra(Constants.TAG_ITEM_ID))
            itemId = getIntent().getStringExtra(Constants.TAG_ITEM_ID);
        if (getIntent().hasExtra(Constants.TAG_COUNTRYID))
            countryId = getIntent().getStringExtra(Constants.TAG_COUNTRYID);

        display = this.getWindowManager().getDefaultDisplay();
        dialog = new ProgressDialog(Addresses.this, R.style.AppCompatAlertDialogStyle);

        back.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        nullLay.setVisibility(View.GONE);

        if (from.equals("profile")) {
            title.setText(getString(R.string.my_address));
        } else {
            title.setText(getString(R.string.select_address));
            if (getIntent().hasExtra(Constants.TAG_SHIPPING_ID)) {
                addressId = getIntent().getExtras().getString(Constants.TAG_SHIPPING_ID);
            }
        }

        /** Setting Listeners for Layout Elements **/
        back.setOnClickListener(this);
        addAddress.setOnClickListener(this);
        continueAddress.setOnClickListener(this);
        //   addressList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        addressList.setOnItemClickListener(this);

        //To set Adapter in addresslist
        setAdapter();
        intializeAddress();
        //To get saved address
        getAddress();

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (from.equals("checkout")) {
            addressId = addresses.get(position).getShippingId();
            shippingAddress= addresses.get(position).getCountrycode();
            addressAdapter.notifyDataSetChanged();
            hasChanges = true;
        }
    }

    private void intializeAddress() {
        addAddress.setVisibility(View.GONE);
        continueAddress.setVisibility(View.GONE);
        addressList.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
    }

    //To initialize and set Adapter
    private void setAdapter() {
        addressAdapter = new AddressAdapter(Addresses.this, addresses);
        addressList.setAdapter(addressAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Method for get saved addresses
     **/
    private void getAddress() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> paramMap = new HashMap<String, String>();
            paramMap.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            paramMap.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            paramMap.put(Constants.TAG_USERID, GetSet.getUserId());
            if (from.equals("profile")) {
                paramMap.put(Constants.TAG_ITEM_ID, "0");
            } else {
                paramMap.put(Constants.TAG_ITEM_ID, itemId);
            }
            Call<ShippingAddressRes> call = apiInterface.getShippingAddress(paramMap);
            call.enqueue(new Callback<ShippingAddressRes>() {
                @Override
                public void onResponse(Call<ShippingAddressRes> call, retrofit2.Response<ShippingAddressRes> response) {
                    if (response.isSuccessful()) {
                        if (response.body().getStatus().equalsIgnoreCase("true")) {
                            addresses.clear();
                            for (ShippingAddressRes.Result result : response.body().getResult()) {
                                if (result.getShippingId() == null) {
                                    result.setShippingId(result.getShippingid());
                                }

                                if (result.getNickName() == null) {
                                    result.setNickName(result.getNickname());
                                }

                                if (result.getFullName() == null) {
                                    result.setFullName(result.getName());
                                }

                                if (result.getZipCode() == null) {
                                    result.setZipCode(result.getZipcode());
                                }

                                if (result.getPhoneNo() == null) {
                                    result.setPhoneNo(result.getPhone());
                                }
                                if (result.getDefaultShipping().equals("1")) {
                                    addresses.add(0, result);
                                } else {
                                    addresses.add(result);
                                }
                            }
                        } else if (response.body().getStatus().equalsIgnoreCase(Constants.TAG_FALSE_CODE)   ){
                            JoysaleApplication.disabledialog(Addresses.this, "" + response.body().getMessage(), GetSet.getUserId());
                        }else if (response.body().getStatus().equalsIgnoreCase("error")) {
                            JoysaleApplication.disabledialog(Addresses.this, response.body().getMessage(), GetSet.getUserId());
                        }
                    }

                    addAddress.setVisibility(View.VISIBLE);
                    addressList.setVisibility(View.VISIBLE);
                    progress.setVisibility(View.GONE);

                    if (addresses.size() == 0) {
                        to = "primary";
                        nullLay.setVisibility(View.VISIBLE);
                    } else {
                        to = null;
                        nullLay.setVisibility(View.GONE);
                    }

                    if (from.equals("profile")) {
                        continueAddress.setVisibility(View.GONE);
                    } else {
                        continueAddress.setVisibility(View.VISIBLE);
                    }
                    addressAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(Call<ShippingAddressRes> call, Throwable t) {
                    Log.e(TAG, "getAddress: " + t.getMessage());
                    call.cancel();
                }
            });
        }
    }

    /**
     * method for get saved addresses
     **/
    private void getSavedOrDeleteAddress(final String apiName, final String shippingId, final int position) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_SHIPPING_ID, shippingId);

            Call<HashMap<String, String>> call = null;
            if (apiName.equals(Constants.TAG_REMOVE_SHIPPING)) {
                call = apiInterface.removeShipping(map);
            } else if (apiName.equals(Constants.TAG_DEFAULTSHIPPING)) {
                call = apiInterface.setDefaultShipping(map);
            }
            if (call != null) {
                call.enqueue(new Callback<HashMap<String, String>>() {
                    @Override
                    public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }

                        if (response.isSuccessful() && response.body().get(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                            hasChanges = true;
                            if (apiName.equals(Constants.TAG_REMOVE_SHIPPING)) {
                                addresses.remove(position);
                            } else {
                                for (int i = 0; i < addresses.size(); i++) {
                                    addresses.get(i).setDefaultShipping("0");
                                }
                                addresses.get(position).setDefaultShipping("1");
                                ShippingAddressRes.Result address = addresses.get(position);
                                addresses.remove(position);
                                addresses.add(0, address);
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    addressAdapter.notifyDataSetChanged();
                                }
                            });
                        }else if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase(Constants.TAG_FALSE_CODE)){
                            JoysaleApplication.disabledialog(Addresses.this, "" + response.body().get(Constants.TAG_MESSAGE), GetSet.getUserId());
                        }
                    }

                    @Override
                    public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                        call.cancel();
                    }
                });
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.ADDRESS_REQUEST_CODE && resultCode == RESULT_OK) {
            hasChanges = true;
            getAddress();
        } else if (requestCode == Constants.ADDRESS_EDIT_REQUEST_CODE && resultCode == RESULT_OK) {
            hasChanges = true;
            getAddress();
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(Addresses.this, isConnected);
    }

    /**
     * Adapter for Addresses
     **/

    private class AddressAdapter extends BaseAdapter {
        ArrayList<ShippingAddressRes.Result> addressesList;
        ViewHolder holder = null;
        Context mContext;

        public AddressAdapter(Context ctx, ArrayList<ShippingAddressRes.Result> data) {
            mContext = ctx;
            addressesList = data;
        }

        @Override
        public int getCount() {
            return addressesList.size();
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
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.address_item, parent, false);//layout
                holder = new ViewHolder();

                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.addressLine = (TextView) convertView.findViewById(R.id.addressLine);
                holder.city = (TextView) convertView.findViewById(R.id.city);
                holder.country = (TextView) convertView.findViewById(R.id.country);
                holder.options = (ImageView) convertView.findViewById(R.id.options);
                holder.main = (RelativeLayout) convertView.findViewById(R.id.main);
                holder.selectedView = (TextView) convertView.findViewById(R.id.selectedView);
                holder.defaultText = (TextView) convertView.findViewById(R.id.defaultText);

                holder.selectedView.bringToFront();

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final ShippingAddressRes.Result address = addressesList.get(position);
            if (addressId != null && addressId.equals(address.getShippingId())) {
                holder.selectedView.setVisibility(View.VISIBLE);
            } else {
                holder.selectedView.setVisibility(View.INVISIBLE);
            }

            if (address.getDefaultShipping().equals("1")) {
                holder.defaultText.setVisibility(View.VISIBLE);
            } else {
                holder.defaultText.setVisibility(View.GONE);
            }

            holder.title.setText(address.getNickName());
            holder.name.setText(address.getFullName());
            holder.addressLine.setText(address.getAddress1() + " , " + address.getAddress2());
            holder.city.setText(address.getCity() + " , " + address.getZipCode());
            holder.country.setText(address.getCountryName() != null ? address.getCountryName() : address.getCountry());

            holder.options.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] values;
                    if (from.equals("profile")) {
                        if (address.getDefaultShipping().equals("1")) {
                            values = new String[]{getString(R.string.edit)};
                        } else {
                            values = new String[]{getString(R.string.edit), getString(R.string.remove), getString(R.string.mark_default)};
                        }
                    } else {
                        values = new String[]{getString(R.string.edit), getString(R.string.remove)};
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(Addresses.this,
                            R.layout.share_new, android.R.id.text1, values);
                    LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View layout = layoutInflater.inflate(R.layout.share, null);
                    if (LocaleManager.isRTL(Addresses.this)) {
                        layout.setAnimation(AnimationUtils.loadAnimation(Addresses.this, R.anim.grow_from_topleft_to_bottomright));
                    } else {
                        layout.setAnimation(AnimationUtils.loadAnimation(Addresses.this, R.anim.grow_from_topright_to_bottomleft));
                    }
                    final PopupWindow popup = new PopupWindow(Addresses.this);
                    popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    popup.setContentView(layout);
                    popup.setWidth(displayMetrics.widthPixels * 50 / 100);
                    popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                    popup.setFocusable(true);

                    final ListView lv = (ListView) layout.findViewById(R.id.lv);
                    lv.setAdapter(adapter);
                    popup.showAsDropDown(v, -((displayMetrics.widthPixels * 45 / 100)), -60);

                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int pos, long id) {
                            switch (pos) {
                                case 0:
                                    Intent i = new Intent(Addresses.this, AddAddress.class);
                                    i.putExtra(Constants.TAG_FROM, from);
                                    i.putExtra("to", Constants.TAG_EDIT);
                                    i.putExtra(Constants.TAG_DATA, address);
                                    startActivityForResult(i, Constants.ADDRESS_EDIT_REQUEST_CODE);
                                    popup.dismiss();
                                    break;
                                case 1:
                                    dialog.setMessage(getString(R.string.pleasewait));
                                    dialog.setCancelable(false);
                                    dialog.setCanceledOnTouchOutside(false);
                                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                                        Drawable drawable = new ProgressBar(Addresses.this).getIndeterminateDrawable().mutate();
                                        drawable.setColorFilter(ContextCompat.getColor(Addresses.this, R.color.progressColor),
                                                PorterDuff.Mode.SRC_IN);
                                        dialog.setIndeterminateDrawable(drawable);
                                    }
                                    dialog.show();
                                    getSavedOrDeleteAddress(Constants.TAG_REMOVE_SHIPPING, address.getShippingId(), position);
                                    popup.dismiss();
                                    break;
                                case 2:
                                    dialog.setMessage(getString(R.string.pleasewait));
                                    dialog.setCancelable(false);
                                    dialog.setCanceledOnTouchOutside(false);
                                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                                        Drawable drawable = new ProgressBar(Addresses.this).getIndeterminateDrawable().mutate();
                                        drawable.setColorFilter(ContextCompat.getColor(Addresses.this, R.color.progressColor),
                                                PorterDuff.Mode.SRC_IN);
                                        dialog.setIndeterminateDrawable(drawable);
                                    }
                                    dialog.show();
                                    getSavedOrDeleteAddress(Constants.TAG_DEFAULTSHIPPING, address.getShippingId(), position);
                                    popup.dismiss();
                                    break;
                            }
                        }
                    });
                }
            });

            return convertView;
        }

        class ViewHolder {
            TextView title, name, addressLine, city, country, defaultText, selectedView;
            ImageView options;
            RelativeLayout main;
        }
    }

    @Override
    public void onBackPressed() {
        if (from.equals(Constants.TAG_CHECKOUT)) {
            Intent i = new Intent();
            i.putExtra(Constants.TAG_HAS_CHANGES, hasChanges);
            i.putExtra(Constants.TAG_SHIPPING_ID, "" + addressId);
            setResult(RESULT_OK, i);
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                onBackPressed();
                break;
            case R.id.addAddress:
                Intent i = new Intent(Addresses.this, AddAddress.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                i.putExtra(Constants.TAG_FROM, from);
                Log.d(TAG, "onClick: "+countryId);
                if (countryId != null) {
                    i.putExtra(Constants.TAG_COUNTRYID, countryId);
                }
                if (addresses != null && addresses.size() == 0) {
                    i.putExtra("to", "primary");
                } else {
                    if (to != null) {
                        i.putExtra("to", "primary");
                    } else {
                        i.putExtra("to", Constants.TAG_ADD);
                    }
                }
                startActivityForResult(i, Constants.ADDRESS_REQUEST_CODE);
                break;
            case R.id.continueAddress:
                boolean addressSelected = false;
                boolean hasDefaultAddress = false;
                ShippingAddressRes.Result selectedAddress = null;

                for (ShippingAddressRes.Result address : addresses) {
                    if (("" + addressId).equals(address.getShippingId())) {
                        addressSelected = true;
                        selectedAddress = address;
                        break;
                    }
                }
                for (ShippingAddressRes.Result address : addresses) {
                    if (address.getDefaultShipping().equals("1")) {
                        hasDefaultAddress = true;
                        break;
                    }
                }
                if (addressSelected) {
                    Intent j = new Intent();
                    j.putExtra(Constants.TAG_DEFAULTSHIPPING, hasDefaultAddress);
                    j.putExtra(Constants.TAG_SHIPPING_DATA, selectedAddress);
                    j.putExtra(Constants.TAG_SHIPPING_ID, selectedAddress.getShippingId() != null ? selectedAddress.getShippingId() : selectedAddress.getShippingid());
                    j.putExtra(Constants.TAG_HAS_CHANGES, hasChanges);
                    j.putExtra("shippingCountryCode",selectedAddress.getCountrycode());
                    setResult(RESULT_OK, j);
                    finish();
                } else {
                    Toast.makeText(Addresses.this, getString(R.string.select_shipping_address), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
