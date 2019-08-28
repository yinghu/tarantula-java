package com.tarantula.platform.service;

import com.tarantula.*;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.PresenceIndex;
import com.tarantula.platform.SystemValidator;

import java.util.concurrent.ConcurrentHashMap;

public class SystemValidatorProvider implements TokenValidatorProvider {

    private static TarantulaLogger log = JDKLogger.getLogger(SystemValidatorProvider.class);

    private SystemValidator systemValidator;
    private int timeoutInMinutes;
    private int timeoutInSeconds;

    private ServiceContext serviceContext;
    private ConcurrentHashMap<String,Presence> pMap;
    private DataStore dataStore;
    public TokenValidator tokenValidator(){
        return systemValidator.tokenValidator();
    }
    public Presence presence(String systemId){
        return pMap.computeIfAbsent(systemId,(k)->{
            PresenceIndex px = new PresenceIndex();
            px.distributionKey(systemId);
            dataStore.load(px);
            px.dataStore(dataStore);
            px.registerEventService(this.serviceContext.eventService(Distributable.INTEGRATION_SCOPE));
            return px;
        });
    }
    public void offSession(String systemId){
        Presence presence = pMap.remove(systemId);
        presence.disabled(true);
        presence.update();
    }
    public void timeout(int minutes,int seconds){
        this.timeoutInMinutes = minutes;
        this.timeoutInSeconds = seconds;
    }
    @Override
    public String name() {
        return TokenValidatorProvider.NAME;
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.dataStore =  this.serviceContext.dataStore("presence",this.serviceContext.partitionNumber());
    }

    @Override
    public void waitForData() {
        log.info("System validator provider started");
    }

    @Override
    public void start() throws Exception {
        this.pMap = new ConcurrentHashMap<>();
        this.systemValidator = new SystemValidator();
        this.systemValidator.systemValidatorProvider(this);
        this.systemValidator.timeout(this.timeoutInMinutes,this.timeoutInSeconds);
        this.systemValidator.start();
    }

    @Override
    public void shutdown() throws Exception {
        this.systemValidator.shutdown();
    }
}
