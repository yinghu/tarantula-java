package com.icodesoftware.integration.udp;

import com.icodesoftware.integration.GameChannel;

import java.util.concurrent.ScheduledFuture;

/**
 * Created by yinghu lu on 12/10/2020.
 */
public class GameChannelBinding {
    public GameChannel gameChannel;
    public ScheduledFuture<?> pingSchedule;
    public ScheduledFuture<?> retrySchedule;

    public GameChannelBinding(GameChannel gameChannel){
        this.gameChannel = gameChannel;
    }
}
