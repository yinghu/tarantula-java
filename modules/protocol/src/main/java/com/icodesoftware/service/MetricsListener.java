package com.icodesoftware.service;

public interface MetricsListener {

    void onUpdated(String mkey,double delta);
}
