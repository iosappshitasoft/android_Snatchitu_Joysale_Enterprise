package com.ni.utils;

import com.google.gson.annotations.SerializedName;

// ClientToken.java
public class ClientToken {

  @SerializedName("value")
  private String value;

  public String getValue() {
    return value;
  }
}