package com.tarantula;

/**
 * Updated by yinghu on 5/25/2020
 */
public interface OnAccess extends OnApplication, DataStore.Updatable {

    String ACCESS_KEY = "accessKey";
    String ACCESS_ID = "accessId";
    String PAYLOAD = "payload";
    String PASSWORD = "password";
    String GOOGLE = "google";
    String STRIPE = "stripe";

    Object property(String name);
    void property(String name,Object value);
}
