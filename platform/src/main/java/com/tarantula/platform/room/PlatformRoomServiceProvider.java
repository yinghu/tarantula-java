package com.tarantula.platform.room;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.protocol.GameServerListener;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.ConfigurationServiceProvider;
import com.icodesoftware.service.ReloadListener;
import com.icodesoftware.service.ServiceContext;

import com.tarantula.game.GameZone;
import com.tarantula.game.Rating;
import com.tarantula.game.Stub;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.GameCluster;
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
    private final GameServiceProvider gameServiceProvider;
    private ServiceContext serviceContext;
    private ClusterProvider clusterProvider;
    private DistributionRoomService distributionRoomService;

    private DataStore dataStore;
    private Configuration configuration;
    //private int roomPoolSizePerZone;
    private ConcurrentHashMap<String,GameZone> gameZoneIndex;
    private ConcurrentHashMap<String, GameRoom> gameRoomIndex;
    private ArrayBlockingQueue<ConnectionStub>  pendingConnections;
    private ConcurrentHashMap<String,ConnectionStub> connectionIndex;

    private String type;
    private String registerKey;
    private String reloadKey;
    private String typeLobby;
    private boolean dedicated;

    private ClusterProvider.ClusterStore clusterStore;
    ArrayList<String> kickoff = new ArrayList<>();

    private boolean timerEnabled = false;
    private int timer = 1000;

    public PlatformRoomServiceProvider(GameCluster gameCluster, GameServiceProvider gameServiceProvider){
        this.name = (String)gameCluster.property(GameCluster.GAME_SERVICE);
        this.gameCluster = gameCluster;
        this.dedicated = (boolean)gameCluster.property(GameCluster.DEDICATED);
        this.gameServiceProvider = gameServiceProvider;
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
        logger.warn("Room service provider started for ["+gameCluster.property(GameCluster.NAME)+"]["+typeLobby+"]["+this.type+"]["+dedicated+"]");
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

    public GameRoom onRoomViewed(String zoneId,String roomId){
        GameZone gameZone = gameZoneIndex.get(zoneId);
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
        GameZone gameZone = gameZoneIndex.get(zoneId);
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
    }

    @Override
    public <T extends Configurable> void register(T t) {
        logger.warn("Game Zone Registered With ["+t.configurationTypeId()+"/"+t.configurationName()+"]");
        GameZone gameZone = (GameZone)t;
        gameZoneIndex.put(gameZone.distributionKey(),gameZone);
    }

    @Override
    public <T extends Configurable> void release(T t) {
        gameZoneIndex.remove(t.distributionKey());
    }


    private void onDisConnection(String serverId){
        ConnectionStub connectionStub = connectionIndex.remove(serverId);
        if(connectionStub==null) return;
        pendingConnections.remove(connectionStub);
        Collection<byte[]> _cb = clusterStore.indexGet(typeLobby);
        logger.warn("cb->"+_cb.size()+">>");
    }

    @Override
    public String typeId() {
        return this.typeLobby;
    }

    @Override
    public boolean onConnection(Connection connection) {
        boolean[] gameZone ={false};
        gameZoneIndex.forEach((k,v)->{
            if(connection.configurationName().equals(v.configurationTypeId()+"/"+v.configurationName())){
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
            if(connectionStub.configurationName().equals(v.configurationTypeId()+"/"+v.configurationName())){
                gameZone[0] = v;
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
        logger.warn("Connection->"+connection.serverId()+">>"+typeLobby+">>"+connection.timeout());
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
