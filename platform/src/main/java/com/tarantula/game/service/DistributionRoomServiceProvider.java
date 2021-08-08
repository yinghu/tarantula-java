package com.tarantula.game.service;

import com.icodesoftware.*;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.*;

import java.util.concurrent.ConcurrentHashMap;

public class DistributionRoomServiceProvider implements GameRoomServiceProvider {

    private TarantulaLogger logger;
    private final String name;
    private DataStore dataStore;
    private DistributionRoomService distributionRoomService;

    private ConcurrentHashMap<String,GameZone> gameZoneIndex = new ConcurrentHashMap<>();

    public DistributionRoomServiceProvider(String name){
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
        String roomId = distributionRoomService.register(name,arena,rating);
        return distributionRoomService.join(name,arena,roomId,rating.owner());
    }
    public void leave(Arena arena,String roomId,String systemId){
        this.distributionRoomService.leave(name,arena,roomId,systemId);
    }
    public String onRegister(Arena arena,Rating rating){
        logger.warn("Zone registered->"+arena.owner());
        GameZone gameZone = this.gameZoneIndex.get(arena.owner());

        return rating.owner()+"/"+arena.level;
    }
    public GameRoom onJoin(Arena arena,String roomId,String systemId){
        logger.warn("Zone joined->"+arena.owner());
        GameRoom stub = new GameRoom(true);
        stub.roomId = roomId;
        return stub;
    }
    public void onLeave(String roomId,String systemId){
        logger.warn(systemId+" leave->"+roomId);
    }


    public  void onLoaded(GameZone loaded){
        logger.warn("zone loaded in room service provider->"+loaded.distributionKey());
        gameZoneIndex.put(loaded.distributionKey(),loaded);
    }
    public void onUpdated(GameZone updated){
        logger.warn("zone updated in room service provider->"+updated.distributionKey());
    }
    public void onRemoved(GameZone remoted){
        logger.warn("zone removed in room service provider->"+remoted.distributionKey());
        gameZoneIndex.remove(remoted.distributionKey());
        gameZoneIndex.remove(remoted.distributionKey());
    }
}
