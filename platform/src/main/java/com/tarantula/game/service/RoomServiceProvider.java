package com.tarantula.game.service;

import com.icodesoftware.*;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.ServiceProvider;
import com.tarantula.game.*;

public class RoomServiceProvider implements ServiceProvider, Configurable.Listener {

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
    public GameRoom join(Arena arena, Rating rating){
        return distributionRoomService.join(name,arena,rating);
    }
    public void leave(Arena arena,String roomId,String systemId){
        this.distributionRoomService.leave(name,arena,roomId,systemId);
    }

    public GameRoom onJoin(Rating rating){
        GameRoom stub = new GameRoom();
        stub.roomId = "roomId";
        return stub;
    }
    public void onLeave(String roomId,String systemId){
        logger.warn(systemId+" leave->"+roomId);
    }
    public void registerGameZone(GameZone gameZone){
        gameZone.registerListener(this);
        logger.warn("Game zone registered->"+gameZone.name());
    }
    public void releaseGameZone(GameZone gameZone){
        logger.warn("Game zone released->"+gameZone.name());
    }
    public <T extends Configurable> void onUpdated(T updated){

        logger.warn("zone updated in room service provider->"+updated.distributionKey());
    }
}
