package com.ni.addons.soldto;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ni.helper.NetworkReceiver;
import com.ni.SnatchItUp.BaseActivity;
import com.ni.SnatchItUp.DetailActivity;
import com.ni.SnatchItUp.JoysaleApplication;
import com.ni.SnatchItUp.ProfileActivity;
import com.ni.SnatchItUp.R;
import com.ni.model.FollowersResponse;
import com.ni.utils.ApiClient;
import com.ni.utils.ApiInterface;
import com.ni.utils.Constants;
import com.ni.utils.GetSet;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class SoldTo extends BaseActivity {

    RecyclerView listView;
    ImageView backbtn;
    TextView title, none, sold;
    LinearLayout nullLay;
    AVLoadingIndicatorView progress;
    ApiInterface apiInterface;

    RecyclerViewAdapter itemAdapter;
    ArrayList<FollowersResponse.Result> followings = new ArrayList<>();
    LinearLayoutManager LayoutManager;
    String selecteduserId="", itemId="";
    public static boolean fromSold = false;
    private static final String TAG = DetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.soldto_layout);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        backbtn = (ImageView) findViewById(R.id.backbtn);
        listView = (RecyclerView) findViewById(R.id.listView);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
        title = (TextView) findViewById(R.id.title);
        nullLay = (LinearLayout) findViewById(R.id.nullLay);
        none = (TextView) findViewById(R.id.none);
        sold = (TextView) findViewById(R.id.sold);

        itemId = getIntent().getExtras().getString("item_id");


        Log.v(TAG,"item_id"+itemId);

        title.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
        nullLay.setVisibility(View.GONE);

        title.setText(getString(R.string.soldto));
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        none.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                changeSoldStatus("");
            }
        });
        sold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selecteduserId.equalsIgnoreCase("")){
                    Toast.makeText(getApplicationContext(), getString(R.string.select_user),Toast.LENGTH_SHORT).show();
                }else {
                    changeSoldStatus(selecteduserId);
                }
            }
        });

        try {
            LayoutManager = new LinearLayoutManager(this);
            listView.setLayoutManager(LayoutManager);

            itemAdapter = new RecyclerViewAdapter(followings);
            listView.setAdapter(itemAdapter);
            if (JoysaleApplication.isNetworkAvailable(this)) {
                listView.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
                getFollowings();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {

    }


    private void getFollowings() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_ITEM_ID, itemId);
            Log.v("Followuingsold","params"+map);
            Call<FollowersResponse> call = apiInterface.getSoldTo(map);
            call.enqueue(new Callback<FollowersResponse>() {
                @Override
                public void onResponse(Call<FollowersResponse> call, retrofit2.Response<FollowersResponse> response) {
                    Log.v("FollowersResponse", "="+response.toString());
                    ArrayList<HashMap<String, String>> temp = new ArrayList<HashMap<String, String>>();
                    if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        if (response.body().getResult() != null){
                            followings.addAll(response.body().getResult());
                        }
                    }
                    listView.setVisibility(View.VISIBLE);
                    progress.setVisibility(View.GONE);
                    itemAdapter.notifyDataSetChanged();
                    if (followings.size() == 0) {
                        progress.setVisibility(View.GONE);
                        nullLay.setVisibility(View.VISIBLE);
                    } else {
                        nullLay.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(Call<FollowersResponse> call, Throwable t) {
                    call.cancel();
                    Log.e("error", "onFailure:"+t.getMessage());
                }
            });
        }
    }

    /**
     * Adapter for Following user
     **/

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

        ArrayList<FollowersResponse.Result> followingsList;

        public RecyclerViewAdapter(ArrayList<FollowersResponse.Result> followingsList) {
            this.followingsList = followingsList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.followers_list, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final FollowersResponse.Result result = followingsList.get(position);

            Picasso.get().load(result.getUserImage()).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(holder.userImage);
            holder.userName.setText(result.getFullName());
            holder.location.setText(result.getUserName());
            holder.followStatus.setVisibility(View.GONE);

            if (result.getUserId().equals(selecteduserId)) {
                holder.tick.setVisibility(View.VISIBLE);
            } else {
                holder.tick.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return followingsList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView userImage, followStatus, tick;
            TextView userName, location;
            RelativeLayout mainLay;

            public MyViewHolder(View view) {
                super(view);
                userImage = (ImageView) view.findViewById(R.id.userImage);
                followStatus = (ImageView) view.findViewById(R.id.followStatus);
                userName = (TextView) view.findViewById(R.id.userName);
                location = (TextView) view.findViewById(R.id.location);
                mainLay = view.findViewById(R.id.mainLay);
                tick = (ImageView) view.findViewById(R.id.tick);

                mainLay.setOnClickListener(this);
                userImage.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.userImage:
                        Intent u = new Intent(SoldTo.this, ProfileActivity.class);
                        u.putExtra(Constants.TAG_USER_ID, followingsList.get(getAdapterPosition()).getUserId());
                        startActivity(u);
                        break;
                    case R.id.mainLay:
                        selecteduserId = followingsList.get(getAdapterPosition()).getUserId();
                        notifyDataSetChanged();
                        break;
                }
            }
        }
    }

    private void  changeSoldStatus(final String buyerid) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_VALUE, "1");
            map.put(Constants.TAG_ITEM_ID, itemId);
            if (!buyerid.isEmpty()) {
                map.put("buyer_id", buyerid);
            } else {
                map.put("buyer_id","");
            }
            Log.i(TAG, "changeSoldStatus: "+map);
            progressDialog.show();

            Call<HashMap<String, String>> call = apiInterface.changeSoldStatus(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    try {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }

                        String status = response.body().get(Constants.TAG_STATUS);
                        if (status.equalsIgnoreCase("true")) {
                            fromSold = true;
                            if (response.body().get(Constants.TAG_MESSAGE).equalsIgnoreCase("Item Status changed to Sold")) {
                                Toast.makeText(SoldTo.this, getString(R.string.item_status_changed_to_sold), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(SoldTo.this, getString(R.string.item_status_changed_to_available), Toast.LENGTH_LONG).show();
                            }
                            finish();
                        } else {
                            Toast.makeText(SoldTo.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
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
