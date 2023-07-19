package com.tarantula.platform.service;

import com.icodesoftware.OnAccess;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.ServiceContext;

import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.configuration.*;
import com.tarantula.platform.service.metrics.GameClusterMetrics;


public class AmazonAWSProvider extends AuthObject{

    private PlatformConfigurationServiceProvider configurationServiceProvider;
    public AmazonAWSProvider(PlatformGameServiceProvider gameServiceProvider, MetricsListener metricsListener){
        super(gameServiceProvider.gameCluster().typeId(),"");
        this.configurationServiceProvider = gameServiceProvider.configurationServiceProvider();
        this.applicationMetricsListener = metricsListener;
    }

    public String name(){
        return OnAccess.AMAZON;
    }

    @Override
    public void setup(ServiceContext serviceContext){
        super.setup(serviceContext);
        logger = serviceContext.logger(AmazonAWSProvider.class);
    }


    public boolean upload(String name,byte[] content){
        AmazonCredentialConfiguration credentialConfiguration = configurationServiceProvider.credentialConfiguration(OnAccess.AMAZON);
        if(credentialConfiguration==null){
            logger.warn("no aws credential available ["+typeId+"]");
            return false;
        }
        onMetrics(GameClusterMetrics.ACCESS_AMAZON_S3_COUNT);
        try{
            S3Client s3Client = credentialConfiguration.s3Client();
            S3Upload s3Upload = new S3Upload();
            s3Upload.upload(s3Client,serviceContext,name,content);
            return true;
        }catch (Exception ex){
            logger.error("upload error",ex);
            return false;
        }
    }
}
