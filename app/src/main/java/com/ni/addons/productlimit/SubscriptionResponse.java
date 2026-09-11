package com.ni.addons.productlimit;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class SubscriptionResponse{

	@SerializedName("result")
	private List<ResultItem> result;

	@SerializedName("status")
	private String status;

	public void setResult(List<ResultItem> result){
		this.result = result;
	}

	public List<ResultItem> getResult(){
		return result;
	}

	public void setStatus(String status){
		this.status = status;
	}

	public String getStatus(){
		return status;
	}
}