package com.icodesoftware;

public interface OnAccess extends OnApplication, DataStore.Updatable {

    String ACCESS_KEY = "accessKey";
    String ACCESS_ID = "accessId";
    String COMMAND = "command";
    String SERVICE_TAG = "serviceTag";
    String PAYLOAD = "payload";
    String PASSWORD = "password";

    String GOOGLE = "google";
    String STRIPE = "stripe";
    String FACEBOOK = "facebook";
    String APPLE_STORE = "appleStore";

    String LOGIN = "login";
    String DEVICE_ID = "deviceId";

    String MODULE_ID = "moduleId";
    String MODULE_CODE_BASE = "codebase";
    String MODULE_ARTIFACT = "moduleArtifact";
    String MODULE_VERSION = "moduleVersion";
    String MODULE_NAME = "moduleName";
    String MODULE_INDEX = "moduleIndex";
    String DESCRIPTION = "description";
    String NAME = "name";
    String ACCESS_CONTROL = "accessControl";
    String DEPLOY_PRIORITY = "deployPriority";

    String STORE_TRANSACTION_ID = "storeTransactionId";
    String STORE_PRODUCT_ID = "storeProductId";
    String STORE_QUANTITY = "storeQuantity";
    String STORE_MESSAGE = "storeMessage";

    Object property(String name);
    void property(String name,Object value);
}
