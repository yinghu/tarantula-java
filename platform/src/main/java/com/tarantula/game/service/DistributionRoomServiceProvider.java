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

    private ConcurrentHashMap<String,GameRoomRegistryManager> gameRoomRegistryManagers = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,GameRoomManager> gameRoomManagers = new ConcurrentHashMap<>();

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
        GameRoomRegistryManager gameRoomRegistryManager = gameRoomRegistryManagers.get(arena.owner());
        return gameRoomRegistryManager.register(rating.owner()).instanceId();
    }
    public GameRoom onJoin(Arena arena,String roomId,String systemId){
        GameRoomManager gameRoomManager = gameRoomManagers.get(arena.owner());
        GameRoom gameRoom = gameRoomManager.join(arena,roomId,systemId);
        return gameRoom;
    }
    public void onLeave(String roomId,String systemId){
        logger.warn(systemId+" leave->"+roomId);
    }


    public  void onLoaded(GameZone loaded){
        logger.warn("zone loaded in room service provider->"+loaded.distributionKey());
        gameRoomRegistryManagers.put(loaded.distributionKey(),new GameRoomRegistryManager(dataStore,loaded));
        gameRoomManagers.put(loaded.distributionKey(),new GameRoomManager(dataStore,loaded));
    }
    public void onUpdated(GameZone updated){
        logger.warn("zone updated in room service provider->"+updated.distributionKey());
    }
    public void onRemoved(GameZone remoted){
        logger.warn("zone removed in room service provider->"+remoted.distributionKey());
        gameRoomRegistryManagers.remove(remoted.distributionKey());
        gameRoomManagers.remove(remoted.distributionKey());
    }
}
