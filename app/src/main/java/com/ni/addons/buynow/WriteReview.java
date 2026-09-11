package com.ni.addons.buynow;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.ni.helper.LocaleManager;
import com.ni.helper.NetworkReceiver;
import com.ni.SnatchItUp.BaseActivity;
import com.ni.SnatchItUp.JoysaleApplication;
import com.ni.SnatchItUp.R;
import com.ni.model.MySalesResponse;
import com.ni.model.ReviewObject;
import com.ni.utils.ApiClient;
import com.ni.utils.ApiInterface;
import com.ni.utils.Constants;
import com.ni.utils.GetSet;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by hitasoft on 17/1/17.
 * <p>
 * This class is to Write a Product Review.
 */

public class WriteReview extends BaseActivity implements View.OnClickListener {

    /**
     * Declare Layout Elements
     **/
    ImageView backbtn;
    RatingBar ratingBar;
    EditText reviewTitle, review;
    TextView submit;
    ProgressDialog dialog;


    MySalesResponse.Result data = new MySalesResponse().new Result();


    /**
     * Declare Variables
     **/
    String from;
    int position;
    static final String TAG = "WriteReview";
    ApiInterface apiInterface;
    String start, itemId = "", sellerId = "", reviewId = "",reviewType = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.write_review);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        backbtn = (ImageView) findViewById(R.id.backbtn);
        ratingBar = (RatingBar) findViewById(R.id.rating_bar);
        reviewTitle = (EditText) findViewById(R.id.reviewTitle);
        review = (EditText) findViewById(R.id.review);
        submit = (TextView) findViewById(R.id.submit);

        itemId = (String) getIntent().getExtras().get("item_id");


        sellerId = (String) getIntent().getExtras().get("user_id");


        dialog = new ProgressDialog(WriteReview.this, R.style.AppCompatAlertDialogStyle);

        from = (String) getIntent().getExtras().get(Constants.TAG_FROM);
        data = (MySalesResponse.Result) getIntent().getExtras().get("data");
        if(itemId == null){
            itemId = data.getOrderid();
        }

        reviewType = getIntent().getStringExtra(Constants.TAG_REVIEW_TYPE);

       // position = (int) getIntent().getExtras().get("position");

        if(from != null) {
            if (from.equals("edit")) {
                ratingBar.setRating(Float.parseFloat(data.getRating()));
                reviewTitle.setText(data.getReviewTitle());
                review.setText(data.getReviewDes());
            }else{
                getReview();
            }
        }

        if (LocaleManager.isRTL(WriteReview.this)) {
            reviewTitle.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            review.setGravity(Gravity.RIGHT | Gravity.TOP);
        } else {
            reviewTitle.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            review.setGravity(Gravity.LEFT | Gravity.TOP);
        }

        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable().getCurrent();
        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.starColor), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(0).setColorFilter(getResources().getColor(R.color.secondaryText), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.starColor), PorterDuff.Mode.SRC_ATOP);

        backbtn.setOnClickListener(this);
        submit.setOnClickListener(this);

    }

    private void writeReview(final String rating, final String reviewTitle, final String review) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());


            if(sellerId != null && !sellerId.equals(""))
                map.put(Constants.TAG_SELLERID, sellerId);
            else
                map.put(Constants.TAG_SELLERID, data.getOrderitems().getSellerId());

            if(itemId != null &&!itemId.equals(""))
                map.put(Constants.TAG_ORDERID, itemId);
            else
                map.put(Constants.TAG_ORDERID, data.getOrderid());

            map.put(Constants.TAG_REVIEW_TYPE,reviewType);
            map.put(Constants.TAG_RATING, rating);
            map.put(Constants.TAG_REVIEW_TITLE, reviewTitle);
            map.put(Constants.TAG_REVIEW_DES, review);
            if (from.equals("edit")) {
                map.put(Constants.TAG_REVIEW_ID, data.getReviewId());
            } else if (!reviewId.equals("")) {
                map.put(Constants.TAG_REVIEW_ID, reviewId);
            } else {
                map.put(Constants.TAG_REVIEW_ID, "0");
            }
            Log.d(TAG, "writeReview: "+map);
            Call<HashMap<String, String>> call = apiInterface.updateReview(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    if (dialog.isShowing() && dialog != null) {
                        dialog.dismiss();
                    }
                    try {
                        if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                            if (from.equalsIgnoreCase("edit")) {
                                Intent i = new Intent(WriteReview.this, MySalesnOrder.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                                Toast.makeText(WriteReview.this, getString(R.string.rated_successfully), Toast.LENGTH_SHORT).show();
                            } else if (from.equalsIgnoreCase("notification")) {
                                finish();
                                Toast.makeText(WriteReview.this, getString(R.string.rated_successfully), Toast.LENGTH_SHORT).show();
                            }else{
                                Intent i = new Intent(WriteReview.this, MySalesnOrder.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                                Toast.makeText(WriteReview.this, getString(R.string.rated_successfully), Toast.LENGTH_SHORT).show();

                            }
                        } else {
                            Toast.makeText(WriteReview.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                        }
                    }catch (NullPointerException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    if (dialog.isShowing() && dialog != null) {
                        dialog.dismiss();
                    }
                }
            });
        } else {
            if (dialog.isShowing() && dialog != null) {
                dialog.dismiss();
            }
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(WriteReview.this, isConnected);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                finish();
                break;
            case R.id.submit:
                if (ratingBar.getRating() == 0) {
                    Toast.makeText(WriteReview.this, getString(R.string.please_give_your_rating), Toast.LENGTH_SHORT).show();
                } else if (reviewTitle.getText().toString().trim().length() == 0) {
                    Toast.makeText(WriteReview.this, getString(R.string.please_fill_title), Toast.LENGTH_SHORT).show();
                } else if (review.getText().toString().trim().length() == 0) {
                    Toast.makeText(WriteReview.this, getString(R.string.please_fill_review), Toast.LENGTH_SHORT).show();
                } else {
                    dialog.setMessage(getString(R.string.pleasewait));
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                        Drawable drawable = new ProgressBar(this).getIndeterminateDrawable().mutate();
                        drawable.setColorFilter(ContextCompat.getColor(this, R.color.progressColor),
                                PorterDuff.Mode.SRC_IN);
                        dialog.setIndeterminateDrawable(drawable);
                    }
                    dialog.show();
                    writeReview(String.valueOf((int) ratingBar.getRating()), reviewTitle.getText().toString().trim(), review.getText().toString().trim());
                }
                break;
        }
    }

    //addons Review
    private void getReview() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_ITEM_ID, itemId);
            Log.i(TAG, "getReview: " + map);
            dialog.show();
            Call<ReviewObject> call = apiInterface.reviewDetails(map);
            call.enqueue(new Callback<ReviewObject>() {
                @Override
                public void onResponse(Call<ReviewObject> call, retrofit2.Response<ReviewObject> response) {

//                    Log.v("reviewDetails", "=" + response.body().toString());
                    if (dialog.isShowing() && dialog != null) {
                        dialog.dismiss();
                    }
                    try {
                        if (response.body().getStatus().equalsIgnoreCase("true")) {

                            reviewId = response.body().getResult().getReviewId();
                            ratingBar.setRating(Float.parseFloat(response.body().getResult().getRating()));
                            reviewTitle.setText(response.body().getResult().getReviewTitle());
                            review.setText(response.body().getResult().getReviewDes());
                            Log.d(TAG, "onResponse: "+reviewId);
                            //   finish();
                            //Toast.makeText(WriteReview.this, getString(R.string.rated_successfully), Toast.LENGTH_SHORT).show();
                        } else if (response.body().getStatus().equalsIgnoreCase("false")) {
                            // Toast.makeText(WriteReview.this, getString(R.string.no_reviews), Toast.LENGTH_SHORT).show();
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ReviewObject> call, Throwable t) {
                    Log.v("reviewDetails", "onFailure=" + t.getMessage());

                    if (dialog.isShowing() && dialog != null) {
                        dialog.dismiss();
                    }
                }
            });
        } else {

            if (dialog.isShowing() && dialog != null) {
                dialog.dismiss();
            }
        }
    }

}
