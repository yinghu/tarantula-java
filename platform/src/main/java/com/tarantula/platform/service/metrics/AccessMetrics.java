package com.tarantula.platform.service.metrics;


import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceContext;

public class AccessMetrics extends AbstractMetrics{

    public final static String GOOGLE_COUNT = "googleCount";
    public final static String PASSWORD_COUNT = "passwordCount";
    public final static String DEVICE_COUNT = "deviceCount";
    public final static String FACEBOOK_COUNT = "facebookCount";
    public final static String GAME_CENTER_COUNT = "gameCenterCount";
    public final static String DEVELOPER_LOGIN_COUNT = "developerLoginCount";

    @Override
    void _setup(ServiceContext serviceContext) {
        this.name = Metrics.ACCESS;
        this.logger = serviceContext.logger(AccessMetrics.class);
        this.categories = new String[6];
        this.categories[0]=GOOGLE_COUNT;
        this.categories[1]=PASSWORD_COUNT;
        this.categories[2]=DEVICE_COUNT;
        this.categories[3]=FACEBOOK_COUNT;
        this.categories[4]=GAME_CENTER_COUNT;
        this.categories[5]=DEVELOPER_LOGIN_COUNT;
        this.dataStore = serviceContext.dataStore("access_metrics",serviceContext.partitionNumber());
    }
}
