package com.tarantula.game.service;

import com.icodesoftware.*;
import com.icodesoftware.protocol.*;
import com.icodesoftware.service.ApplicationPreSetup;
import com.icodesoftware.service.ApplicationSchema;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.TokenValidatorProvider;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class PlatformGameContext implements GameContext {

    private final ServiceContext serviceContext;
    private final TarantulaLogger logger;

    private final PlatformGameServiceProvider platformGameServiceProvider;
    private final TokenValidatorProvider tokenValidatorProvider;

    public PlatformGameContext(ServiceContext serviceContext,PlatformGameServiceProvider platformGameServiceProvider,TarantulaLogger logger){
        this.serviceContext = serviceContext;
        this.tokenValidatorProvider = (TokenValidatorProvider) serviceContext.serviceProvider(TokenValidatorProvider.NAME);
        this.platformGameServiceProvider = platformGameServiceProvider;
        this.logger = logger;
    }

    public ScheduledFuture<?> schedule(SchedulingTask task){
        return this.serviceContext.schedule(task);
    }
    public void log(String message,int level){
        switch (level){
            case OnLog.DEBUG:
                this.logger.debug(message);
                break;
            case OnLog.INFO:
                this.logger.info(message);
                break;
            case OnLog.WARN:
                this.logger.warn(message);
                break;
        }

    }
    public void log(String message,Exception error,int level){
        switch (level){
            case OnLog.WARN:
                if(error!=null){
                    this.logger.warn(message);
                }
                else{
                    this.logger.warn(message,error);
                }
                break;
            case OnLog.ERROR:
                this.logger.error(message,error);
                break;
        }
    }

    @Override
    public GameServiceProxy gameServiceProxy(short serviceId) {
        return platformGameServiceProvider.gameServiceProxy(serviceId);
    }

    public GameServiceProvider gameServiceProvider(){
        return platformGameServiceProvider.gameServiceProvider();
    }
    @Override
    public ApplicationSchema applicationSchema(){
        return platformGameServiceProvider.gameCluster();
    }

    public TokenValidatorProvider.AuthVendor authorVendor(String name){
        return tokenValidatorProvider.authVendor(name);
    }


}
