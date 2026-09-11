package com.ni.SnatchItUp;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ni.helper.NetworkReceiver;
import com.ni.model.HomeItemResponse;
import com.ni.utils.ApiClient;
import com.ni.utils.ApiInterface;
import com.ni.utils.AppUtils;
import com.ni.utils.Constants;
import com.ni.utils.GetSet;
import com.ni.utils.PhotoUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;


/**
 * Created by hitasoft on 29/6/16.
 * <p>
 * This class is for Provide a List of Liked Products.
 */

public class LikedItems extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = LikedItems.class.getSimpleName();
    /**
     * Declare Layout Elements
     **/
    TextView userName;
    LinearLayout nullLay;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeLayout;

    public static RecyclerViewAdapter itemAdapter;
    public static ArrayList<HomeItemResponse.Item> likedItems = new ArrayList<>();
    GridLayoutManager LayoutManager;
    ArrayList<String> likedId = new ArrayList<String>();

    /**
     * Declare Variables
     **/
    public String userId = "";
    public Context context;
    public static boolean flag = true;
    int screenWidth, screenHeight, previousTotal = 0, visibleThreshold = 5, mPosition, currentPage = 0, firstVisibleItem, visibleItemCount, totalItemCount;
    boolean loading = true, pulldown = false;
    ApiInterface apiInterface;

    public static LikedItems newInstance(int position, String userId) {
        LikedItems fragment = new LikedItems();
        Bundle args = new Bundle();
        args.putInt(Constants.TAG_POSITION, position);
        args.putString(Constants.TAG_USERID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPosition = getArguments().getInt(Constants.TAG_POSITION);
        userId = getArguments().getString(Constants.TAG_USERID);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.my_listing, container, false);

        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        nullLay = (LinearLayout) v.findViewById(R.id.nullLay);
        swipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipeLayout);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                    visibleItemCount = recyclerView.getChildCount();
                    totalItemCount = LayoutManager.getItemCount();
                    firstVisibleItem = LayoutManager.findFirstVisibleItemPosition();

                    if (loading) {
                        if (totalItemCount > previousTotal) {
                            loading = false;
                            previousTotal = totalItemCount;
                            currentPage++;
                        }
                    }
                    if (!loading && (totalItemCount - visibleItemCount)
                            <= (firstVisibleItem + visibleThreshold)) {
                        // End has been reached
                        nullLay.setVisibility(View.GONE);
                        swipeRefresh();
                        getLikedItems(currentPage);
                        loading = true;
                    }
                }
            });
        }
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        nullLay.setVisibility(View.GONE);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
                   super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(final RecyclerView rv, final int dx, final int dy) {
                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = LayoutManager.getItemCount();
                firstVisibleItem = LayoutManager.findFirstVisibleItemPosition();

                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                        currentPage++;
                    }
                }
                if (!loading && (totalItemCount - visibleItemCount)
                        <= (firstVisibleItem + visibleThreshold)) {
                    // End has been reached
                    nullLay.setVisibility(View.GONE);
                    swipeRefresh();
                    getLikedItems(currentPage);
                    loading = true;
                }
            }
        });

        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.progressColor));

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        screenWidth = width / 2;
        screenHeight = width * 35 / 100;

        context = getActivity();
        likedItems.clear();

        //To get Liked Id from Api
