package com.ni.addons.buynow;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ni.helper.NetworkReceiver;
import com.ni.SnatchItUp.JoysaleApplication;
import com.ni.SnatchItUp.ProfileActivity;
import com.ni.SnatchItUp.R;
import com.ni.model.ReviewResponse;
import com.ni.utils.ApiClient;
import com.ni.utils.ApiInterface;
import com.ni.utils.Constants;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;


/**
 * Created by hitasoft on 17/1/17.
 * <p>
 * This class is to Display Reviews List.
 */

public class Review extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = Review.class.getSimpleName();
    /**
     * Declare Layout Elements
     **/
    LinearLayout nullLay;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeLayout;

    LinearLayoutManager LayoutManager;
    public static RecyclerViewAdapter itemAdapter;
    ArrayList<ReviewResponse.Result> reviews = new ArrayList<>();

    /**
     * Declare Variables
     **/
    private String userId = "";
    public Context context;
    public static boolean flag = true;
    boolean loading = true, pulldown = false;
    int mPosition, previousTotal = 0, currentPage = 0, visibleThreshold = 5, firstVisibleItem, visibleItemCount, totalItemCount;
    ApiInterface apiInterface;

    public static Review newInstance(int position, String userId) {
        Review fragment = new Review();
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
        View v = inflater.inflate(R.layout.review, container, false);
        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        nullLay = (LinearLayout) v.findViewById(R.id.nullLay);
        swipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipeLayout);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        nullLay.setVisibility(View.GONE);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
                // code
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
                    initializeReview();
                    getReviews(currentPage);
                    loading = true;
                }
            }
        });

        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.progressColor));

        context = getActivity();

        loadData();

    }

    /**
     * Method for set adapter to recycler view
     **/

    private void setAdapter() {
        recyclerView.setHasFixedSize(true);
        LayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(LayoutManager);

        itemAdapter = new RecyclerViewAdapter(reviews);
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
            if (reviews.size() == 0) {
                try {
                    if (JoysaleApplication.isNetworkAvailable(context)) {
                        if (flag) {
                            initializeReview();
                            getReviews(0);
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
            initializeReview();
            getReviews(0);
        } else {
            swipeLayout.setRefreshing(false);
        }
    }

    /**
     * Method for get reviews list of user
     **/
    private void getReviews(final int pageCount) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            int offset = (pageCount * 20);
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, userId);
            map.put(Constants.TAG_OFFSET, Integer.toString(offset));
            map.put(Constants.TAG_LIMIT, "20");

            Call<ReviewResponse> call = apiInterface.getReview(map);
            call.enqueue(new Callback<ReviewResponse>() {
                @Override
                public void onResponse(Call<ReviewResponse> call, retrofit2.Response<ReviewResponse> response) {
                    if (pulldown) {
                        reviews.clear();
                    }

                    if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        reviews.addAll(response.body().getResult());
                    }
                    if (pulldown) {
                        pulldown = false;
                        loading = true;
                    }
                    recyclerView.setVisibility(View.VISIBLE);
                    swipeLayout.setRefreshing(false);
                    itemAdapter.notifyDataSetChanged();
                    if (reviews.size() == 0) {
                        nullLay.setVisibility(View.VISIBLE);
                    } else {
                        nullLay.setVisibility(View.GONE);
                    }
                    flag = true;
                }

                @Override
                public void onFailure(Call<ReviewResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void initializeReview() {
        nullLay.setVisibility(View.GONE);
        swipeRefresh();
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

        ArrayList<ReviewResponse.Result> reviewList;

        public RecyclerViewAdapter(ArrayList<ReviewResponse.Result> reviewList) {
            this.reviewList = reviewList;
        }

        @Override
        public RecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.review_list_items, parent, false);
            return new RecyclerViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerViewAdapter.MyViewHolder holder, int position) {
            final ReviewResponse.Result result = reviewList.get(position);

            Picasso.get().load(result.getUserImage())
                    .centerCrop()
                    .fit()
                    .placeholder(R.drawable.appicon)
                    .error(R.drawable.appicon)
                    .into(holder.userImage);

            holder.userName.setText(result.getFullName());
            holder.title.setText(result.getReviewTitle());
            holder.review.setText(result.getReviewDes());

            try {
                holder.ratingBar.setRating(Float.parseFloat(result.getRating()));
                long Date = Long.parseLong(result.getCreatedDate());
                holder.itemName.setText(result.getItemName() + " " + getString(R.string.on) + " " + JoysaleApplication.getDate(getActivity(), Date));
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            LayerDrawable stars = (LayerDrawable) holder.ratingBar.getProgressDrawable().getCurrent();
            stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.starColor), PorterDuff.Mode.SRC_ATOP);
            stars.getDrawable(0).setColorFilter(getResources().getColor(R.color.secondaryText), PorterDuff.Mode.SRC_ATOP);
            stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.starColor), PorterDuff.Mode.SRC_ATOP);
        }

        @Override
        public int getItemCount() {
            return reviewList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView userImage;
            TextView userName, itemName, title, review;
            RatingBar ratingBar;

            public MyViewHolder(View view) {
                super(view);
                userImage = (ImageView) view.findViewById(R.id.userImage);
                ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);
                userName = (TextView) view.findViewById(R.id.userName);
                itemName = (TextView) view.findViewById(R.id.itemName);
                title = (TextView) view.findViewById(R.id.title);
                review = (TextView) view.findViewById(R.id.review);

                userImage.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.userImage:
                        Intent u = new Intent(context, ProfileActivity.class);
                        u.putExtra(Constants.TAG_USER_ID, reviewList.get(getAdapterPosition()).getUserId());
                        startActivity(u);
                        break;
                }
            }
        }
    }

}
