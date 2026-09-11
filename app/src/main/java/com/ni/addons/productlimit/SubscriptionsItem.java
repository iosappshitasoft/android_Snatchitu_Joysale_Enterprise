package com.ni.addons.productlimit;

import com.google.gson.annotations.SerializedName;

public class SubscriptionsItem {

	@SerializedName("listcount")
	private String listcount;

	@SerializedName("price")
	private String price;

	@SerializedName("subscription_price")
	private String subscriptionPrice;

	@SerializedName("name")
	private String name;

	@SerializedName("id")
	private String id;

	public void setListcount(String listcount){
		this.listcount = listcount;
	}

	public String getListcount(){
		return listcount;
	}

	public void setPrice(String price){
		this.price = price;
	}

	public String getPrice(){
		return price;
	}

	public void setSubscriptionPrice(String subscriptionPrice){
		this.subscriptionPrice = subscriptionPrice;
	}

	public String getSubscriptionPrice(){
		return subscriptionPrice;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public void setId(String id){
		this.id = id;
	}

	public String getId(){
		return id;
	}
}