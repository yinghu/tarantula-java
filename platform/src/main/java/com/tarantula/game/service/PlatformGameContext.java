package com.tarantula.game.service;

import com.icodesoftware.*;
import com.icodesoftware.protocol.*;
import com.icodesoftware.service.*;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.presence.pvp.PlayerEloRatingProxy;

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
        Rating rating = platformGameServiceProvider.presenceServiceProvider().rating(session);
        return new PlayerEloRatingProxy(rating,this.platformGameServiceProvider);
    }

    @Override
    public Achievement achievement(Session session) {
        return this.platformGameServiceProvider.achievementServiceProvider().achievement(session);

    }

    public ApplicationResource.Redeemer redeemer(Session session){
        return new ApplicationRedeemerProxy(session,platformGameServiceProvider.gameCluster());
    }

    public Configurable lookup(long distributionId){
        ConfigurableObject configurableObject = new ConfigurableObject();
        configurableObject.distributionId(distributionId);
        ApplicationPreSetup applicationPreSetup = applicationSchema().applicationPreSetup();
        Descriptor item = applicationSchema().application("item");
        return  applicationPreSetup.load(item,distributionId);
    }

    public ClusterProvider.Node node(){
        return serviceContext.node();
    }

    public Metrics metrics(){
        return this.platformGameServiceProvider.metrics();
    }

    @Override
    public void registerConfigurableListener(String serviceName,String name, Configurable.Listener listener) {
        ServiceProvider serviceProvider = platformGameServiceProvider.serviceProvider(serviceName);
        if(serviceProvider==null) return;
        serviceProvider.addConfigurableListener(name,listener);
    }
}
