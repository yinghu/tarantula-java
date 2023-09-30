package com.tarantula.game.service;

import com.icodesoftware.*;
import com.icodesoftware.protocol.GameContext;
import com.icodesoftware.protocol.GameServiceProvider;
import com.icodesoftware.protocol.GameServiceProxy;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.Rating;
import com.tarantula.game.SimpleStub;

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

    public void updateStatistics(Room room,String systemId,long stub,String name,double delta){
        Statistics statistics = this.platformGameServiceProvider.presenceServiceProvider().statistics(new SimpleStub(systemId,stub));
        statistics.entry(name).update(delta).update();
    }
    public void updateExperience(Room room,String systemId,long stub,double delta){
        Rating rating = this.platformGameServiceProvider.presenceServiceProvider().rating(new SimpleStub(systemId,stub));
        logger.warn("SystemId->"+systemId+">>>STUB>"+stub);
        logger.warn("Rating->"+rating.rank+">>"+rating.level+">>>"+rating.xp+">>"+delta+">>>"+room.arena().xp());
        rating.update(delta,room.arena().xp()).update();
    }

    @Override
    public Transaction transaction() {
        return platformGameServiceProvider.gameCluster().transaction();
    }

    public void startGame(Session session, byte[] payload){}
    public void updateGame(Session session,byte[] payload){}
    public void endGame(Session session,byte[] payload){

    }

    @Override
    public void setup(GameContext gameContext) {

    }
}
