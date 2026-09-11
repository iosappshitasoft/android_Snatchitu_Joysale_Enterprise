package com.ni.addons.buynow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.ni.SnatchItUp.BaseActivity;
import com.ni.SnatchItUp.FragmentMainActivity;
import com.ni.SnatchItUp.JoysaleApplication;
import com.ni.SnatchItUp.R;
import com.ni.utils.Constants;

/**
 * Created by hitasoft on 14/9/16.
 * <p>
 * This class is to Display Orders and Sales in a Tab view.
 */

public class MySalesnOrder extends BaseActivity implements View.OnClickListener {
    /**
     * Declare Layout Elements
     **/
    static TabLayout slidingTabLayout;
    ViewPager mViewPager;
    TextView title;
    ImageView backBtn;
    ViewPagerAdapter mAdapter;

    /**
     * Declare Variables
     **/
    int mNumFragments = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.salesnorder);

        backBtn = (ImageView) findViewById(R.id.backbtn);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        slidingTabLayout = (TabLayout) findViewById(R.id.slideTab);
        title = (TextView) findViewById(R.id.title);

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        title.setText(getString(R.string.myorders_sales));
        applySystemBarInsets(findViewById(R.id.actionbar), mViewPager);

        backBtn.setOnClickListener(this);

        //To initialize Adapter
        setupAdapter();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(MySalesnOrder.this, isConnected);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getIntent().hasExtra(Constants.TAG_FROM)) {
            FragmentMainActivity.homeItemList.clear();
            if (FragmentMainActivity.itemAdapter != null) {
                FragmentMainActivity.itemAdapter.notifyDataSetChanged();
            }
            Intent i = new Intent(MySalesnOrder.this, FragmentMainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(i);
            finish();
        } else {
            finish();
        }
    }

    //To initialize and set Adapter in Viewpager
    public void setupAdapter() {
        CharSequence titles[] = {getString(R.string.myorders), getString(R.string.mysales)};
        mAdapter = new ViewPagerAdapter(getSupportFragmentManager(), titles, mNumFragments);
        mViewPager.setAdapter(mAdapter);
        slidingTabLayout.setupWithViewPager(mViewPager);
    }

    public static class ViewPagerAdapter extends FragmentStatePagerAdapter {
        CharSequence titles[];
        int numbOfTabs;

        public ViewPagerAdapter(FragmentManager fm, CharSequence titles[], int noOfTabs) {
            super(fm);
            this.titles = titles;
            this.numbOfTabs = noOfTabs;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new MyOrder();
            } else if (position == 1) {
                return new MySale();
            } else {
                return null;
            }
        }

        @Override
        public int getCount() {
            return numbOfTabs;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }

    @Override
    public void onClick(View v) {
        if (getIntent().hasExtra(Constants.TAG_FROM)) {
            FragmentMainActivity.homeItemList.clear();
            if (FragmentMainActivity.itemAdapter != null) {
                FragmentMainActivity.itemAdapter.notifyDataSetChanged();
            }
            Intent i = new Intent(MySalesnOrder.this, FragmentMainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(i);
            finish();
        } else {
            finish();
        }
    }
}

