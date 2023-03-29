package com.icodesoftware.integration.udp;

import com.icodesoftware.OnLog;
import com.icodesoftware.SchedulingTask;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.protocol.GameContext;
import com.icodesoftware.protocol.GameServiceProxy;

import java.util.concurrent.ScheduledFuture;

public class DedicatedGameContext implements GameContext {

    private TarantulaLogger logger;

    public DedicatedGameContext(TarantulaLogger logger){
        this.logger = logger;
    }

    @Override
    public GameServiceProxy gameServiceProxy(short serviceId) {
        return null;
    }

    @Override
    public ScheduledFuture<?> schedule(SchedulingTask task) {
        return null;
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
}
