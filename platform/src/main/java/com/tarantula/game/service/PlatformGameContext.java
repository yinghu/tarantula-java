package com.tarantula.game.service;

import com.icodesoftware.*;
import com.icodesoftware.protocol.GameContext;
import com.icodesoftware.protocol.GameServiceProvider;
import com.icodesoftware.protocol.GameServiceProxy;
import com.icodesoftware.service.ServiceContext;

import java.util.concurrent.ScheduledFuture;

public class PlatformGameContext implements GameContext, GameServiceProvider {

    private final ServiceContext serviceContext;
    private final TarantulaLogger logger;

    private final PlatformGameServiceProvider platformGameServiceProvider;

    public PlatformGameContext(ServiceContext serviceContext,PlatformGameServiceProvider platformGameServiceProvider,TarantulaLogger logger){
        this.serviceContext = serviceContext;
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

    public void updateStatistics(String systemId,String name,double delta){
        Statistics statistics = this.platformGameServiceProvider.presenceServiceProvider().statistics(systemId);
        statistics.entry(name).update(delta).update();
    }
    public void updateExperience(String systemId,double delta){
        //this.platformGameServiceProvider.presenceServiceProvider().rating(systemId).update(delta
    }
}
