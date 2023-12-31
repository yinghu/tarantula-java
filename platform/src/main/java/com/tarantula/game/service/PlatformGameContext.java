package com.tarantula.game.service;

import com.icodesoftware.*;
import com.icodesoftware.protocol.*;
import com.icodesoftware.service.ApplicationSchema;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.TokenValidatorProvider;

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
    public ApplicationSchema applicationSchema(){
        return platformGameServiceProvider.gameCluster();
    }

    public TokenValidatorProvider.AuthVendor authorVendor(String name){
        return tokenValidatorProvider.authVendor(name);
    }

    public  void registerTournamentListener(Tournament.Listener listener){
        this.platformGameServiceProvider.tournamentServiceProvider().registerTournamentListener(listener);
    }
    public void recoverableRegistry(RecoverableListener recoverableListener){
        serviceContext.recoverableRegistry(recoverableListener);
    }
    public void onMetrics(String category,double delta){
        platformGameServiceProvider.onUpdated(category,delta);
    }

    public Statistics statistics(Session session){
        return this.platformGameServiceProvider.presenceServiceProvider().statistics(session);
    }

    @Override
    public Rating rating(Session session) {
        return this.platformGameServiceProvider.presenceServiceProvider().rating(session);
    }

    @Override
    public Achievement achievement(Session session) {
        return this.platformGameServiceProvider.achievementServiceProvider().achievement(session);

    }
}
