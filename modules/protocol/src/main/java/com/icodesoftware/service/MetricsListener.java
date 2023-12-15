package com.icodesoftware.service;

public interface MetricsListener {

    void onUpdated(String category,double delta);
}
