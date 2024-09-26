package com.tarantula.platform.room;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.protocol.Channel;

import com.icodesoftware.protocol.GameServerListener;

import com.icodesoftware.protocol.Room;
import com.icodesoftware.service.*;
import com.icodesoftware.util.BufferUtil;
import com.icodesoftware.util.SnowflakeKey;
import com.tarantula.cci.udp.UDPChannel;
import com.tarantula.cci.udp.UDPEndpoint;
import com.tarantula.game.*;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.OnAccessTrack;
import com.icodesoftware.util.ScheduleRunner;
import com.tarantula.platform.event.GameClusterSyncEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.*;

public class PlatformRoomServiceProvider implements ConfigurationServiceProvider, GameServerListener,ReloadListener {

    private static final String CONFIG = "game-room-settings";

    public static final String NAME = "room";

    private TarantulaLogger logger;
    private final String serviceType;
    private final GameCluster gameCluster;
    private final PlatformGameServiceProvider gameServiceProvider;

    private ServiceContext serviceContext;
    private ClusterProvider clusterProvider;

    private ClusterProvider.ClusterStore serverClusterStore;
    private DataStore dataStore;

    private ConcurrentHashMap<Long,GameZoneIndex> gameZoneIndex;
    private ConcurrentHashMap<Long, GameRoom> gameRoomIndex;
    private ConcurrentHashMap<String,ConnectionStub> connectionIndex;

    private String playMode;
    private String typeLobby;

    private String registerKey;
    private String reloadKey;

    private int maxDedicatedServerConnections;
    private int maxRoomPoolSizePerBucket;
    private int minRoomPoolSizePerBucket;

    private boolean pushChannelEnabled = true;
    private boolean dedicated;

    private int timer;
    private SchedulingTask schedulingTask;
    private ScheduledFuture scheduledFuture;
    private volatile boolean started;

    private UDPEndpoint udpEndpoint;


