
package com.ni.model;

import com.google.gson.annotations.Expose;

import java.io.Serializable;


public class DeleteResponse implements Serializable {


    @Expose
    private String status;

    @Expose
    private String message;
    @Expose
    private String status_code;


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

    public void setStatus_code(String status_code) {
        this.status_code = status_code;
    }

    public String getStatus_code() {
        return status_code;
    }


}