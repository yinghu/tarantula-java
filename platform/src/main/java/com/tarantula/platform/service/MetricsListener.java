package com.tarantula.platform.service;

public interface MetricsListener {

    void onUpdated(String mkey,double delta);
}
