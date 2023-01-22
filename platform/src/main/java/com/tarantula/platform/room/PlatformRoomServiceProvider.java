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
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.ScheduleRunner;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PlatformRoomServiceProvider implements ConfigurationServiceProvider, GameServerListener, ReloadListener {

    private static final String CONFIG = "game-room-settings";
    private static final String DS_SUFFIX = "_room";

    public static final String NAME = "RoomService";

    private TarantulaLogger logger;
    private final String name;
    private final GameCluster gameCluster;

    private ServiceContext serviceContext;
    private ClusterProvider clusterProvider;
    private DistributionRoomService distributionRoomService;

    private ClusterProvider.ClusterStore clusterStore;
    private DataStore dataStore;

    private ConcurrentHashMap<String,GameZoneIndex> gameZoneIndex;
    private ConcurrentHashMap<String, GameRoom> gameRoomIndex;
    private ArrayBlockingQueue<ConnectionStub>  pendingConnections;
    private ConcurrentHashMap<String,ConnectionStub> connectionIndex;


    private String playMode;
    private String typeLobby;

    private String registerKey;
    private String reloadKey;

    private int maxDedicatedServerConnections;
    private int maxRoomPoolSizePerZone;
    private int minRoomPoolSizePerZone;

    private boolean dedicated;

    ArrayList<String> kickoff = new ArrayList<>();
    private boolean timerEnabled = false;
    private int timer;
    private SchedulingTask schedulingTask;

    public PlatformRoomServiceProvider(GameCluster gameCluster, GameServiceProvider gameServiceProvider){
        this.gameCluster = gameCluster;
        this.name = (String)gameCluster.property(GameCluster.GAME_SERVICE);
        this.typeLobby = (String) this.gameCluster.property(GameCluster.GAME_LOBBY);
        this.playMode = (String) gameCluster.property(GameCluster.MODE);
        this.dedicated = (boolean)gameCluster.property(GameCluster.DEDICATED);
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.clusterProvider = serviceContext.clusterProvider();
        this.distributionRoomService = this.serviceContext.clusterProvider().serviceProvider(DistributionRoomService.NAME);
        this.dataStore = serviceContext.dataStore(name.replace("-","_")+DS_SUFFIX,serviceContext.node().partitionNumber());
        this.gameZoneIndex = new ConcurrentHashMap<>();
        this.gameRoomIndex = new ConcurrentHashMap<>();
        Configuration configuration = serviceContext.configuration(CONFIG);
        JsonObject jsonObject = ((JsonElement)configuration.property(playMode)).getAsJsonObject();
        this.maxDedicatedServerConnections = jsonObject.get("maxDedicatedServerConnections").getAsInt();
        this.maxRoomPoolSizePerZone = jsonObject.get("maxRoomPoolSizePerZone").getAsInt();
        this.minRoomPoolSizePerZone = jsonObject.get("minRoomPoolSizePerZone").getAsInt();
        if(this.dedicated){
            this.pendingConnections = new ArrayBlockingQueue<>(maxDedicatedServerConnections);
            this.connectionIndex = new ConcurrentHashMap<>();
        }
        this.timerEnabled = jsonObject.get("connectionCheckEnabled").getAsBoolean();
        this.timer = jsonObject.get("connectionCheckInterval").getAsInt();
        this.registerKey = this.serviceContext.deploymentServiceProvider().registerGameServerListener(this);
        this.reloadKey = this.clusterProvider.registerReloadListener(this);
        if(this.dedicated && timerEnabled) {
            this.schedulingTask = new ScheduleRunner(timer,()->{
                onSchedule();
                this.serviceContext.schedule(this.schedulingTask);
            });
            this.serviceContext.schedule(schedulingTask);
        }
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
        logger.warn("Room service provider started for ["+gameCluster.property(GameCluster.NAME)+"]["+typeLobby+"]["+this.playMode+"]["+dedicated+"]["+maxRoomPoolSizePerZone+"]");
    }

    @Override
    public void shutdown() throws Exception {
        this.clusterProvider.unregisterReloadListener(reloadKey);
        this.serviceContext.deploymentServiceProvider().unregisterGameServerListener(registerKey);
    }

    public GameZone gameZoneFromZoneId(String zoneId){
        return gameZoneIndex.get(zoneId).gameZone;
    }

    public GameRoom join(GameZone gameZone, Rating rating){
        return dedicated?remoteJoin(gameZone,rating):localJoin(gameZone,rating);
    }
    public void leave(Stub stub){
        if(dedicated){
            this.distributionRoomService.onLeaveRoom(name,stub.zoneId,stub.roomId,stub.systemId());
            return;
        }
        GameZoneIndex index = gameZoneIndex.get(stub.zoneId);
        GameRoom gameRoom = gameRoom(index,stub.roomId);
        if(gameRoom==null) {
            logger.warn("Room Missed->"+stub.zoneId+">>"+stub.roomId);
            return;
        }
        gameRoom.leave(stub.systemId(),(room,entry)-> {
            if(room.empty()) {
                room.reset();
                index.pendingRooms.offer(room.roomId());
            }
        });
        logger.warn(gameRoom.toString());
    }

    public GameRoom onRoomViewed(String zoneId,String roomId){
        GameZoneIndex gameZone = gameZoneIndex.get(zoneId);
        GameRoom gameRoom = gameRoom(gameZone,roomId);
        return gameRoom!=null?gameRoom.view():null;
    }
    public GameRoom onRoomJoined(String zoneId,String roomId, String systemId){
        GameZoneIndex gameZone = gameZoneIndex.get(zoneId);
        GameRoom gameRoom = gameRoom(gameZone,roomId);
        if(gameRoom==null) return null;
        return gameRoom.join(systemId,(room,entry) -> {});
    }
    public void onRoomLeft(String zoneId,String roomId,String systemId){
        GameZoneIndex gameZone = gameZoneIndex.get(zoneId);
        GameRoom gameRoom = gameRoom(gameZone,roomId);
        if(gameRoom==null) {
            logger.warn("Room Missed->"+zoneId+">>"+roomId);
            return;
        }
        gameRoom.leave(systemId,(room,entry) -> {
            //if(!room.full())
        });
    }

    @Override
    public <T extends Configurable> void register(T t) {
        //logger.warn("Game Zone Registered With ["+t.configurationTypeId()+"/"+t.configurationName()+"]["+minRoomPoolSizePerZone+"]");
        GameZone gameZone = (GameZone)t;
        GameZoneIndex index = new GameZoneIndex();
        index.gameZone = gameZone;
        index.maxRoomPoolSize = new AtomicInteger(maxRoomPoolSizePerZone);
        if(dedicated) {
            index.roomStore = this.clusterProvider.clusterStore(gameZone.oid(), false, false, true);
        }
        else{
            index.pendingRooms = new ArrayBlockingQueue<>(maxRoomPoolSizePerZone);
        }
        index.roomIndex = new IndexSet(GameRoom.LABEL);
        index.roomIndex.distributionKey(serviceContext.node().nodeId());
        this.dataStore.createIfAbsent(index.roomIndex,true);
        int[] rooms = {0};
        index.roomIndex.keySet().forEach(k->{
            GameRoom room = gameRoom(index,k);
            logger.warn(room.toString());
            room.reset();
            rooms[0]++;
            if(dedicated){
                byte[] roomId = k.getBytes();
                if(clusterStore.mapSetIfAbsent(roomId,"{}".getBytes())==null){
                    index.roomStore.queueOffer(roomId);
                }
            }
            else{
                index.pendingRooms.offer(k);
            }
        });
        if(rooms[0] < minRoomPoolSizePerZone){
            int remaining = minRoomPoolSizePerZone - rooms[0];
            logger.warn("Creating game room on node->"+serviceContext.node().nodeName()+" : "+remaining);
            for(int i=0; i<remaining;i++){
                GameRoom gameRoom = createGameRoom(gameZone.playMode(),gameZone.capacity());
                if(this.dataStore.create(gameRoom)){
                    rooms[0]++;
                    index.roomIndex.addKey(gameRoom.roomId());
                    if(dedicated){
                        byte[] rkey = gameRoom.roomId().getBytes();
                        if(clusterStore.mapSetIfAbsent(rkey,"{}".getBytes())==null){
                            index.roomStore.queueOffer(rkey);
                        }
                    }
                    else{
                        index.pendingRooms.offer(gameRoom.roomId());
                    }
                }
            }
            this.dataStore.update(index.roomIndex);
        }
        int roomPoolRemaining = index.maxRoomPoolSize.addAndGet((-1)*rooms[0]);
        logger.warn(gameZone+" Remaining Room Pool Size ["+roomPoolRemaining+"]");
        gameZoneIndex.put(gameZone.distributionKey(),index);
    }

    @Override
    public <T extends Configurable> void release(T t) {
        gameZoneIndex.remove(t.distributionKey());
    }


    @Override
    public String typeId() {
        return this.typeLobby;
    }

    @Override
    public boolean onConnection(Connection connection) {
        boolean gameZoneExisted = gameZone(connection.configurationName())!=null;
        if(!gameZoneExisted) return false;
        clusterStore.indexSet(typeLobby,connection.toBinary());
        this.clusterProvider.deployService().onRegisterConnection(connection);
        return true;
    }

    @Override
    public boolean onChannel(Channel channel) {
        ChannelStub channelStub = (ChannelStub)channel;
        ConnectionStub connectionStub = connectionIndex.get(channelStub.serverId);
        if(connectionStub==null){
            logger.warn("no server connection ["+channelStub.serverId);
            return false;
        }
        GameZoneIndex gameZone = gameZone(connectionStub.configurationName());
        if(gameZone==null) throw new RuntimeException("no lobby available");
        byte[] roomId = gameZone.roomStore.queuePoll();
        if(roomId==null){
            logger.warn("No room id available on game zone->"+connectionStub.configurationName());
            return false;
        }
        clusterStore.mapRemove(roomId);
        clusterStore.indexSet(connectionStub.serverId(),roomId);
        channelStub.roomId = new String(roomId);
        ClusterProvider.ClusterStore channelStore = this.clusterProvider.clusterStore(channelStub.serverId,false,false,true);
        channelStore.queueOffer(channelStub.toBinary());
        return true;
    }
    public void onDisConnection(Connection connection){
        clusterStore.indexRemove(typeLobby,connection.toBinary());
        this.clusterProvider.deployService().onReleaseConnection(connection);
        cleanConnection(connection);
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
    public void reload(int partition, boolean localMember) {
        gameRoomIndex.clear();
    }

    private void cleanConnection(Connection connection){
        ClusterProvider.ClusterStore channelStore = this.clusterProvider.clusterStore(connection.serverId(),false,false,true);
        channelStore.clear();
        GameZoneIndex gameZone = gameZone(connection.configurationName());
        Collection<byte[]> ids = clusterStore.indexGet(connection.serverId());
        ids.forEach(k->{
            if(gameZone != null && clusterStore.mapSetIfAbsent(k,"{}".getBytes()) == null){
                gameZone.roomStore.queueOffer(k);
            }
        });
        clusterStore.indexRemove(connection.serverId());
    }

    private void onDisConnection(String serverId){
        ConnectionStub connectionStub = connectionIndex.remove(serverId);
        if(connectionStub==null) return;
        pendingConnections.remove(connectionStub);
        Collection<byte[]> _cb = clusterStore.indexGet(typeLobby);
        _cb.forEach(c->{
            if(Arrays.equals(c,connectionStub.toBinary())){
                clusterStore.indexRemove(typeLobby,c);
                cleanConnection(connectionStub);
            }
        });
        logger.warn("Disconnection->"+serverId+">>"+clusterStore.indexGet(typeLobby).size()+">>"+pendingConnections.size());
    }

    private void onSchedule() {
        kickoff.clear();
        connectionIndex.forEach((k,v)->{
            if(v.onTimeout(timer)) kickoff.add(k);
        });
        kickoff.forEach(k->{
            logger.warn("Connection kickoff->"+k);
            onDisConnection(k);
        });
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

    private GameZoneIndex gameZone(String configurationName){
        GameZoneIndex[] gameZone ={null};
        gameZoneIndex.forEach((k,v)->{
            if(configurationName.equals(v.gameZone.configurationTypeId()+"/"+v.gameZone.configurationName())){
                gameZone[0] = v;
            }
        });
        return gameZone[0];
    }

    private GameRoom gameRoom(GameZoneIndex zoneIndex,String roomId){
        GameZone gameZone = zoneIndex.gameZone;
        if(roomId!=null){
            return gameRoomIndex.computeIfAbsent(roomId,(k)->{
                GameRoom _gameRoom = this.createGameRoom(gameZone.playMode(),gameZone.capacity());
                _gameRoom.distributionKey(roomId);
                if(!this.dataStore.load(_gameRoom)) return null;
                _gameRoom.dataStore(this.dataStore);
                _gameRoom.load();
                return _gameRoom;
            });
        }
        GameRoom gameRoom = this.createGameRoom(gameZone.playMode(),gameZone.capacity());
        if(!this.dataStore.create(gameRoom)) return null;
        gameRoom.dataStore(this.dataStore);
        zoneIndex.roomIndex.addKey(gameRoom.roomId());
        this.dataStore.update(zoneIndex.roomIndex);
        gameRoomIndex.put(gameRoom.roomId(),gameRoom);
        return gameRoom;
    }

    private GameRoom localJoin(GameZone gameZone, Rating rating){
        GameZoneIndex index = gameZoneIndex.get(gameZone.distributionKey());
        String roomId = index.pendingRooms.poll();
        if(roomId==null && index.maxRoomPoolSize.decrementAndGet() < 0) return null;
        GameRoom gameRoom = gameRoom(index,roomId);
        if(gameRoom==null) return null;
        GameRoom joined = gameRoom.join(rating.systemId(),(room,entry) -> {
            if(!room.full()) index.pendingRooms.offer(roomId);
        });
        logger.warn(gameRoom.toString());
        joined.setup(gameZone,null,rating);
        joined.distributionKey(roomId);
        return joined;
    }

    private GameRoom remoteJoin(GameZone gameZone, Rating rating){
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
        Channel channel = channelStub.toChannel(connectionStub.clientConnection(),connectionStub.serverKey,connectionStub.timeout());
        room.setup(gameZone,channel,rating);
        if(channelStub.totalJoined == gameZone.capacity()){
            logger.warn(channelStub.toString());
        }
        else{
            cStore.queueOffer(channelStub.toBinary());
        }
        return room;
    }

}
