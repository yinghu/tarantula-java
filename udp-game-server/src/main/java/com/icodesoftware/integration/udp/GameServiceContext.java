package com.icodesoftware.integration.udp;

import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.protocol.GameContext;
import com.icodesoftware.service.ApplicationSchema;
import com.icodesoftware.service.TokenValidatorProvider;

import java.util.concurrent.ScheduledFuture;

public class GameServiceContext implements GameContext {

    private TarantulaLogger logger = JDKLogger.getLogger(GameServiceContext.class);

    @Override
    public ScheduledFuture<?> schedule(SchedulingTask task) {
        return null;
    }

    @Override
    public void log(String message, int level) {
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

    @Override
    public void log(String message, Exception error, int level) {
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
    public ApplicationSchema applicationSchema() {
        return null;
    }

    @Override
    public TokenValidatorProvider.AuthVendor authorVendor(String name) {
        return null;
    }

    @Override
    public void registerTournamentListener(Tournament.Listener listener) {

    }

    @Override
    public void recoverableRegistry(RecoverableListener recoverableListener) {

    }

    @Override
    public void onMetrics(String category, double delta) {

    }

    @Override
    public Statistics statistics(Session session) {
        return null;
    }

    @Override
    public Rating rating(Session session) {
        return null;
    }

    @Override
    public Achievement achievement(Session session) {
        return null;
    }
}
