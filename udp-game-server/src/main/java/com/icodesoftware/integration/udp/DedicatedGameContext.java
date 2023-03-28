package com.icodesoftware.integration.udp;

import com.icodesoftware.SchedulingTask;
import com.icodesoftware.protocol.GameContext;
import com.icodesoftware.protocol.GameServiceProxy;

import java.util.concurrent.ScheduledFuture;

public class DedicatedGameContext implements GameContext {
    @Override
    public GameServiceProxy gameServiceProxy(short serviceId) {
        return null;
    }

    @Override
    public ScheduledFuture<?> schedule(SchedulingTask task) {
        return null;
    }

    @Override
    public void log(String message, int level) {

    }

    @Override
    public void log(String message, Exception error, int level) {

    }
}
