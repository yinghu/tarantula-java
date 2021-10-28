package com.tarantula.platform.room;

import com.icodesoftware.*;
import com.icodesoftware.service.ConfigurationServiceProvider;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.Arena;
import com.tarantula.game.GameZone;
import com.tarantula.game.Rating;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.RoomRegistry;
import com.tarantula.platform.presence.PresenceServiceProvider;
import com.tarantula.platform.service.cluster.OneTimeRunner;

import java.util.concurrent.ConcurrentHashMap;

public class RoomServiceProvider  implements ConfigurationServiceProvider {

    private static final String CONFIG = "game-room-settings";
    private static final String DS_SUFFIX = "_room";

    private TarantulaLogger logger;
    private final String name;
    private final GameCluster gameCluster;
    private ServiceContext serviceContext;
    private DistributionRoomService distributionRoomService;
    private DataStore dataStore;
    private Configuration configuration;
    private int roomCapacity;
    private int roomPoolSizePerZone;
    private ConcurrentHashMap<String,GameZone> gameZoneIndex;
    private ConcurrentHashMap<String,GameRoom> gameRoomIndex;

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
        this.gameRoomIndex = new ConcurrentHashMap<>();
        this.configuration = serviceContext.configuration(CONFIG);
        this.roomCapacity = ((Number)configuration.property("roomCapacity")).intValue();
        this.roomPoolSizePerZone =((Number)configuration.property("roomPoolSizePerZone")).intValue();
        this.logger = serviceContext.logger(PresenceServiceProvider.class);
    }
    @Override
    public void start() throws Exception {
        logger.warn("Room service provider started for ["+gameCluster.property(GameCluster.NAME)+"] Mode ["+gameCluster.property(GameCluster.MODE)+"]");
    }

    @Override
    public void shutdown() throws Exception {

    }

    public GameRoom join(GameZone gameZone, Rating rating){
        RoomJoinStub roomRegistry = this.distributionRoomService.register(name,gameZone.distributionKey(),rating);
        if(!roomRegistry.joined) return null;
        GameRoom room = this.distributionRoomService.join(name,roomRegistry.ticket,roomRegistry.roomId,rating.systemId());
        room.setup(gameZone.arena(roomRegistry.level));
        return room;
    }
    public void leave(String roomId,String systemId){
        this.distributionRoomService.leave(name,roomId,systemId);
    }
    public RoomJoinStub onRegister(String gameZoneId,Rating rating){
        GameZone gameZone = gameZoneIndex.get(gameZoneId);
        Arena arena = gameZoneIndex.get(gameZoneId).arena(rating.arenaLevel);
        GameRoomRegistry pending = gameZone.roomRegistryQueue().poll();
        if(pending==null) return new RoomJoinStub();
        if(pending.empty()) pending.reset(arena);
        this.logger.warn(pending.distributionKey()+" polled");
        this.logger.warn(pending+" status1>>");
        int ret = pending.addPlayer(rating.systemId());
        if(ret == RoomRegistry.NOT_JOINED) return new RoomJoinStub();
        if(ret == RoomRegistry.JOINED || ret == RoomRegistry.ALREADY_JOINED) gameZone.roomRegistryQueue().offer(pending);
        this.dataStore.update(pending);
        this.logger.warn(pending+" status2>>"+ret);
        return new RoomJoinStub(pending.arenaLevel,pending.instanceId(),pending.joinTicket);
    }
    public void onRelease(String zoneId,String roomId){
        GameZone gameZone = gameZoneIndex.get(zoneId);
        if(gameZone!=null){
            GameRoomRegistry released = gameZone.roomRegistry().get(roomId);
            boolean removed = gameZone.roomRegistryQueue().remove(released);
            released.reset();
            this.dataStore.update(released);
            logger.warn(released.distributionKey()+" released->"+removed);
            logger.warn(released+" released");
            gameZone.roomRegistryQueue().offer(released);
        }
    }
    public GameRoom onView(String roomId){
        GameRoom gameRoom = gameRoomIndex.computeIfAbsent(roomId,(k)->{
            GameRoom _gameRoom = new GameRoom();
            _gameRoom.distributionKey(roomId);
            this.dataStore.createIfAbsent(_gameRoom,true);
            _gameRoom.dataStore(this.dataStore);
            _gameRoom.load();
            return _gameRoom;
        });
        return gameRoom.view();
    }
    public GameRoom onJoin(String ticket,String roomId, String systemId){
        if(!validateTicket(ticket)) return null;
        GameRoom gameRoom = gameRoomIndex.computeIfAbsent(roomId,(k)->{
            GameRoom _gameRoom = new GameRoom();
            _gameRoom.distributionKey(roomId);
            this.dataStore.createIfAbsent(_gameRoom,true);
            _gameRoom.dataStore(this.dataStore);
            _gameRoom.load();
            return _gameRoom;
        });
        return gameRoom.join(systemId);
    }
    public void onLeave(String roomId,String systemId){
        GameRoom gameRoom = gameRoomIndex.get(roomId);
        if(gameRoom.leave(systemId)) this.serviceContext.schedule(new OneTimeRunner(100,()->this.distributionRoomService.release(name,gameRoom.index(),roomId)));
    }
    public void onCreate(String zoneId,String roomId){
        GameRoom gameRoom = new GameRoom(roomCapacity);
        gameRoom.index(zoneId);
        gameRoom.distributionKey(roomId);
        this.dataStore.createIfAbsent(gameRoom,true);
        gameRoom.dataStore(dataStore);
        gameRoom.load();
        gameRoomIndex.put(gameRoom.distributionKey(),gameRoom);
    }
    public void onLoad(String roomId){
        GameRoom gameRoom = new GameRoom();
        gameRoom.distributionKey(roomId);
        this.dataStore.createIfAbsent(gameRoom,true);
        gameRoom.dataStore(dataStore);
        gameRoom.load();
        gameRoomIndex.put(gameRoom.distributionKey(),gameRoom);
    }
    @Override
    public <T extends Configurable> void register(T t) {
        GameZone gameZone = (GameZone)t;
        gameZoneIndex.put(gameZone.distributionKey(),gameZone);
        if(!this.distributionRoomService.localManaged(t.distributionKey())) return;
        int[] pendingRoomSize = new int[]{roomPoolSizePerZone};
        this.dataStore.list(new GameRoomRegistryQuery(gameZone.distributionKey()),r->{
            gameZone.roomRegistry().put(r.instanceId(),r);
            if(!r.fullJoined()) gameZone.roomRegistryQueue().offer(r);
            pendingRoomSize[0]--;
            distributionRoomService.load(name,r.instanceId());
            return true;
        });
        if(pendingRoomSize[0]<0) return;
        for(int i=0;i<pendingRoomSize[0];i++){
            GameRoomRegistry gameRoomRegistry = new GameRoomRegistry();
            gameRoomRegistry.owner(gameZone.distributionKey());
            gameRoomRegistry.joinTicket = "joinTicket";
            this.dataStore.create(gameRoomRegistry);
            gameZone.roomRegistry().put(gameRoomRegistry.instanceId(),gameRoomRegistry);
            distributionRoomService.create(name,gameZone.distributionKey(),gameRoomRegistry.instanceId());
        }
    }

    @Override
    public <T extends Configurable> void release(T t) {
        gameZoneIndex.remove(t.distributionKey());
    }

    private boolean validateTicket(String ticket){
        return ticket.equals("joinTicket");
    }
}
