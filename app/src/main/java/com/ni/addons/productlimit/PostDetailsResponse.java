package com.ni.addons.productlimit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public class PostDetailsResponse implements Serializable {

    @Expose
    private List<Result> result;

    public Collection<? extends Result> getResult() {
        return result;
    }

    public void setResult(List<Result> result) {
        this.result = result;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Expose
    private String status;


    public class Result implements Serializable {

        @SerializedName("freepost")
        private String freepost;
        @SerializedName("balance_post")
        private String balance_post;

        public int getSubscription_enable() {
            return subscription_enable;
        }

        public void setSubscription_enable(int subscription_enable) {
            this.subscription_enable = subscription_enable;
        }

        @SerializedName("subscription_enable")
        private int subscription_enable;

        public String getFreepost() {
            return freepost;
        }

        public void setFreepost(String freepost) {
            this.freepost = freepost;
        }

        public String getBalance_post() {
            return balance_post;
        }

        public void setBalance_post(String balance_post) {
            this.balance_post = balance_post;
        }

        public String getTotalpost() {
            return totalpost;
        }

        public void setTotalpost(String totalpost) {
            this.totalpost = totalpost;
        }

        @SerializedName("total_post")
        private String totalpost;

    }



}
