package com.tarantula.platform.service.metrics;


import com.icodesoftware.service.ServiceContext;

public class ApplicationMetrics extends AbstractMetrics{

    public final static String GOOGLE_COUNT = "googleCount";
    public final static String STRIPE_COUNT = "stripCount";
    public final static String PASSWORD_COUNT = "passwordCount";
    public final static String DEVICE_COUNT = "deviceCount";
    public final static String FACEBOOK_COUNT = "facebookCount";
    public final static String APPLE_STORE_COUNT = "appleStoreCount";
    public final static String GAME_CENTER_COUNT = "gameCenterCount";

    public final static String GOOGLE_STORE_COUNT = "googleStoreCount";
    public final static String DEVELOPER_LOGIN_COUNT = "developerLoginCount";

    @Override
    void _setup(ServiceContext serviceContext) {
        this.name = "application";
        this.logger = serviceContext.logger(ApplicationMetrics.class);
        this.categories = new String[9];
        this.categories[0]=GOOGLE_COUNT;
        this.categories[1]=STRIPE_COUNT;
        this.categories[2]=PASSWORD_COUNT;
        this.categories[3]=DEVICE_COUNT;
        this.categories[4]=FACEBOOK_COUNT;
        this.categories[5]=APPLE_STORE_COUNT;
        this.categories[6]=GAME_CENTER_COUNT;
        this.categories[7]=GOOGLE_STORE_COUNT;
        this.categories[8]=DEVELOPER_LOGIN_COUNT;
        this.dataStore = serviceContext.dataStore("application_metrics",serviceContext.partitionNumber());

    }
}
