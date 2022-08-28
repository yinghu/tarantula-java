package com.tarantula.platform.service.metrics;


import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceContext;

public class AccessMetrics extends AbstractMetrics{

    public final static String GOOGLE_LOGIN_COUNT = "googleLoginCount";
    public final static String WEB_LOGIN_COUNT = "webLoginCount";
    public final static String DEVICE_LOGIN_COUNT = "deviceLoginCount";
    public final static String FACEBOOK_LOGIN_COUNT = "facebookLoginCount";
    public final static String GAME_CENTER_LOGIN_COUNT = "gameCenterLoginCount";
    public final static String DEVELOPER_LOGIN_COUNT = "developerLoginCount";

    public final static String USER_CREATION_COUNT = "userCreationCount";
    public final static String SUBSCRIPTION_COUNT = "subscriptionCount";

    @Override
    void _setup(ServiceContext serviceContext) {
        this.name = Metrics.ACCESS;
        this.logger = serviceContext.logger(AccessMetrics.class);
        this.categories = new String[8];
        this.categories[0]=GOOGLE_LOGIN_COUNT;
        this.categories[1]=WEB_LOGIN_COUNT;
        this.categories[2]=DEVICE_LOGIN_COUNT;
        this.categories[3]=FACEBOOK_LOGIN_COUNT;
        this.categories[4]=GAME_CENTER_LOGIN_COUNT;
        this.categories[5]=DEVELOPER_LOGIN_COUNT;
        this.categories[6]=USER_CREATION_COUNT;
        this.categories[7]=SUBSCRIPTION_COUNT;
        this.dataStore = serviceContext.dataStore("access_metrics",serviceContext.partitionNumber());
    }
}
