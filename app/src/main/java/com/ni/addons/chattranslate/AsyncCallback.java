package com.ni.addons.chattranslate;

interface AsyncCallback {
    void onSuccess(Object object);

    void onError(Object failure);
}