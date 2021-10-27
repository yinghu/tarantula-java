package com.tarantula.platform.room;

import com.icodesoftware.Configurable;
import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.ConfigurationServiceProvider;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.Arena;
import com.tarantula.game.GameRoom;
import com.tarantula.game.GameZone;
import com.tarantula.game.Rating;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.presence.PresenceServiceProvider;

import java.util.concurrent.ConcurrentHashMap;

public class RoomServiceProvider  implements ConfigurationServiceProvider {

    private static final String DS_SUFFIX = "_room";

    private TarantulaLogger logger;
    private final String name;
    private final GameCluster gameCluster;
    private ServiceContext serviceContext;
    private DistributionRoomService distributionRoomService;
    private DataStore dataStore;

    private ConcurrentHashMap<String,GameZone> gameZoneIndex;
    public RoomServiceProvider(GameCluster gameCluster){
        this.name = (String)gameCluster.property(GameCluster.GAME_SERVICE);
        this.gameCluster = gameCluster;
    }

    @Override
    public String name() {
        return "RoomService";
    }
    @Override
    public void waitForData(){
    }
    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.distributionRoomService = this.serviceContext.clusterProvider(Distributable.DATA_SCOPE).serviceProvider(DistributionRoomService.NAME);
        this.dataStore = serviceContext.dataStore(name.replace("-","_")+DS_SUFFIX,serviceContext.partitionNumber());
        this.gameZoneIndex = new ConcurrentHashMap<>();
        this.logger = serviceContext.logger(PresenceServiceProvider.class);
    }
    @Override
    public void start() throws Exception {
        logger.warn("Room service provider started");
    }

    @Override
    public void shutdown() throws Exception {

    }

    public GameRoom join(GameZone gameZone, Rating rating){
        GameRoomRegistry roomRegistry = this.distributionRoomService.register(name,gameZone.distributionKey(),rating);
        GameRoom room = this.distributionRoomService.join(name,roomRegistry.instanceId(),rating.owner());
        room.setup(gameZone.arena(roomRegistry.arenaLevel),null);
        return room;
    }
    public void leave(String roomId,String systemId){
        this.distributionRoomService.leave(name,roomId,systemId);
    }
    public GameRoomRegistry onRegister(String gameZoneId,Rating rating){
        Arena arena = gameZoneIndex.get(gameZoneId).arena(rating.arenaLevel);
        GameRoomRegistry gameRoomRegistry = new GameRoomRegistry(arena);
        gameRoomRegistry.addPlayer(rating.systemId());
        this.dataStore.create(gameRoomRegistry);
        return gameRoomRegistry;
    }
    public void onRelease(String zoneId,String roomId){

    }
    public GameRoom onJoin(String roomId, String systemId){
        GameRoom gameRoom = new GameRoom(true);
        gameRoom.distributionKey(roomId);

        return gameRoom;
    }
    public void onLeave(String roomId,String systemId){

    }


    @Override
    public <T extends Configurable> void register(T t) {
        if(!this.distributionRoomService.localManaged(t.distributionKey())) return;
        GameZone gameZone = (GameZone)t;
        gameZoneIndex.put(gameZone.distributionKey(),gameZone);
    }

    @Override
    public <T extends Configurable> void release(T t) {
        gameZoneIndex.remove(t.distributionKey());
    }
    
}
