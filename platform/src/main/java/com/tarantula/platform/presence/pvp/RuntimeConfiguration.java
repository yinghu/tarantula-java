package com.tarantula.platform.presence.pvp;

import java.util.concurrent.atomic.AtomicInteger;

public class RuntimeConfiguration {

    public int teamCreationWaitingTime = 5;
    public int seasonTimeGap = 60;
    public int seasonRunningTime = 180;
    public int reMatchWaitingTime = 180;

    // seconds end
    public int championsLeaderBoardThreshold = 2050;
    public int championsLeaderBoardSize = 100;
    public int matchEloDifferenceThreshold = 1000;
    public int botFillEloThreshold = 300;
    public int coolDownTime = 60;

    public int matchMakingSnapshotSize = 100;
    public AtomicInteger matchMakingPoolSize = new AtomicInteger(100);
    public int matchMakingListSize = 5;
}
