package com.ni.SnatchItUp;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;
import static android.text.util.Linkify.WEB_URLS;
import static android.view.View.INVISIBLE;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.ni.SnatchItUp.AddProductDetail.linkifyHtml;
import static com.ni.SnatchItUp.FragmentMainActivity.REQUEST_CAMERA_PERMISSION;
import static com.ni.SnatchItUp.FragmentMainActivity.homeItemList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.text.Html;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.ads.AdView;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.google.gson.Gson;
import com.ni.addons.BannerAdUtils;
import com.ni.addons.buynow.AddAddress;
import com.ni.addons.buynow.Addresses;
import com.ni.addons.buynow.Checkout;
import com.ni.addons.soldto.SoldTo;
import com.ni.external.CustomFontHelper;
import com.ni.external.TimeAgo;
import com.ni.external.URLSpanNoUnderline;
import com.ni.external.parallaxscroll.ParallaxScrollView;
import com.ni.helper.LocaleManager;
import com.ni.helper.NetworkReceiver;
import com.ni.helper.location.LocationHelper;
import com.ni.model.AffiliateItemsResponse;
import com.ni.model.BeforeAddResponse;
import com.ni.model.CheckPromotionResponse;
import com.ni.model.FollowersResponse;
import com.ni.model.HomeItemResponse;
import com.ni.model.MyPromotionResponse;
import com.ni.model.ShippingAddressRes;
import com.ni.utils.ApiClient;
import com.ni.utils.ApiInterface;
import com.ni.utils.AppUtils;
import com.ni.utils.Constants;
import com.ni.utils.DiscountUtils;
import com.ni.utils.PhotoUtils;
import com.ni.utils.GetSet;
//import com.mapbox.api.staticmap.v1.MapboxStaticMap;
//import com.mapbox.api.staticmap.v1.StaticMapCriteria;
//import com.mapbox.geojson.Point;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.wang.avi.AVLoadingIndicatorView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by hitasoft
 * <p>
 * This class is for Display Details of a Product
 */

public class DetailActivity extends BaseActivity implements OnClickListener, ParallaxScrollView.OnScrollViewListener {

    private static final String TAG = DetailActivity.class.getSimpleName();
    /**
     * Declare Layout Elements
     **/
    ViewPager viewPager;
    RecyclerView userRecyclerView;
    RecyclerView relatedRecycleView;
    Target target;
    ParallaxScrollView sview;
    DisplayMetrics displaymetrics;
    RelativeLayout actionbar, main, reviewLay;
    LinearLayout commentLay, detailLay, youtubeLay, imageCountLay, filterLay, promoCodeLay;
    AVLoadingIndicatorView progress;
    RatingBar ratingBar;
    ProgressDialog progressDialog;
    public TextView title, itemPrice, originalPrice, likeCount, userName, itemStatus, postedTime, viewCount,
            moreItems, location, chat, titleText, call, ratingCount, txtImageCount, offer,
            txtRelatedProducts, itemCond, commentCount, txtDetails, txtDescription, discountRibbon,
            discountedLabel, promoCodeText, promoCopyBtn, promoCodeTitle;
    ImageView backBtn, shareBtn, settingBtn, mblVerify, fbVerify, mailVerify, userImg, map, likeImg, edit;
    ImageView btnFacebook, btnTwitter, btnWhatsapp, btnMore, locationImg;
    View detailsDivider;

