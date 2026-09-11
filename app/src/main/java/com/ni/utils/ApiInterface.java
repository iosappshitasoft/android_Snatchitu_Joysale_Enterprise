package com.ni.utils;


/*import com.app.addons.productlimit.PostDetailsResponse;
import com.app.addons.productlimit.SubResponseData;
import com.app.addons.productlimit.SubscriptionResponse;*/

import com.ni.addons.productlimit.PostDetailsResponse;
import com.ni.addons.productlimit.SubResponseData;
import com.ni.addons.productlimit.SubscriptionResponse;
import com.ni.model.AdHistoryResponse;
import com.ni.model.AddAddressResponse;
import com.ni.model.AddProductResponse;
import com.ni.model.AdminBannerDataResponse;
import com.ni.model.AdminDataResponse;
import com.ni.model.AffiliateItemsResponse;
import com.ni.model.BeforeAddResponse;
import com.ni.model.CategoryResponse;
import com.ni.model.ChatResponse;
import com.ni.model.CheckPromotionResponse;
import com.ni.model.CheckPromoResponse;
import com.ni.model.CommentsResponse;
import com.ni.model.DeleteResponse;
import com.ni.model.ExchangeResponse;
import com.ni.model.FollowersResponse;
import com.ni.model.GetAdResponse;
import com.ni.model.GetCountResponse;
import com.ni.model.HelpResponse;
import com.ni.model.HomeItemResponse;
import com.ni.model.HomeProductResponse;
import com.ni.model.InsightsResponse;
import com.ni.model.LoginResponse;
import com.ni.model.MessagesResponse;
import com.ni.model.MyPromotionResponse;
import com.ni.model.MySalesResponse;
import com.ni.model.NotificationResponse;
import com.ni.model.ProfileResponse;
import com.ni.model.PromotionResponse;
import com.ni.model.ReadMessageResponse;
import com.ni.model.ReviewObject;
import com.ni.model.ReviewResponse;
import com.ni.model.SendCommentResponse;
import com.ni.model.ShippingAddressRes;
import com.ni.model.TrackingResponse;
import com.ni.model.UpdateOfferResponse;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiInterface {

    @Multipart
    @POST("uploadimage")
    Call<Map<String, String>> uploadProfileImage(@Part MultipartBody.Part image, @Part("user_id") RequestBody user_id);

    @FormUrlEncoded
    @POST("admindatas")
    Call<AdminDataResponse> getAdminData(@FieldMap Map<String, String> fieldMap);

    @FormUrlEncoded
    @POST("homebannerdatas")
    Call<AdminBannerDataResponse> getAdminBannerData(@FieldMap Map<String, String> fieldMap);

    @FormUrlEncoded
    @POST("updatereadmessagge")
    Call<ReadMessageResponse> updateReadMessage(@FieldMap Map<String, String> fieldMap);

    //Addons WebAPI
    @FormUrlEncoded
    @POST("awsupload")
    Call<ReadMessageResponse> uploadAws(@FieldMap Map<String, String> fieldMap);

    @FormUrlEncoded
    @POST("signup")
    Call<HashMap<String, String>> signUp(@FieldMap Map<String, String> fieldMap);

    @FormUrlEncoded
    @POST("login")
    Call<LoginResponse> logIn(@FieldMap Map<String, String> fieldMap);

    @FormUrlEncoded
    @POST("sociallogin")
    Call<LoginResponse> socialLogIn(@FieldMap Map<String, String> fieldMap);

    @FormUrlEncoded
    @POST("profile")
    Call<ProfileResponse> getProfileInformation(@FieldMap HashMap<String, String> map);

    @FormUrlEncoded
    @POST("Editprofile")
    Call<ProfileResponse> editProfile(@FieldMap HashMap<String, String> map);

    @FormUrlEncoded
    @POST("changepassword")
    Call<HashMap<String, String>> changePassword(@FieldMap HashMap<String, String> map);

    @FormUrlEncoded
    @POST("forgetpassword")
    Call<HashMap<String, String>> forgotPassword(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("adddeviceid")
    Call<ResponseBody> addDeviceId(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("pushsignout")
    Call<ResponseBody> removeDeviceId(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("getitems")
    Call<HomeItemResponse> getHomeItems(@FieldMap Map<String, String> map);

    @Headers("Content-Type: application/json")
    @POST("affiliateitems")
    Call<AffiliateItemsResponse> getAffiliateItems(@Body Map<String, String> map);

    @FormUrlEncoded
    @POST("homepageitems")
    Call<HomeProductResponse> getHomePageItems(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("getuserproducts")
    Call<HomeItemResponse> getUserProducts(@FieldMap HashMap<String, String> hashMap);

    @FormUrlEncoded
    @POST("getuserproducts")
    Call<AdminBannerDataResponse.Result.FeatureproductsData> getUserProductsnew(@FieldMap HashMap<String, String> hashMap);

    @FormUrlEncoded
    @POST("getcategory")
    Call<CategoryResponse> getCategories(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("getcountdetails")
    Call<GetCountResponse> getCountDetails(@FieldMap HashMap<String, String> apiRequest);

    @FormUrlEncoded
    @POST("Followuser")
    Call<HashMap<String, String>> followUser(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("Unfollowuser")
    Call<HashMap<String, String>> unFollowUser(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("Getfollowerid")
    Call<ResponseBody> getFollowingId(@FieldMap Map<String, String> map);

    //not used
    @FormUrlEncoded
    @POST("Getlikedid")
    Call<ResponseBody> getLikedId(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("Followersdetails")
    Call<FollowersResponse> getFollowersDetails(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("Followingdetails")
    Call<FollowersResponse> getFollowingDetails(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("myexchanges")
    Call<ExchangeResponse> getExchanges(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("getchatid")
    Call<HashMap<String, String>> getChatId(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("getchat")
    Call<ChatResponse>
    getChat(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("sendchat")
    Call<HashMap<String, String>> sendChat(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("exchangestatus")
    Call<HashMap<String, String>> changeExchangeStatus(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("searchbyitem")
    Call<HomeItemResponse> getItemData(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("updateview")
    Call<HashMap<String, String>> updateView(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("solditem")
    Call<HashMap<String, String>> changeSoldStatus(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("deleteproduct")
    Call<HashMap<String, String>> deleteProduct(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("Sendofferreq")
    Call<HashMap<String, String>> createOffer(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("checkItemstatus")
    Call<HashMap<String, String>> checkItemStatus(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("itemlike")
    Call<HashMap<String, String>> likeItem(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("reportitem")
    Call<HashMap<String, String>> reportItem(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("getShippingAddress")
    Call<ShippingAddressRes> getShippingAddress(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("addshipping")
    Call<AddAddressResponse> addShippingAddress(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("Chataction")
    Call<HashMap<String, String>> updateBlockStatus(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("SafetyTips")
    Call<HashMap<String, String>> getSafetyTips(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("createexchange")
    Call<HashMap<String, String>> createExchange(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("removeshipping")
    Call<HashMap<String, String>> removeShipping(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("setdefaultshipping")
    Call<HashMap<String, String>> setDefaultShipping(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("productbeforeadd")
    Call<BeforeAddResponse> getProductBeforeAdd(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("addproduct")
    Call<AddProductResponse> addProduct(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("getpromotion")
    Call<PromotionResponse> getPromotion(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("processingPayment")
    Call<HashMap<String, String>> payForPromotion(@FieldMap Map<String, String> map);



    @FormUrlEncoded
    @POST("mypromotions")
    Call<MyPromotionResponse> getMyPromotion(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("Checkpromotion")
    Call<CheckPromotionResponse> checkPromotion(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("getcomments")
    Call<CommentsResponse> getComments(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("postcomment")
    Call<SendCommentResponse> postComment(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("deletecomment")
    Call<HashMap<String, String>> deleteComment(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("messages")
    Call<MessagesResponse> getMessages(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("helppage")
    Call<HelpResponse> getHelpList(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("notification")
    Call<NotificationResponse> getNotifications(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("offerstatus")
    Call<UpdateOfferResponse> updateOfferStatus(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("updatereview")
    Call<HashMap<String, String>> updateReview(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("orderstatus")
    Call<HashMap<String, String>> addShippingDetails(@FieldMap Map<String, String> map);


    @FormUrlEncoded
    @POST("gettrackdetails")
    Call<TrackingResponse> getTrackDetails(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("mysales")
    Call<MySalesResponse> getMySales(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("myorders")
    Call<MySalesResponse> getMyOrders(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("getreview")
    Call<ReviewResponse> getReview(@FieldMap Map<String, String> map);

    @Headers("Content-Type: application/json")
    @POST("checkpromo")
    Call<CheckPromoResponse> checkPromo(@Body Map<String, String> map);

    @FormUrlEncoded
    @POST("buynowPayment")
    Call<HashMap<String, String>> buyNowPayment(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("getadwithus")
    Call<GetAdResponse> getAdWithUs(@FieldMap HashMap<String, String> hashMap);

    @FormUrlEncoded
    @POST("banneravailability")
    Call<ResponseBody> checkDateAvailability(@FieldMap HashMap<String, String> hashMap);

    @FormUrlEncoded
    @POST("addbanner")
    Call<HashMap<String, String>> addBanner(@FieldMap HashMap<String, String> hashMap);

    @FormUrlEncoded
    @POST("balancesheet")
    Call<HashMap<String, String>> balanceSheet(@FieldMap Map<String, String> map);


    @FormUrlEncoded
    @POST("getadhistory")
    Call<AdHistoryResponse> getAdHistory(@FieldMap HashMap<String, String> hashMap);

    @FormUrlEncoded
    @POST("getinsights")
    Call<InsightsResponse> getInsights(@FieldMap HashMap<String, String> hashMap);

    @FormUrlEncoded
    @POST("addstripedetails")
    Call<ResponseBody> addStripeDetails(@FieldMap HashMap<String, String> hashMap);

    //Addons ProductLimitation
    @FormUrlEncoded
    @POST("mysubscription")
    Call<SubscriptionResponse> getMySubscription(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("getsubscription")
    Call<SubResponseData> getSubscription(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("postdetails")
    Call<PostDetailsResponse> getMyPostDetails(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("subscriptionPayment")
    Call<HashMap<String, String>> subscriptionPayment(@FieldMap Map<String, String> map);

    //Addons Mobile OTP
    @FormUrlEncoded
    @POST("phonelogin")
    Call<LoginResponse> phoneLogin(@FieldMap Map<String, String> fieldMap);

    //Addons SoldTo
    @FormUrlEncoded
    @POST("soldto")
    Call<FollowersResponse> getSoldTo(@FieldMap Map<String, String> map);

    //Addons UserReview
    @FormUrlEncoded
    @POST("reviewdetails")
    Call<ReviewObject> reviewDetails(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("deleteaccount")
    Call<DeleteResponse> deleteAccount(@FieldMap Map<String, String> map);


    @FormUrlEncoded
    @POST("checkcallstatus")
    Call<HashMap<String, String>> getCallStatus(@FieldMap Map<String, String> map);
}