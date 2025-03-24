package com.tarantula.platform.presence.pvp;

import java.util.concurrent.atomic.AtomicInteger;

public class RuntimeConfiguration {

    public AtomicInteger teamCreationWaitingTime = new AtomicInteger(5); // 5 seconds

    public AtomicInteger seasonRunningTime = new AtomicInteger(24*60*60); //1 day

    public AtomicInteger reMatchWaitingTime = new AtomicInteger(5); //5 seconds

    public AtomicInteger botFillEloThreshold = new AtomicInteger(300);

    public AtomicInteger coolDownTime = new AtomicInteger(60); //60 seconds

    public AtomicInteger matchMakingPoolSize = new AtomicInteger(1); //
}
