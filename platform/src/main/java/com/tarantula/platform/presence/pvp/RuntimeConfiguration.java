package com.tarantula.platform.presence.pvp;

import java.util.concurrent.atomic.AtomicInteger;

public class RuntimeConfiguration {

    public AtomicInteger teamCreationWaitingTime = new AtomicInteger(60); // 60 seconds

    public AtomicInteger seasonRunningTime = new AtomicInteger(5*60); // 30 minutes

    public AtomicInteger reMatchWaitingTime = new AtomicInteger(5*60); //5 minutes

    public AtomicInteger botFillEloThreshold = new AtomicInteger(3000);

    public AtomicInteger coolDownTime = new AtomicInteger(60*60); //1 hour seconds

    public AtomicInteger matchMakingPoolSize = new AtomicInteger(100); //
}
