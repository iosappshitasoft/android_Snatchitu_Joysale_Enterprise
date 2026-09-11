package com.ni.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class AffiliateItemsResponse implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("result")
    private List<AffiliateItem> result;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<AffiliateItem> getResult() {
        return result;
    }

    public void setResult(List<AffiliateItem> result) {
        this.result = result;
    }

    public static class AffiliateItem implements Serializable {

        @SerializedName("item_id")
        private String itemId;

        @SerializedName("item_title")
        private String itemTitle;

        @SerializedName("item_description")
        private String itemDescription;

        @SerializedName("item_price")
        private String itemPrice;

        @SerializedName("currency")
        private String currency;

        @SerializedName("category_id")
        private String categoryId;

        @SerializedName("user_id")
        private String userId;

        @SerializedName("user_name")
        private String userName;

        @SerializedName("user_image")
        private String userImage;

        @SerializedName("created_date")
        private String createdDate;

        @SerializedName("photos")
        private List<HomeItemResponse.Photo> photos;

        @SerializedName("affiliate_url")
        private String affiliateUrl;

        @SerializedName("promo_code")
        private String promoCode;

        @SerializedName("discount_percent")
        private String discountPercent;

        @SerializedName("discount_percentage")
        private String discountPercentage;

        @SerializedName("is_affiliate")
        private String isAffiliate;

        public String getItemId() {
            return itemId;
        }

        public void setItemId(String itemId) {
            this.itemId = itemId;
        }

        public String getItemTitle() {
            return itemTitle;
        }

        public void setItemTitle(String itemTitle) {
            this.itemTitle = itemTitle;
        }

        public String getItemDescription() {
            return itemDescription;
        }

        public void setItemDescription(String itemDescription) {
            this.itemDescription = itemDescription;
        }

        public String getItemPrice() {
            return itemPrice;
        }

        public void setItemPrice(String itemPrice) {
            this.itemPrice = itemPrice;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public String getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(String categoryId) {
            this.categoryId = categoryId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getUserImage() {
            return userImage;
        }

        public void setUserImage(String userImage) {
            this.userImage = userImage;
        }

        public String getCreatedDate() {
            return createdDate;
        }

        public void setCreatedDate(String createdDate) {
            this.createdDate = createdDate;
        }

        public List<HomeItemResponse.Photo> getPhotos() {
            return photos;
        }

        public void setPhotos(List<HomeItemResponse.Photo> photos) {
            this.photos = photos;
        }

        public String getAffiliateUrl() {
            return affiliateUrl;
        }

        public void setAffiliateUrl(String affiliateUrl) {
            this.affiliateUrl = affiliateUrl;
        }

        public String getPromoCode() {
            return promoCode;
        }

        public void setPromoCode(String promoCode) {
            this.promoCode = promoCode;
        }

        public String getDiscountPercent() {
            return discountPercent;
        }

        public void setDiscountPercent(String discountPercent) {
            this.discountPercent = discountPercent;
        }

        public String getDiscountPercentage() {
            return discountPercentage;
        }

        public void setDiscountPercentage(String discountPercentage) {
            this.discountPercentage = discountPercentage;
        }

        public String getIsAffiliate() {
            return isAffiliate;
        }

        public void setIsAffiliate(String isAffiliate) {
            this.isAffiliate = isAffiliate;
        }
    }
}
