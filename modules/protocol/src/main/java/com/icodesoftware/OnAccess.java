package com.icodesoftware;

public interface OnAccess extends OnApplication, DataStore.Updatable {

    String ACCESS_KEY = "accessKey";
    String ACCESS_ID = "accessId";
    String COMMAND = "command";
    String SERVICE_TAG = "serviceTag";
    String PAYLOAD = "payload";
    String PASSWORD = "password";
    String VALIDATOR = "validator";
    String PRIMARY_USER = "primary";
    String VALIDATED = "validated";
    String ACTIVATED = "activated";
    //String BALANCE = "balance";
    String EMAIL_ADDRESS = "emailAddress";

    String GOOGLE = "google";
    String STRIPE = "stripe";
    String FACEBOOK = "facebook";
    String AMAZON = "amazon";

    String APPLE_STORE = "appleStore";
    String GAME_CENTER = "gameCenter";

    String GOOGLE_PLAY = "googlePlay";
    String GOOGLE_STORE = "googleStore";

    String DEVELOPER_STORE = "developerStore";

    String APPLICATION_STORE = "applicationStore";

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
    String STORE_BUNDLE_ID = "bundleId";
    String STORE_RECEIPT = "receipt";

    String SYSTEM_ID = "systemId";
    String SESSION = "session";
    String TYPE_ID = "typeId";
    String PROVIDER = "provider";

    String MYSQL = "mysql";
    String WEB_HOOK = "webHook";


    Object property(String name);
    void property(String name,Object value);
}
