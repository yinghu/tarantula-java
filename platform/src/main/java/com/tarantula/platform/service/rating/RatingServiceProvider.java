package com.tarantula.platform.service.rating;

import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.service.ServiceContext;
import com.tarantula.platform.service.ServiceProvider;

public class RatingServiceProvider implements ServiceProvider {

    private JDKLogger logger = JDKLogger.getLogger(RatingServiceProvider.class);
    private final String NAME;
    public RatingServiceProvider(String name){
        NAME = name;
    }

    public Rating rating(String systemId){
        return new Rating();
    }
    public void elo(Rating rating1,Rating rating2){

    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        logger.warn("set up rating service->"+NAME);
        serviceContext.dataStore(NAME,serviceContext.partitionNumber());
    }

    @Override
    public void waitForData() {

    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
