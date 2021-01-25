package com.icodesoftware;

/**
 * Updated by yinghu on 5/25/2020
 */
public interface OnAccess extends OnApplication, DataStore.Updatable {

    String ACCESS_KEY = "accessKey";
    String ACCESS_ID = "accessId";
    String COMMAND = "command";
    String SERVICE_TAG = "serviceTag";
    String PAYLOAD = "payload";
    String PASSWORD = "password";
    String GOOGLE = "google";
    String STRIPE = "stripe";

    String LOGIN = "login";
    String DEVICE_ID = "deviceId";
    String MODULE_ID = "moduleId";
    String MODULE_CODE_BASE = "codebase";
    String MODULE_ARTIFACT = "moduleArtifact";
    String MODULE_VERSION = "moduleVersion";

    Object property(String name);
    void property(String name,Object value);
}
