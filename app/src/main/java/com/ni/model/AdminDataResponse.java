
package com.ni.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class AdminDataResponse implements Serializable {

    @Expose
    private Result result;
    @Expose
    private String status;

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public class Result implements Serializable {

        public String getGoogleAdsType() {
            return googleAdsType;
        }

        public void setGoogleAdsType(String googleAdsType) {
            this.googleAdsType = googleAdsType;
        }

        @SerializedName("admin_currency_code")
        private String adminCurrencyCode;
        @SerializedName("google_ads")
        private String googleAdsType;

        public String getLogo_imageurl() {
            return logo_imageurl;
        }

        public void setLogo_imageurl(String logo_imageurl) {
            this.logo_imageurl = logo_imageurl;
        }

        @SerializedName("logo_imageurl")
        private String logo_imageurl;

        public String getGoogle_ads_android() {
            return google_ads_android;
        }

        public void setGoogle_ads_android(String google_ads_android) {
            this.google_ads_android = google_ads_android;
        }

        @SerializedName("google_ads_android")
        private String google_ads_android;
        @SerializedName("mapbox_token")
        private String mapbox_token;

        public String getMapbox_token() {
            return mapbox_token;
        }

        public void setMapbox_token(String mapbox_token) {
            this.mapbox_token = mapbox_token;
        }

        @SerializedName("admin_payment_type")
        private String adminPaymentType;

        public String getApprtcUrl() {
            return apprtcUrl;
        }

        public void setApprtcUrl(String apprtcUrl) {
            this.apprtcUrl = apprtcUrl;
        }

        public String getInterstitial_ad_key() {
            return interstitial_ad_key;
        }

        public void setInterstitial_ad_key(String interstitial_ad_key) {
            this.interstitial_ad_key = interstitial_ad_key;
        }

        @SerializedName("apprtc_url")
        private String apprtcUrl;
        @SerializedName("interstitial_ad_key")
        private String interstitial_ad_key;
        @SerializedName("banner")
        private String banner;
        @SerializedName("bannerData")
        private List<BannerData> bannerData;
        @SerializedName("block")
        private String block;
        @SerializedName("buynow")
        private String buyNow;
        @SerializedName("socket_url")
        private String mSocketUrl;
        @SerializedName("category")
        private List<BeforeAddResponse.Category> category;
        @SerializedName("chat_template")
        private List<ChatTemplate> chatTemplate;
        @SerializedName("distance_type")
        private String distanceType;
        @SerializedName("exchange")
        private String exchange;
        @SerializedName("facebook_appid")
        private String facebookAppid;
        @SerializedName("facebook_secret")
        private String facebookSecret;
        @SerializedName("giving_away")
        private String givingAway;
        @SerializedName("paid_banner")
        private String paidBanner;
        @SerializedName("site_maintenance")
        private String siteMaintenance;
        @SerializedName("site_maintenance_text")
        private String siteMaintenanceText;
        @SerializedName("promotion")
        private String promotion;
        @SerializedName("price_range")
        private PriceRange priceRange;
        @SerializedName("stripePublicKey")
        private String stripePublicKey;
        @SerializedName("email_verification")
        private String emailVerification;
        @SerializedName("distance")
        private String distance;

        public String getEmailVerification() {
            return emailVerification;
        }

        public void setEmailVerification(String emailVerification) {
            this.emailVerification = emailVerification;
        }


        public String getBanner() {
            return banner;
        }

        public void setBanner(String banner) {
            this.banner = banner;
        }

        public List<BannerData> getBannerData() {
            return bannerData;
        }

        public void setBannerData(List<BannerData> bannerData) {
            this.bannerData = bannerData;
        }

        public String getBuyNow() {
            return buyNow;
        }

        public void setBuyNow(String buyNow) {
            this.buyNow = buyNow;
        }

        public String getBlock() {
            return block;
        }

        public void setBlock(String block) {
            this.block = block;
        }

        public List<BeforeAddResponse.Category> getCategory() {
            return category;
        }

        public void setCategory(List<BeforeAddResponse.Category> category) {
            this.category = category;
        }

        public String getmSocketUrl() {
            return mSocketUrl;
        }

        public void setmSocketUrl(String mSocketUrl) {
            this.mSocketUrl = mSocketUrl;
        }

        public List<ChatTemplate> getChatTemplate() {
            return chatTemplate;
        }

        public void setChatTemplate(List<ChatTemplate> chatTemplate) {
            this.chatTemplate = chatTemplate;
        }

        public String getDistanceType() {
            return distanceType;
        }

        public void setDistanceType(String distanceType) {
            this.distanceType = distanceType;
        }

        public String getDistance() {
            return distance;
        }

        public void setDistance(String distance) {
            this.distance = distance;
        }

        public String getExchange() {
            return exchange;
        }

        public void setExchange(String exchange) {
            this.exchange = exchange;
        }

        public String getFacebookAppid() {
            return facebookAppid;
        }

        public void setFacebookAppid(String facebookAppid) {
            this.facebookAppid = facebookAppid;
        }

        public String getFacebookSecret() {
            return facebookSecret;
        }

        public void setFacebookSecret(String facebookSecret) {
            this.facebookSecret = facebookSecret;
        }

        public String getGivingAway() {
            return givingAway;
        }

        public void setGivingAway(String givingAway) {
            this.givingAway = givingAway;
        }

        public String getPromotion() {
            return promotion;
        }

        public void setPromotion(String promotion) {
            this.promotion = promotion;
        }

        public String getPaidBanner() {
            return paidBanner;
        }

        public void setPaidBanner(String paidBanner) {
            this.paidBanner = paidBanner;
        }

        public String getSiteMaintenance() {
            return siteMaintenance;
        }

        public void setSiteMaintenance(String siteMaintenance) {
            this.siteMaintenance = siteMaintenance;
        }

        public String getSiteMaintenanceText() {
            return siteMaintenanceText;
        }

        public void setSiteMaintenanceText(String siteMaintenanceText) {
            this.siteMaintenanceText = siteMaintenanceText;
        }

        public PriceRange getPriceRange() {
            return priceRange;
        }

        public void setPriceRange(PriceRange priceRange) {
            this.priceRange = priceRange;
        }

        public String getStripePublicKey() {
            return stripePublicKey;
        }

        public void setStripePublicKey(String stripePublicKey) {
            this.stripePublicKey = stripePublicKey;
        }

        public String getAdminCurrencyCode() {
            return adminCurrencyCode;
        }

        public void setAdminCurrencyCode(String adminCurrencyCode) {
            this.adminCurrencyCode = adminCurrencyCode;
        }

        public String getAdminPaymentType() {
            return adminPaymentType;
        }

        public void setAdminPaymentType(String adminPaymentType) {
            this.adminPaymentType = adminPaymentType;
        }
    }

    public class BannerData implements Serializable {
        @SerializedName("bannerImage")
        public String bannerImage;
        @SerializedName("bannerURL")
        public String bannerURL;
        @SerializedName("bannerTitle")
        public String bannerTitle;
        @SerializedName("bannerContent")
        public String bannerContent;

        public String getLogo_imageurl() {
            return logo_imageurl;
        }

        public void setLogo_imageurl(String logo_imageurl) {
            this.logo_imageurl = logo_imageurl;
        }

        @SerializedName("logo_imageurl")
        public String logo_imageurl;

        public String getBannerTitle() {
            return bannerTitle;
        }

        public void setBannerTitle(String bannerTitle) {
            this.bannerTitle = bannerTitle;
        }

        public String getBannerContent() {
            return bannerContent;
        }

        public void setBannerContent(String bannerContent) {
            this.bannerContent = bannerContent;
        }
        public String getBannerImage() {
            return bannerImage;
        }

        public void setBannerImage(String bannerImage) {
            this.bannerImage = bannerImage;
        }

        public String getBannerURL() {
            return bannerURL;
        }

        public void setBannerURL(String bannerURL) {
            this.bannerURL = bannerURL;
        }
    }

    public class Category implements Serializable {

        @SerializedName("category_id")
        private String categoryId;
        @SerializedName("category_img")
        private String categoryImg;
        @SerializedName("category_name")
        private String categoryName;

        public String getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(String categoryId) {
            this.categoryId = categoryId;
        }

        public String getCategoryImg() {
            return categoryImg;
        }

        public void setCategoryImg(String categoryImg) {
            this.categoryImg = categoryImg;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

    }

    public class ChatTemplate implements Serializable {

        @SerializedName("name")
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    public class PriceRange implements Serializable {
        @SerializedName("before_decimal_notation")
        private long beforeDecimal;
        @SerializedName("after_decimal_notation")
        private long afterDecimal;

        public long getBeforeDecimal() {
            return beforeDecimal;
        }

        public void setBeforeDecimal(long beforeDecimal) {
            this.beforeDecimal = beforeDecimal;
        }

        public long getAfterDecimal() {
            return afterDecimal;
        }

        public void setAfterDecimal(long afterDecimal) {
            this.afterDecimal = afterDecimal;
        }
    }

}
