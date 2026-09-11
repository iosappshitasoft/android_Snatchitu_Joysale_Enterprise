package com.ni.helper;

import android.content.Intent;

public class PaymentEvent {
    private Intent paymentResult;
    private int requestCode;
    private int resultCode;

    public PaymentEvent(int requestCode, int resultCode, Intent data) {
        this.paymentResult = data;
        this.requestCode = requestCode;
        this.resultCode = resultCode;
    }

    public Intent getPaymentResult() {
        return paymentResult;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public int getResultCode() {
        return resultCode;
    }
}
