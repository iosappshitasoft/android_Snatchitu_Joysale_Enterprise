package com.ni.addons.productlimit;

import com.google.gson.annotations.SerializedName;

public class SubResponseData {

	@SerializedName("result")
	private Result result;

	@SerializedName("status")
	private String status;

	public void setResult(Result result){
		this.result = result;
	}

	public Result getResult(){
		return result;
	}

	public void setStatus(String status){
		this.status = status;
	}

	public String getStatus(){
		return status;
	}
}