//        getLikedId();

        //To get Liked Items from Api
        loadData();

    }

    //To initialize the adapter
    private void setAdapter() {
        recyclerView.setHasFixedSize(true);
        LayoutManager = new GridLayoutManager(context, 2);
        recyclerView.setLayoutManager(LayoutManager);

        itemAdapter = new RecyclerViewAdapter(likedItems);
        recyclerView.setAdapter(itemAdapter);
    }

    private void swipeRefresh() {
        swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
            }
        });
    }

    private void loadData() {
        try {
            if (likedItems.size() == 0) {
                try {
                    if (JoysaleApplication.isNetworkAvailable(context)) {
                        if (flag) {
                            nullLay.setVisibility(View.GONE);
                            swipeRefresh();
                            getLikedItems(0);
                            flag = false;
                        }
                    }
                    setAdapter();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                setAdapter();
                nullLay.setVisibility(View.GONE);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRefresh() {
        if (!pulldown) {
            currentPage = 0;
            previousTotal = 0;
            pulldown = true;
//            getLikedId();
            nullLay.setVisibility(View.GONE);
            swipeRefresh();
            getLikedItems(0);
        } else {
            swipeLayout.setRefreshing(false);
        }
    }

    /**
     * Function for get the list of liked items
     **/
    private void getLikedItems(final int pageCount) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            int offset = (pageCount * 20);
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_TYPE, "liked");
            map.put(Constants.TAG_USERID, GetSet.getUserId() != null ? GetSet.getUserId() : "");
            map.put(Constants.TAG_OFFSET, Integer.toString(offset));
            map.put(Constants.TAG_SELLERID, userId);
            map.put(Constants.TAG_LIMIT, "20");
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(context));
            Log.d(TAG, "onResponse: "+map);
            Call<HomeItemResponse> call = apiInterface.getHomeItems(map);
            call.enqueue(new Callback<HomeItemResponse>() {
                @Override
                public void onResponse(Call<HomeItemResponse> call, retrofit2.Response<HomeItemResponse> response) {
                    if (pulldown) {
                        likedItems.clear();
                    }
                    if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        likedItems.clear();
                        likedItems.addAll(response.body().getResult().getItems());
                    }
                    if (pulldown) {
                        pulldown = false;
                        loading = true;
                    }
                    recyclerView.setVisibility(View.VISIBLE);
                    swipeLayout.setRefreshing(false);
                    itemAdapter.notifyDataSetChanged();
                    if (likedItems.size() == 0) {
                        nullLay.setVisibility(View.VISIBLE);
                    } else {
                        nullLay.setVisibility(View.GONE);
                    }
                    flag = true;
                }

                @Override
                public void onFailure(Call<HomeItemResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    /**
     * Function for the liked item ids
     **/
    private void getLikedId() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());

            Call<ResponseBody> call = apiInterface.getLikedId(map);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                    JSONObject res = null;
                    try {
                        res = new JSONObject(response.body().string());
                        if (res.getString(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                            JSONArray jsonArray = res.optJSONArray(Constants.TAG_RESULT);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                likedId.add(jsonArray.getString(i));
                            }
                        }else if (res.getString(Constants.TAG_STATUS).equalsIgnoreCase(Constants.TAG_FALSE_CODE)){
                            JoysaleApplication.disabledialog(context, "" + res.getString(Constants.TAG_MESSAGE), GetSet.getUserId());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }






    /**
     * Adapter for Liked Product Adapter
     */
    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

        ArrayList<HomeItemResponse.Item> likedItemsList;

        public RecyclerViewAdapter(ArrayList<HomeItemResponse.Item> likedItemsList) {
            this.likedItemsList = likedItemsList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.mylisting_list_items, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            HomeItemResponse.Item item = likedItemsList.get(position);
            PhotoUtils.loadListImage(holder.singleImage,
                    PhotoUtils.getFirstListThumbUrl(item),
                    screenWidth, R.drawable.image_placeholder);
            if (item.getItemStatus().equalsIgnoreCase("sold")) {
                holder.productType.setVisibility(View.VISIBLE);
                holder.productType.setText(getString(R.string.sold));
                holder.productType.setBackground(getResources().getDrawable(R.drawable.soldbg));
            } else {
                if (Constants.PROMOTION) {
                    if (item.getPromotionType().equalsIgnoreCase("Ad")) {
                        holder.productType.setVisibility(View.VISIBLE);
                        holder.productType.setText(getString(R.string.ad));
                        holder.productType.setBackground(getResources().getDrawable(R.drawable.adbg));
                    } else if (item.getPromotionType().equalsIgnoreCase("Urgent")) {
                        holder.productType.setVisibility(View.VISIBLE);
                        holder.productType.setText(getString(R.string.urgent));
                        holder.productType.setBackground(getResources().getDrawable(R.drawable.urgentbg));
                    } else {
                        holder.productType.setVisibility(View.GONE);
                    }
                } else {
                    holder.productType.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return likedItemsList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView singleImage;
            TextView productType;

            public MyViewHolder(View view) {
                super(view);
                singleImage = (ImageView) view.findViewById(R.id.singleImage);
                productType = (TextView) view.findViewById(R.id.productType);

                singleImage.getLayoutParams().width = screenWidth;
                /*Rectangle Image*/
//                singleImage.getLayoutParams().height = screenHeight;
                /*Square Image*/
                singleImage.getLayoutParams().height = screenWidth - JoysaleApplication.dpToPx(context, 15);

                singleImage.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.singleImage:
//                        if (likedId.contains(likedItems.get(getAdapterPosition()).get(Constants.TAG_ID))) {
//                            likedItems.get(getAdapterPosition()).put(Constants.TAG_LIKED, "yes");
//                        } else {
//                            likedItems.get(getAdapterPosition()).put(Constants.TAG_LIKED, "no");
//                        }

                        Intent i = new Intent(context,
                                DetailActivity.class);
                        i.putExtra(Constants.TAG_DATA, likedItems.get(getAdapterPosition()));
                        i.putExtra(Constants.TAG_POSITION, getAdapterPosition());
                        i.putExtra(Constants.TAG_FROM, "liked");
                        startActivity(i);
                        break;
                }
            }
        }
    }
}
