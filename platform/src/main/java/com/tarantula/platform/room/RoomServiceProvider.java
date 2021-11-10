package com.tarantula.platform.room;

import com.icodesoftware.*;
import com.icodesoftware.protocol.GameChannelListener;
import com.icodesoftware.service.ConfigurationServiceProvider;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.cci.udp.GameChannel;
import com.tarantula.game.Arena;
import com.tarantula.game.GameZone;
import com.tarantula.game.Rating;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.RoomRegistry;
import com.tarantula.platform.service.cluster.OneTimeRunner;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class RoomServiceProvider  implements ConfigurationServiceProvider, GameChannelListener,SchedulingTask {

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
    private ConcurrentHashMap<String, GameRoom> gameRoomIndex;
    private ConcurrentLinkedDeque<ConnectionStub>  pendingConnections;
    private ConcurrentHashMap<String,ConnectionStub> connectionIndex;

    private String type;
    private String registerKey;
    private String typeLobby;

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
        //pull connection
    }
    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.distributionRoomService = this.serviceContext.clusterProvider(Distributable.DATA_SCOPE).serviceProvider(DistributionRoomService.NAME);
        this.dataStore = serviceContext.dataStore(name.replace("-","_")+DS_SUFFIX,serviceContext.partitionNumber());
        this.gameZoneIndex = new ConcurrentHashMap<>();
        this.gameRoomIndex = new ConcurrentHashMap<>();
        this.pendingConnections = new ConcurrentLinkedDeque<>();
        this.connectionIndex = new ConcurrentHashMap<>();
        this.configuration = serviceContext.configuration(CONFIG);
        this.roomCapacity = ((Number)configuration.property("roomCapacity")).intValue();
        this.roomPoolSizePerZone =((Number)configuration.property("roomPoolSizePerZone")).intValue();
        this.type = (String) gameCluster.property(GameCluster.MODE);
        typeLobby = (String) this.gameCluster.property(GameCluster.GAME_LOBBY);
        this.registerKey = this.serviceContext.deploymentServiceProvider().registerGameChannelListener(this);
        this.logger = serviceContext.logger(RoomServiceProvider.class);
    }
    @Override
    public void start() throws Exception {
        logger.warn("Room service provider started for ["+gameCluster.property(GameCluster.NAME)+"] Mode ["+type+"]");
    }

    @Override
    public void shutdown() throws Exception {
        this.serviceContext.deploymentServiceProvider().unregisterConfigurableListener(registerKey);
    }

    public GameRoom join(GameZone gameZone, Rating rating){
        if(type.equals(GameZone.PLAY_MODE_PVE)){
            GameRoom gameRoom =gameRoomIndex.computeIfAbsent(rating.systemId(),k-> new PVEGameRoom());
            gameRoom.join(rating.systemId(),room->true);
            gameRoom.setup(gameZone.arena(rating.arenaLevel));
            return gameRoom;
        }
        RoomJoinStub roomRegistry = this.distributionRoomService.register(name,gameZone.distributionKey(),rating);
        if(!roomRegistry.joined) return null;
        GameRoom room = this.distributionRoomService.join(name,roomRegistry.ticket,roomRegistry.roomId,rating.systemId());
        if(room==null) return null;
        room.setup(gameZone.arena(roomRegistry.level));
        return room;
    }
    public void leave(String roomId,String systemId){
        if(type.equals(GameZone.PLAY_MODE_PVE)) {
            GameRoom gameRoom = gameRoomIndex.remove(systemId);
            gameRoom.leave(systemId,room->true);
            return;
        }
        this.distributionRoomService.leave(name,roomId,systemId);
    }
    public RoomJoinStub onRegister(String gameZoneId,Rating rating){
        GameZone gameZone = gameZoneIndex.get(gameZoneId);
        Arena arena = gameZoneIndex.get(gameZoneId).arena(rating.arenaLevel);
        GameRoomRegistry pending = gameZone.roomRegistryQueue().poll();
        if(pending==null) return new RoomJoinStub();
        int ret = pending.addPlayer(rating.systemId(),room->{
            if(room.empty()) room.reset(arena);
            return true;
        });
        if(ret == RoomRegistry.NOT_JOINED) return new RoomJoinStub();
        if(ret == RoomRegistry.JOINED || ret == RoomRegistry.ALREADY_JOINED) gameZone.roomRegistryQueue().offerFirst(pending);
        this.dataStore.update(pending);
        return new RoomJoinStub(pending.arenaLevel,pending.instanceId(),pending.joinTicket);
    }
    public void onRelease(String zoneId,String roomId,String systemId){
        GameZone gameZone = gameZoneIndex.get(zoneId);
        if(gameZone!=null){
            GameRoomRegistry released = gameZone.roomRegistry().get(roomId);
            released.removePlayer(systemId,room->{
                if(room.empty()) {
                    room.reset();
                    gameZone.roomRegistryQueue().offer(released);
                }
                return true;
            });
            this.dataStore.update(released);
        }
    }
    public void onSync(String zoneId,String roomId,String[] joined){
        GameZone gameZone = gameZoneIndex.get(zoneId);
        GameRoomRegistry roomRegistry = gameZone.roomRegistry().get(roomId);
        if(!roomRegistry.sync(joined)){
            logger.warn("RoomRegistry Synced->"+roomRegistry.distributionKey()+">>>"+roomRegistry);
            this.dataStore.update(roomRegistry);
        }
        if(!roomRegistry.fullJoined()) gameZone.roomRegistryQueue().offer(roomRegistry);
    }
    public GameRoom onView(String roomId){
        GameRoom gameRoom = gameRoomIndex.computeIfAbsent(roomId,(k)->{
            PVPGameRoom _gameRoom = new PVPGameRoom();
            _gameRoom.distributionKey(roomId);
            if(!this.dataStore.load(_gameRoom)) return null;
            _gameRoom.dataStore(this.dataStore);
            _gameRoom.load();
            return _gameRoom;
        });
        return gameRoom!=null?gameRoom.view():null;
    }
    public GameRoom onJoin(String ticket, String roomId, String systemId){
        if(!validateTicket(ticket)) return null;
        GameRoom gameRoom = gameRoomIndex.computeIfAbsent(roomId,(k)->{
            PVPGameRoom _gameRoom = new PVPGameRoom();
            _gameRoom.distributionKey(roomId);
            if(!this.dataStore.load(_gameRoom)) return null;
            _gameRoom.dataStore(this.dataStore);
            _gameRoom.load();
            return _gameRoom;
        });
        if(gameRoom==null) return null;
        return gameRoom.join(systemId,room->{
            ConnectionStub connectionStub = pendingConnections.poll();
            if(connectionStub==null) return false;
            GameChannel gameChannel = connectionStub.gameChannel();
            if(gameChannel==null) return false;
            gameRoom.channel(gameChannel);
            pendingConnections.offer(connectionStub);
            return true;
        });

    }
    public void onLeave(String roomId,String systemId){
        GameRoom gameRoom = gameRoomIndex.get(roomId);
        gameRoom.leave(systemId,room-> {
                room.resetIfEmpty();
                return true;
            }
        );
        this.serviceContext.schedule(new OneTimeRunner(100,()->this.distributionRoomService.release(name,gameRoom.index(),roomId,systemId)));
    }
    public void onCreate(String zoneId,String roomId){
        PVPGameRoom gameRoom = new PVPGameRoom(roomCapacity);
        gameRoom.index(zoneId);
        gameRoom.distributionKey(roomId);
        this.dataStore.createIfAbsent(gameRoom,true);
        gameRoom.dataStore(dataStore);
        gameRoom.load();
        gameRoomIndex.put(gameRoom.distributionKey(),gameRoom);
        this.serviceContext.schedule(new OneTimeRunner(100,()->{
            this.distributionRoomService.sync(name,gameRoom.index(),gameRoom.roomId(),gameRoom.joined());
        }));
    }
    public void onLoad(String roomId){
        PVPGameRoom gameRoom = new PVPGameRoom();
        gameRoom.distributionKey(roomId);
        this.dataStore.createIfAbsent(gameRoom,true);
        gameRoom.dataStore(dataStore);
        gameRoom.load();
        gameRoomIndex.put(gameRoom.distributionKey(),gameRoom);
        this.serviceContext.schedule(new OneTimeRunner(100,()->{
            this.distributionRoomService.sync(name,gameRoom.index(),gameRoom.roomId(),gameRoom.joined());
        }));
    }
    @Override
    public <T extends Configurable> void register(T t) {
        GameZone gameZone = (GameZone)t;
        gameZoneIndex.put(gameZone.distributionKey(),gameZone);
        if(type.equals(GameZone.PLAY_MODE_PVE)){
            return;
        }
        if(!this.distributionRoomService.localManaged(t.distributionKey())) return;
        int[] pendingRoomSize = new int[]{roomPoolSizePerZone};
        this.dataStore.list(new GameRoomRegistryQuery(gameZone.distributionKey()),r->{
            gameZone.roomRegistry().put(r.instanceId(),r);
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


    @Override
    public String typeId() {
        return this.typeLobby;
    }

    @Override
    public void onConnection(Connection connection) {
        ConnectionStub connectionStub = (ConnectionStub)connection;
        pendingConnections.offer(connectionStub);
        connectionIndex.put(connection.serverId(),connectionStub);
    }

    @Override
    public void onChannel(Channel channel) {
        ChannelStub channelStub = (ChannelStub)channel;
        String serverId = channelStub.serverId;
        ConnectionStub connectionStub = connectionIndex.get(serverId);
        connectionStub.addChannel(channelStub);
    }
    public void onDisConnection(Connection connection){
        ConnectionStub connectionStub = connectionIndex.remove(connection.serverId());
        if(connectionStub!=null){
            pendingConnections.remove(connectionStub);
            connectionStub.close();
        }
    }
    public void onPing(String serverId){
        ConnectionStub connectionStub = connectionIndex.get(serverId);
        if(connectionStub!=null) connectionStub.ping();
    }

    @Override
    public boolean oneTime() {
        return false;
    }

    @Override
    public long initialDelay() {
        return 5000;
    }

    @Override
    public long delay() {
        return 5000;
    }

    @Override
    public void run() {

    }
}
