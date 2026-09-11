package com.ni.addons.buynow;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.ni.external.timessquare.CalendarCellDecorator;
import com.ni.external.timessquare.CalendarPickerView;
import com.ni.external.timessquare.DefaultDayViewAdapter;
import com.ni.helper.LocaleManager;
import com.ni.helper.NetworkReceiver;
import com.ni.SnatchItUp.BaseActivity;
import com.ni.SnatchItUp.JoysaleApplication;
import com.ni.SnatchItUp.R;
import com.ni.model.MySalesResponse;
import com.ni.model.TrackingResponse;
import com.ni.utils.ApiClient;
import com.ni.utils.ApiInterface;
import com.ni.utils.Constants;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by hitasoft on 17/9/16.
 * <p>
 * This class is to Display a Shipping Details.
 */

public class ShippingDetail extends BaseActivity implements View.OnClickListener {

    /**
     * Declare Layout Elements
     **/
    ImageView backBtn, settingbtn, titleimage;
    TextView title, save, shippingdate;
    EditText couriername, courierservice, trackingid, otherdetails;
    ProgressDialog dialog;
    private CalendarPickerView calendarView;

    MySalesResponse.Result data = new MySalesResponse().new Result();
    SimpleDateFormat inputParser = new SimpleDateFormat(inputFormat);
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
    String trackingId;

