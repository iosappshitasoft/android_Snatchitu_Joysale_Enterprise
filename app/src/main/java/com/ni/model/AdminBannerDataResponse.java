package com.ni.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AdminBannerDataResponse {
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("result")
    @Expose
    private Result result;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public class Result {

        @SerializedName("banner1")
        @Expose
        private String banner1;
        @SerializedName("banner1Data")
        @Expose
        private List<Banner1Datum> banner1Data;
        @SerializedName("banner2")
        @Expose
        private String banner2;
        @SerializedName("banner2Data")
        @Expose
        private List<Banner1Datum> banner2Data;
        @SerializedName("banner3")
        @Expose
        private String banner3;
        @SerializedName("banner3Data")
        @Expose
        private List<Banner1Datum> banner3Data;
        @SerializedName("banner4")
        @Expose
        private String banner4;
        @SerializedName("banner4Data")
        @Expose
        private List<Banner1Datum> banner4Data;
        @SerializedName("banner5")
        @Expose
        private String banner5;
        @SerializedName("banner5Data")
        @Expose
        private List<Banner1Datum> banner5Data;
        @SerializedName("banner6Data")
        @Expose
        private List<Banner1Datum> banner6Data;
        @SerializedName("topbrands")
        @Expose
        private String topbrands;
        @SerializedName("topbrandsData")
        @Expose
        private List<TopbrandsDatum> topbrandsData;
        @SerializedName("featureproductsData")
        @Expose
        private FeatureproductsData featureproductsData;

        public class TopbrandsDatum {

            @SerializedName("categoryimage")
            @Expose
            private String categoryimage;
            @SerializedName("categoryname")
            @Expose
            private String categoryname;
            @SerializedName("categoryid")
            @Expose
            private String categoryid;

            public String getCategoryimage() {
                return categoryimage;
            }

            public void setCategoryimage(String categoryimage) {
                this.categoryimage = categoryimage;
            }

            public String getCategoryname() {
                return categoryname;
            }

            public void setCategoryname(String categoryname) {
                this.categoryname = categoryname;
            }

            public String getCategoryid() {
                return categoryid;
            }

            public void setCategoryid(String categoryid) {
                this.categoryid = categoryid;
            }

        }
        public class FeatureproductsData {

            @SerializedName("items")
            @Expose
            private List<Item> items;

            public List<Item> getItems() {
                return items;
            }

            public void setItems(List<Item> items) {
                this.items = items;
            }

            public class Item {

                @SerializedName("id")
                @Expose
                private String id;
                @SerializedName("item_title")
                @Expose
                private String itemTitle;
                @SerializedName("item_description")
                @Expose
                private String itemDescription;
                @SerializedName("item_condition")
                @Expose
                private String itemCondition;
                @SerializedName("item_condition_id")
                @Expose
                private String itemConditionId;
                @SerializedName("price")
                @Expose
                private String price;
                @SerializedName("quantity")
                @Expose
                private String quantity;
                @SerializedName("youtube_link")
                @Expose
                private String youtubeLink;
                @SerializedName("item_status")
                @Expose
                private String itemStatus;
                @SerializedName("size")
                @Expose
                private List<Object> size;
                @SerializedName("seller_name")
                @Expose
                private String sellerName;
                @SerializedName("seller_username")
                @Expose
                private String sellerUsername;
                @SerializedName("seller_id")
                @Expose
                private String sellerId;
                @SerializedName("seller_img")
                @Expose
                private String sellerImg;
                @SerializedName("seller_rating")
                @Expose
                private Object sellerRating;
                @SerializedName("rating_user_count")
                @Expose
                private String ratingUserCount;
                @SerializedName("mobile_no")
                @Expose
                private Object mobileNo;
                @SerializedName("show_seller_mobile")
                @Expose
                private String showSellerMobile;
                @SerializedName("facebook_verification")
                @Expose
                private String facebookVerification;
                @SerializedName("mobile_verification")
                @Expose
                private String mobileVerification;
                @SerializedName("email_verification")
                @Expose
                private String emailVerification;
                @SerializedName("currency_code")
                @Expose
                private String currencyCode;
                @SerializedName("currency_mode")
                @Expose
                private String currencyMode;
                @SerializedName("currency_position")
                @Expose
                private String currencyPosition;
                @SerializedName("product_url")
                @Expose
                private String productUrl;
                @SerializedName("formatted_price")
                @Expose
                private String formattedPrice;
                @SerializedName("likes_count")
                @Expose
                private String likesCount;
                @SerializedName("comments_count")
                @Expose
                private Object commentsCount;
                @SerializedName("views_count")
                @Expose
                private String viewsCount;
                @SerializedName("liked")
                @Expose
                private String liked;
                @SerializedName("report")
                @Expose
                private String report;
                @SerializedName("posted_time")
                @Expose
                private String postedTime;
                @SerializedName("latitude")
                @Expose
                private Double latitude;
                @SerializedName("longitude")
                @Expose
                private Double longitude;
                @SerializedName("location")
                @Expose
                private String location;
                @SerializedName("shipping_time")
                @Expose
                private String shippingTime;
                @SerializedName("best_offer")
                @Expose
                private String bestOffer;
                @SerializedName("buy_type")
                @Expose
                private String buyType;
                @SerializedName("paypal_id")
                @Expose
                private String paypalId;
                @SerializedName("category_id")
                @Expose
                private String categoryId;
                @SerializedName("category_name")
                @Expose
                private String categoryName;
                @SerializedName("subcat_id")
                @Expose
                private String subcatId;
                @SerializedName("subcat_name")
                @Expose
                private String subcatName;
                @SerializedName("child_category_id")
                @Expose
                private String childCategoryId;
                @SerializedName("child_category_name")
                @Expose
                private String childCategoryName;
                @SerializedName("promotion_type")
                @Expose
                private String promotionType;
                @SerializedName("item_approve")
                @Expose
                private String itemApprove;
                @SerializedName("exchange_buy")
                @Expose
                private String exchangeBuy;
                @SerializedName("make_offer")
                @Expose
                private String makeOffer;
                @SerializedName("instant_buy")
                @Expose
                private String instantBuy;
                @SerializedName("country_id")
                @Expose
                private String countryId;
                @SerializedName("shipping_cost")
                @Expose
                private String shippingCost;
                @SerializedName("total_price")
                @Expose
                private String totalPrice;
                @SerializedName("formatted_total_price")
                @Expose
                private String formattedTotalPrice;

                public List<HomeItemResponse.Photo> getPhotos() {
                    return photos;
                }

                public void setPhotos(List<HomeItemResponse.Photo> photos) {
                    this.photos = photos;
                }

                @SerializedName("photos")
                @Expose
                private List<HomeItemResponse.Photo> photos;
                @SerializedName("filters")
                @Expose
                private List<BeforeAddResponse.Filters> filters;

                public String getId() {
                    return id;
                }

                public void setId(String id) {
                    this.id = id;
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

                public String getItemCondition() {
                    return itemCondition;
                }

                public void setItemCondition(String itemCondition) {
                    this.itemCondition = itemCondition;
                }

                public String getItemConditionId() {
                    return itemConditionId;
                }

                public void setItemConditionId(String itemConditionId) {
                    this.itemConditionId = itemConditionId;
                }

                public String getPrice() {
                    return price;
                }

                public void setPrice(String price) {
                    this.price = price;
                }

                public String getQuantity() {
                    return quantity;
                }

                public void setQuantity(String quantity) {
                    this.quantity = quantity;
                }

                public String getYoutubeLink() {
                    return youtubeLink;
                }

                public void setYoutubeLink(String youtubeLink) {
                    this.youtubeLink = youtubeLink;
                }

                public String getItemStatus() {
                    return itemStatus;
                }

                public void setItemStatus(String itemStatus) {
                    this.itemStatus = itemStatus;
                }

                public List<Object> getSize() {
                    return size;
                }

                public void setSize(List<Object> size) {
                    this.size = size;
                }

                public String getSellerName() {
                    return sellerName;
                }

                public void setSellerName(String sellerName) {
                    this.sellerName = sellerName;
                }

                public String getSellerUsername() {
                    return sellerUsername;
                }

                public void setSellerUsername(String sellerUsername) {
                    this.sellerUsername = sellerUsername;
                }

                public String getSellerId() {
                    return sellerId;
                }

                public void setSellerId(String sellerId) {
                    this.sellerId = sellerId;
                }

                public String getSellerImg() {
                    return sellerImg;
                }

                public void setSellerImg(String sellerImg) {
                    this.sellerImg = sellerImg;
                }

                public Object getSellerRating() {
                    return sellerRating;
                }

                public void setSellerRating(Object sellerRating) {
                    this.sellerRating = sellerRating;
                }

                public String getRatingUserCount() {
                    return ratingUserCount;
                }

                public void setRatingUserCount(String ratingUserCount) {
                    this.ratingUserCount = ratingUserCount;
                }

                public Object getMobileNo() {
                    return mobileNo;
                }

                public void setMobileNo(Object mobileNo) {
                    this.mobileNo = mobileNo;
                }

                public String getShowSellerMobile() {
                    return showSellerMobile;
                }

                public void setShowSellerMobile(String showSellerMobile) {
                    this.showSellerMobile = showSellerMobile;
                }

                public String getFacebookVerification() {
                    return facebookVerification;
                }

                public void setFacebookVerification(String facebookVerification) {
                    this.facebookVerification = facebookVerification;
                }

                public String getMobileVerification() {
                    return mobileVerification;
                }

                public void setMobileVerification(String mobileVerification) {
                    this.mobileVerification = mobileVerification;
                }

                public String getEmailVerification() {
                    return emailVerification;
                }

                public void setEmailVerification(String emailVerification) {
                    this.emailVerification = emailVerification;
                }

                public String getCurrencyCode() {
                    return currencyCode;
                }

                public void setCurrencyCode(String currencyCode) {
                    this.currencyCode = currencyCode;
                }

                public String getCurrencyMode() {
                    return currencyMode;
                }

                public void setCurrencyMode(String currencyMode) {
                    this.currencyMode = currencyMode;
                }

                public String getCurrencyPosition() {
                    return currencyPosition;
                }

                public void setCurrencyPosition(String currencyPosition) {
                    this.currencyPosition = currencyPosition;
                }

                public String getProductUrl() {
                    return productUrl;
                }

                public void setProductUrl(String productUrl) {
                    this.productUrl = productUrl;
                }

                public String getFormattedPrice() {
                    return formattedPrice;
                }

                public void setFormattedPrice(String formattedPrice) {
                    this.formattedPrice = formattedPrice;
                }

                public String getLikesCount() {
                    return likesCount;
                }

                public void setLikesCount(String likesCount) {
                    this.likesCount = likesCount;
                }

                public Object getCommentsCount() {
                    return commentsCount;
                }

                public void setCommentsCount(Object commentsCount) {
                    this.commentsCount = commentsCount;
                }

                public String getViewsCount() {
                    return viewsCount;
                }

                public void setViewsCount(String viewsCount) {
                    this.viewsCount = viewsCount;
                }

                public String getLiked() {
                    return liked;
                }

                public void setLiked(String liked) {
                    this.liked = liked;
                }

                public String getReport() {
                    return report;
                }

                public void setReport(String report) {
                    this.report = report;
                }

                public String getPostedTime() {
                    return postedTime;
                }

                public void setPostedTime(String postedTime) {
                    this.postedTime = postedTime;
                }

                public Double getLatitude() {
                    return latitude;
                }

                public void setLatitude(Double latitude) {
                    this.latitude = latitude;
                }

                public Double getLongitude() {
                    return longitude;
                }

                public void setLongitude(Double longitude) {
                    this.longitude = longitude;
                }

                public String getLocation() {
                    return location;
                }

                public void setLocation(String location) {
                    this.location = location;
                }

                public String getShippingTime() {
                    return shippingTime;
                }

                public void setShippingTime(String shippingTime) {
                    this.shippingTime = shippingTime;
                }

                public String getBestOffer() {
                    return bestOffer;
                }

                public void setBestOffer(String bestOffer) {
                    this.bestOffer = bestOffer;
                }

                public String getBuyType() {
                    return buyType;
                }

                public void setBuyType(String buyType) {
                    this.buyType = buyType;
                }

                public String getPaypalId() {
                    return paypalId;
                }

                public void setPaypalId(String paypalId) {
                    this.paypalId = paypalId;
                }

                public String getCategoryId() {
                    return categoryId;
                }

                public void setCategoryId(String categoryId) {
                    this.categoryId = categoryId;
                }

                public String getCategoryName() {
                    return categoryName;
                }

                public void setCategoryName(String categoryName) {
                    this.categoryName = categoryName;
                }

                public String getSubcatId() {
                    return subcatId;
                }

                public void setSubcatId(String subcatId) {
                    this.subcatId = subcatId;
                }

                public String getSubcatName() {
                    return subcatName;
                }

                public void setSubcatName(String subcatName) {
                    this.subcatName = subcatName;
                }

                public String getChildCategoryId() {
                    return childCategoryId;
                }

                public void setChildCategoryId(String childCategoryId) {
                    this.childCategoryId = childCategoryId;
                }

                public String getChildCategoryName() {
                    return childCategoryName;
                }

                public void setChildCategoryName(String childCategoryName) {
                    this.childCategoryName = childCategoryName;
                }

                public String getPromotionType() {
                    return promotionType;
                }

                public void setPromotionType(String promotionType) {
                    this.promotionType = promotionType;
                }

                public String getItemApprove() {
                    return itemApprove;
                }

                public void setItemApprove(String itemApprove) {
                    this.itemApprove = itemApprove;
                }

                public String getExchangeBuy() {
                    return exchangeBuy;
                }

                public void setExchangeBuy(String exchangeBuy) {
                    this.exchangeBuy = exchangeBuy;
                }

                public String getMakeOffer() {
                    return makeOffer;
                }

                public void setMakeOffer(String makeOffer) {
                    this.makeOffer = makeOffer;
                }

                public String getInstantBuy() {
                    return instantBuy;
                }

                public void setInstantBuy(String instantBuy) {
                    this.instantBuy = instantBuy;
                }

                public String getCountryId() {
                    return countryId;
                }

                public void setCountryId(String countryId) {
                    this.countryId = countryId;
                }

                public String getShippingCost() {
                    return shippingCost;
                }

                public void setShippingCost(String shippingCost) {
                    this.shippingCost = shippingCost;
                }

                public String getTotalPrice() {
                    return totalPrice;
                }

                public void setTotalPrice(String totalPrice) {
                    this.totalPrice = totalPrice;
                }

                public String getFormattedTotalPrice() {
                    return formattedTotalPrice;
                }

                public void setFormattedTotalPrice(String formattedTotalPrice) {
                    this.formattedTotalPrice = formattedTotalPrice;
                }


                public List<BeforeAddResponse.Filters> getFilters() {
                    return filters;
                }

                public void setFilters(List<BeforeAddResponse.Filters> filters) {
                    this.filters = filters;
                }

                public class Photo {

                    @SerializedName("item_url_main_350")
                    @Expose
                    private String itemUrlMain350;
                    @SerializedName("height")
                    @Expose
                    private String height;
                    @SerializedName("width")
                    @Expose
                    private String width;
                    @SerializedName("item_image")
                    @Expose
                    private String itemImage;
                    @SerializedName("item_url_main_original")
                    @Expose
                    private String itemUrlMainOriginal;

                    public String getItemUrlMain350() {
                        return itemUrlMain350;
                    }

                    public void setItemUrlMain350(String itemUrlMain350) {
                        this.itemUrlMain350 = itemUrlMain350;
                    }

                    public String getHeight() {
                        return height;
                    }

                    public void setHeight(String height) {
                        this.height = height;
                    }

                    public String getWidth() {
                        return width;
                    }

                    public void setWidth(String width) {
                        this.width = width;
                    }

                    public String getItemImage() {
                        return itemImage;
                    }

                    public void setItemImage(String itemImage) {
                        this.itemImage = itemImage;
                    }

                    public String getItemUrlMainOriginal() {
                        return itemUrlMainOriginal;
                    }

                    public void setItemUrlMainOriginal(String itemUrlMainOriginal) {
                        this.itemUrlMainOriginal = itemUrlMainOriginal;
                    }

                }

            }
        }

        public String getBanner1() {
            return banner1;
        }

        public void setBanner1(String banner1) {
            this.banner1 = banner1;
        }

        public List<Banner1Datum> getBanner1Data() {
            return banner1Data;
        }

        public void setBanner1Data(List<Banner1Datum> banner1Data) {
            this.banner1Data = banner1Data;
        }

        public String getBanner2() {
            return banner2;
        }

        public void setBanner2(String banner2) {
            this.banner2 = banner2;
        }

        public List<Banner1Datum> getBanner2Data() {
            return banner2Data;
        }

        public void setBanner2Data(List<Banner1Datum> banner2Data) {
            this.banner2Data = banner2Data;
        }

        public String getBanner3() {
            return banner3;
        }

        public void setBanner3(String banner3) {
            this.banner3 = banner3;
        }

        public List<Banner1Datum> getBanner3Data() {
            return banner3Data;
        }

        public void setBanner3Data(List<Banner1Datum> banner3Data) {
            this.banner3Data = banner3Data;
        }

        public String getBanner4() {
            return banner4;
        }

        public void setBanner4(String banner4) {
            this.banner4 = banner4;
        }

        public List<Banner1Datum> getBanner4Data() {
            return banner4Data;
        }

        public void setBanner4Data(List<Banner1Datum> banner4Data) {
            this.banner4Data = banner4Data;
        }

        public String getBanner5() {
            return banner5;
        }

        public void setBanner5(String banner5) {
            this.banner5 = banner5;
        }

        public List<Banner1Datum> getBanner5Data() {
            return banner5Data;
        }

        public void setBanner5Data(List<Banner1Datum> banner5Data) {
            this.banner5Data = banner5Data;
        }

        public List<Banner1Datum> getBanner6Data() {
            return banner6Data;
        }

        public void setBanner6Data(List<Banner1Datum> banner6Data) {
            this.banner6Data = banner6Data;
        }

        public String getTopbrands() {
            return topbrands;
        }

        public void setTopbrands(String topbrands) {
            this.topbrands = topbrands;
        }

        public List<TopbrandsDatum> getTopbrandsData() {
            return topbrandsData;
        }

        public void setTopbrandsData(List<TopbrandsDatum> topbrandsData) {
            this.topbrandsData = topbrandsData;
        }

        public FeatureproductsData getFeatureproductsData() {
            return featureproductsData;
        }

        public void setFeatureproductsData(FeatureproductsData featureproductsData) {
            this.featureproductsData = featureproductsData;
        }


        public class Banner1Datum {

            @SerializedName("bannerImage")
            @Expose
            private String bannerImage;
            @SerializedName("bannerURL")
            @Expose
            private String bannerURL;
            @SerializedName("bannerTitle")
            @Expose
            private String bannerTitle;
            @SerializedName("bannerContent")
            @Expose
            private String bannerContent;

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

        }
    }
}
