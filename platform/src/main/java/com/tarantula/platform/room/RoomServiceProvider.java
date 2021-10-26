package com.tarantula.platform.room;

import com.icodesoftware.Configurable;
import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.ConfigurationServiceProvider;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.ServiceProvider;
import com.tarantula.game.Arena;
import com.tarantula.game.GameRoom;
import com.tarantula.game.GameZone;
import com.tarantula.game.Rating;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.IndexSet;
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

    private ConcurrentHashMap<String,IndexSet> roomRegistryIndex;
    private ConcurrentHashMap<String,IndexSet> roomIndex;
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
        this.roomRegistryIndex = new ConcurrentHashMap<>();
        this.roomIndex = new ConcurrentHashMap<>();
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
        String roomId = this.distributionRoomService.register(name,gameZone.distributionKey(),rating);
        return this.distributionRoomService.join(name,gameZone.arena(rating.arenaLevel),roomId,rating.owner());
    }

    public String onRegister(GameZone gameZone,Rating rating){
        Arena arena = gameZone.arena(rating.arenaLevel);

        return rating.systemId();
    }
    public GameRoom onJoin(Arena arena, String roomId, String systemId){
        GameRoom gameRoom = new GameRoom(true);
        gameRoom.setup(arena,null);
        return gameRoom;
    }
    public void onLeave(String roomId,String systemId){}

    @Override
    public <T extends Configurable> void register(T t) {
        logger.warn("register->"+t.distributionKey());
    }

    @Override
    public <T extends Configurable> void release(T t) {
        logger.warn("release->"+t.distributionKey());
    }
    
}