    /**
     * Declare Variables
     **/
    static final String TAG = "ShippingDetail";
    public static final String inputFormat = "dd-MM-yyyy";
    String sdate, from, Trackid = "", Shippingdate = "", Otherdetails = "", Courierservice = "", Couriername = "";
    int position, mYear, mMonth, mDay, noOfTimesCalled = 0;
    long shippingTimeStamp;
    ApiInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shipping_detail);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        backBtn = (ImageView) findViewById(R.id.backbtn);
        titleimage = (ImageView) findViewById(R.id.userImg);
        settingbtn = (ImageView) findViewById(R.id.settingbtn);
        couriername = (EditText) findViewById(R.id.couriername);
        courierservice = (EditText) findViewById(R.id.shippingservice);
        trackingid = (EditText) findViewById(R.id.trackingid);
        otherdetails = (EditText) findViewById(R.id.otherdetails);
        shippingdate = (TextView) findViewById(R.id.shippingdate);
        title = (TextView) findViewById(R.id.username);
        save = (TextView) findViewById(R.id.save);
        calendarView = findViewById(R.id.calendarView);

        data = (MySalesResponse.Result) getIntent().getExtras().get("data");
        from = (String) getIntent().getExtras().get(Constants.TAG_FROM);
        position = (Integer) getIntent().getExtras().get("position");

        Locale usLocale = new Locale("en", "US");
        final Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);
        final Calendar lastYear = Calendar.getInstance();

        calendarView.setCustomDayView(new DefaultDayViewAdapter());
        Typeface normalTF, boldTF;
        normalTF = Typeface.createFromAsset(this.getAssets(), "font_regular.ttf");
        boldTF = Typeface.createFromAsset(this.getAssets(), "font_bold.ttf");
        calendarView.setDateTypeface(normalTF);
        calendarView.setTitleTypeface(boldTF);
        calendarView.setDecorators(Collections.<CalendarCellDecorator>emptyList());
        calendarView.init(lastYear.getTime(), nextYear.getTime(), usLocale) //
                .inMode(CalendarPickerView.SelectionMode.SINGLE) //
                .withSelectedDate(new Date());

        backBtn.setVisibility(View.VISIBLE);
        titleimage.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        title.setText(getString(R.string.shipping_details));
        couriername.setFilters(new InputFilter[]{JoysaleApplication.EMOJI_FILTER, new InputFilter.LengthFilter(60)});
        courierservice.setFilters(new InputFilter[]{JoysaleApplication.EMOJI_FILTER, new InputFilter.LengthFilter(60)});
        trackingid.setFilters(new InputFilter[]{JoysaleApplication.EMOJI_FILTER, new InputFilter.LengthFilter(60)});
        otherdetails.setFilters(new InputFilter[]{JoysaleApplication.EMOJI_FILTER});

        save.setOnClickListener(this);
        shippingdate.setOnClickListener(this);
        backBtn.setOnClickListener(this);

        setdata();

        dialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = new ProgressBar(this).getIndeterminateDrawable().mutate();
            drawable.setColorFilter(ContextCompat.getColor(this, R.color.progressColor),
                    PorterDuff.Mode.SRC_IN);
            dialog.setIndeterminateDrawable(drawable);
        }
    }

    public void setdata() {
        if (from.equals("order") || ((data.getStatus().equalsIgnoreCase("claimed") || data.getStatus().equalsIgnoreCase("delivered")
                || data.getStatus().equalsIgnoreCase("paid")) && from.equals("sale"))) {
            couriername.setEnabled(false);
            trackingid.setEnabled(false);
            courierservice.setEnabled(false);
            otherdetails.setEnabled(false);
            save.setVisibility(View.GONE);
            shippingdate.setEnabled(false);
        }

        if (LocaleManager.isRTL(ShippingDetail.this)) {
            shippingdate.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            couriername.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            trackingid.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            courierservice.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            otherdetails.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        } else {
            shippingdate.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            couriername.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            trackingid.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            courierservice.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            otherdetails.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        }

        Typeface tf;
        tf = Typeface.createFromAsset(this.getAssets(), "font_regular.ttf");
        couriername.setTypeface(tf);
        if (data != null && data.getTrackingdetails() != null) {
            if ((data.getStatus().equalsIgnoreCase("shipped") ||
                    data.getStatus().equalsIgnoreCase("claimed")) && from.equals("enable")) {
                title.setText(getString(R.string.edit_tracking));
            }
            trackingId = data.getTrackingdetails().getId();
            couriername.setText(data.getTrackingdetails().getCouriername());
            courierservice.setText(data.getTrackingdetails().getCourierservice());
            trackingid.setText(data.getTrackingdetails().getTrackingid());
            otherdetails.setText(data.getTrackingdetails().getNotes());
            long ordDate = Long.parseLong(data.getTrackingdetails().getShippingdate()) * 1000;
            shippingTimeStamp = Long.parseLong(data.getTrackingdetails().getShippingdate());
            sdate = JoysaleApplication.getDate(ordDate, "MMM-dd-yyyy");
            shippingdate.setText(sdate);
            Picasso.get().load(data.getOrderitems().getOrderImage()).into(titleimage);
        }
    }

    private boolean compareDates(String compareStringOne) {
        Date date;
        Date dateCompareOne;
        Calendar now = Calendar.getInstance();

        int day = now.get(Calendar.DATE);
        int month = now.get(Calendar.MONTH) + 1;
        int year = now.get(Calendar.YEAR);

        date = parseDate(day + "-" + month + "-" + year);
        Log.e(TAG, "CurentDate=" + date.toString());
        dateCompareOne = parseDate(compareStringOne);

        Log.e(TAG, "dateCompareOne=" + dateCompareOne.toString());

        if (dateCompareOne.before(date)) {
            return false;
        } else return true;
    }

    private Date parseDate(String date) {
        try {
            return inputParser.parse(date);
        } catch (java.text.ParseException e) {
            return new Date(0);
        }
    }


    private void addShippingDetails() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_ORDER_ID, data.getOrderid());
            map.put(Constants.TAG_CH_STATUS, "Track");
            map.put(Constants.TAG_COURIER_NAME, Couriername);
            map.put(Constants.TAG_COURIER_SERVICE, Courierservice);
            map.put(Constants.TAG_TRACKID, Trackid);
            map.put(Constants.TAG_NOTES, Otherdetails);
            map.put(Constants.TAG_SHIPPING_DATE, String.valueOf(shippingTimeStamp));
            if (data.getTrackingdetails() != null && data.getTrackingdetails().getId() != null) {
                map.put(Constants.TAG_ID, data.getTrackingdetails().getId());
            } else {
                map.put(Constants.TAG_ID, "0");
            }

            Call<HashMap<String, String>> call = apiInterface.  addShippingDetails(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {

                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    if (response.isSuccessful()) {
                        String status = response.body().get(Constants.TAG_STATUS);

                        if (from.equals("sale") && data.getStatus().equalsIgnoreCase("processing")) {
                            dialog.show();
                            getTrackingDetails(status);
                        } else {
                            if (status.equalsIgnoreCase("true")) {
                                MySalesResponse.Trackingdetails trackingDetails = new MySalesResponse().new Trackingdetails();
                                trackingDetails.setId(trackingId);
                                trackingDetails.setShippingdate(String.valueOf(shippingTimeStamp));
                                trackingDetails.setCouriername(Couriername);
                                trackingDetails.setCourierservice(Courierservice);
                                trackingDetails.setTrackingid(Trackid);
                                trackingDetails.setNotes(Otherdetails);
                                data.setTrackingdetails(trackingDetails);
                                Toast.makeText(ShippingDetail.this, getString(R.string.tracking_details_updated), Toast.LENGTH_SHORT).show();
                                finish();
                                Intent i = new Intent(ShippingDetail.this, OrderDetail.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                i.putExtra(Constants.TAG_DATA, data);
                                i.putExtra(Constants.TAG_FROM, "sale");
                                i.putExtra("position", position);
                                startActivity(i);
                            } else {
                                Toast.makeText(ShippingDetail.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    JoysaleApplication.dialog(ShippingDetail.this, getString(R.string.alert), getString(R.string.somethingwrong));
                }
            });
        }
    }

    private void getTrackingDetails(String status) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_ORDERID, data.getOrderid());

            Call<TrackingResponse> call = apiInterface.getTrackDetails(map);
            call.enqueue(new Callback<TrackingResponse>() {
                @Override
                public void onResponse(Call<TrackingResponse> call, retrofit2.Response<TrackingResponse> response) {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    try {
                        if (response.isSuccessful() && status.equals(Constants.TAG_TRUE)) {
                            if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                                trackingId = response.body().getResult().getId();
                            } else {
                                trackingId = "0";
                            }
                            if (from.equals("sale") && MySale.Saleary.size() > 0) {
                                MySalesResponse.Trackingdetails trackingDetails = new MySalesResponse().new Trackingdetails();
                                trackingDetails.setId(trackingId);
                                trackingDetails.setShippingdate(String.valueOf(shippingTimeStamp));
                                trackingDetails.setCouriername(Couriername);
                                trackingDetails.setCourierservice(Courierservice);
                                trackingDetails.setTrackingid(Trackid);
                                trackingDetails.setNotes(Otherdetails);
                                data.setTrackingdetails(trackingDetails);
                                data.setStatus("shipped");
                                MySale.Saleary.get(position).setTrackingdetails(trackingDetails);
                                MySale.Saleary.get(position).setStatus("shipped");
                                MySale.saleAdapter.notifyDataSetChanged();
                                Toast.makeText(ShippingDetail.this, getString(R.string.tracking_details_updated), Toast.LENGTH_SHORT).show();
                                finish();
                                Intent i = new Intent(ShippingDetail.this, OrderDetail.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                i.putExtra("data", data);
                                i.putExtra(Constants.TAG_FROM, "sale");
                                i.putExtra("position", position);
                                startActivity(i);
                            }
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<TrackingResponse> call, Throwable t) {
                    call.cancel();
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            });
        } else {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dialog.dismiss();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(ShippingDetail.this, isConnected);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save:
                Shippingdate = shippingdate.getText().toString().trim();
                Couriername = couriername.getText().toString().trim();
                Courierservice = courierservice.getText().toString().trim();
                Trackid = trackingid.getText().toString().trim();
                Otherdetails = otherdetails.getText().toString().trim();
                if (Shippingdate.length() == 0 || Couriername.length() == 0 || Courierservice.length() == 0 ||
                        Trackid.length() == 0) {
                    Toast.makeText(ShippingDetail.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT).show();
                } else {
                    dialog.show();
                    addShippingDetails();
                }
                break;
            case R.id.backbtn:
                onBackPressed();
                break;
            case R.id.shippingdate:
                openSquareCalendar();
//                openDatePicker();
                break;
        }
    }

    private void openSquareCalendar() {
        calendarView.setVisibility(View.VISIBLE);
        calendarView.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {
                calendarView.setVisibility(View.GONE);
                if (!compareDates(dateFormatter.format(date.getTime()))) {
                    if (noOfTimesCalled % 2 == 0) {
                        noOfTimesCalled = 1;
                        JoysaleApplication.dialog(ShippingDetail.this, getString(R.string.alert), "Date Error");
                        shippingdate.setText("");
                    }

                } else {
                    shippingTimeStamp = date.getTime() / 1000;
                    sdate = JoysaleApplication.getDate(date.getTime());
                    shippingdate.setText(sdate);
                    Log.v(TAG, "shippingTimeStamp=" + shippingTimeStamp);
                }

            }

            @Override
            public void onDateUnselected(Date date) {

            }
        });
    }

    private void openDatePicker() {
        Calendar mcurrentDate = Calendar.getInstance();
        mYear = mcurrentDate.get(Calendar.YEAR);
        mMonth = mcurrentDate.get(Calendar.MONTH);
        mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);
        noOfTimesCalled = 0;

        DatePickerDialog mDatePicker = new DatePickerDialog(ShippingDetail.this, R.style.DatePickerTheme, new DatePickerDialog.OnDateSetListener() {


            public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {

                Calendar newDate = Calendar.getInstance();
                newDate.set(selectedyear, selectedmonth, selectedday);
                if (!compareDates(dateFormatter.format(newDate.getTime()))) {
                    if (noOfTimesCalled % 2 == 0) {
                        noOfTimesCalled = 1;
                        JoysaleApplication.dialog(ShippingDetail.this, getString(R.string.alert), "Date Error");
                        shippingdate.setText("");
                    }

                } else {
                    shippingTimeStamp = newDate.getTimeInMillis() / 1000;
                    sdate = JoysaleApplication.getDate(newDate.getTimeInMillis(), "MMM-dd-yyyy");
                    shippingdate.setText(sdate);
                    Log.v(TAG, "shippingTimeStamp=" + shippingTimeStamp);
                }
            }
        }, mYear, mMonth, mDay);
        mDatePicker.show();
    }

    @Override
    public void onBackPressed() {
        if (calendarView.getVisibility() == View.VISIBLE) {
            calendarView.setVisibility(View.GONE);
        } else {
            finish();
        }
    }
}
