package com.tarantula.game.service;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.ServiceProvider;
import com.tarantula.game.Rating;
import com.tarantula.game.Stub;


public class RoomServiceProvider implements ServiceProvider {

    private TarantulaLogger logger;
    private final String name;
    private DataStore dataStore;
    private DistributionRoomService distributionRoomService;
    public RoomServiceProvider(String name){
        this.name = name;
    }
    @Override
    public void setup(ServiceContext serviceContext) {
        this.logger = serviceContext.logger(PlayerDataProvider.class);
        this.dataStore = serviceContext.dataStore(name.replace("-","_"),serviceContext.partitionNumber());
        this.distributionRoomService = serviceContext.clusterProvider(Distributable.DATA_SCOPE).serviceProvider(DistributionRoomService.NAME);
    }
    @Override
    public String name() {
        return name;
    }

    @Override
    public void start() throws Exception {
        logger.warn("room service provider started");
    }

    @Override
    public void shutdown() throws Exception {
        logger.warn("room service provider shutdown");
    }
    public Stub join(Rating rating){
        return distributionRoomService.join(name,rating);
    }
    public void leave(String roomId,String systemId){
        this.distributionRoomService.leave(name,roomId,systemId);
    }

    public Stub onJoin(Rating rating){
        Stub stub = new Stub();
        stub.roomId = "roomId";
        return stub;
    }
    public void onLeave(String roomId,String systemId){
        logger.warn(systemId+" leave->"+roomId);
    }
}
