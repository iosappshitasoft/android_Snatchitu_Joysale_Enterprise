package com.ni.addons.buynow;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ni.external.FragmentChangeListener;
import com.ni.helper.NetworkReceiver;
import com.ni.SnatchItUp.JoysaleApplication;
import com.ni.SnatchItUp.R;
import com.ni.model.MySalesResponse;
import com.ni.utils.ApiClient;
import com.ni.utils.ApiInterface;
import com.ni.utils.AppUtils;
import com.ni.utils.Constants;
import com.ni.utils.GetSet;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by hitasoft on 14/9/16.
 * <p>
 * This class is to Display an Orders List.
 */

public class MyOrder extends Fragment
        implements FragmentChangeListener, AbsListView.OnScrollListener, SwipeRefreshLayout.OnRefreshListener {

    /**
     * Declare Layout Elements
     **/
    ListView mListView;
    LinearLayout nullLay;
    AVLoadingIndicatorView progress;
    SwipeRefreshLayout swipeLayout;

    public Context context;
    public static OrderAdapter orderAdapter;
    public static ArrayList<MySalesResponse.Result> Orderary = new ArrayList<>();
    Boolean isUserdeleted = false;

    /**
     * Declare Variables
     **/
    static final String TAG = "MyOrder";
    public static int padding, topPadding, leftPadding;
    boolean pulldown = false, loading = true;
    int visibleThreshold = 0, previousTotal = 0, currentPage = 0;
    private ApiInterface apiInterface;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");
        return inflater.inflate(R.layout.exchangefragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        mListView = (ListView) getView().findViewById(R.id.listView);
        progress = (AVLoadingIndicatorView) getView().findViewById(R.id.progress);
        nullLay = (LinearLayout) getView().findViewById(R.id.nullLay);
        swipeLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeLayout);

        context = getActivity();
        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.progressColor));
        swipeLayout.setOnRefreshListener(this);

        padding = JoysaleApplication.dpToPx(getActivity(), 10);
        topPadding = JoysaleApplication.dpToPx(getActivity(), 10);
        leftPadding = JoysaleApplication.dpToPx(getActivity(), 15);

        mListView.setDivider(null);
        mListView.setDividerHeight(padding);

        Orderary.clear();

        initializeOrder();

        // To get incoming exchanges
        getOrder(0);

        // To initialize and set Adpater
        orderAdapter = new OrderAdapter(getActivity(), Orderary);
        mListView.setAdapter(orderAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!Orderary.get(position).getUserstatus().equalsIgnoreCase("2")) {
                    Intent i = new Intent(getActivity(), OrderDetail.class);
                    i.putExtra("data", Orderary.get(position));
                    i.putExtra(Constants.TAG_FROM, "order");
                    i.putExtra(Constants.TAG_POSITION, position);
                    startActivity(i);
                } else {
                    openDeleteDialog();
                }
            }
        });

        mListView.setOnScrollListener(this);

    }

    @Override
    public void onCentered() {

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem == 0) {
            swipeLayout.setEnabled(true);
        } else {
            swipeLayout.setEnabled(false);
        }

        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
                currentPage++;
            }
        }

        if (!loading
                && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
            // I load the next page of thumbnails using a background task,
            if (currentPage != 0) {
                initializeOrder();
                getOrder(currentPage);
                loading = true;
            }
        }
    }

    @Override
    public void onRefresh() {
        if (!pulldown) {
            pulldown = true;
            currentPage = 0;
            if (JoysaleApplication.isNetworkAvailable(getActivity())) {
                initializeOrder();
                getOrder(0);
            } else {
                swipeLayout.setRefreshing(false);
            }
        } else {
            swipeLayout.setRefreshing(false);
        }
    }

    private void swipeRefresh() {
        swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
            }
        });
    }

    /**
     * Method for get the incoming exchanges
     **/

    private void getOrder(final int pageCount) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            int offset = (pageCount * 20);
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_OFFSET, String.valueOf(offset));
            map.put(Constants.TAG_LIMIT, "20");
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(context));

            Log.d(TAG, "getOrder: " + map);

            Call<MySalesResponse> call = apiInterface.getMyOrders(map);
            call.enqueue(new Callback<MySalesResponse>() {
                @Override
                public void onResponse(Call<MySalesResponse> call, retrofit2.Response<MySalesResponse> response) {

                    if (pulldown) {
                        Orderary.clear();
                    }
                    MySalesResponse mySalesResponse = response.body();
                    if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        List<MySalesResponse.Result> list = response.body().getResult();
                        /*
                         * if (commentObj.getUserstatus()!=null) {
                         * if (commentObj.getUserstatus().equalsIgnoreCase("2")) {
                         * isUserdeleted = true;
                         * } else {
                         * isUserdeleted = false;
                         * }
                         * }
                         */
                        if (response.body().getMessage() == null) {
                            Orderary.addAll(response.body().getResult());
                        }

                    }

                    progress.setVisibility(View.GONE);

                    if (pulldown) {
                        pulldown = false;
                        loading = true;
                        previousTotal = 0;
                    }

                    swipeLayout.setRefreshing(false);
                    mListView.setVisibility(View.VISIBLE);
                    orderAdapter.notifyDataSetChanged();
                    Log.d(TAG, "onResponse: " + Orderary.toString());
                    if (Orderary.size() == 0) {
                        nullLay.setVisibility(View.VISIBLE);
                    } else {
                        nullLay.setVisibility(View.INVISIBLE);
                    }
                }

                @Override
                public void onFailure(Call<MySalesResponse> call, Throwable t) {
                    progress.setVisibility(View.GONE);
                }
            });
        } else {
            progress.setVisibility(View.GONE);
        }
    }

    /**
     * Method for Order Initialization
     **/

    private void initializeOrder() {
        nullLay.setVisibility(View.INVISIBLE);
        if (pulldown) {
            mListView.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
        } else if (Orderary.size() > 0) {
            mListView.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
            swipeRefresh();
        } else {
            mListView.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Adapter for Orders
     **/

    public class OrderAdapter extends BaseAdapter {
        ArrayList<MySalesResponse.Result> Items;
        ViewHolder holder = null;
        String date;
        Context mContext;

        public OrderAdapter(Context ctx, ArrayList<MySalesResponse.Result> data) {
            mContext = ctx;
            Items = data;
        }

        @Override
        public int getCount() {
            return Items.size();
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
                convertView = inflater.inflate(R.layout.orderitem, parent, false);// layout
                holder = new ViewHolder();

                holder.myitemImage = (ImageView) convertView.findViewById(R.id.myitemImage);
                holder.itemname = (TextView) convertView.findViewById(R.id.itemname);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.orderstatus = (TextView) convertView.findViewById(R.id.orderstatus);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            try {
                final MySalesResponse.Result result = Items.get(position);

                if (result.getStatus().equalsIgnoreCase("shipped")) {
                    holder.orderstatus.setVisibility(View.VISIBLE);
                    holder.orderstatus.setText(getString(R.string.item_shipped));
                    holder.orderstatus.setBackground(getResources().getDrawable(R.drawable.orange_round_corner));
                    if (result.getSaledate() != null && !result.getSaledate().equals("")) {
                        long ordDate = Long.parseLong(result.getSaledate());
                        date = JoysaleApplication.getDate(getActivity(), ordDate);
                    }
                    holder.time.setText(getString(R.string.order_on) + " " + date);
                } else if (result.getStatus().equalsIgnoreCase("delivered") ||
                        result.getStatus().equalsIgnoreCase("paid")) {
                    holder.orderstatus.setVisibility(View.VISIBLE);
                    holder.orderstatus.setText(getString(R.string.item_delivered));
                    holder.orderstatus.setBackground(getResources().getDrawable(R.drawable.round_corner_primary));
                    if (result.getSaledate() != null && !result.getSaledate().equals("")) {
                        long ordDate = Long.parseLong(result.getSaledate());
                        date = JoysaleApplication.getDate(getActivity(), ordDate);
                    }
                    holder.time.setText(getString(R.string.order_on) + " " + date);

                } else if (result.getStatus().equalsIgnoreCase("claimed")) {
                    holder.orderstatus.setVisibility(View.VISIBLE);
                    holder.orderstatus.setText(getString(R.string.claimed));
                    holder.orderstatus.setBackground(getResources().getDrawable(R.drawable.round_corner_primary));
                    if (result.getSaledate() != null && !result.getSaledate().equals("")) {
                        long ordDate = Long.parseLong(result.getSaledate());
                        date = JoysaleApplication.getDate(getActivity(), ordDate);
                    }
                    holder.time.setText(getString(R.string.order_on) + " " + date);

                } else if (result.getStatus().equalsIgnoreCase("cancelled")) {
                    holder.orderstatus.setVisibility(View.VISIBLE);
                    holder.orderstatus.setText(getString(R.string.order_canceled));
                    holder.orderstatus.setBackground(getResources().getDrawable(R.drawable.red_round_corner));
                    if (result.getSaledate() != null && !result.getSaledate().equals("")) {
                        long ordDate = Long.parseLong(result.getSaledate());
                        date = JoysaleApplication.getDate(getActivity(), ordDate);
                    }
                    holder.time.setText(getString(R.string.order_on) + " " + date);

                } else if (result.getStatus().equalsIgnoreCase("processing")) {
                    holder.orderstatus.setVisibility(View.VISIBLE);
                    holder.orderstatus.setText(getString(R.string.under_processing));
                    holder.orderstatus.setBackground(getResources().getDrawable(R.drawable.dark_round_corner));
                    if (result.getSaledate() != null && !result.getSaledate().equals("")) {
                        long ordDate = Long.parseLong(result.getSaledate());
                        date = JoysaleApplication.getDate(getActivity(), ordDate);
                    }
                    holder.time.setText(getString(R.string.order_on) + " " + date);

                } else if (result.getStatus().equalsIgnoreCase("pending")) {
                    holder.orderstatus.setVisibility(View.VISIBLE);
                    holder.orderstatus.setText(getString(R.string.pending));
                    holder.orderstatus.setBackground(getResources().getDrawable(R.drawable.dark_round_corner));
                    if (result.getSaledate() != null && !result.getSaledate().equals("")) {
                        long ordDate = Long.parseLong(result.getSaledate());
                        date = JoysaleApplication.getDate(getActivity(), ordDate);
                    }
                    holder.time.setText(getString(R.string.order_on) + " " + date);

                } else {
                    holder.orderstatus.setVisibility(View.GONE);
                }

                holder.orderstatus.setPadding(leftPadding, topPadding, leftPadding, topPadding);
                holder.itemname.setText(JoysaleApplication.stripHtml(result.getOrderitems().getItemname()));
                Picasso.get().load(result.getOrderitems().getOrderImage()).into(holder.myitemImage);

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }

        class ViewHolder {
            ImageView myitemImage;
            TextView itemname, orderstatus, time;
        }
    }

    private void openDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context,
                com.google.android.material.R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        builder.setMessage(getString(R.string.account_delete));
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        Typeface typeface = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeface = getResources().getFont(R.font.font_regular);
        } else {
            typeface = ResourcesCompat.getFont(context, R.font.font_regular);
        }
        TextView textView = dialog.findViewById(android.R.id.message);
        textView.setTypeface(typeface);

        Button btn1 = dialog.findViewById(android.R.id.button1);
        btn1.setTypeface(typeface);

        Button btn2 = dialog.findViewById(android.R.id.button2);
        btn2.setTypeface(typeface);

    }
}