    public PlatformRoomServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        this.gameServiceProvider = gameServiceProvider;
        this.gameCluster = gameServiceProvider.gameCluster();
        this.serviceType = this.gameCluster.serviceType();
        this.typeLobby = this.gameCluster.lobbyType();
        this.playMode = this.gameCluster.playMode();
        this.dedicated = this.gameCluster.dedicated();
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.clusterProvider = serviceContext.clusterProvider();
        this.dataStore = gameCluster.applicationPreSetup().dataStore(gameCluster,NAME);
        this.gameZoneIndex = new ConcurrentHashMap<>();
        this.gameRoomIndex = new ConcurrentHashMap<>();
        Configuration configuration = serviceContext.configuration(CONFIG);
        JsonObject jsonObject = ((JsonElement)configuration.property(playMode)).getAsJsonObject();
        this.maxDedicatedServerConnections = jsonObject.get("maxDedicatedServerConnections").getAsInt();
        this.maxRoomPoolSizePerBucket = jsonObject.get("maxRoomPoolSizePerBucket").getAsInt();
        this.minRoomPoolSizePerBucket = jsonObject.get("minRoomPoolSizePerBucket").getAsInt();
        this.pushChannelEnabled = jsonObject.get("pushChannelEnabled").getAsBoolean();
        this.serverClusterStore = this.serviceContext.clusterProvider().clusterStore(ClusterProvider.ClusterStore.SMALL,gameCluster.typeId()+"."+NAME);
        if(this.dedicated){
            this.connectionIndex = new ConcurrentHashMap<>();
        }
        String checkInterval = dedicated? "dedicatedCheckInterval" : "checkInterval";
        this.timer = ((Number)configuration.property(checkInterval)).intValue();
        this.schedulingTask = new ScheduleRunner(timer,()->{
            onSchedule();
            this.scheduledFuture = this.serviceContext.schedule(this.schedulingTask);
        });
        this.scheduledFuture = this.serviceContext.schedule(schedulingTask);
        this.logger = JDKLogger.getLogger(PlatformRoomServiceProvider.class);
        this.registerKey = this.serviceContext.deploymentServiceProvider().registerGameServerListener(this);
        this.reloadKey = this.clusterProvider.registerReloadListener(this);
    }

    @Override
    public void start() throws Exception {
        logger.warn("Room service provider started for ["+serviceType+"]["+typeLobby+"]["+this.playMode+"]["+dedicated+"]["+maxRoomPoolSizePerBucket+"]["+pushChannelEnabled+"]["+started+"]");
        if(!dedicated) return;
        Collection<byte[]> cb = serverClusterStore.indexGet(typeLobby);
        cb.forEach(b->{
            byte[] data = serverClusterStore.mapGet(b);
            ConnectionStub c = new ConnectionStub();
            c.fromBinary(data);
            c.serverKey = this.serviceContext.deploymentServiceProvider().serverKey(typeLobby);
            onConnectionRegistered(c);
            onConnectionStarted(c);
        });
    }

    @Override
    public void shutdown() throws Exception {
        if(scheduledFuture!=null && !scheduledFuture.isCancelled()) scheduledFuture.cancel(true);
        this.clusterProvider.unregisterReloadListener(reloadKey);
        this.serviceContext.deploymentServiceProvider().unregisterGameServerListener(registerKey);
        gameRoomIndex.forEach((k,r)->{
            udpEndpoint.releaseChannel(r.channelId());
        });
    }

    public Channel registerChannel(Stub stub, Session.TimeoutListener timeoutListener){
        //local join only
        GameRoom room = gameRoomIndex.get(stub.roomId);
        Channel channel = room.registerChannel(stub.session(),timeoutListener,this.gameServiceProvider.gameServiceProvider());
        udpEndpoint.registerChannel((UDPChannel)channel);
        return channel;
    }

    public Channel channel(int sessionId){
        return udpEndpoint.channel(sessionId);
    }
    public Room room(long roomId){
        GameRoom gameRoom = new GameRoomHeader();
        gameRoom.distributionId(roomId);
        dataStore.load(gameRoom);
        gameRoom.dataStore(dataStore);
        gameRoom.load();
        return gameRoom;
    }
    public GameZone gameZoneFromZoneId(long zoneId){
        return gameZoneIndex.get(zoneId).gameZone;
    }

    public GameRoom join(Stub stub, GameZone gameZone){
        GameZoneIndex index = gameZoneIndex.get(gameZone.distributionId());
        if(dedicated) return remoteJoin(index);
        RoomStub roomStub = index.pendingRoomStubs.poll();
        if(roomStub==null){
            logger.warn("No Stub Available Now");
            serviceContext.schedule(new ScheduleRunner(100,()->{
                byte[] lockKey = BufferUtil.fromLong(index.gameZone.distributionId());
                try{
                    serverClusterStore.mapLock(lockKey);
                    OnPartition[] buckets = serviceContext.buckets();
                    for(int i=0; i<serviceContext.node().bucketNumber();i++){
                        if(!buckets[i].opening()) continue;
                        for (int j = 0;j<minRoomPoolSizePerBucket; j++){
                            this.createGameRoom(index,i);
                        }
                    }
                }finally {
                    serverClusterStore.mapUnlock(lockKey);
                }
            }));
            return null;
        }
        GameRoom gameRoom = gameRoomIndex.get(roomStub.roomId);
        return gameRoom.join(stub,roomStub);
    }

    public void leave(Stub stub){
        gameServiceProvider.presenceServiceProvider().onLeave(stub);
        gameServiceProvider.gameServiceProvider().onLeft(stub);
        if(dedicated) return; //close from channel close
        GameZoneIndex index = gameZoneIndex.get(stub.zoneId);
        if(pushChannelEnabled){
            Channel channel = udpEndpoint.channel(stub.sessionId);
            if(channel!=null){
                channel.close();
            }
        }
        GameRoom gameRoom = gameRoomIndex.get(stub.roomId);
        if(gameRoom==null) return;
        gameRoom.leave(stub);
        if(gameRoom.empty()) resetGameRoom(index,gameRoom);
    }

    @Override
    public <T extends Configurable> void register(T t) {
        logger.warn("Game Zone Registered With ["+t.configurationTypeId()+"]["+minRoomPoolSizePerBucket+"]["+t.distributionId()+"]["+dedicated+"]["+started+"]");
        GameZone gameZone = (GameZone)t;
        GameZoneIndex index = new GameZoneIndex();
        index.gameZone = gameZone;
        index.start(dedicated,serviceContext.node().bucketNumber(),maxRoomPoolSizePerBucket,maxDedicatedServerConnections);
        if(dedicated) {
            gameZoneIndex.put(gameZone.distributionId(),index);
            logger.warn("Initializing max server connections per node ["+maxDedicatedServerConnections+"]["+dedicated+"]");
            return;
        }
        byte[] lockKey = BufferUtil.fromLong(index.gameZone.distributionId());
        try{
            serverClusterStore.mapLock(lockKey);
            GameRoomQuery query = new GameRoomQuery(index.gameZone.distributionId());
            OnPartition[] buckets = serviceContext.buckets();
            this.dataStore.list(query).forEach(r->{
                if(buckets[r.bucket()].opening()){
                    loadGameRoom(index,r);
                    index.rooms[r.bucket()].incrementAndGet();
                }
            });
            for(int i=0; i<serviceContext.node().bucketNumber();i++){
                if(!buckets[i].opening()) continue;
                if(index.rooms[i].get() < minRoomPoolSizePerBucket){
                    int remaining = minRoomPoolSizePerBucket - index.rooms[i].get();
                    logger.warn("Creating game room on zone/bucket : "+index.gameZone.distributionId()+"/"+i+" : "+remaining);
                    for(int j=0; j<remaining;j++){
                        this.createGameRoom(index,i);
                    }
                }
            }
        }finally {
            serverClusterStore.mapUnlock(lockKey);
        }
        gameZoneIndex.put(gameZone.distributionId(),index);
    }

    @Override
    public <T extends Configurable> void release(T t) {
        if(dedicated) {
            gameZoneIndex.remove(t.distributionId());
            return;
        }
        GameZoneIndex index = gameZoneIndex.get(t.distributionId());
        ArrayList<Long> pendingReleased = new ArrayList<>();
        gameRoomIndex.forEach((k,r)->{
            if(r.zoneId()==index.gameZone.distributionId()){
                pendingReleased.add(k);
                if(pushChannelEnabled()) udpEndpoint.releaseChannel(r.channelId());
            }
        });
        pendingReleased.forEach(roomId->{
            GameRoom remove = gameRoomIndex.remove(roomId);
            closeGameRoom(remove);
        });
        gameRoomIndex.remove(t.distributionId());
    }


    @Override
    public String typeId() {
        return this.typeLobby;
    }

    @Override
    public OnAccess onConnection(Connection connection) {
        GameZoneIndex index = fromConfigurationName(connection.configurationName());
        if(index==null) {
            logger.warn("No game lobby available for ["+connection.configurationName()+"]");
            return new OnAccessTrack(false,"game lobby ["+connection.configurationName()+"] not available");
        }
        if(!dedicated){
            logger.warn("Game cluster needs to setup as dedicated for ["+typeId()+"]");
            return new OnAccessTrack(false,"game cluster needs to setup as dedicated");
        }
        byte[] lockKey = index.gameZone.distributionKey().getBytes();
        serverClusterStore.mapLock(lockKey);
        serverClusterStore.mapGet(lockKey);
        serverClusterStore.mapUnlock(lockKey);
        int timeout = udpEndpoint.sessionTimeout();
        connection.timeout(timeout);
        this.clusterProvider.deployService().onRegisterConnection(connection);
        OnAccess onAccess = new OnAccessTrack();
        onAccess.successful(true);
        onAccess.property("sessionTimeout",timeout);
        onAccess.property("capacity",index.gameZone.capacity());
        onAccess.property("duration",index.gameZone.roundDuration());
        onAccess.property("overtime",index.gameZone.roundOvertime());
        onAccess.property("joinsOnStart",index.gameZone.joinsOnStart());
        onAccess.property("gameServiceProvider",gameCluster.gameServiceProvider);
        return onAccess;
    }


    @Override
    public boolean onChannel(Channel channel) {
        ChannelStub channelStub = (ChannelStub)channel;
        ConnectionStub connectionStub = connectionIndex.get(channelStub.serverId);
        if(connectionStub == null){
            logger.warn("no server connection ["+channelStub.serverId);
            return false;
        }
        GameZoneIndex index = fromConfigurationName(connectionStub.configurationName());
        if(index == null) throw new RuntimeException("no lobby available");
        for(int i=0;i<index.gameZone.capacity();i++){
            channelStub.sessionId(channelStub.sessionId()+i);
            byte[] channelData = channelStub.toBinary();
            ClusterProvider.ClusterStore channelStore = channelStore(channelStub.serverId);
            channelStore.queueOffer(channelData);
        }
        return true;
    }

    public void onStartConnection(Connection connection){
        ConnectionStub connectionStub = connectionIndex.get(connection.serverId());
        if(connectionStub==null) {
            logger.warn("Connection is not available ["+connection.serverId()+"]");
            return;
        }
        byte[] serverId = connectionStub.serverId().getBytes();
        byte[] data = connectionStub.toBinary();
        serverClusterStore.mapSet(serverId,data);
        serverClusterStore.indexSet(typeLobby,serverId);
        this.clusterProvider.deployService().onStartConnection(connection);
    }

    public void onDisConnection(Connection connection){
        this.clusterProvider.deployService().onReleaseConnection(connection);//clean node local cache
        cleanConnection(connection);
    }

    public void onConnectionStarted(Connection connection){
        ConnectionStub connectionStub = connectionIndex.get(connection.serverId());
        if(connectionStub==null) {
            logger.warn("Connection is not available ["+connection.serverId()+"]");
            return;
        }
        connectionStub.started.set(true);
        GameZoneIndex index = fromConfigurationName(connectionStub.configurationName());
        if(index==null){
            logger.warn("Game lobby is not available ["+connectionStub.configurationName()+"]");
            return;
        }
        index.pendingConnections.offer(connectionStub);
        logger.warn("Connection started on ["+connection.configurationName()+"]");
    }

    public void onConnectionVerified(String serverId){
        ConnectionStub connectionStub = connectionIndex.get(serverId);
        if(connectionStub==null) {
            logger.warn("Connection is not available ["+serverId+"] on ping");
            return;
        }
        connectionStub.ping();
    }

    @Override
    public void onConnectionRegistered(Connection connection) {
        ConnectionStub connectionStub = (ConnectionStub)connection;
        connectionStub.init();
        connectionIndex.put(connection.serverId(),connectionStub);
        logger.warn("Connection registered on ["+connection.configurationName()+"]");
    }

    public void onConnectionReleased(Connection connection){
        ConnectionStub connectionStub = connectionIndex.remove(connection.serverId());
        if(connectionStub==null) {
            logger.warn("Connection is not available ["+connection.serverId()+"]");
            return;
        }
        GameZoneIndex index = fromConfigurationName(connectionStub.configurationName());
        boolean removed = index.pendingConnections.remove(connectionStub);
        logger.warn("Connection released on ["+connection.configurationName()+"]["+removed+"]");
    }

    @Override
    public void onReload() {
        if(!started) return;
        OnPartition[] buckets = this.serviceContext.buckets();
        ArrayList<GameRoom> roomsClosed = new ArrayList<>();
        gameRoomIndex.forEach((k,v)->{
            if(!buckets[v.bucket()].opening()){
                roomsClosed.add(v);
                GameZoneIndex index = gameZoneIndex.get(v.zoneId());
                if(index.rooms[v.bucket()].get()>0){
                    index.rooms[v.bucket()].set(0);
                }
            }
        });
        roomsClosed.forEach(close->{
            gameRoomIndex.remove(close.roomId());
            closeGameRoom(close);
        });
        logger.warn("Total room closed : "+roomsClosed.size());
        roomsClosed.clear();
        HashMap<Integer,Boolean> bucketReload = new HashMap<>();
        gameZoneIndex.forEach((k,v)->{
            for(OnPartition onPartition : buckets){
                if(onPartition.opening()&&v.rooms[onPartition.partition()].get()==0){
                    bucketReload.put(onPartition.partition(),true);
                }
                else{
                    bucketReload.put(onPartition.partition(),false);
                }
            }
        });
        gameZoneIndex.forEach((k,v)->{
            byte[] lockKey = BufferUtil.fromLong(v.gameZone.distributionId());
            try{
                serverClusterStore.mapLock(lockKey);
                GameRoomQuery query = new GameRoomQuery(v.gameZone.distributionId());
                this.dataStore.list(query).forEach(r->{
                    if(buckets[r.bucket()].opening() && bucketReload.get(r.bucket)){
                        loadGameRoom(v,r);
                        v.rooms[r.bucket()].incrementAndGet();
                    }
                });

                for(int i=0; i<serviceContext.node().bucketNumber();i++){
                    if(!bucketReload.get(i)) continue;
                    if(v.rooms[i].get() < minRoomPoolSizePerBucket){
                        int remaining = minRoomPoolSizePerBucket - v.rooms[i].get();
                        logger.warn("Creating game room on zone/bucket : "+v.gameZone.distributionId()+"/"+i+" : "+remaining);
                        for(int j=0; j<remaining;j++){
                            this.createGameRoom(v,i);
                        }
                    }
                }
            }finally {
                serverClusterStore.mapUnlock(lockKey);
            }
            for(int i=0;i<serviceContext.node().bucketNumber();i++){
                logger.warn("Rooms on bucket ["+v.rooms[i].get()+"] on ["+k+"]["+i+"]");
            }
        });
    }


    public void onStart(EndPoint endPoint){
        if(endPoint.name().equals(EndPoint.UDP_ENDPOINT)){
            this.udpEndpoint = (UDPEndpoint)endPoint;
            if(pushChannelEnabled){
                OnPartition[] buckets = serviceContext.buckets();
                gameZoneIndex.forEach((k,v)->{
                    if(!dedicated){
                        int[] roomSize = {0};
                        gameRoomIndex.forEach((rk,rv)->{
                            if(rv.empty() && rv.zoneId()==k && buckets[rv.bucket()].opening()){
                                rv.setup(udpEndpoint.createChannels(v.gameZone.capacity()));
                                roomSize[0]++;
                            }
                        });
                        logger.warn("Total running/pending rooms ["+"/"+roomSize[0]+"] on ["+typeLobby+"]["+v.gameZone.name()+"]");
                    }
                });
            }
            started = true;
        }
    }


    private void cleanConnection(Connection connection){
        serverClusterStore.mapRemove(connection.serverId().getBytes());
        serverClusterStore.indexRemove(typeLobby,connection.toBinary());
        ClusterProvider.ClusterStore channelStore = this.channelStore(connection.serverId());
        channelStore.queueClear();
        channelStore.destroy();
        logger.warn("Connection cluster store destroyed ["+connection.serverId()+"]["+typeLobby+"]");
    }

    private void onSchedule() {
        if(!dedicated) return;
        ArrayList<String> kickoff = new ArrayList<>();
        kickoff.clear();
        connectionIndex.forEach((k,v)->{
            if(v.onTimeout(timer)) kickoff.add(k);
        });
        kickoff.forEach(k->{
            logger.warn("Connection kickoff->"+k);
            onDisConnection(connectionIndex.get(k));
        });
    }



    private GameZoneIndex fromConfigurationName(String configurationName){
        GameZoneIndex[] gameZone ={null};
        gameZoneIndex.forEach((k,v)->{
            if(configurationName.trim().equals(v.gameZone.configurationTypeId()+"/"+v.gameZone.configurationName())){
                gameZone[0] = v;
            }
        });
        return gameZone[0];
    }

    private void loadGameRoom(GameZoneIndex zoneIndex,GameRoom gameRoom){
        gameRoom.dataStore(this.dataStore);
        gameRoom.load();
        gameRoomIndex.put(gameRoom.roomId(),gameRoom);
        if(!gameRoom.empty()) return;
        resetGameRoom(zoneIndex,gameRoom);
    }

    private void createGameRoom(GameZoneIndex zoneIndex,int bucket){
        if(zoneIndex.rooms[bucket].get() >= maxRoomPoolSizePerBucket) {
            logger.warn("Bucket ["+bucket+"] room overflow with max size ["+maxRoomPoolSizePerBucket+"]");
            return;
        }
        GameZone gameZone = zoneIndex.gameZone;
        GameRoom gameRoom = new GameRoomHeader(gameZone,dedicated,bucket);
        gameRoom.ownerKey(SnowflakeKey.from(zoneIndex.gameZone.distributionId()));
        if(!this.dataStore.create(gameRoom)) return;
        gameRoom.dataStore(this.dataStore);
        gameRoom.load();
        gameRoomIndex.put(gameRoom.roomId(),gameRoom);
        zoneIndex.rooms[bucket].incrementAndGet();
        resetGameRoom(zoneIndex,gameRoom);
    }

    private void resetGameRoom(GameZoneIndex index,GameRoom room){
        for(int i=0;i<index.gameZone.capacity();i++){
            RoomStub stub = new RoomStub(room.distributionId(),i);
            index.pendingRoomStubs.remove(stub);
            index.pendingRoomStubs.offer(stub);
        }
        if(!started || !pushChannelEnabled) return;
        room.setup(udpEndpoint.createChannels(index.gameZone.capacity()));
    }
    private void closeGameRoom(GameRoom closed){
        GameZoneIndex index = gameZoneIndex.get(closed.zoneId());
        for(int i=0;i<closed.capacity();i++){
            RoomStub stub = new RoomStub(closed.distributionId(),i);
            index.pendingRoomStubs.remove(stub);
        }
        closed.close();
        udpEndpoint.releaseChannel(closed.channelId());
    }


    private GameRoom remoteJoin(GameZoneIndex gameZoneIndex){
        GameZone gameZone = gameZoneIndex.gameZone;
        ConnectionStub connectionStub = gameZoneIndex.pendingConnections.poll();
        if(connectionStub==null){
            logger.warn("no game server connection for ["+gameZone.configurationTypeId()+"/"+gameZone.configurationName()+"]");
            return null;
        }
        gameZoneIndex.pendingConnections.offerFirst(connectionStub);
        ClusterProvider.ClusterStore channelStore = this.channelStore(connectionStub.serverId());
        byte[] ret = channelStore.queuePoll();
        if(ret == null){
            logger.warn("no channel available for connection ["+connectionStub.serverId()+"]");
            return null;
        }
        ChannelStub channelStub = new ChannelStub();
        channelStub.fromBinary(ret);
        Channel channel = channelStub.toChannel(connectionStub.clientConnection(),connectionStub.serverKey,connectionStub.timeout());
        GameRoom joined = gameZoneIndex.gameRoom.view();
        joined.assign(channel);
        return joined;
    }

    private ClusterProvider.ClusterStore channelStore(String serverId){
        return clusterProvider.clusterStore(ClusterProvider.ClusterStore.SMALL,serverId,false,false,true);
    }

    public boolean pushChannelEnabled(){
        return pushChannelEnabled;
    }

    @Override
    public void onGameClusterEvent(String query,byte[] payload){
        serviceContext.schedule(new ScheduleRunner(100,()->{
            //distribute the game cluster event to player target bucket node
            String[] params = query.split("#");
            GameClusterSyncEvent event = new GameClusterSyncEvent(typeId(),query,payload);
            String targetApp = gameCluster.typeId()+"/lobby";
            RoutingKey routingKey = serviceContext.eventService().routingKey(params[0],targetApp);
            event.destination(routingKey.route());
            serviceContext.eventService().publish(event);
        }));
    }
    @Override
    public void onGameClusterEventUpdated(String query,byte[] payload){
        //callback on player target bucket node
        String[] params = query.split("#");
        OnAccessTrack onAccessTrack = new OnAccessTrack();
        onAccessTrack.systemId(Long.parseLong(params[0]));
        onAccessTrack.command(params[1]);
        onAccessTrack.property(OnAccess.PAYLOAD,payload);
        gameServiceProvider.gameServiceProvider().onGameEvent(onAccessTrack);
    }
}
