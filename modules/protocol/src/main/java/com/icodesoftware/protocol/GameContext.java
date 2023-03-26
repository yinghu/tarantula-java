package com.icodesoftware.protocol;


import com.icodesoftware.SchedulingTask;

import java.util.concurrent.ScheduledFuture;

public interface GameContext{

    GameServiceProxy gameServiceProxy(short serviceId);
    ScheduledFuture<?> schedule(SchedulingTask task);

}
