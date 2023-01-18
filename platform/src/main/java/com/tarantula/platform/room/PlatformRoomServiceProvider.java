package com.tarantula.platform.room;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.protocol.GameServerListener;
import com.icodesoftware.protocol.UDPEndpointServiceProvider;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.ConfigurationServiceProvider;
import com.icodesoftware.service.ReloadListener;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.cci.udp.GameChannel;
import com.tarantula.game.Arena;
import com.tarantula.game.GameZone;
import com.tarantula.game.Rating;
import com.tarantula.game.Stub;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.RoomRegistry;
import com.tarantula.platform.service.cluster.OneTimeRunner;
import com.tarantula.platform.util.SystemUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;

public class PlatformRoomServiceProvider implements ConfigurationServiceProvider, GameServerListener,SchedulingTask, ReloadListener {

    private static final String CONFIG = "game-room-settings";
    private static final String DS_SUFFIX = "_room";

    public static final String NAME = "RoomService";

    private TarantulaLogger logger;
    private final String name;
    private final GameCluster gameCluster;
    private ServiceContext serviceContext;
    private ClusterProvider clusterProvider;
    private DistributionRoomService distributionRoomService;

    private DataStore dataStore;
    private Configuration configuration;
    //private int roomPoolSizePerZone;
    private ConcurrentHashMap<String,GameZoneIndex> gameZoneIndex;
    private ConcurrentHashMap<String, GameRoom> gameRoomIndex;
    private ArrayBlockingQueue<ConnectionStub>  pendingConnections;
    private ConcurrentHashMap<String,ConnectionStub> connectionIndex;
    private ConcurrentHashMap<String,GameChannelIndex> gameChannelIndex;

    private String type;
    private String registerKey;
    private String reloadKey;
    private String typeLobby;

    private ClusterProvider.ClusterStore clusterStore;
    ArrayList<String> kickoff = new ArrayList<>();

    private boolean timerEnabled = false;
    private int timer = 1000;

    public PlatformRoomServiceProvider(GameCluster gameCluster, GameServiceProvider gameServiceProvider){
        this.name = (String)gameCluster.property(GameCluster.GAME_SERVICE);
        this.gameCluster = gameCluster;
    }

