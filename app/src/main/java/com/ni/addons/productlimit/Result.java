package com.ni.addons.productlimit;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Result {

	@SerializedName("subscriptions")
	private List<SubscriptionsItem> subscriptions;

	@SerializedName("currency_symbol")
	private String currencySymbol;

	@SerializedName("currency_code")
	private String currencyCode;

	public void setSubscriptions(List<SubscriptionsItem> subscriptions){
		this.subscriptions = subscriptions;
	}

	public List<SubscriptionsItem> getSubscriptions(){
		return subscriptions;
	}

	public void setCurrencySymbol(String currencySymbol){
		this.currencySymbol = currencySymbol;
	}

	public String getCurrencySymbol(){
		return currencySymbol;
	}

	public void setCurrencyCode(String currencyCode){
		this.currencyCode = currencyCode;
	}

	public String getCurrencyCode(){
		return currencyCode;
	}
}