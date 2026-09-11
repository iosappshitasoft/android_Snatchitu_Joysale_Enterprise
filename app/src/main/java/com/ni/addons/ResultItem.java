package com.ni.addons;

import com.google.gson.annotations.SerializedName;

public class ResultItem{

	@SerializedName("transaction_id")
	private String transactionId;

	@SerializedName("paid_amount")
	private Double paidAmount;

	@SerializedName("total_post")
	private int totalPost;

	@SerializedName("current_status")
	private String currentStatus;

	@SerializedName("available_posts")
	private int availablePosts;

	@SerializedName("subscription_plan")
	private String subscriptionPlan;

	@SerializedName("status")
	private String status;

	public void setTransactionId(String transactionId){
		this.transactionId = transactionId;
	}

	public String getTransactionId(){
		return transactionId;
	}

	public void setPaidAmount(Double paidAmount){
		this.paidAmount = paidAmount;
	}

	public Double getPaidAmount(){
		return paidAmount;
	}

	public void setTotalPost(int totalPost){
		this.totalPost = totalPost;
	}

	public int getTotalPost(){
		return totalPost;
	}

	public void setCurrentStatus(String currentStatus){
		this.currentStatus = currentStatus;
	}

	public String getCurrentStatus(){
		return currentStatus;
	}

	public void setAvailablePosts(int availablePosts){
		this.availablePosts = availablePosts;
	}

	public int getAvailablePosts(){
		return availablePosts;
	}

	public void setSubscriptionPlan(String subscriptionPlan){
		this.subscriptionPlan = subscriptionPlan;
	}

	public String getSubscriptionPlan(){
		return subscriptionPlan;
	}

	public void setStatus(String status){
		this.status = status;
	}

	public String getStatus(){
		return status;
	}
}