    @Override
    public String name() {
        return NAME;
    }
    @Override
    public void waitForData(){
        //pull connection
    }
    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.clusterProvider = serviceContext.clusterProvider();
        this.distributionRoomService = this.serviceContext.clusterProvider().serviceProvider(DistributionRoomService.NAME);
        this.dataStore = serviceContext.dataStore(name.replace("-","_")+DS_SUFFIX,serviceContext.node().partitionNumber());
        this.gameZoneIndex = new ConcurrentHashMap<>();
        this.gameRoomIndex = new ConcurrentHashMap<>();
        this.gameChannelIndex = new ConcurrentHashMap<>();
        this.pendingConnections = new ArrayBlockingQueue<>(10);
        this.connectionIndex = new ConcurrentHashMap<>();
        this.configuration = serviceContext.configuration(CONFIG);
        this.type = (String) gameCluster.property(GameCluster.MODE);
        JsonObject jsonObject = ((JsonElement)configuration.property(type)).getAsJsonObject();
        this.timerEnabled = jsonObject.get("connectionCheckEnabled").getAsBoolean();
        this.timer = jsonObject.get("connectionCheckInterval").getAsInt();
        this.typeLobby = (String) this.gameCluster.property(GameCluster.GAME_LOBBY);
        this.registerKey = this.serviceContext.deploymentServiceProvider().registerGameServerListener(this);
        this.reloadKey = this.clusterProvider.registerReloadListener(this);
        if(timerEnabled) this.serviceContext.schedule(this);
        this.clusterStore = this.serviceContext.clusterProvider().clusterStore(typeLobby);
        this.logger = serviceContext.logger(PlatformRoomServiceProvider.class);
    }
    @Override
    public void start() throws Exception {
        Collection<byte[]> cb = clusterStore.indexGet(typeLobby);
        cb.forEach(b->{
            ConnectionStub c = new ConnectionStub();
            c.fromBinary(b);
            c.serverKey = this.serviceContext.deploymentServiceProvider().serverKey(typeLobby);
            onConnectionRegistered(c);
        });
        logger.warn("Room service provider started for ["+gameCluster.property(GameCluster.NAME)+"]["+typeLobby+"]["+this.type+"]");
    }

    @Override
    public void shutdown() throws Exception {
        this.clusterProvider.unregisterReloadListener(reloadKey);
        this.serviceContext.deploymentServiceProvider().unregisterGameServerListener(registerKey);
    }

    public GameRoom join(GameZone gameZone, Rating rating){
        if(gameZone.playMode().equals(GameZone.PLAY_MODE_PVE)){
            String roomId = serviceContext.node().bucketName()+"/"+ SystemUtil.oid();
            GameRoom gameRoom =gameRoomIndex.computeIfAbsent(roomId,k-> this.createGameRoom(gameZone.playMode(),gameZone.capacity()));
            gameRoom.join(rating.systemId());
            gameRoom.setup(gameZone,rating);
            gameRoom.distributionKey(roomId);
            return gameRoom;
        }
        ConnectionStub connectionStub = pendingConnections.poll();
        if(connectionStub==null){
            logger.warn("no game server connection for ["+gameZone.configurationTypeId()+"/"+gameZone.configurationName()+"]");
            return null;
        }
        pendingConnections.offer(connectionStub);
        ClusterProvider.ClusterStore cStore = this.clusterProvider.clusterStore(connectionStub.serverId(),false,false,true);
        byte[] ret = cStore.queuePoll();
        if(ret == null){
            logger.warn("no channel available for connection ["+connectionStub.serverId()+"]");
            return null;
        }
        ChannelStub channelStub = new ChannelStub();
        channelStub.fromBinary(ret);
        channelStub.sessionId();
        GameRoom room = this.distributionRoomService.onJoinRoom(name,gameZone.distributionKey(),channelStub.roomId,rating.systemId());
        room.distributionKey(channelStub.roomId);
        room.channel(channelStub.toChannel(connectionStub.clientConnection(),connectionStub.serverKey,connectionStub.timeout()));
        room.setup(gameZone,rating);
        if(channelStub.totalJoined == gameZone.capacity()){
            logger.warn(channelStub.toString());
        }
        else{
            cStore.queueOffer(channelStub.toBinary());
        }
        return room;

    }
    public void leave(Stub stub){
        if(stub.playMode.equals(GameZone.PLAY_MODE_PVE)) {
            GameRoom gameRoom = gameRoomIndex.remove(stub.roomId);
            if(gameRoom==null) return;
            gameRoom.leave(stub.systemId());
            return;
        }
        this.distributionRoomService.onLeaveRoom(name,stub.roomId,stub.systemId());
    }
    public RoomJoinStub onRoomRegistered(String gameZoneId,Rating rating){
        GameZone gameZone = gameZoneIndex.get(gameZoneId).gameZone;
        Arena arena = gameZone.arena(rating.arenaLevel);
        GameRoomRegistry pending = gameZone.roomRegistryQueue().poll();
        if(pending==null) return new RoomJoinStub();
        int ret = pending.addPlayer(rating.systemId(),room->{
            if(room.empty()) room.reset(arena.level(),gameZone.capacity());
            return true;
        });
        if(ret == RoomRegistry.NOT_JOINED) return new RoomJoinStub();
        if(ret == RoomRegistry.JOINED || ret == RoomRegistry.ALREADY_JOINED) gameZone.roomRegistryQueue().offerFirst(pending);
        this.dataStore.update(pending);
        return new RoomJoinStub(pending.arenaLevel,pending.instanceId());
    }
    public void onRelease(String zoneId,String roomId,String systemId){
        GameZoneIndex indexGameZone = gameZoneIndex.get(zoneId);
        if(indexGameZone!=null){
            GameRoomRegistry released = indexGameZone.gameZone.roomRegistry().get(roomId);
            released.removePlayer(systemId,room->{
                if(room.empty()) {
                    room.reset();
                    indexGameZone.gameZone.roomRegistryQueue().offer(released);
                }
                return true;
            });
            this.dataStore.update(released);
        }
    }
    public void onSync(String zoneId,String roomId,String[] joined){
        GameZone gameZone = gameZoneIndex.get(zoneId).gameZone;
        GameRoomRegistry roomRegistry = gameZone.roomRegistry().get(roomId);
        roomRegistry.sync(joined,room->{
            if(!room.fullJoined())gameZone.roomRegistryQueue().offer(room);
            this.dataStore.update(room);
            return true;
        });
    }
    public GameRoom onRoomViewed(String zoneId,String roomId){
        GameZone gameZone = gameZoneIndex.get(zoneId).gameZone;
        GameRoom gameRoom = gameRoomIndex.computeIfAbsent(roomId,(k)->{
            GameRoom _gameRoom = this.createGameRoom(gameZone.playMode(),0);
            _gameRoom.distributionKey(roomId);
            if(!this.dataStore.load(_gameRoom)) return null;
            _gameRoom.dataStore(this.dataStore);
            _gameRoom.load();
            return _gameRoom;
        });
        return gameRoom!=null?gameRoom.view():null;
    }
    public GameRoom onRoomJoined(String zoneId,String roomId, String systemId){
        GameZone gameZone = gameZoneIndex.get(zoneId).gameZone;
        GameRoom gameRoom = gameRoomIndex.computeIfAbsent(roomId,(k)->{
            GameRoom _gameRoom = this.createGameRoom(gameZone.playMode(),gameZone.capacity());
            _gameRoom.distributionKey(roomId);
            if(!this.dataStore.load(_gameRoom)) return null;
            _gameRoom.dataStore(this.dataStore);
            _gameRoom.load();
            return _gameRoom;
        });
        if(gameRoom==null) return null;
        return gameRoom.join(systemId);
    }
    public void onRoomLeft(String roomId,String systemId){
        GameRoom gameRoom = gameRoomIndex.get(roomId);
        gameRoom.leave(systemId);
        //this.serviceContext.schedule(new OneTimeRunner(100,()->this.distributionRoomService.release(name,gameRoom.index(),roomId,systemId)));
    }
    private void onCreate(String zoneId,String roomId){
        GameZone gameZone = gameZoneIndex.get(zoneId).gameZone;
        GameRoom gameRoom = this.createGameRoom(gameZone.playMode(),gameZone.capacity());
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
    public void onRoomLoaded(String zoneId,String roomId){
        GameZone gameZone = gameZoneIndex.get(zoneId).gameZone;
        GameRoom gameRoom = this.createGameRoom(gameZone.playMode(),gameZone.capacity());
        gameRoom.distributionKey(roomId);
        gameRoom.index(zoneId);
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
        logger.warn(t.configurationTypeId()+"/"+t.configurationName());
        GameZone gameZone = (GameZone)t;
        String zkey = t.distributionKey();
        GameZoneIndex clusterIndex = this.distributionRoomService.localManaged(zkey);
        clusterIndex.gameZone = gameZone;
        gameZoneIndex.put(zkey,clusterIndex);
        /**
        if(gameZone.playMode().equals(GameZone.PLAY_MODE_PVE)) return;
        if(!clusterIndex.localManaged) return;
        int[] pendingRoomSize = new int[]{roomPoolSizePerZone};
        this.dataStore.list(new GameRoomRegistryQuery(gameZone.distributionKey()),r->{
            gameZone.roomRegistry().put(r.instanceId(),r);
            pendingRoomSize[0]--;
            distributionRoomService.onLoadRoom(name,gameZone.distributionKey(),r.instanceId());
            return true;
        });
        if(pendingRoomSize[0]<0) return;
        for(int i=0;i<pendingRoomSize[0];i++){
            GameRoomRegistry gameRoomRegistry = new GameRoomRegistry();
            gameRoomRegistry.owner(gameZone.distributionKey());
            this.dataStore.create(gameRoomRegistry);
            gameZone.roomRegistry().put(gameRoomRegistry.instanceId(),gameRoomRegistry);
            onCreate(gameZone.distributionKey(),gameRoomRegistry.instanceId());
        }**/
    }

    @Override
    public <T extends Configurable> void release(T t) {
        gameZoneIndex.remove(t.distributionKey());
    }


    private void onDisConnection(String serverId){
        ConnectionStub connectionStub = connectionIndex.remove(serverId);
        if(connectionStub==null) return;
        pendingConnections.remove(connectionStub);
        //connectionStub.close();
        Collection<byte[]> _cb = clusterStore.indexGet(typeLobby);
        logger.warn("cb->"+_cb.size()+">>"+gameChannelIndex.size());
        ArrayList<String> removed = new ArrayList<>();
        gameChannelIndex.forEach((k,v)->{
            if(v.serverId.equals(serverId)){
                removed.add(k);
            }
        });
        removed.forEach(k->gameChannelIndex.remove(k));
    }

    @Override
    public String typeId() {
        return this.typeLobby;
    }

    @Override
    public boolean onConnection(Connection connection) {
        boolean[] gameZone ={false};
        gameZoneIndex.forEach((k,v)->{
            if(connection.configurationName().equals(v.gameZone.configurationTypeId()+"/"+v.gameZone.configurationName())){
                gameZone[0] = true;
            }
        });
        if(!gameZone[0]) return false;
        clusterStore.indexSet(typeLobby,connection.toBinary());
        this.clusterProvider.deployService().onRegisterConnection(connection);
        return true;
    }


    @Override
    public void onChannel(Channel channel) {
        ChannelStub channelStub = (ChannelStub)channel;
        ConnectionStub connectionStub = connectionIndex.get(channelStub.serverId);
        GameZone[] gameZone ={null};
        gameZoneIndex.forEach((k,v)->{
            if(connectionStub.configurationName().equals(v.gameZone.configurationTypeId()+"/"+v.gameZone.configurationName())){
                gameZone[0] = v.gameZone;
            }
        });
        if(gameZone[0]==null) throw new RuntimeException("no lobby available");
        GameRoom gameRoom = createGameRoom(gameZone[0].playMode(),gameZone[0].capacity());
        this.dataStore.create(gameRoom);
        channelStub.roomId = gameRoom.roomId();
        ClusterProvider.ClusterStore channelStore = this.clusterProvider.clusterStore(channelStub.serverId,false,false,true);
        channelStore.queueOffer(channelStub.toBinary());
    }
    public void onDisConnection(Connection connection){
        clusterStore.indexRemove(typeLobby,connection.toBinary());
        this.clusterProvider.deployService().onReleaseConnection(connection);
        ClusterProvider.ClusterStore channelStore = this.clusterProvider.clusterStore(connection.serverId(),false,false,true);
        channelStore.clear();
    }

    public void onConnectionVerified(String serverId){
        ConnectionStub connectionStub = connectionIndex.get(serverId);
        if(connectionStub==null) return;
        connectionStub.ping();
    }
    @Override
    public void onConnectionRegistered(Connection connection) {
        ConnectionStub connectionStub = (ConnectionStub)connection;
        connectionStub.init();
        pendingConnections.offer(connectionStub);
        connectionIndex.put(connection.serverId(),connectionStub);
        logger.warn("Connection->"+connection.serverId()+">>"+typeLobby+connection.timeout());
    }
    public void onConnectionReleased(Connection connection){
        onDisConnection(connection.serverId());
    }
    @Override
    public boolean oneTime() {
        return true;
    }

    @Override
    public long initialDelay() {
        return timer;
    }

    @Override
    public long delay() {
        return 0;
    }

    @Override
    public void run() {
        kickoff.clear();
        connectionIndex.forEach((k,v)->{
            if(v.onTimeout(timer)) kickoff.add(k);
        });
        kickoff.forEach(k->{
            logger.warn("Connection kickoff->"+k);
            onDisConnection(k);
        });
        this.serviceContext.schedule(this);
    }
    private GameRoom createGameRoom(String type,int roomCapacity){
        GameRoom gameRoom = null;
        switch (type){
            case GameZone.PLAY_MODE_PVE:
                gameRoom = new PVEGameRoom();
                break;
            case GameZone.PLAY_MODE_PVP:
                gameRoom = new PVPGameRoom(roomCapacity);
                break;
            case GameZone.PLAY_MODE_TVE:
                gameRoom = new TVEGameRoom(roomCapacity);
                break;
            case GameZone.PLAY_MODE_TVT:
                gameRoom = new TVTGameRoom(roomCapacity);
                break;
        }
        return gameRoom;
    }

    @Override
    public void reload(int partition, boolean localMember) {
        gameRoomIndex.clear();
    }
}