    /**
     * Declare Varaibles
     **/
    public static boolean fromEdit = false;
    public boolean fromCall = false, isSeller = false;
    int screenWidth, productHeight, listHeight, screenheight, position, screenHalf;
    String chatId = "", from, shopaddress, productLikecount;
    boolean chatClicked = false;
    private HomeItemResponse.Item itemMap = new HomeItemResponse().new Item();
    private ArrayList<HomeItemResponse.Item> MoreItems = new ArrayList<>();
    private List<HomeItemResponse.Item> userItemList = new ArrayList<>();
    ArrayList<String> resizedImageList = new ArrayList<String>();
    ArrayList<String> originalImageList = new ArrayList<String>();
    ItemAdapter itemAdapter;
    RelatedAdapter relatedAdapter;
    ViewPagerAdapter pagerAdapter;
    ApiInterface apiInterface;
    private boolean isTitleVisibility = false;
    private NumberFormat numberFormat;
    AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_main_layout);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        numberFormat = NumberFormat.getInstance(new Locale("en", "US"));

        title = (TextView) findViewById(R.id.title);
        itemPrice = (TextView) findViewById(R.id.itemPrice);
        originalPrice = (TextView) findViewById(R.id.originalPrice);
        discountedLabel = (TextView) findViewById(R.id.discountedLabel);
        discountRibbon = (TextView) findViewById(R.id.discountRibbon);
        promoCodeLay = (LinearLayout) findViewById(R.id.promoCodeLay);
        promoCodeTitle = (TextView) findViewById(R.id.promoCodeTitle);
        promoCodeText = (TextView) findViewById(R.id.promoCodeText);
        promoCopyBtn = (TextView) findViewById(R.id.promoCopyBtn);
        itemCond = (TextView) findViewById(R.id.itemCond);
        likeCount = (TextView) findViewById(R.id.likesCount);
        commentCount = (TextView) findViewById(R.id.commentCount);
        userName = (TextView) findViewById(R.id.userName);
        txtDescription = findViewById(R.id.txtDescription);
        filterLay = findViewById(R.id.filterLay);
        txtDetails = findViewById(R.id.txtDetails);
        detailsDivider = findViewById(R.id.detailsDivider);
        postedTime = (TextView) findViewById(R.id.postedTime);
        viewCount = (TextView) findViewById(R.id.viewCount);
        chat = (TextView) findViewById(R.id.chat);
        offer = (TextView) findViewById(R.id.offer);
        backBtn = (ImageView) findViewById(R.id.backbtn);
        shareBtn = (ImageView) findViewById(R.id.shareBtn);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        userImg = (ImageView) findViewById(R.id.userImage);
        actionbar = (RelativeLayout) findViewById(R.id.actionbar);
        userRecyclerView = findViewById(R.id.userRecyclerView);
        relatedRecycleView = findViewById(R.id.relatedRecycleView);
        sview = (ParallaxScrollView) findViewById(R.id.scrollView);
        moreItems = (TextView) findViewById(R.id.moretext);
        main = (RelativeLayout) findViewById(R.id.main);
        txtImageCount = findViewById(R.id.txtImageCount);
        location = (TextView) findViewById(R.id.location);
        itemStatus = (TextView) findViewById(R.id.itemStatus);
        map = (ImageView) findViewById(R.id.banner);
        commentLay = (LinearLayout) findViewById(R.id.commentLay);
        settingBtn = (ImageView) findViewById(R.id.settingBtn);
        likeImg = (ImageView) findViewById(R.id.likereditBtn);
        edit = (ImageView) findViewById(R.id.edit);
        mblVerify = (ImageView) findViewById(R.id.mblverify);
        fbVerify = (ImageView) findViewById(R.id.fbverify);
        mailVerify = (ImageView) findViewById(R.id.mailverify);
        detailLay = (LinearLayout) findViewById(R.id.detailLay);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
        titleText = (TextView) findViewById(R.id.title_text);
        call = (TextView) findViewById(R.id.call);
        adView = findViewById(R.id.descriptionAdView);
        if (adView == null) {
            adView = findViewById(R.id.adView);
        }

        //Addon BuyNow
        reviewLay = (RelativeLayout) findViewById(R.id.reviewLay);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingCount = (TextView) findViewById(R.id.ratingCount);


        txtRelatedProducts = findViewById(R.id.txtRelatedProducts);
        btnFacebook = findViewById(R.id.btnFacebook);
        btnTwitter = findViewById(R.id.btnTwitter);
        btnWhatsapp = findViewById(R.id.btnWhatsapp);
        btnMore = findViewById(R.id.btnMore);
        youtubeLay = findViewById(R.id.youtubeLay);
        imageCountLay = findViewById(R.id.imageCountLay);
        locationImg = findViewById(R.id.locationImg);
        progressDialog = new ProgressDialog(DetailActivity.this, R.style.AppCompatAlertDialogStyle);

        actionbar.bringToFront();

        MoreItems = new ArrayList<>();

        //email enable/disable
        if (!Constants.pref.getBoolean(Constants.PREF_EMAILVERIFICATION, Constants.EMAILVERIFICATION)) {
            mailVerify.setVisibility(View.GONE);
        } else {
            mailVerify.setVisibility(View.VISIBLE);
        }

        //Addon Banner Ad - load near description
        if (adView != null) {
            BannerAdUtils.loadBannerAd(this, adView);
        }

        backBtn.setOnClickListener(this);
        shareBtn.setOnClickListener(this);
        sview.setOnScrollViewListener(this);
        commentCount.setOnClickListener(this);
        likeCount.setOnClickListener(this);
        userImg.setOnClickListener(this);
        commentLay.setOnClickListener(this);
        settingBtn.setOnClickListener(this);
        offer.setOnClickListener(this);
        chat.setOnClickListener(this);
        likeImg.setOnClickListener(this);
        edit.setOnClickListener(this);
        detailLay.setOnClickListener(null);
        call.setOnClickListener(this);
        btnFacebook.setOnClickListener(this);
        btnTwitter.setOnClickListener(this);
        btnWhatsapp.setOnClickListener(this);
        btnMore.setOnClickListener(this);
        youtubeLay.setOnClickListener(this);
        locationImg.setOnClickListener(this);
        if (promoCopyBtn != null) {
            promoCopyBtn.setOnClickListener(this);
        }

        applyRtlUi();


        position = getIntent().getIntExtra(Constants.TAG_POSITION, 0);
        from = getIntent().getStringExtra(Constants.TAG_FROM);
        itemMap = (HomeItemResponse.Item) getIntent().getExtras().get(Constants.TAG_DATA);
        Log.d(TAG, "onCreateItem: " + itemMap.getItemConditionId());
        Log.d(TAG, "onCreateItem: " + itemMap.getItemCondition());
        productLikecount = itemMap.getLikesCount();

        displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;
        screenheight = displaymetrics.heightPixels;
        screenWidth = displaymetrics.widthPixels;
        listHeight = width * 75 / 100;
        screenHalf = width / 2;
        productHeight = displaymetrics.heightPixels * 45 / 100;

        ViewGroup.LayoutParams params = viewPager.getLayoutParams();
        params.height = productHeight;
        viewPager.requestLayout();

        //Addon BuyNow
        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable().getCurrent();
        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.starColor), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(0).setColorFilter(getResources().getColor(R.color.secondaryText), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.starColor), PorterDuff.Mode.SRC_ATOP);

        FragmentMainActivity.startAlphaAnimation(titleText, 0, INVISIBLE);
        setData();

        progress.setVisibility(View.VISIBLE);

        userItemList = new ArrayList<>();
        relatedAdapter = new RelatedAdapter(DetailActivity.this, userItemList);
        boolean rtlLists = LocaleManager.isRTL(this);
        relatedRecycleView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, rtlLists));
        relatedRecycleView.setAdapter(relatedAdapter);
        relatedAdapter.notifyDataSetChanged();

        itemAdapter = new ItemAdapter(DetailActivity.this, MoreItems);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, rtlLists));
        userRecyclerView.setAdapter(itemAdapter);

        getImageAry();
        checkUser();
        updateView();

        if (itemMap != null && itemMap.isAffiliateItem()) {
            boolean needsPhotos = PhotoUtils.getFirstDisplayUrl(itemMap) == null;
            String affiliateUrl = itemMap.getAffiliateUrl();
            if (needsPhotos || affiliateUrl == null || affiliateUrl.trim().isEmpty()) {
                loadAffiliateItem();
            }
        } else if (itemMap != null && itemMap.hasDiscount()
                && PhotoUtils.getFirstDisplayUrl(itemMap) == null) {
            loadAffiliateItem();
        }

        viewPager.addOnPageChangeListener(mOnPageChangeListener);

    }

    /**
     * set uploaded item exchangeData to elements
     **/


    private void setData() {
        if (itemMap.getItemCondition() != null && !itemMap.getItemCondition().equals("0")) {
            itemCond.setVisibility(View.VISIBLE);
            itemCond.setText(itemMap.getItemCondition());
        } else {
            itemCond.setVisibility(View.GONE);
        }
        String itemTitle = JoysaleApplication.stripHtml(itemMap.getItemTitle().trim());
        title.setText(itemTitle);
        titleText.setText(itemTitle);
        String curren = itemMap.getCurrencyCode();
        if (itemMap.getPrice().equals("0")) {
            itemPrice.setText(getString(R.string.giving_away));
            itemPrice.setTextColor(getResources().getColor(R.color.primary));
            if (originalPrice != null) {
                originalPrice.setVisibility(View.GONE);
            }
            if (discountRibbon != null) {
                discountRibbon.setVisibility(View.GONE);
            }
        } else if (itemMap.hasDiscount()) {
            String discountPercent = itemMap.getEffectiveDiscountPercent();
            try {
                double original = Double.parseDouble(itemMap.getPrice());
                double percent = Double.parseDouble(discountPercent);
                double discounted = original - (original * percent / 100d);
                itemPrice.setText(DiscountUtils.formatDiscountPrice(itemMap, discounted));
                itemPrice.setTextColor(getResources().getColor(R.color.primaryText));
                if (originalPrice != null) {
                    originalPrice.setVisibility(View.VISIBLE);
                    originalPrice.setText(itemMap.getFormattedPrice());
                    originalPrice.setPaintFlags(originalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
                if (discountRibbon != null) {
                    discountRibbon.setText(getString(R.string.discount_off_format,
                            DiscountUtils.cleanPercent(discountPercent)));
                    applyDiscountRibbonLayout();
                    discountRibbon.setVisibility(View.VISIBLE);
                }
            } catch (NumberFormatException e) {
                itemPrice.setText(itemMap.getFormattedPrice());
                if (originalPrice != null) {
                    originalPrice.setVisibility(View.GONE);
                }
                if (discountRibbon != null) {
                    discountRibbon.setVisibility(View.GONE);
                }
            }
        } else {
            /*if (curren.contains("-")) {
                String cur[] = curren.split("-");
                curren = cur[0];
                if (curren != null) {
                    itemPrice.setText(curren + " " + numberFormat.format(Double.parseDouble(itemMap.getPrice())));
                }
            } else {
                itemPrice.setText(itemMap.getCurrencyCode() + numberFormat.format(Double.parseDouble(itemMap.getPrice())));
            }*/
            itemPrice.setText(itemMap.getFormattedPrice());
            itemPrice.setTextColor(getResources().getColor(R.color.primaryText));
            if (originalPrice != null) {
                originalPrice.setVisibility(View.GONE);
            }
            if (discountRibbon != null) {
                discountRibbon.setVisibility(View.GONE);
            }
        }

        if (discountedLabel != null) {
            if (!"".equals(itemMap.getPromoCode()) && !"".equals(itemMap.getDiscountPercentage())) {
                discountedLabel.setVisibility(View.VISIBLE);
                discountedLabel.setText(getString(R.string.discounted));
                discountedLabel.setTextColor(getResources().getColor(R.color.primary));
            } else {
                discountedLabel.setVisibility(View.GONE);
            }
        }

        setupPromoCodeSection();

        if (itemMap.getYoutubeLink() != null && !itemMap.getYoutubeLink().equals("")) {
            youtubeLay.setVisibility(View.VISIBLE);
        }
        //     Spannable spannedText = (Spannable) Html.fromHtml(itemMap.getItemDescription(), null, new MyTagHandler());
      Log.e(TAG,"comments count : " + itemMap.getCommentsCount());
        likeCount.setText((itemMap.getLikesCount() != null ? itemMap.getLikesCount() : "0") + " " + getResources().getString(R.string.likes));
        commentCount.setText((itemMap.getCommentsCount() != null ? itemMap.getCommentsCount() : "0") + " " + getResources().getString(R.string.comments));
        userName.setText("" + itemMap.getSellerName());
        txtDescription.setText(HtmlCompat.fromHtml(itemMap.getItemDescription().trim(), 0));
        linkifyHtml(itemMap.getItemDescription().trim(), WEB_URLS);

        txtDescription.setMovementMethod(LinkMovementMethod.getInstance());
        //   stripUnderlines(txtDescription);
        //txtDescription.loadDataWithBaseURL(null, itemMap.getItemDescription(), "text/html", "utf-8", null);
        setFilterData();

        viewCount.setText((itemMap.getViewsCount() != null ? JoysaleApplication.format(Double.parseDouble(itemMap.getViewsCount())) : "0") + " " + getResources().getString(R.string.views));
        moreItems.setText(getResources().getString(R.string.more_items_from) + " " + itemMap.getSellerName());
        location.setText(itemMap.getLocation());
        shopaddress = itemMap.getLocation();

        if (!itemMap.isAffiliateItem()
                && itemMap.getSellerId() != null
                && !itemMap.getSellerId().equals(GetSet.getUserId())) {
            checkItemStatus();
        }

        title.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        title.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);

        try {
            long timestamp = 0;
            String time = itemMap.getPostedTime();
            if (time != null) {
                timestamp = Long.parseLong(time) * 1000;
            }
            TimeAgo timeAgo = new TimeAgo(DetailActivity.this);
            postedTime.setText(timeAgo.timeAgo(timestamp));
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Addon Mapbox
        if (!itemMap.isAffiliateItem()) {
            if (Constants.ADDON_MAP_BOX_ENABLED) {
        /*    MapboxStaticMap staticImage = MapboxStaticMap.builder()
                    .accessToken(getString(R.string.mapbox_access_token))
                    .styleId(StaticMapCriteria.LIGHT_STYLE)
                    .cameraPoint(Point.fromLngLat(Double.parseDouble(itemMap.getLongitude()), Double.parseDouble(itemMap.getLatitude())))
                    .cameraZoom(13)
                    .width(600) // Image width
                    .height(600) // Image height
                    .retina(true) // Retina 2x image will be returned
                    .build();

            String imageUrl = staticImage.url().toString();
            Log.d(TAG, "setData: " + imageUrl);
            Picasso.get().load(imageUrl).into(map);*/
            } else {

                String url = LocationHelper.getStaticMapUrl(DetailActivity.this, itemMap.getLatitude(), itemMap.getLongitude(), screenWidth);
                Picasso.get().load(url).into(map);
            }
        }

        if (!itemMap.isAffiliateItem() && itemMap.getSellerImg() != null && !itemMap.getSellerImg().equals("")) {
            Picasso.get().load(itemMap.getSellerImg()).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(userImg);
        }
        //  Picasso.get().load(GetSet.getImageUrl()).into(userImg);
        String liked = itemMap.getLiked();

        if (GetSet.isLogged() && liked != null && liked.equalsIgnoreCase("yes")) {
            likeImg.setImageResource((R.drawable.like_icon));
        } else {
            likeImg.setImageResource(R.drawable.unlike_icon);
        }
        if (itemMap.getItemStatus() != null && itemMap.getItemStatus().equalsIgnoreCase("sold")) {
            itemStatus.setVisibility(View.VISIBLE);
            itemStatus.setText(getString(R.string.sold));
            itemStatus.setBackground(getResources().getDrawable(R.drawable.soldbg));
        } else {
            if (Constants.PROMOTION) {
                if (itemMap.getPromotionType() != null && itemMap.getPromotionType().equalsIgnoreCase("Ad")) {
                    itemStatus.setVisibility(View.VISIBLE);
                    itemStatus.setText(getString(R.string.ad));
                    itemStatus.setBackground(getResources().getDrawable(R.drawable.adbg));
                } else if (itemMap.getPromotionType() != null && itemMap.getPromotionType().equalsIgnoreCase("Urgent")) {
                    itemStatus.setVisibility(View.VISIBLE);
                    itemStatus.setText(getString(R.string.urgent));
                    itemStatus.setBackground(getResources().getDrawable(R.drawable.urgentbg));
                } else {
                    itemStatus.setVisibility(View.GONE);
                }
            } else {
                itemStatus.setVisibility(View.GONE);
            }
        }

        //Addon BuyNow
        if (Constants.BUYNOW && !itemMap.isAffiliateItem()) {
            reviewLay.setVisibility(View.VISIBLE);
            try {
                ratingBar.setRating(itemMap.getSellerRating() != null ? Float.parseFloat(itemMap.getSellerRating()) : 0);
                if (itemMap.getRatingUserCount() != null) {
                    ratingCount.setText("(" + itemMap.getRatingUserCount() + ")");
                } else {
                    ratingCount.setText("");
                }

            } catch (NullPointerException | NumberFormatException e) {
                e.printStackTrace();
            }
        } else {
            reviewLay.setVisibility(View.GONE);
        }

        if (!itemMap.isAffiliateItem()) {
            if (itemMap.getFacebookVerification() != null && itemMap.getFacebookVerification().equals("true")) {
                fbVerify.setImageResource(R.drawable.fb_veri);
            } else {
                fbVerify.setImageResource(R.drawable.fb_unveri);
            }
            if (itemMap.getEmailVerification() != null && itemMap.getEmailVerification().equals("true")) {
                mailVerify.setImageResource(R.drawable.mail_veri);
            } else {
                mailVerify.setImageResource(R.drawable.mail_unveri);
            }
            if (itemMap.getMobileVerification() != null && itemMap.getMobileVerification().equals("true")) {
                mblVerify.setImageResource(R.drawable.mob_veri);
            } else {
                mblVerify.setImageResource(R.drawable.mob_unveri);
            }

            if (itemMap.getShowSellerMobile() != null && itemMap.getShowSellerMobile().equals("true")) {
                call.setVisibility(View.VISIBLE);
            } else {
                call.setVisibility(View.GONE);
            }
        }

        if (itemMap.isAffiliateItem()) {
            setupAffiliateDetailUi();
        }
    }

    private void applyRtlUi() {
        boolean rtl = LocaleManager.isRTL(this);
        if (actionbar != null) {
            actionbar.setLayoutDirection(rtl ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        }
        View optionsLay = findViewById(R.id.optionsLay);
        if (optionsLay != null) {
            optionsLay.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
        if (backBtn != null) {
            backBtn.setScaleX(rtl ? -1f : 1f);
        }
        applyDiscountRibbonLayout();
        if (title != null) {
            title.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            title.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        }
        if (txtDescription != null) {
            txtDescription.setGravity(Gravity.START);
            txtDescription.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        }
        if (txtDetails != null) {
            txtDetails.setGravity(Gravity.START);
            txtDetails.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        }
        if (promoCodeTitle != null) {
            promoCodeTitle.setGravity(Gravity.START);
            promoCodeTitle.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        }
        if (promoCodeText != null) {
            promoCodeText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            promoCodeText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        }
        if (moreItems != null) {
            moreItems.setGravity(Gravity.START);
            moreItems.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        }
        if (txtRelatedProducts != null) {
            txtRelatedProducts.setGravity(Gravity.START);
            txtRelatedProducts.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        }
        if (postedTime != null) {
            postedTime.setGravity(Gravity.END);
            postedTime.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        }
        if (sview != null) {
            sview.setLayoutDirection(rtl ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        }
        if (detailLay != null) {
            detailLay.setLayoutDirection(rtl ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        }
        if (promoCodeLay != null) {
            promoCodeLay.setLayoutDirection(rtl ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        }
    }

    private void applyDiscountRibbonLayout() {
        DiscountUtils.applyCornerRibbon(this, discountRibbon, 36, 22);
    }

    private void setupPromoCodeSection() {
        if (promoCodeLay == null) {
            return;
        }
        String promoCode = itemMap.getPromoCode();
        boolean showPromo = promoCode != null && !promoCode.trim().isEmpty();
        if (showPromo) {
            promoCodeLay.setVisibility(View.VISIBLE);
            DiscountUtils.applyPromoCodeTheme(this, promoCodeLay, findViewById(R.id.promoCodeInputLay),
                    promoCodeTitle, promoCodeText, null);
            if (promoCodeText != null) {
                promoCodeText.setText(promoCode.trim());
            }
        } else {
            promoCodeLay.setVisibility(View.GONE);
        }
    }

    private void copyPromoCode() {
        String promoCode = itemMap.getPromoCode();
        if (promoCode == null || promoCode.trim().isEmpty()) {
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.promo_code), promoCode.trim()));
            Toast.makeText(this, getString(R.string.promo_code_copied), Toast.LENGTH_SHORT).show();
        }
        if (promoCodeText != null) {
            promoCodeText.setText(promoCode.trim());
        }
    }

    private void setFilterData() {
        filterLay.removeAllViews();
        if (itemMap.getFilters() != null && itemMap.getFilters().size() > 0) {
            txtDetails.setVisibility(View.VISIBLE);
            detailsDivider.setVisibility(View.VISIBLE);
        } else {
            txtDetails.setVisibility(View.GONE);
            detailsDivider.setVisibility(View.GONE);
        }
        for (BeforeAddResponse.Filters filter : itemMap.getFilters()) {
            View view = LayoutInflater.from(this).inflate(R.layout.item_filter_details, null);
            TextView txtParentLabel = view.findViewById(R.id.txtParentLabel);
            TextView txtChildLabel = view.findViewById(R.id.txtChildLabel);
            LinearLayout linearLayoutItem = view.findViewById(R.id.linearLayoutItem);
            txtParentLabel.setText(filter.getSelectedParentLabel());

            if (filter.getType().equalsIgnoreCase(Constants.TAG_DROPDOWN)) {
                txtChildLabel.setText(filter.getChildLabel());

                if (filter.getChildLabel() != null && !filter.getChildLabel().equalsIgnoreCase("")) {
                    linearLayoutItem.setVisibility(View.VISIBLE);
                } else {
                    linearLayoutItem.setVisibility(View.GONE);

                }

            } else if (filter.getType().equalsIgnoreCase(Constants.TAG_RANGE)) {
                txtChildLabel.setText(filter.getSelectedRangeValue());
                if (filter.getSelectedRangeValue() != null && !filter.getSelectedRangeValue().equalsIgnoreCase("")) {
                    linearLayoutItem.setVisibility(View.VISIBLE);
                } else {
                    linearLayoutItem.setVisibility(View.GONE);

                }
            } else if (filter.getType().equalsIgnoreCase(Constants.TAG_MULTILEVEL)) {

                String temp;
               /*
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    temp = "" + Html.fromHtml(filter.getSelectedSubParentLabel() + " &#8226; " + filter.getChildLabel(), Html.FROM_HTML_MODE_LEGACY);
                } else {
                    temp = "" + Html.fromHtml(filter.getSelectedSubParentLabel() + " &#8226; " + filter.getChildLabel());
                }
                 txtChildLabel.setText(temp);
                 if ((filter.getSelectedSubParentLabel() != null) || (filter.getChildLabel() != null)) {
                    linearLayoutItem.setVisibility(View.VISIBLE);
                } else {
                    linearLayoutItem.setVisibility(View.GONE);
                }
                */
                Log.e(TAG, "filter.getChildLabel() : " + filter.getChildLabel() + "shankari" +filter.getSelectedSubParentLabel() +"");
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    if(filter.getChildLabel() != null  ) {
                        temp = "" + Html.fromHtml(filter.getSelectedSubParentLabel() + " &#8226; " + filter.getChildLabel(), Html.FROM_HTML_MODE_LEGACY);
                        txtChildLabel.setText(temp);
                    }
                } else {
                    if(filter.getChildLabel() != null) {
                        temp = "" + Html.fromHtml(filter.getSelectedSubParentLabel() + " &#8226; " + filter.getChildLabel());
                        txtChildLabel.setText(temp);
                    }
                }
                if ((filter.getSelectedSubParentLabel() != null) && (filter.getChildLabel() != null)) {
                    linearLayoutItem.setVisibility(View.VISIBLE);
                    txtDetails.setVisibility(View.VISIBLE);
                    detailsDivider.setVisibility(View.VISIBLE);
                } else {
                    linearLayoutItem.setVisibility(View.GONE);
                    txtDetails.setVisibility(View.GONE);
                    detailsDivider.setVisibility(View.GONE);
                }

            }
            filterLay.addView(view);
        }
    }

    /**
     * for change the bottom button by user
     **/

    private void checkUser() {
        if (itemMap.isAffiliateItem() && (itemMap.getSellerId() == null
                || GetSet.getUserId() == null
                || !itemMap.getSellerId().equals(GetSet.getUserId()))) {
            setupAffiliateCta();
            return;
        }
        if (itemMap.getSellerId().equals(GetSet.getUserId())) {
            edit.setVisibility(View.VISIBLE);
            likeImg.setVisibility(View.GONE);
            isSeller = true;
            chat.setText(getString(R.string.insights));
            setChatIcon(chat, getString(R.string.insights));
            chat.setTextColor(getResources().getColor(R.color.white));
            chat.setBackgroundColor(getResources().getColor(R.color.colorSecondary1));
            if (itemMap.getItemStatus().equalsIgnoreCase("sold")) {
                offer.setText(getString(R.string.mark_as_available));
            } else {
                if (Constants.PROMOTION) {
                    if (itemMap.getPromotionType().equals("Normal")) {
//                        offer.setVisibility(View.GONE);
                        offer.setText(getString(R.string.promote_your_product));
                    } else {
                        offer.setText(getString(R.string.promotion));
                    }
                } else {
                    offer.setVisibility(View.GONE);
                    chat.setBackground(getDrawable(R.drawable.rounded_border));
                    chat.setTextColor(getResources().getColor(R.color.white));
                }
            }
        } else {
            edit.setVisibility(View.GONE);
            likeImg.setVisibility(View.VISIBLE);
            chat.setVisibility(View.VISIBLE);
            isSeller = false;
            setBuyerChatCta(chat, getString(R.string.snatch_it_up));
            if (Constants.BUYNOW) {
                offer.setText(getString(R.string.instantbuy));
                if (itemMap.getInstantBuy().equals("0") || itemMap.getInstantBuy().equals("2")
                        || itemMap.getItemStatus().equalsIgnoreCase("sold")) {
                    offer.setVisibility(View.GONE);
                } else {
                    offer.setVisibility(View.VISIBLE);
                }
                if (itemMap.getPrice().equals("0")) {
                    offer.setVisibility(View.GONE);
                }
            } else {
                offer.setText(getString(R.string.make_an_offer));
                if (itemMap.getMakeOffer().equals("0")) {
                    offer.setVisibility(View.VISIBLE);
                } else {
                    offer.setVisibility(View.GONE);
                }
            }
        }
    }

    private void setupAffiliateCta() {
        edit.setVisibility(View.GONE);
        likeImg.setVisibility(View.VISIBLE);
        isSeller = false;
        offer.setVisibility(View.GONE);
        chat.setVisibility(View.VISIBLE);
        chat.setBackgroundColor(getResources().getColor(R.color.primary));
        chat.setTextColor(getResources().getColor(R.color.black));
        setAffiliateChatCtaNew(getString(R.string.snatch_it_up),chat);
    }

    private void setAffiliateChatCtaNew(String text,TextView textView) {
        textView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 18);
        String label = text != null ? text.trim() : "";
        if (label.equalsIgnoreCase(getString(R.string.snatch_it_up))) {
            android.text.SpannableStringBuilder builder = new android.text.SpannableStringBuilder();
            builder.append("  ");

            //Log.e("pppppppp", "setAffiliateChatCtaNew: "+itemMap.getIsAffiliate());

            if(!"0".equals(itemMap.getIsAffiliate())) {
                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.converted_icon);
                if (drawable != null) {
                    drawable = drawable.mutate();
                    int size = (int) (textView.getTextSize() * 1.1);
                    drawable.setBounds(0, 0, size, size);
                    android.text.style.ImageSpan imageSpan =
                            new android.text.style.ImageSpan(drawable, android.text.style.ImageSpan.ALIGN_BOTTOM);
                    builder.setSpan(imageSpan, 0, 1, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            builder.append(label);
            textView.setText(builder);
        } else {
            textView.setText(label);
        }
        CustomFontHelper.setCustomFont(textView, "font_bold.ttf", this);
        textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    }

    private void setupAffiliateDetailUi() {
        if (shareBtn != null) {
            shareBtn.setVisibility(View.GONE);
        }

        View locationLay = findViewById(R.id.Lay);
        if (locationLay != null) {
            locationLay.setVisibility(View.GONE);
        }
        if (map != null) {
            map.setVisibility(View.GONE);
        }
        if (locationImg != null) {
            locationImg.setVisibility(View.GONE);
        }
        if (location != null) {
            location.setVisibility(View.GONE);
        }

        if (userImg != null) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) userImg.getLayoutParams();
            if (lp != null) {
                lp.topMargin = JoysaleApplication.dpToPx(DetailActivity.this, 16);
                userImg.setLayoutParams(lp);
            }
            userImg.setImageResource(R.drawable.applogo);
            userImg.setClickable(false);
            userImg.setOnClickListener(null);
        }

        if (userName != null) {
            userName.setText(getString(R.string.affiliate_profile_name));
        }

        if (reviewLay != null) {
            reviewLay.setVisibility(View.GONE);
        }
        if (call != null) {
            call.setVisibility(View.GONE);
        }
        if (mailVerify != null) {
            mailVerify.setVisibility(View.GONE);
        }
        if (mblVerify != null) {
            mblVerify.setVisibility(View.GONE);
        }
        if (fbVerify != null) {
            fbVerify.setVisibility(View.GONE);
        }
        if (mblVerify != null && mblVerify.getParent() instanceof View) {
            ((View) mblVerify.getParent()).setVisibility(View.GONE);
        }
        if (btnFacebook != null) {
            btnFacebook.setVisibility(View.GONE);
        }
        if (btnTwitter != null) {
            btnTwitter.setVisibility(View.GONE);
        }
        if (btnWhatsapp != null) {
            btnWhatsapp.setVisibility(View.GONE);
        }
        if (btnMore != null) {
            btnMore.setVisibility(View.GONE);
        }
        if (btnFacebook != null && btnFacebook.getParent() instanceof View) {
            ((View) btnFacebook.getParent()).setVisibility(View.GONE);
        }

        setupAffiliateCta();
    }

    private void setBuyerChatCta(TextView textView, String text) {
        if (itemMap != null && itemMap.isAffiliateItem()) {
            setAffiliateChatCtaNew(text,textView);
            return;
        }
        textView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 18);
        String label = text != null ? text.trim() : "";
        boolean hideChatIcon = itemMap != null && (itemMap.isAffiliateItem() || itemMap.hasDiscount());
        if (label.equalsIgnoreCase(getString(R.string.snatch_it_up)) && !hideChatIcon) {
            android.text.SpannableStringBuilder builder = new android.text.SpannableStringBuilder();
            builder.append("  ");
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.s_message1);
            if (drawable != null) {
                drawable = drawable.mutate();
                int size = (int) (textView.getTextSize() * 1.1);
                drawable.setBounds(0, 0, size, size);
                android.text.style.ImageSpan imageSpan =
                        new android.text.style.ImageSpan(drawable, android.text.style.ImageSpan.ALIGN_BOTTOM);
                builder.setSpan(imageSpan, 0, 1, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            builder.append(label);
            textView.setText(builder);
        } else {
            textView.setText(label);
        }
        CustomFontHelper.setCustomFont(textView,
                hideChatIcon ? "font_bold.ttf" : "font_regular.ttf", this);
        textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    }

    private boolean isBuyerChatCta(String buttonText) {
        if (buttonText == null) {
            return false;
        }
        String trimmed = buttonText.trim();
        return trimmed.equalsIgnoreCase(getString(R.string.chat))
                || trimmed.equalsIgnoreCase(getString(R.string.snatch_it_up));
    }

    private void loadAffiliateItem() {
        if (!JoysaleApplication.isNetworkAvailable(DetailActivity.this)) {
            return;
        }
        fetchAffiliateItems(0);
    }

    private void fetchAffiliateItems(final int offset) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
        map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
        map.put(Constants.TAG_LIMIT, String.valueOf(Constants.ITEM_LIMIT));
        map.put(Constants.TAG_OFFSET, String.valueOf(offset));

        Log.e(TAG, "affiliateitems params: " + new Gson().toJson(map));

        Call<AffiliateItemsResponse> call = apiInterface.getAffiliateItems(map);
        call.enqueue(new Callback<AffiliateItemsResponse>() {
            @Override
            public void onResponse(Call<AffiliateItemsResponse> call, retrofit2.Response<AffiliateItemsResponse> response) {
                try {
                    Log.e(TAG, "affiliateitems code: " + response.code());
                    if (response.body() != null) {
                        Log.e(TAG, "affiliateitems response: " + new Gson().toJson(response.body()));
                    } else if (response.errorBody() != null) {
                        Log.e(TAG, "affiliateitems errorBody: " + response.errorBody().string());
                    } else {
                        Log.e(TAG, "affiliateitems response: null");
                    }

                    if (!response.isSuccessful() || response.body() == null
                            || !Constants.TAG_TRUE.equalsIgnoreCase(response.body().getStatus())
                            || response.body().getResult() == null) {
                        Log.e(TAG, "affiliateitems invalid/empty status or result");
                        return;
                    }

                    List<AffiliateItemsResponse.AffiliateItem> result = response.body().getResult();
                    if (result.isEmpty()) {
                        Log.e(TAG, "affiliateitems result empty. message: " + response.body().getMessage());
                        return;
                    }

                    AffiliateItemsResponse.AffiliateItem matched = null;
                    for (AffiliateItemsResponse.AffiliateItem affiliateItem : result) {
                        if (affiliateItem != null && affiliateItem.getItemId() != null
                                && affiliateItem.getItemId().equals(itemMap.getId())) {
                            matched = affiliateItem;
                            break;
                        }
                    }

                    if (matched != null) {
                        Log.e(TAG, "affiliateitems matched item: " + new Gson().toJson(matched));
                        applyAffiliateItem(matched);
                        setData();
                        pagerAdapter = null;
                        bindProductImages();
                        checkUser();
                    } else if (result.size() >= Constants.ITEM_LIMIT) {
                        Log.e(TAG, "affiliateitems item not found in offset " + offset + ", loading next page");
                        fetchAffiliateItems(offset + Constants.ITEM_LIMIT);
                    } else {
                        Log.e(TAG, "affiliateitems no match for item_id: " + itemMap.getId());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "loadAffiliateItem error", e);
                }
            }

            @Override
            public void onFailure(Call<AffiliateItemsResponse> call, Throwable t) {
                Log.e(TAG, "affiliateitems failure: " + (t != null ? t.getMessage() : "unknown"), t);
            }
        });
    }

    private void applyAffiliateItem(AffiliateItemsResponse.AffiliateItem affiliateItem) {
        itemMap.setIsAffiliate("1");
        if (affiliateItem.getAffiliateUrl() != null && !affiliateItem.getAffiliateUrl().trim().isEmpty()) {
            itemMap.setAffiliateUrl(affiliateItem.getAffiliateUrl().trim());
        }
        if (affiliateItem.getPromoCode() != null) {
            itemMap.setPromoCode(affiliateItem.getPromoCode());
        }
        if (affiliateItem.getDiscountPercent() != null && !affiliateItem.getDiscountPercent().trim().isEmpty()) {
            itemMap.setDiscountPercent(affiliateItem.getDiscountPercent().trim());
        }
        if (affiliateItem.getDiscountPercentage() != null && !affiliateItem.getDiscountPercentage().trim().isEmpty()) {
            itemMap.setDiscountPercentage(affiliateItem.getDiscountPercentage().trim());
        }
        if ((itemMap.getItemTitle() == null || itemMap.getItemTitle().trim().isEmpty())
                && affiliateItem.getItemTitle() != null) {
            itemMap.setItemTitle(affiliateItem.getItemTitle());
        }
        if ((itemMap.getItemDescription() == null || itemMap.getItemDescription().trim().isEmpty())
                && affiliateItem.getItemDescription() != null) {
            itemMap.setItemDescription(affiliateItem.getItemDescription());
        }
        if ((itemMap.getPrice() == null || itemMap.getPrice().trim().isEmpty())
                && affiliateItem.getItemPrice() != null) {
            itemMap.setPrice(affiliateItem.getItemPrice());
            if (itemMap.getFormattedPrice() == null || itemMap.getFormattedPrice().trim().isEmpty()) {
                String currency = affiliateItem.getCurrency() != null ? affiliateItem.getCurrency() : "";
                itemMap.setFormattedPrice((currency + " " + affiliateItem.getItemPrice()).trim());
            }
        }
        if ((itemMap.getCurrencyCode() == null || itemMap.getCurrencyCode().trim().isEmpty())
                && affiliateItem.getCurrency() != null) {
            itemMap.setCurrencyCode(affiliateItem.getCurrency());
        }
        if ((itemMap.getCategoryId() == null || itemMap.getCategoryId().trim().isEmpty())
                && affiliateItem.getCategoryId() != null) {
            itemMap.setCategoryId(affiliateItem.getCategoryId());
        }
        if ((itemMap.getSellerId() == null || itemMap.getSellerId().trim().isEmpty())
                && affiliateItem.getUserId() != null) {
            itemMap.setSellerId(affiliateItem.getUserId());
        }
        if ((itemMap.getSellerName() == null || itemMap.getSellerName().trim().isEmpty())
                && affiliateItem.getUserName() != null) {
            itemMap.setSellerName(affiliateItem.getUserName());
        }
        if ((itemMap.getSellerImg() == null || itemMap.getSellerImg().trim().isEmpty())
                && affiliateItem.getUserImage() != null) {
            itemMap.setSellerImg(affiliateItem.getUserImage());
        }
        if ((itemMap.getPostedTime() == null || itemMap.getPostedTime().trim().isEmpty())
                && affiliateItem.getCreatedDate() != null) {
            itemMap.setPostedTime(affiliateItem.getCreatedDate());
        }
        if (affiliateItem.getPhotos() != null && !affiliateItem.getPhotos().isEmpty()) {
            PhotoUtils.applyAffiliatePhotos(itemMap, affiliateItem.getPhotos());
        }
    }

    private void openAffiliateUrl() {
        copyPromoCodeIfAvailable(true);
        String url = itemMap.getAffiliateUrl();
        if (url == null || url.trim().isEmpty()) {
            Toast.makeText(DetailActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
            return;
        }
        url = url.trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        Uri uri = Uri.parse(url);
        try {
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .build();
            customTabsIntent.launchUrl(this, uri);
        } catch (Exception e) {
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(browserIntent);
            } catch (Exception fallback) {
                Log.e(TAG, "openAffiliateUrl error", fallback);
                Toast.makeText(DetailActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void copyPromoCodeIfAvailable(boolean showToast) {
        String promoCode = itemMap.getPromoCode();
        if (promoCode == null || promoCode.trim().isEmpty()) {
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.promo_code), promoCode.trim()));
            if (showToast) {
                Toast.makeText(this, getString(R.string.promo_code_copied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * for get the images from json to array
     **/

    private void bindProductImages() {
        resizedImageList.clear();
        originalImageList.clear();
        getImageAry();
    }

    private void getImageAry() {
        if (itemMap == null || itemMap.getPhotos() == null || itemMap.getPhotos().isEmpty()) {
            Log.v(TAG, "photos empty");
            if (progress != null) {
                progress.setVisibility(View.GONE);
            }
            return;
        }
        try {
            for (HomeItemResponse.Photo photo : itemMap.getPhotos()) {
                String displayUrl = PhotoUtils.getGalleryUrl(photo);
                if (displayUrl == null) {
                    displayUrl = PhotoUtils.getListThumbUrl(photo);
                }
                if (displayUrl == null) {
                    continue;
                }
                originalImageList.add(displayUrl);
                resizedImageList.add(displayUrl);
            }
            if (resizedImageList.isEmpty()) {
                Log.v(TAG, "photos empty after resolve");
                if (progress != null) {
                    progress.setVisibility(View.GONE);
                }
                return;
            }
            if (pagerAdapter == null) {
                pagerAdapter = new ViewPagerAdapter(DetailActivity.this, resizedImageList);
                viewPager.setAdapter(pagerAdapter);
            } else {
                pagerAdapter.notifyDataSetChanged();
            }
            txtImageCount.setText(String.format(getString(R.string.images_count), ("" + 1), ("" + resizedImageList.size())));
            viewPager.setCurrentItem(0);
            imageCountLay.setVisibility(resizedImageList.size() > 1 ? View.VISIBLE : View.GONE);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrollStateChanged(int arg0) {
            if (arg0 == 0) {
                try {
                    if (arg0 == viewPager.getCurrentItem()) {
                        viewPager.setCurrentItem(0);
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            try {
                if (position == viewPager.getCurrentItem()) {
                    int index = viewPager.getCurrentItem();
                    View itemView = viewPager.findViewWithTag("pos" + index);
                    if (itemView != null) {
                        txtImageCount.setText(String.format(getString(R.string.images_count), ("" + (index + 1)), ("" + resizedImageList.size())));
                    }
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onPageSelected(int position) {

        }
    };

    public void dialog(String name, String imageurl) {
        final Dialog dialog = new Dialog(DetailActivity.this, R.style.DialogSlideAnim);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.contactme_dialog);

        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, WRAP_CONTENT);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        window.setAttributes(wlp);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        TextView contactName = (TextView) dialog.findViewById(R.id.contactName);
        LinearLayout send = (LinearLayout) dialog.findViewById(R.id.send);
        ImageView contactImg = (ImageView) dialog.findViewById(R.id.contactImg);
        final EditText contactMsg = (EditText) dialog.findViewById(R.id.contactMsg);
        final EditText makeOffer = (EditText) dialog.findViewById(R.id.makeOffer);
        LinearLayout offerLay = (LinearLayout) dialog.findViewById(R.id.offerLay);
        LinearLayout emptyLay = (LinearLayout) dialog.findViewById(R.id.emptyLay);
        final RelativeLayout mainLay = (RelativeLayout) dialog.findViewById(R.id.mainLay);

        contactMsg.setFilters(new InputFilter[]{JoysaleApplication.EMOJI_FILTER, new InputFilter.LengthFilter(500)});
        makeOffer.setFilters(new InputFilter[]{new JoysaleApplication.DecimalDigitsInputFilter(SplashActivity.beforeDecimal, SplashActivity.afterDecimal)});
        contactName.setText(name);
        Picasso.get().load(imageurl).into(contactImg);

        if (name.equals(getString(R.string.make_an_offer))) {
            offerLay.setVisibility(View.VISIBLE);
        } else {
            offerLay.setVisibility(View.GONE);
        }

        emptyLay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mainLay.getWindowToken(), 0);
                dialog.dismiss();
            }
        });

        send.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (JoysaleApplication.isNetworkAvailable(DetailActivity.this)) {
                    if (contactMsg.getText().toString().trim().length() != 0 && makeOffer.getText().toString().trim().length() != 0) {
                        if (!(itemMap.getPrice().equals("0")) && Float.parseFloat(itemMap.getPrice()) <= Float.parseFloat(makeOffer.getText().toString())) {
                            Toast.makeText(DetailActivity.this, getString(R.string.offer_should_not_above), Toast.LENGTH_SHORT).show();
                        } else if (!(itemMap.getPrice().equals("0")) && Float.parseFloat(makeOffer.getText().toString()) == 0) {
                            Toast.makeText(DetailActivity.this, getString(R.string.offer_should_not_zero), Toast.LENGTH_SHORT).show();
                        } else {
                            initializeGetChat();
                            progressDialog.setMessage(getString(R.string.pleasewait));
                            progressDialog.setCancelable(false);
                            progressDialog.setCanceledOnTouchOutside(false);
                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                                Drawable drawable = new ProgressBar(DetailActivity.this).getIndeterminateDrawable().mutate();
                                drawable.setColorFilter(ContextCompat.getColor(DetailActivity.this, R.color.progressColor),
                                        PorterDuff.Mode.SRC_IN);
                                progressDialog.setIndeterminateDrawable(drawable);
                            }
                            progressDialog.show();
                            getChatId("offer", contactMsg.getText().toString().trim(), makeOffer.getText().toString().trim());
                            //makeOffer(contactMsg.getText().toString().trim(), makeOffer.getText().toString().trim());
                            dialog.dismiss();
                        }
                    } else {
                        Toast.makeText(DetailActivity.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    JoysaleApplication.dialog(DetailActivity.this, getResources().getString(R.string.error), getResources().getString(R.string.checkconnection));
                }

            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    private void initializeGetChat() {
        if (chatClicked) {
            this.progressDialog.setMessage(getString(R.string.pleasewait));
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            this.progressDialog.show();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            try {
                //      sview.setViewsBounds(ParallaxScollListView.ZOOM_X2);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * for show the popupmenu window
     **/

    public void shareImage(View v) {
        final ArrayList<String> values = new ArrayList<>();
        Log.v(TAG, "makeofferfromapi=" + itemMap.getMakeOffer());
        if (itemMap.getSellerId().equals(GetSet.getUserId())) {
            if (itemMap.getItemStatus().equalsIgnoreCase("sold")) {
                values.add(getString(R.string.delete_product));
                values.add(getString(R.string.mark_as_available));
            } else {
                values.add(getString(R.string.delete_product));
                values.add(getString(R.string.mark_as_sold));
            }
        } else {
            if (Constants.EXCHANGE && itemMap.getExchangeBuy().equalsIgnoreCase("1")) {
                values.add(getString(R.string.request_exchange));
            }
            if (Constants.BUYNOW && (itemMap.getMakeOffer().equals("0")) && !(itemMap.getPrice().equals("0"))) {
                values.add(getString(R.string.make_an_offer));
            }

            Log.e(TAG,"itemMap.getReport():" + itemMap.getReport() +"logged : " +GetSet.isLogged()) ;
            if (GetSet.isLogged() && itemMap.getReport().equals("yes")) {
                values.add(getString(R.string.undo_report));
            } else {
                values.add(getString(R.string.report_product));
            }

        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.share_new, android.R.id.text1, values);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.share, null);
        layout.setAnimation(AnimationUtils.loadAnimation(this, R.anim.grow_from_topright_to_bottomleft));
        final PopupWindow popup = new PopupWindow(DetailActivity.this);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setContentView(layout);
        popup.setWidth(displaymetrics.widthPixels * 60 / 100);
        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setFocusable(true);
        popup.showAtLocation(main, Gravity.TOP | Gravity.END, 0, 20);

        final ListView lv = (ListView) layout.findViewById(R.id.lv);
        lv.setAdapter(adapter);
        popup.showAsDropDown(v);

        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                popup.dismiss();
                openAction(values.get(position));
            }
        });
    }

    public void openAction(String from) {
        Log.v(TAG, "from=" + from);
        if (from.equals(getString(R.string.delete_product))) {
            if (GetSet.isLogged()) {
                confirmdialog(getString(R.string.delete_product_confirmation));
            } else {
                Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                startActivity(j);
            }
        } else if (from.equals(getString(R.string.mark_as_available))) {
            if (GetSet.isLogged()) {
                confirmdialog(getString(R.string.back_sale_confirmation));
            } else {
                Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                startActivity(j);
            }
        } else if (from.equals(getString(R.string.mark_as_sold))) {
            if (GetSet.isLogged()) {

                //Addon SoldTo
                if (Constants.ADDON_SOLD_TO) {
                    getFollowings();
                } else {
                    confirmdialog(getString(R.string.sold_product_confirmation));

                }
            } else {
                Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                startActivity(j);
            }
        } else if (from.equals(getString(R.string.request_exchange))) {
            if (GetSet.isLogged()) {
                if (itemMap.getItemStatus().equals("sold")) {
                    Toast.makeText(this, getString(R.string.item_sold_message_other), Toast.LENGTH_SHORT).show();
                } else {
                    Intent i = new Intent(DetailActivity.this, CreateExchange.class);
                    i.putExtra("itemId", itemMap.getId());
                    startActivity(i);
                }
            } else {
                Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                startActivity(j);
            }
        } else if (from.equals(getString(R.string.make_an_offer))) {
            if (GetSet.isLogged()) {
                dialog(getString(R.string.make_an_offer), itemMap.getSellerImg());
            } else {
                Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                startActivity(j);
            }
        } else if (from.equals(getString(R.string.undo_report))) {
            if (GetSet.isLogged()) {
                confirmdialog(getString(R.string.undoreport_product_confirmation));
            } else {
                Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                startActivity(j);
            }
        } else if (from.equals(getString(R.string.report_product))) {
            if (GetSet.isLogged()) {
                confirmdialog(getString(R.string.report_product_confirmation));
            } else {
                Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                startActivity(j);
            }
        }
    }
     //Addon soldto
    private void getFollowings() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_ITEM_ID, itemMap.getId());
            Log.v("getFollowings", "params" + map);
            Call<FollowersResponse> call = apiInterface.getSoldTo(map);
            call.enqueue(new Callback<FollowersResponse>() {
                @Override
                public void onResponse(Call<FollowersResponse> call, retrofit2.Response<FollowersResponse> response) {
                    ArrayList<HashMap<String, String>> temp = new ArrayList<HashMap<String, String>>();
                    if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        if (response.body().getResult() != null) {
                            //Addon SoldTo
                            Intent j = new Intent(DetailActivity.this, SoldTo.class);
                            j.putExtra("item_id", itemMap.getId());
                            startActivity(j);
                        } else {
                            confirmdialog(getString(R.string.sold_product_confirmation));
                        }
                    }

                }

                @Override
                public void onFailure(Call<FollowersResponse> call, Throwable t) {
                    call.cancel();
                    Log.e("error", "onFailure:" + t.getMessage());
                }
            });
        }
    }

    public void confirmdialog(final String Message) {
        final Dialog dialog = new Dialog(DetailActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);

        dialog.getWindow().setLayout(displaymetrics.widthPixels * 90 / 100, WRAP_CONTENT);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        // wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        TextView message = (TextView) dialog.findViewById(R.id.alert_msg);
        TextView ok = (TextView) dialog.findViewById(R.id.alert_button);
        TextView cancel = (TextView) dialog.findViewById(R.id.cancel_button);

        message.setText(Message);

        cancel.setVisibility(View.VISIBLE);
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Message.equals(getResources().getString(R.string.report_product_confirmation)) ||
                        Message.equals(getResources().getString(R.string.undoreport_product_confirmation))) {
                    reportItem();
                    //new ReportItem().execute(itemMap.getId());
                } else if (Message.equals(getResources().getString(R.string.delete_product_confirmation))) {
                    deleteProduct();
                    //new deleteProduct().execute();
                } else if (Message.equals(getResources().getString(R.string.back_sale_confirmation)) ||
                        Message.equals(getResources().getString(R.string.sold_product_confirmation))) {
                    if (itemMap.getItemStatus().equalsIgnoreCase("sold")) {
                        initializeSoldStatus();
                        changeSoldStatus("0");
                    } else {
                        initializeSoldStatus();
                        changeSoldStatus("1");
                    }
                }
                dialog.dismiss();
            }
        });
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    private void initializeSoldStatus() {
        progressDialog.setMessage(getString(R.string.pleasewait));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    public void approveDialog(final Context ctx) {
        final Dialog dialog = new Dialog(DetailActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);

        dialog.getWindow().setLayout(displaymetrics.widthPixels * 90 / 100, WRAP_CONTENT);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        // wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        TextView message = (TextView) dialog.findViewById(R.id.alert_msg);
        TextView ok = (TextView) dialog.findViewById(R.id.alert_button);
        TextView cancel = (TextView) dialog.findViewById(R.id.cancel_button);

        message.setText(getString(R.string.product_waiting_for_admin_approval));

        cancel.setVisibility(View.GONE);

        ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    switch (from) {
                        case "home":
                            FragmentMainActivity.homeItemList.remove(position);
                            FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                            break;
                        case "mylisting":
                            MyListing.addedItems.remove(position);
                            MyListing.itemAdapter.notifyDataSetChanged();
                            break;
                        case "liked":
                            LikedItems.likedItems.remove(position);
                            LikedItems.itemAdapter.notifyDataSetChanged();
                            break;
                        case "detail":
                            MoreItems.remove(position);
                            itemAdapter.notifyDataSetChanged();
                            break;
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
                ((Activity) ctx).finish();
            }
        });
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    private void stripUnderlines(TextView textView) {
        Spannable s = (Spannable) (textView.getText());
        URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan span : spans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL());
            s.setSpan(span, start, end, 0);
        }
        textView.setText(s.toString().trim());
    }

    @Override
    public void onBackPressed() {
        //MoreItems.clear();
        //this is correct
       // super.onBackPressed();
        this.finish();
    }


    @Override
    public void onScrollChanged(ParallaxScrollView v, int x, int y, int oldX,
                                int oldY) {
        if (y >= (screenheight * 0.47)) {
            if (!isTitleVisibility) {
                FragmentMainActivity.startAlphaAnimation(titleText, 1000, View.VISIBLE);
                isTitleVisibility = true;
            }
        } else {
            if (isTitleVisibility) {
                FragmentMainActivity.startAlphaAnimation(titleText, 1000, INVISIBLE);
                isTitleVisibility = false;
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //once api is done then remove it
        if (fromEdit) {
            fromEdit = false;
            progressDialog.setMessage(getString(R.string.pleasewait));
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            loadHomeItems();
            //new homeLoadItems().execute();
        }
        if (!fromCall) {
            progress.setVisibility(View.VISIBLE);
            getHomeDatas(0);
            getUserProducts();
        } else {
            progress.setVisibility(View.GONE);
            fromCall = false;
        }

        //Addon SoldTo
        if (Constants.ADDON_SOLD_TO) {
            if (SoldTo.fromSold) {
                SoldTo.fromSold = false;
                itemStatus.setVisibility(View.VISIBLE);
                itemStatus.setText(getString(R.string.sold));
                itemStatus.setBackground(getResources().getDrawable(R.drawable.soldbg));
                offer.setText(getString(R.string.mark_as_available));
                itemMap.setItemStatus("sold");
            }
        }

    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(DetailActivity.this, isConnected);
    }

    class ViewPagerAdapter extends PagerAdapter {
        Context context;
        LayoutInflater inflater;
        ArrayList<String> temp;

        public ViewPagerAdapter(Context act, ArrayList<String> newary) {
            this.temp = newary;
            this.context = act;
        }

        public int getCount() {
            return temp.size();
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        public Object instantiateItem(ViewGroup collection, final int position) {
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View itemView = inflater.inflate(R.layout.layout_product_image,
                    collection, false);
            itemView.setTag("pos" + position);

            ImageView image = itemView.findViewById(R.id.imgDisplay);
            ProgressBar imageLoading = itemView.findViewById(R.id.imageLoading);
            imageLoading.getIndeterminateDrawable().setColorFilter(context.getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY);

            String imageUrl = null;
            if (resizedImageList != null && position >= 0 && position < resizedImageList.size()) {
                imageUrl = resizedImageList.get(position);
            }
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                imageLoading.setVisibility(View.GONE);
                image.setImageResource(R.drawable.applogo);
            } else {
                int width = screenWidth > 0 ? screenWidth : context.getResources().getDisplayMetrics().widthPixels;
                int height = productHeight > 0 ? productHeight : width;
                Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.applogo)
                        .error(R.drawable.applogo)
                        .resize(width, height)
                        .centerInside()
                        .onlyScaleDown()
                        .into(image, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                imageLoading.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e(TAG, "onError: " + (e != null ? e.getMessage() : "unknown"));
                                imageLoading.setVisibility(View.GONE);
                            }
                        });
            }

            image.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(DetailActivity.this, SingleView.class);
                    i.putExtra("mimages", originalImageList);
                    i.putExtra("position", position);
                    startActivity(i);
                }
            });

            collection.addView(itemView);
            return itemView;

        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == ((View) arg1);

        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }

    /**
     * Function to get a Home Datas
     **/

    private void getHomeDatas(final int pageCount) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            int offset = (pageCount * 20);
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_TYPE, "moreitems");
            map.put(Constants.TAG_SELLERID, itemMap.getSellerId());
            map.put(Constants.TAG_ITEM_ID, itemMap.getId());
            map.put(Constants.TAG_OFFSET, Integer.toString(offset));
            map.put(Constants.TAG_LIMIT, "20");
            if (GetSet.isLogged()) {
                map.put(Constants.TAG_USERID, GetSet.getUserId());
            } else {
                map.put(Constants.TAG_USERID, "");
            }
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(DetailActivity.this));
            Call<HomeItemResponse> call = apiInterface.getHomeItems(map);
            Log.e(TAG,"GetHomeITEMS : " + new Gson().toJson(map));
            call.enqueue(new Callback<HomeItemResponse>() {
                @Override
                public void onResponse(Call<HomeItemResponse> call, retrofit2.Response<HomeItemResponse> response) {
                    Log.e(TAG,"GetHomeITEMS  res: " + new Gson().toJson(response.body()));
                    MoreItems.clear();
                    if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        MoreItems.addAll(response.body().getResult().getItems());
                    } else if (response.isSuccessful() && response.body().getStatus().equalsIgnoreCase("error")) {
                        JoysaleApplication.disabledialog(DetailActivity.this, "" + response.body().getMessage(), GetSet.getUserId());
                    }
                    progress.setVisibility(View.GONE);
                    if (MoreItems.size() == 0) {
                        moreItems.setVisibility(View.GONE);
                        userRecyclerView.setVisibility(View.GONE);
                    } else {
                        itemAdapter.notifyDataSetChanged();
                        userRecyclerView.getLayoutParams().height = listHeight;
                    }
                }

                @Override
                public void onFailure(Call<HomeItemResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void getUserProducts() {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_CATEGORYID, itemMap.getCategoryId());
            if (itemMap.getSubcatId() == null || itemMap.getSubcatId().equals("")) {
                map.put(Constants.TAG_SUBCATEGORY_ID, "0");
            } else {
                map.put(Constants.TAG_SUBCATEGORY_ID, itemMap.getSubcatId());
            }
            map.put(Constants.TAG_PRODUCT_ID, itemMap.getId());
            if (GetSet.isLogged()) {
                map.put(Constants.TAG_USERID, GetSet.getUserId());
            }
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(DetailActivity.this));

            Call<HomeItemResponse> call = apiInterface.getUserProducts(map);
            call.enqueue(new Callback<HomeItemResponse>() {
                @Override
                public void onResponse(Call<HomeItemResponse> call, retrofit2.Response<HomeItemResponse> response) {
                    userItemList.clear();
                    if (response.isSuccessful()) {
                        if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                            userItemList.addAll(response.body().getResult().getItems());
                        } else if (response.body().getStatus().equalsIgnoreCase("error")) {
                            JoysaleApplication.disabledialog(DetailActivity.this, "" + response.body().getMessage(), GetSet.getUserId());
                        } else if (response.body().getStatus().equalsIgnoreCase(Constants.TAG_FALSE_CODE)) {
                            JoysaleApplication.disabledialog(DetailActivity.this, "" + response.body().getMessage(), GetSet.getUserId());
                        }

                        if (userItemList.size() == 0) {
                            txtRelatedProducts.setVisibility(View.GONE);
                            relatedRecycleView.setVisibility(View.GONE);
                        } else {
                            relatedAdapter.notifyDataSetChanged();
                            relatedRecycleView.getLayoutParams().height = listHeight;
                        }
                    } else {
                        txtRelatedProducts.setVisibility(View.GONE);
                        relatedRecycleView.setVisibility(View.GONE);
                    }

                }

                @Override
                public void onFailure(Call<HomeItemResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    /**
     * adapter for showing more items
     **/

    public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.MyViewHolder> {
        ArrayList<HomeItemResponse.Item> homeItems = new ArrayList<>();
        private Context mContext;

        public ItemAdapter(Context ctx, ArrayList<HomeItemResponse.Item> data) {
            mContext = ctx;
            homeItems = data;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View convertView = inflater.inflate(R.layout.home_list_items,
                    parent, false);// layout
            return new MyViewHolder(convertView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            try {
                final HomeItemResponse.Item tempMap = homeItems.get(position);

                holder.mainLay.setPadding(0, 0, JoysaleApplication.dpToPx(mContext, 5), 0);

                PhotoUtils.loadListImage(holder.singleImage,
                        PhotoUtils.getFirstListThumbUrl(tempMap),
                        screenHalf, R.drawable.white_roundcorner);
                holder.itemName.setText(JoysaleApplication.stripHtml(tempMap.getItemTitle()));
                DiscountUtils.bindListingDiscount(mContext, tempMap,
                        holder.itemPrice, holder.originalPrice, holder.discountBadge);
                DiscountUtils.bindAffiliateLabel(mContext, tempMap,
                        holder.affiliateLabel, holder.priceLay);
                holder.location.setText(tempMap.getLocation());

                if (tempMap.getItemStatus().equalsIgnoreCase("sold")) {
                    holder.productType.setVisibility(View.VISIBLE);
                    holder.productType.setText(getString(R.string.sold));
                    holder.productType.setBackground(getResources().getDrawable(R.drawable.soldbg));
                } else {
                    if (Constants.PROMOTION) {
                        if (tempMap.getPromotionType().equalsIgnoreCase("Ad")) {
                            holder.productType.setVisibility(View.VISIBLE);
                            holder.productType.setText(getString(R.string.ad));
                            holder.productType.setBackground(getResources().getDrawable(R.drawable.adbg));
                        } else if (tempMap.getPromotionType().equalsIgnoreCase("Urgent")) {
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

                long timestamp = 0;
                String time = tempMap.getPostedTime();
                if (time != null) {
                    timestamp = Long.parseLong(time) * 1000;
                }
                TimeAgo timeAgo = new TimeAgo(mContext);
                holder.postedTime.setText(timeAgo.timeAgo(timestamp));

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return homeItems.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView singleImage;
            TextView itemPrice, originalPrice, itemName, location, postedTime, productType, discountBadge, affiliateLabel;
            RelativeLayout imageLay;
            LinearLayout mainLay, priceLay;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                singleImage = (ImageView) itemView.findViewById(R.id.singleImage);
                itemPrice = (TextView) itemView.findViewById(R.id.priceText);
                originalPrice = (TextView) itemView.findViewById(R.id.originalPrice);
                discountBadge = (TextView) itemView.findViewById(R.id.discountBadge);
                affiliateLabel = (TextView) itemView.findViewById(R.id.affiliateLabel);
                priceLay = (LinearLayout) itemView.findViewById(R.id.priceLay);
                itemName = (TextView) itemView.findViewById(R.id.itemName);
                productType = (TextView) itemView.findViewById(R.id.productType);
                location = (TextView) itemView.findViewById(R.id.location);
                postedTime = (TextView) itemView.findViewById(R.id.postedTime);
                imageLay = (RelativeLayout) itemView.findViewById(R.id.imageLay);
                mainLay = (LinearLayout) itemView.findViewById(R.id.mainLay);

                singleImage.getLayoutParams().height = screenHalf;
                imageLay.getLayoutParams().height = screenHalf;
                singleImage.getLayoutParams().width = screenHalf;
                imageLay.getLayoutParams().width = screenHalf;

                mainLay.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            Intent i = new Intent(DetailActivity.this, DetailActivity.class);
                            i.putExtra(Constants.TAG_DATA, MoreItems.get(getAdapterPosition()));
                            i.putExtra(Constants.TAG_POSITION, position);
                            i.putExtra(Constants.TAG_FROM, "detail");
                            startActivity(i);
                        } catch (IndexOutOfBoundsException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

        }
    }

    public class RelatedAdapter extends RecyclerView.Adapter<RelatedAdapter.MyViewHolder> {

        Context context;
        List<HomeItemResponse.Item> userItemList = new ArrayList<>();

        public RelatedAdapter(Context context, List<HomeItemResponse.Item> userItemList) {
            this.context = context;
            this.userItemList = userItemList;
        }

        @NonNull
        @Override
        public RelatedAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.home_list_items,
                    parent, false);// layout
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RelatedAdapter.MyViewHolder holder, int position) {
            final HomeItemResponse.Item item = userItemList.get(position);
            holder.mainLay.setPadding(0, 0, JoysaleApplication.dpToPx(context, 5), 0);

            PhotoUtils.loadListImage(holder.singleImage,
                    PhotoUtils.getFirstListThumbUrl(item),
                    screenHalf, R.drawable.white_roundcorner);
            holder.itemName.setText(JoysaleApplication.stripHtml(item.getItemTitle()));
            DiscountUtils.bindListingDiscount(context, item,
                    holder.itemPrice, holder.originalPrice, holder.discountBadge);
            DiscountUtils.bindAffiliateLabel(context, item,
                    holder.affiliateLabel, holder.priceLay);
            holder.location.setText(item.getLocation());

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

            long timestamp = 0;
            String time = item.getPostedTime();
            if (time != null) {
                timestamp = Long.parseLong(time) * 1000;
            }
            TimeAgo timeAgo = new TimeAgo(context);
            holder.postedTime.setText(timeAgo.timeAgo(timestamp));
        }

        @Override
        public int getItemCount() {
            return userItemList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView singleImage;
            TextView itemPrice, originalPrice, itemName, location, postedTime, productType, discountBadge, affiliateLabel;
            RelativeLayout imageLay;
            LinearLayout mainLay, priceLay;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                singleImage = (ImageView) itemView.findViewById(R.id.singleImage);
                itemPrice = (TextView) itemView.findViewById(R.id.priceText);
                originalPrice = (TextView) itemView.findViewById(R.id.originalPrice);
                discountBadge = (TextView) itemView.findViewById(R.id.discountBadge);
                affiliateLabel = (TextView) itemView.findViewById(R.id.affiliateLabel);
                priceLay = (LinearLayout) itemView.findViewById(R.id.priceLay);
                itemName = (TextView) itemView.findViewById(R.id.itemName);
                productType = (TextView) itemView.findViewById(R.id.productType);
                location = (TextView) itemView.findViewById(R.id.location);
                postedTime = (TextView) itemView.findViewById(R.id.postedTime);
                imageLay = (RelativeLayout) itemView.findViewById(R.id.imageLay);
                mainLay = (LinearLayout) itemView.findViewById(R.id.mainLay);

                singleImage.getLayoutParams().height = screenHalf;
                imageLay.getLayoutParams().height = screenHalf;
                singleImage.getLayoutParams().width = screenHalf;
                imageLay.getLayoutParams().width = screenHalf;

                mainLay.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            Intent i = new Intent(DetailActivity.this, DetailActivity.class);
                            i.putExtra(Constants.TAG_DATA, userItemList.get(getAdapterPosition()));
                            i.putExtra(Constants.TAG_POSITION, position);
                            i.putExtra(Constants.TAG_FROM, "detail");
                            startActivity(i);
                        } catch (IndexOutOfBoundsException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    /**
     * Function for update the view
     **/

    private void updateView() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_ITEM_ID, itemMap.getId());
            Call<HashMap<String, String>> call = apiInterface.updateView(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    if (response.isSuccessful()) {
                        if (response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            String count = itemMap.getViewsCount();
                            viewCount.setText(JoysaleApplication.format(Double.parseDouble(count)) + " " + getResources().getString(R.string.views));
                            final int view = (Integer.parseInt(count) + 1);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    switch (from) {
                                        case "home":
                                            if (FragmentMainActivity.homeItemList.size() > 0) {
                                                FragmentMainActivity.homeItemList.get(position).setViewsCount(Integer.toString(view));
                                                FragmentMainActivity.itemAdapter.notifyItemChanged(position);
                                            }
                                            break;
                                        case "mylisting":
                                            if (MyListing.addedItems.size() > 0)
                                                MyListing.addedItems.get(position).setViewsCount(Integer.toString(view));
                                            break;
                                        case "liked":
                                            if (LikedItems.likedItems.size() > 0)
                                                LikedItems.likedItems.get(position).setViewsCount(Integer.toString(view));
                                            break;
                                        case "detail":
                                            if (MoreItems.size() > 0 && (MoreItems.size() > position)) {
                                                MoreItems.get(position).setViewsCount(Integer.toString(view));
                                            }
                                            break;
                                    }
                                }
                            });
                        } else if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase(Constants.TAG_FALSE_CODE)) {
                            JoysaleApplication.disabledialog(DetailActivity.this, "" + response.body().get(Constants.TAG_MESSAGE), GetSet.getUserId());
                        } else if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                            if (response.body().get("deleted_userid").equalsIgnoreCase(GetSet.getUserId())) {
                                JoysaleApplication.disabledialog(DetailActivity.this, "" + response.body().get(Constants.TAG_MESSAGE), GetSet.getUserId());
                            } else {
                                openDeleteDialog();
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    /**
     * Function for get chat id between logined user and the seller
     **/

    private void getChatId(final String from, final String message, final String offer) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
            map.put(Constants.TAG_RECEIVER_ID, itemMap.getSellerId());

            Call<HashMap<String, String>> call = apiInterface.getChatId(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    if (response.isSuccessful()) {
                        if (response.body().get(Constants.TAG_STATUS).equals("true")) {
                            chatId = response.body().get("chat_id");
                        } else if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase(Constants.TAG_FALSE_CODE)) {
                            JoysaleApplication.disabledialog(DetailActivity.this, "" + response.body().get(Constants.TAG_MESSAGE), GetSet.getUserId());
                        } else if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                            if (response.body().get("deleted_user").equalsIgnoreCase(GetSet.getUserId())) {
                                JoysaleApplication.disabledialog(DetailActivity.this, "" + response.body().get(Constants.TAG_MESSAGE), GetSet.getUserId());
                            } else {
                                openDeleteDialog();
                            }

                        }
                        if (from.equals("offer"))
                            makeOffer(message, offer, chatId);
                        chat.setOnClickListener(DetailActivity.this);
                        if (from.equals("chat")) {
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            chatClicked = false;
                            Log.v(TAG, "sellerName=" + itemMap.getSellerName());
                            Intent i = new Intent(DetailActivity.this, ChatActivity.class);
                            i.putExtra(Constants.TAG_USERNAME, itemMap.getSellerUsername());
                            i.putExtra(Constants.TAG_USER_ID, itemMap.getSellerId());
                            i.putExtra(Constants.CHATID, chatId);
                            i.putExtra(Constants.TAG_USERIMAGE_M, itemMap.getSellerImg());
                            i.putExtra(Constants.TAG_FULL_NAME, itemMap.getSellerName());
                            i.putExtra(Constants.TAG_MOBILE_NO, itemMap.getMobileNo());
                            i.putExtra(Constants.TAG_SHOW_MOBILE_NO, itemMap.getShowSellerMobile());
                            i.putExtra("data", itemMap);
                            i.putExtra(Constants.TAG_FROM, "detail");
                            i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(i);
                        }
                    } else {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                    t.printStackTrace();
                }
            });
        }
    }

    /**
     * Function for change the product to sold and back to sale
     **/

    private void changeSoldStatus(final String value) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_VALUE, value);
            map.put(Constants.TAG_ITEM_ID, itemMap.getId());

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
                            if (response.body().get(Constants.TAG_MESSAGE).equalsIgnoreCase("Item Status changed to Sold")) {
                                Toast.makeText(DetailActivity.this, getString(R.string.item_status_changed_to_sold), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(DetailActivity.this, getString(R.string.item_status_changed_to_available), Toast.LENGTH_LONG).show();
                            }
                            String value = "";
                            String promotionType = "";
                            if (itemMap.getItemStatus().equalsIgnoreCase("sold")) {
                                value = "onsale";
                                offer.setText(getString(R.string.promote_your_product));
                             offer.setVisibility(View.GONE);
                                promotionType = itemMap.getPromotionType();
                            } else {
                                value = "sold";
                                offer.setText(getString(R.string.mark_as_available));
                                itemMap.setPromotionType("Normal");
                                promotionType = itemMap.getPromotionType();
                            }
                            itemMap.setItemStatus(value);

                            switch (from) {
                                case "home":
                                    FragmentMainActivity.homeItemList.get(position).setItemStatus(value);
                                    FragmentMainActivity.homeItemList.get(position).setPromotionType(promotionType);
                                    FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                                    break;
                                case "mylisting":
                                    MyListing.addedItems.get(position).setItemStatus(value);
                                    MyListing.addedItems.get(position).setPromotionType(promotionType);
                                    MyListing.itemAdapter.notifyDataSetChanged();
                                    break;
                                case "liked":
                                    LikedItems.likedItems.get(position).setItemStatus(value);
                                    LikedItems.likedItems.get(position).setPromotionType(promotionType);
                                    LikedItems.itemAdapter.notifyDataSetChanged();
                                    break;
                                case "detail":
                                    MoreItems.get(position).setItemStatus(value);
                                    MoreItems.get(position).setPromotionType(promotionType);
                                    itemAdapter.notifyDataSetChanged();
                                    break;
                            }
                            finish();
                        } else if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase(Constants.TAG_FALSE_CODE)) {
                            JoysaleApplication.disabledialog(DetailActivity.this, "" + response.body().get(Constants.TAG_MESSAGE), GetSet.getUserId());
                        } else {
                            Toast.makeText(DetailActivity.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
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

    /**
     * Function for remove the product from listing
     **/

    private void deleteProduct() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_ITEM_ID, itemMap.getId());

            Call<HashMap<String, String>> call = apiInterface.deleteProduct(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                        Toast.makeText(DetailActivity.this, getString(R.string.product_deleted_duccessfully), Toast.LENGTH_LONG).show();
                        finish();
                        switch (from) {
                            case "home":
                                FragmentMainActivity.homeItemList.remove(position);
                                FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                                break;
                            case "mylisting":
                                MyListing.addedItems.remove(position);
                                MyListing.itemAdapter.notifyDataSetChanged();
                                break;
                            case "liked":
                                LikedItems.likedItems.remove(position);
                                LikedItems.itemAdapter.notifyDataSetChanged();
                                break;
                            case "detail":
                                MoreItems.remove(position);
                                itemAdapter.notifyDataSetChanged();
                                break;
                        }
                    } else if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase(Constants.TAG_FALSE_CODE)) {
                        JoysaleApplication.disabledialog(DetailActivity.this, "" + response.body().get(Constants.TAG_MESSAGE), GetSet.getUserId());
                    } else {
                        Toast.makeText(DetailActivity.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    /**
     * Function for send the offer request to seller
     **/

    private void makeOffer(final String message, final String offerPrice, final String chat_id) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            long unixTime = System.currentTimeMillis() / 1000L;
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
            map.put(Constants.TAG_CHAT_ID, chat_id);
            map.put(Constants.TAG_SOURCE_ID, itemMap.getId());
            map.put(Constants.TAG_CREATED_DATE, Long.toString(unixTime));
            map.put(Constants.TAG_MESSAGE, message);
            map.put(Constants.TAG_OFFER_PRICE, offerPrice);
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(DetailActivity.this));

            Log.d(TAG, "makeOffer: " + map);
            Call<HashMap<String, String>> call = apiInterface.createOffer(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    try {
                        String status = response.body().get(Constants.TAG_STATUS);
                        if (status.equalsIgnoreCase("true")) {
                            Toast.makeText(DetailActivity.this, getString(R.string.message_send_successfully), Toast.LENGTH_LONG).show();
                        } else if ((status.equals("false"))) {
                            Toast.makeText(DetailActivity.this, getString(R.string.conversation_blocked), Toast.LENGTH_SHORT).show();
                        } else if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase(Constants.TAG_FALSE_CODE)) {
                            JoysaleApplication.disabledialog(DetailActivity.this, "" + response.body().get(Constants.TAG_MESSAGE), GetSet.getUserId());
                        } else {
                            Toast.makeText(DetailActivity.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
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

    /**
     * Function  for checking item approval or not
     **/

    private void checkItemStatus() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_ITEM_ID, itemMap.getId());
            Call<HashMap<String, String>> call = apiInterface.checkItemStatus(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                        if (response.body().get(Constants.TAG_ITEM_APPROVE).equals("0")) {
                            approveDialog(DetailActivity.this);
                        }
                    } else if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase(Constants.TAG_FALSE_CODE)) {
                        JoysaleApplication.disabledialog(DetailActivity.this, "" + response.body().get(Constants.TAG_MESSAGE), GetSet.getUserId());
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    /**
     * Function for like_icon & unlike the product
     **/

    private void likeItem() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_ITEM_ID, itemMap.getId());
            Call<HashMap<String, String>> call = apiInterface.likeItem(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    likeCount.setOnClickListener(DetailActivity.this);
                    likeImg.setOnClickListener(DetailActivity.this);
                    String status = response.body().get(Constants.TAG_STATUS);
                    String results = response.body().get(Constants.TAG_RESULT);
                    String deleteduserid = response.body().get("deleted_user");
                    if (!status.equals("true")) {
                        JoysaleApplication.dialog(DetailActivity.this, getString(R.string.alert), getString(R.string.somethingwrong));
                    } else if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase(Constants.TAG_FALSE_CODE)) {
                        JoysaleApplication.disabledialog(DetailActivity.this, "" + response.body().get(Constants.TAG_MESSAGE), GetSet.getUserId());
                    } else if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                        if (deleteduserid.equalsIgnoreCase(GetSet.getUserId())) {
                            JoysaleApplication.disabledialog(DetailActivity.this, "" + response.body().get("message"), GetSet.getUserId());
                        } else {
                            openDeleteDialog();
                        }
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void changeLikeStatus() {
        String flag = "no";
        String count = itemMap.getLikesCount();
        if (itemMap.getLiked().equals("no")) {
            count = Integer.toString((Integer.parseInt(count) + 1));
            flag = "yes";
            likeCount.setText(count + " " + getResources().getString(R.string.likes));
            likeImg.setImageResource(R.drawable.like_icon);
            itemMap.setLikesCount(count);
            itemMap.setLiked("yes");
        } else {
            count = Integer.toString((Integer.parseInt(count) - 1));
            flag = "no";
            likeCount.setText(count + " " + getResources().getString(R.string.likes));
            likeImg.clearColorFilter();
            likeImg.setImageResource(R.drawable.unlike_icon);
            itemMap.setLikesCount(count);
            itemMap.setLiked("no");
        }
        switch (from) {
            case "home":
                if (FragmentMainActivity.homeItemList.size() > 0) {
                    FragmentMainActivity.homeItemList.get(position).setLikesCount(count);
                    FragmentMainActivity.homeItemList.get(position).setLiked(flag);
                    FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                }
                break;
            case "mylisting":
                if (MyListing.addedItems.size() > 0) {
                    MyListing.addedItems.get(position).setLikesCount(count);
                    MyListing.addedItems.get(position).setLiked(flag);
                    MyListing.itemAdapter.notifyDataSetChanged();
                }
                break;
            case "liked":
                if (LikedItems.likedItems.size() > 0) {
                    LikedItems.likedItems.get(position).setLikesCount(count);
                    LikedItems.likedItems.get(position).setLiked(flag);
                    LikedItems.itemAdapter.notifyDataSetChanged();
                }
                break;
            case "detail":
                Log.d(TAG, "changeLikeStatus: " + MoreItems.size());
               /* if (MoreItems.size() > 0) {
                    MoreItems.get(position).setLikesCount(count);
                    MoreItems.get(position).setLiked(flag);
                    itemAdapter.notifyDataSetChanged();
                }*/
                break;
        }
    }

    /**
     * Function for report and undo report
     */

    private void reportItem() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_ITEM_ID, itemMap.getId());
            Call<HashMap<String, String>> call = apiInterface.reportItem(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    String value = "";
                    String status = response.body().get(Constants.TAG_STATUS);
                    String message = response.body().get(Constants.TAG_MESSAGE);
                    if (status.equalsIgnoreCase("true")) {
                        if (message.equalsIgnoreCase("Reported Successfully")) {
                            message = getString(R.string.reported_successfully);
                        } else {
                            message = getString(R.string.unreported_successfully);
                        }
                        Toast.makeText(DetailActivity.this, message, Toast.LENGTH_LONG).show();
                        if (itemMap.getReport().equals("yes")) {
                            itemMap.setReport("no");
                            value = "no";
                        } else {
                            itemMap.setReport("yes");
                            value = "yes";
                        }
                        switch (from) {
                            case "home":
                                FragmentMainActivity.homeItemList.get(position).setReport(value);
                                FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                                break;
                            case "mylisting":
                                MyListing.addedItems.get(position).setReport(value);
                                MyListing.itemAdapter.notifyDataSetChanged();
                                break;
                            case "liked":
                                LikedItems.likedItems.get(position).setReport(value);
                                LikedItems.itemAdapter.notifyDataSetChanged();
                                break;
                            case "detail":
                               /* MoreItems.get(position).setReport(value);
                                itemAdapter.notifyDataSetChanged();*/
                                break;
                        }
                    } else if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase(Constants.TAG_FALSE_CODE)) {
                        JoysaleApplication.disabledialog(DetailActivity.this, "" + response.body().get(Constants.TAG_MESSAGE), GetSet.getUserId());
                    } else if (response.body().get(Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                        openDeleteDialog();
                    } else {
                        JoysaleApplication.dialog(DetailActivity.this, getString(R.string.alert), getString(R.string.somethingwrong));
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    /**
     * Function for get the promotional details of the product
     **/

    private void getPromotionDetails() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_ITEM_ID, itemMap.getId());
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(getApplicationContext()));

            Call<CheckPromotionResponse> call = apiInterface.checkPromotion(map);
            call.enqueue(new Callback<CheckPromotionResponse>() {
                @Override
                public void onResponse(Call<CheckPromotionResponse> call, retrofit2.Response<CheckPromotionResponse> response) {
                    if (response.body() != null && response.body().getStatus() != null && response.body().getStatus().equalsIgnoreCase("true")) {
                        CheckPromotionResponse.Result promotion = response.body().getResult();
                        MyPromotionResponse.Result result = new MyPromotionResponse().new Result();
                        result.setCurrencyCode(promotion.getCurrencyCode());
                        result.setCurrencySymbol(promotion.getCurrencySymbol());
                        result.setId(promotion.getId());
                        result.setItemApprove(promotion.getItemApprove());
                        result.setItemId(promotion.getItemId());
                        result.setItemImage(promotion.getItemImage());
                        result.setItemName(promotion.getItemName());
                        result.setFormattedPaidAmount(promotion.getFormattedPaidAmount());
                        result.setPromotionName(promotion.getPromotionName());
                        result.setStatus(promotion.getStatus());
                        result.setTransactionId(promotion.getTransactionId());
                        result.setUpto(promotion.getUpto());
                        Intent j = new Intent(DetailActivity.this, PromotionDetail.class);
                        j.putExtra(Constants.TAG_DATA, result);
                        startActivity(j);
                    } else {
                        Toast.makeText(DetailActivity.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                    }
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

                }

                @Override
                public void onFailure(Call<CheckPromotionResponse> call, Throwable t) {

                }
            });
        }
    }

    private void loadHomeItems() {
        if (NetworkReceiver.isConnected()) {
            final ArrayList<HomeItemResponse.Item> HomeItems = new ArrayList<>();
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_ITEM_ID, itemMap.getId());
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_LANG_TYPE, AppUtils.getCurrentLanguageCode(getApplicationContext()));
            Log.d(TAG, "loadHomeItems: " + map);
            Call<HomeItemResponse> call = apiInterface.getItemData(map);
            call.enqueue(new Callback<HomeItemResponse>() {
                @Override
                public void onResponse(Call<HomeItemResponse> call, retrofit2.Response<HomeItemResponse> response) {
                    if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        HomeItems.addAll(response.body().getResult().getItems());
                    } else if (response.body().getStatus().equalsIgnoreCase(Constants.TAG_FALSE_CODE)) {
                        JoysaleApplication.disabledialog(DetailActivity.this, "" + response.body().getMessage(), GetSet.getUserId());
                    } else if (response.body().getStatus().equalsIgnoreCase("error")) {
                        if (response.body().getDeleteduserid().equalsIgnoreCase(GetSet.getUserId())) {
                            JoysaleApplication.disabledialog(DetailActivity.this, "" + response.body().getMessage(), GetSet.getUserId());
                        } else {
                            openDeleteDialog();
                        }
                    }
                    if (HomeItems.size() == 0) {
                        Toast.makeText(DetailActivity.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                    } else {
                        itemMap = new HomeItemResponse().new Item();
                        itemMap = HomeItems.get(0);
                        setData();
                        pagerAdapter = null;
                        bindProductImages();
                        checkUser();
                        viewPager.addOnPageChangeListener(mOnPageChangeListener);
                        switch (from) {
                            case "home":
                                if (FragmentMainActivity.homeItemList.size() > 0) {
                                    FragmentMainActivity.homeItemList.set(position, HomeItems.get(0));
                                    FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                                }
                                break;
                            case "mylisting":
                                if (MyListing.addedItems.size() > 0) {
                                    MyListing.addedItems.set(position, HomeItems.get(0));
                                    MyListing.itemAdapter.notifyDataSetChanged();
                                }
                                break;
                            case "liked":
                                if (LikedItems.likedItems.size() > 0) {
                                    LikedItems.likedItems.set(position, HomeItems.get(0));
                                    LikedItems.itemAdapter.notifyDataSetChanged();
                                }
                                break;
                            case "detail":
                                //  MoreItems.get(position).put(Constants.TAG_REPORT, value);
                                //  itemAdapter.notifyDataSetChanged();
                                break;
                        }
                    }
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }

                @Override
                public void onFailure(Call<HomeItemResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    /**
     * Method for get saved addresses
     **/

    private void getAddress() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_ITEM_ID, itemMap.getId());
            Call<ShippingAddressRes> call = apiInterface.getShippingAddress(map);
            call.enqueue(new Callback<ShippingAddressRes>() {
                @Override
                public void onResponse(Call<ShippingAddressRes> call, retrofit2.Response<ShippingAddressRes> response) {
                    ArrayList<ShippingAddressRes.Result> addressAry = new ArrayList<>();
                    HashMap<String, String> map;
                    try {
                        if (response.isSuccessful() && response.body().getStatus().equalsIgnoreCase("true")) {
                            addressAry.addAll(response.body().getResult());
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }

                            if (addressAry.size() > 0) {
                                boolean haveDefaultAddress = false;
                                ShippingAddressRes.Result defaultAddress = new ShippingAddressRes().new Result();
                                for (ShippingAddressRes.Result result : addressAry) {
                                    if (result.getDefaultShipping().equals("1")) {
                                        haveDefaultAddress = true;
                                        defaultAddress = result;
                                        break;
                                    }
                                }
                                //Addon BuyNow
                                Log.d(TAG, "onResponse: " + haveDefaultAddress);
                                if (!haveDefaultAddress) {
                                    Intent l = new Intent(DetailActivity.this, Addresses.class);
                                    l.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    l.putExtra(Constants.TAG_FROM, Constants.TAG_CHECKOUT);
                                    l.putExtra(Constants.TAG_SHIPPING_ID, addressAry.get(0).getShippingId() != null ?
                                            addressAry.get(0).getShippingId() : addressAry.get(0).getShippingid());
                                    l.putExtra(Constants.TAG_ITEM_ID, itemMap.getId());
                                    l.putExtra(Constants.TAG_COUNTRYID, itemMap.getCountryId());
                                    startActivityForResult(l, Constants.ADDRESS_REQ_CODE);
                                } else {
                                    Intent i = new Intent(DetailActivity.this, Checkout.class);
                                    i.putExtra(Constants.TAG_ITEM_DATA, itemMap);
                                    i.putExtra(Constants.TAG_SHIPPING_DATA, defaultAddress);
                                    //i.putExtra(Constants.TAG_TOKEN,clientToken);
                                    startActivity(i);
                                }
                            }
                        } else if (response.body().getStatus().equalsIgnoreCase(Constants.TAG_FALSE_CODE)) {
                            JoysaleApplication.disabledialog(DetailActivity.this, "" + response.body().getMessage(), GetSet.getUserId());
                        } else if (response.isSuccessful() && response.body().getStatus().equalsIgnoreCase("error")) {
                            if (response.body().getDeleteduserid().equalsIgnoreCase(GetSet.getUserId())) {
                                JoysaleApplication.disabledialog(DetailActivity.this, response.body().getMessage(), GetSet.getUserId());
                            } else {
                                openDeleteDialog();
                            }
                        } else {
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }

                            //Addon BuyNow
                            Intent i = new Intent(DetailActivity.this, AddAddress.class);
                            i.putExtra(Constants.TAG_FROM, Constants.TAG_CHECKOUT);
                            i.putExtra("to", "primary");
                            i.putExtra(Constants.TAG_COUNTRYID, itemMap.getCountryId());
                            startActivityForResult(i, Constants.ADDRESS_REQUEST_CODE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ShippingAddressRes> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    /**
     * Onclick Event
     **/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.shareBtn:
                if (itemMap.isAffiliateItem()) {
                    break;
                }
                Log.d(TAG, "product URL" + itemMap.getProductUrl());
                Intent g = new Intent(Intent.ACTION_SEND);
                g.setType("text/plain");
                g.putExtra(Intent.EXTRA_TEXT, itemMap.getProductUrl());
                startActivity(Intent.createChooser(g, "Share"));
                break;
            case R.id.locationImg: {
                //Addon Mapbox
                /*if (Constants.ADDON_MAP_BOX_ENABLED) {
                    Intent intentLoc = new Intent(DetailActivity.this, MainActivity.class);
                    intentLoc.putExtra("lat", "" + itemMap.getLatitude());
                    intentLoc.putExtra("lon", "" + itemMap.getLongitude());
                    intentLoc.putExtra(Constants.TAG_FROM, "detail");
                    startActivity(intentLoc);
                }*/
            }
            break;
            case R.id.backbtn:
                //MoreItems.clear();
                finish();
                break;
            case R.id.edit:
                if (SDK_INT >= Build.VERSION_CODES.Q) {
                    if (ContextCompat.checkSelfPermission(DetailActivity.this, CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(DetailActivity.this, new String[]{CAMERA}, REQUEST_CAMERA_PERMISSION);
                    } else {
                        Intent a = new Intent(DetailActivity.this, AddProductDetail.class);
                        a.putExtra(Constants.TAG_FROM, "edit");
                        a.putExtra(Constants.TAG_DATA, itemMap);
                        Log.d(TAG, "onClickItem: " + itemMap.getItemCondition());
                        //     Log.d(TAG, "onClick: "+itemMap.getFilters().get(0));
                        startActivity(a);
                    }
                } else {
                    if (ContextCompat.checkSelfPermission(DetailActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED
                            && ContextCompat.checkSelfPermission(DetailActivity.this, WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(DetailActivity.this, new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                    } else if (ContextCompat.checkSelfPermission(DetailActivity.this, CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(DetailActivity.this, new String[]{CAMERA}, REQUEST_CAMERA_PERMISSION);
                    } else if (ContextCompat.checkSelfPermission(DetailActivity.this, WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(DetailActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                    } else {
                        Intent a = new Intent(DetailActivity.this, AddProductDetail.class);
                        a.putExtra(Constants.TAG_FROM, "edit");
                        a.putExtra(Constants.TAG_DATA, itemMap);
                        Log.d(TAG, "onClickItem: " + itemMap.getItemCondition());
                        //     Log.d(TAG, "onClick: "+itemMap.getFilters().get(0));
                        startActivity(a);
                    }
                }

                break;
            case R.id.chat:
                if (itemMap.isAffiliateItem() && !isSeller) {
                    openAffiliateUrl();
                    break;
                }
                Log.d(TAG, "onClick1111: " + chat.getText().toString());
                if (GetSet.isLogged()) {
                    Log.d(TAG, "onClick22222: "+chat.getText().toString().equals(getString(R.string.chat)));
                    Log.d(TAG, "onClick: "+getString(R.string.chat)+"    "+chat.getText().toString());
                    if (isBuyerChatCta(chat.getText().toString())) {
                        if (JoysaleApplication.isNetworkAvailable(DetailActivity.this)) {
                            chat.setOnClickListener(null);
                            chatClicked = true;
                            initializeGetChat();
                            getChatId("chat", "", "");
                        } else {
                            JoysaleApplication.dialog(DetailActivity.this, getResources().getString(R.string.error), getResources().getString(R.string.checkconnection));
                        }
                    } else if (chat.getText().toString().equals(getString(R.string.insights))) {
                        if (GetSet.isLogged()) {
                            Intent insight = new Intent(DetailActivity.this, InsightsActivity.class);
                            insight.putExtra(Constants.TAG_DATA, itemMap);
                            insight.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(insight);
                        } else {
                            Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                            startActivity(j);
                        }
                    }

                } else {
                    Intent i = new Intent(DetailActivity.this, WelcomeActivity.class);
                    startActivity(i);
                }
                break;
            case R.id.commentCount:
                Intent c = new Intent(DetailActivity.this, CommentsActivity.class);
                c.putExtra("itemId", itemMap.getId());
                c.putExtra("position", position);
                c.putExtra(Constants.TAG_FROM, from);
                c.putExtra("productName", JoysaleApplication.stripHtml(itemMap.getItemTitle()));
                c.putExtra("productImage", PhotoUtils.getFirstDisplayUrl(itemMap));
                c.putExtra(Constants.TAG_COMMENTCOUNT, itemMap.getCommentsCount());
                startActivityForResult(c, Constants.COMMENTS_REQUEST_CODE);
                break;
            case R.id.settingBtn:
                shareImage(v);
                break;
            case R.id.offer:
                if (GetSet.isLogged()) {
                    if (isSeller) {
                        if (itemMap.getItemStatus().equalsIgnoreCase("sold")) {
                            confirmdialog(getString(R.string.back_sale_confirmation));
                        } else {
                            if (itemMap.getPromotionType().equals("Normal")) {
                                Intent i = new Intent(DetailActivity.this, CreatePromote.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                i.putExtra("itemId", itemMap.getId());
                                i.putExtra(Constants.TAG_FROM, Constants.TAG_DETAIL);
                                startActivity(i);
                            } else {
                                progressDialog.setMessage(getString(R.string.pleasewait));
                                progressDialog.setCancelable(false);
                                progressDialog.setCanceledOnTouchOutside(false);
                                progressDialog.show();
                                getPromotionDetails();
                            }
                        }
                    } else {
                        if (Constants.BUYNOW) {
                            progressDialog.setMessage(getString(R.string.pleasewait));
                            progressDialog.setCancelable(false);
                            progressDialog.setCanceledOnTouchOutside(false);
                            progressDialog.show();
                            getAddress();
                        } else {
                            dialog(getString(R.string.make_an_offer), itemMap.getSellerImg());
                        }
                    }
                } else {
                    Intent i = new Intent(DetailActivity.this, WelcomeActivity.class);
                    startActivity(i);
                }
                break;
            case R.id.likesImg:
            case R.id.likereditBtn:
                if (GetSet.isLogged()) {
                    if (JoysaleApplication.isNetworkAvailable(DetailActivity.this)) {
                        if (itemMap.getLiked().equals("no")) {
                            changeLikeStatus();
                            likeItem();
                        } else {
                            changeLikeStatus();
                            likeItem();
                        }
                    } else {
                        JoysaleApplication.dialog(DetailActivity.this, getResources().getString(R.string.error), getResources().getString(R.string.checkconnection));
                    }

                } else {
                    Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                    startActivity(j);
                }
                break;
            case R.id.userImage:
                if (itemMap.isAffiliateItem()) {
                    break;
                }
                Intent u = new Intent(DetailActivity.this, ProfileActivity.class);
                u.putExtra(Constants.TAG_USER_ID, itemMap.getSellerId());
                startActivity(u);
                break;
            case R.id.call:
                if (GetSet.isLogged()) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + itemMap.getMobileNo()));
                    /*if (ActivityCompat.checkSelfPermission(DetailActivity.this,
                            CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(DetailActivity.this, new String[]{CALL_PHONE}, 101);
                    } else {*/
                    fromCall = true;
                    startActivity(callIntent);
                    //}
                } else {
                    Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                    startActivity(j);
                }
                break;
            case R.id.btnFacebook:
                final String fbContent = JoysaleApplication.stripHtml(itemMap.getItemTitle()) + "\n\n" + itemMap.getProductUrl();
                final String fbPackage = "com.facebook.katana";
                boolean hasFacbook = appInstalledOrNot(fbPackage);
                if (hasFacbook) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.setPackage(fbPackage);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, fbContent);
                    sendIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent, getString(R.string.app_name)));
                } else {
                    Toast.makeText(DetailActivity.this, getString(R.string.fb_messenger_error), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnTwitter:
                final String twitterContent = JoysaleApplication.stripHtml(itemMap.getItemTitle()) + " " + itemMap.getProductUrl();
                final String twiiterPackage = "com.twitter.android";
                boolean hasTwitter = appInstalledOrNot(twiiterPackage);
                if (hasTwitter) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, twitterContent);
                    sendIntent.setType("text/plain");
                    sendIntent.setPackage(twiiterPackage);
                    startActivity(sendIntent);
                } else {
                    Toast.makeText(DetailActivity.this, getString(R.string.twitter_messenger_error), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnWhatsapp:
                final String instaContent = JoysaleApplication.stripHtml(itemMap.getItemTitle()) + "\n\n" + itemMap.getProductUrl();
                String instaPackage = "com.whatsapp";
                boolean hasInsta = appInstalledOrNot(instaPackage);
                if (hasInsta) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, instaContent);
                    sendIntent.setType("text/plain");
                    sendIntent.setPackage(instaPackage);
                    startActivity(sendIntent);
                } else {
                    Toast.makeText(DetailActivity.this, getString(R.string.whatsapp_messenger_error), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnMore:
                final String shareContent = getString(R.string.invite_content) + " " + itemMap.getProductUrl();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, shareContent);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
                break;
            case R.id.youtubeLay:
                Intent intent = YouTubeStandalonePlayer.createVideoIntent(this, Constants.API_KEY, AppUtils.extractYoutubeId(itemMap.getYoutubeLink()));
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                break;
            case R.id.promoCopyBtn:
                copyPromoCode();
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            int permissionCamera = ContextCompat.checkSelfPermission(DetailActivity.this,
                    CAMERA);
            int permissionStorage = ContextCompat.checkSelfPermission(DetailActivity.this,
                    WRITE_EXTERNAL_STORAGE);

            if (SDK_INT >= Build.VERSION_CODES.Q) {
                if (permissionCamera == PackageManager.PERMISSION_GRANTED) {
                    Intent a = new Intent(DetailActivity.this, AddProductDetail.class);
                    a.putExtra(Constants.TAG_FROM, "edit");
                    a.putExtra(Constants.TAG_DATA, itemMap);
                    Log.d(TAG, "onClickItem: " + itemMap.getItemCondition());
                    //     Log.d(TAG, "onClick: "+itemMap.getFilters().get(0));
                    startActivity(a);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.need_permission_to_access), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, 100);
                }
            } else {
                if (permissionCamera == PackageManager.PERMISSION_GRANTED &&
                        permissionStorage == PackageManager.PERMISSION_GRANTED) {
                    Intent a = new Intent(DetailActivity.this, AddProductDetail.class);
                    a.putExtra(Constants.TAG_FROM, "edit");
                    a.putExtra(Constants.TAG_DATA, itemMap);
                    Log.d(TAG, "onClickItem: " + itemMap.getItemCondition());
                    //     Log.d(TAG, "onClick: "+itemMap.getFilters().get(0));
                    startActivity(a);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.need_permission_to_access), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, 100);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.COMMENTS_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String comment_count = "";
                if (data.hasExtra(Constants.TAG_COMMENTCOUNT)) {
                    comment_count = "" + data.getLongExtra(Constants.TAG_COMMENTCOUNT, 0);
                }
                itemMap.setCommentsCount(comment_count);
                commentCount.setText(comment_count + " " + getResources().getString(R.string.comments));
                switch (from) {
                    case "home":
                        notifyPage(FragmentMainActivity.homeItemList, position, comment_count);
                        FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                        break;
                    case "mylisting":
                        notifyPage(MyListing.addedItems, position, comment_count);
                        MyListing.itemAdapter.notifyDataSetChanged();
                        break;
                    case "liked":
                        notifyPage(LikedItems.likedItems, position, comment_count);
                        LikedItems.itemAdapter.notifyDataSetChanged();
                        break;
                }
            }
        }
        //Addon BuyNow
        else if (requestCode == Constants.ADDRESS_REQUEST_CODE && resultCode == RESULT_OK) {
            Intent i = new Intent(DetailActivity.this, Checkout.class);
            ShippingAddressRes.Result shippingResult;
            if (data != null && data.hasExtra(Constants.TAG_SHIPPING_DATA)) {
                shippingResult = (ShippingAddressRes.Result) data.getExtras().get(Constants.TAG_SHIPPING_DATA);
                i.putExtra(Constants.TAG_SHIPPING_DATA, shippingResult);
            }
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            i.putExtra(Constants.TAG_ITEM_DATA, itemMap);
            //i.putExtra(Constants.TAG_TOKEN, clientToken);
            startActivity(i);
        } else if (requestCode == Constants.ADDRESS_REQ_CODE && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra(Constants.TAG_DEFAULTSHIPPING)) {
                if (!data.getBooleanExtra(Constants.TAG_DEFAULTSHIPPING, false)) {
                    if (data.hasExtra(Constants.TAG_SHIPPING_DATA)) {
                        ShippingAddressRes.Result selectedAddress = (ShippingAddressRes.Result) data.getSerializableExtra(Constants.TAG_SHIPPING_DATA);
                        Intent i = new Intent(DetailActivity.this, Checkout.class);
                        i.putExtra(Constants.TAG_ITEM_DATA, itemMap);
                        i.putExtra(Constants.TAG_SHIPPING_DATA, selectedAddress);
                        //i.putExtra(Constants.TAG_TOKEN, clientToken);
                        startActivity(i);
                    }
                }
            }
        }
    }

    private void notifyPage(List<HomeItemResponse.Item> data, int pos, String comment_count) {
        try {
            data.get(pos).setCommentsCount(comment_count);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager packManager = getPackageManager();
        try {
            packManager.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return packManager.getApplicationInfo("" + uri, 0).enabled;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void openDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this, com.google.android.material.R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        builder.setMessage(getString(R.string.account_delete));
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intent = new Intent(DetailActivity.this, FragmentMainActivity.class);
                startActivity(intent);

            }
        });
//        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//            }
//        });
        AlertDialog dialog = builder.create();
        dialog.show();
        Typeface typeface = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeface = getResources().getFont(R.font.font_regular);
        } else {
            typeface = ResourcesCompat.getFont(this, R.font.font_regular);
        }
        TextView textView = dialog.findViewById(android.R.id.message);
        textView.setTypeface(typeface);

        Button btn1 = dialog.findViewById(android.R.id.button1);
        btn1.setTypeface(typeface);

        Button btn2 = dialog.findViewById(android.R.id.button2);
        btn2.setTypeface(typeface);
        dialog.setCancelable(false);

    }
}
