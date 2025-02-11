package com.tarantula.platform.room;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.protocol.Channel;
import com.icodesoftware.protocol.PendingReleaseRoom;
import com.icodesoftware.protocol.GameServerListener;
import com.icodesoftware.service.*;

import com.icodesoftware.util.SnowflakeKey;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.cci.udp.UDPChannel;
import com.tarantula.cci.udp.UDPEndpoint;
import com.tarantula.game.*;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.OnAccessTrack;
import com.icodesoftware.util.ScheduleRunner;
import com.tarantula.platform.event.GameClusterSyncEvent;


import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PlatformRoomServiceProvider implements ConfigurationServiceProvider, GameServerListener, ReloadListener,RoomListener {

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

    private ConcurrentHashMap<String,GameZoneIndex> gameZoneIndex;
    private ConcurrentHashMap<String, GameRoom> gameRoomIndex;
    private ConcurrentHashMap<String,ConnectionStub> connectionIndex;

    private String playMode;
    private String typeLobby;

    private String registerKey;
    private String reloadKey;

    private int maxDedicatedServerConnections;
    private int maxRoomPoolSizePerZone;
    private int minRoomPoolSizePerZone;

    private boolean pushChannelEnabled = true;
    private boolean dedicated;


    private ArrayList<String> kickoff = new ArrayList<>();

    private int timer;
    private SchedulingTask schedulingTask;
    private ScheduledFuture scheduledFuture;
    private boolean started;

    private UDPEndpoint udpEndpoint;
    private ConcurrentHashMap<String, PendingReleaseRoom> pendingReleaseRooms;

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
        this.maxRoomPoolSizePerZone = jsonObject.get("maxRoomPoolSizePerZone").getAsInt();
        this.minRoomPoolSizePerZone = jsonObject.get("minRoomPoolSizePerZone").getAsInt();
        this.pushChannelEnabled = jsonObject.get("pushChannelEnabled").getAsBoolean();
        if(this.dedicated){
            this.connectionIndex = new ConcurrentHashMap<>();
            this.serverClusterStore = this.serviceContext.clusterProvider().clusterStore(ClusterProvider.ClusterStore.SMALL,gameCluster.typeId()+"."+NAME);
        }
        String checkInterval = dedicated? "dedicatedCheckInterval" : "checkInterval";
        this.timer = ((Number)configuration.property(checkInterval)).intValue();
        this.registerKey = this.serviceContext.deploymentServiceProvider().registerGameServerListener(this);
        this.reloadKey = this.clusterProvider.registerReloadListener(this);
        this.schedulingTask = new ScheduleRunner(timer,()->{
            onSchedule();
            this.scheduledFuture = this.serviceContext.schedule(this.schedulingTask);
        });
        this.scheduledFuture = this.serviceContext.schedule(schedulingTask);
        this.logger = JDKLogger.getLogger(PlatformRoomServiceProvider.class);
        this.pendingReleaseRooms = new ConcurrentHashMap<>(this.maxRoomPoolSizePerZone);
    }

    @Override
    public void start() throws Exception {
        logger.info("Room service provider started for ["+serviceType+"]["+typeLobby+"]["+this.playMode+"]["+dedicated+"]["+maxRoomPoolSizePerZone+"]["+pushChannelEnabled+"]");
        this.udpEndpoint = (UDPEndpoint) this.serviceContext.serviceProvider(UDPEndpoint.UDP_ENDPOINT);
        this.started = this.udpEndpoint != null;
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
        gameRoomIndex.forEach((k,r)->r.close());
    }

    public Channel registerChannel(Stub stub, Session.TimeoutListener timeoutListener){
        GameZoneIndex index = gameZoneIndex.get(stub.zoneId);
        if(this.dedicated){
            UDPChannel channel = index.pendingPushChannels.poll();
            if(channel == null){
                UDPChannel[] channels = udpEndpoint.createChannels(index.gameZone.capacity());
                if(channels.length == 0) return null;
                for(int i=0;i<channels.length;i++){
                    if(i==0){
                        channel = channels[i];
                    }
                    else{
                        index.pendingPushChannels.offer(channels[i]);
                    }
                }
            }
            channel.register(stub,this.gameServiceProvider.gameServiceProvider(),this.gameServiceProvider.gameServiceProvider(),this.gameServiceProvider.gameServiceProvider(),timeoutListener);
            udpEndpoint.registerChannel(channel);
            return channel;
        }
        else{//local join case
            GameRoom room = gameRoomIndex.get(stub.roomId);
            if(room==null){
                room = loadGameRoom(index,stub.roomId);
            }
            Channel channel = room.registerChannel(stub,timeoutListener);
            udpEndpoint.registerChannel((UDPChannel)channel);
            //logger.warn("Using assigned channel ["+channel.channelId()+"/"+channel.sessionId()+"]");
            return channel;
        }
    }

    public GameZone gameZoneFromZoneId(String zoneId){
        return gameZoneIndex.get(zoneId).gameZone;
    }

    public GameRoom join(Stub rating, GameZone gameZone){
        return new PVEGameRoom();
        //GameZoneIndex index = gameZoneIndex.get(gameZone.distributionKey());
        //GameRoom gameRoom = dedicated?remoteJoin(rating,index):localJoin(rating,index);
        //return gameRoom;
    }

    public void leave(Stub stub){
        gameServiceProvider.presenceServiceProvider().onLeave(stub);
        if(dedicated) return; //close from channel close
        GameZoneIndex index = gameZoneIndex.get(stub.zoneId);
        if(pushChannelEnabled){
            Channel channel = udpEndpoint.channel(stub.sessionId);
            if(channel!=null) channel.close();
        }
        localLeave(stub.distributionId(),index,stub.roomId,(room,entry)->{
            if(room.totalJoined() != room.totalLeft()) return;
            resetGameRoom(index,room,true);
        });
    }

    @Override
    public <T extends Configurable> void register(T t) {
        logger.info("Game Zone Registered With ["+t.configurationTypeId()+"]["+minRoomPoolSizePerZone+"]");
        GameZone gameZone = (GameZone)t;
        GameZoneIndex index = new GameZoneIndex();
        index.gameZone = gameZone;
        index.maxRoomPoolSize = new AtomicInteger(maxRoomPoolSizePerZone);
        if(dedicated) {
            index.pendingPushChannels = new ArrayBlockingQueue<>(maxRoomPoolSizePerZone*gameZone.capacity());
            index.pendingConnections = new LinkedBlockingDeque<>(maxDedicatedServerConnections);
            index.gameRoom = this.newGameRoom(gameZone.playMode(),gameZone.capacity());
            index.gameRoom.setup(gameServiceProvider.gameServiceProvider(),gameZone,dedicated);
            if(started){
                for(int i=0;i<minRoomPoolSizePerZone;i++){
                    UDPChannel[] channels = udpEndpoint.createChannels(index.gameZone.capacity());
                    for(UDPChannel c : channels){
                        index.pendingPushChannels.offer(c);
                    }
                }
            }
            logger.info("Initializing push channels ["+minRoomPoolSizePerZone+"]");
        }
        else{
            index.pendingRooms = new ArrayBlockingQueue<>(maxRoomPoolSizePerZone);
            index.runningRooms = new LinkedBlockingDeque<>(maxRoomPoolSizePerZone);
            GameRoomQuery query = new GameRoomQuery(serviceContext.node().nodeId(),gameZone.playMode(),gameZone.capacity());
            int[] rooms = {0};
            this.dataStore.list(query).forEach(r->{
                loadGameRoom(index,r);
                rooms[0]++;
            });
            if(rooms[0] < minRoomPoolSizePerZone){
                int remaining = minRoomPoolSizePerZone - rooms[0];
                logger.info("Creating game room on node->"+serviceContext.node().nodeId()+" : "+remaining);
                for(int i=0; i<remaining;i++){
                    GameRoom gameRoom = this.createGameRoom(index,true);
                    if(gameRoom!=null) rooms[0]++;
                }
            }
            int roomPoolRemaining = index.maxRoomPoolSize.addAndGet((-1)*rooms[0]);
            logger.info(gameZone+" Remaining Room Pool Size ["+roomPoolRemaining+"] Capacity ["+gameZone.capacity()+"]");
            if(started && pushChannelEnabled) {
                gameRoomIndex.forEach((rk, rv) -> {
                    rv.setup(udpEndpoint.createChannels(gameZone.capacity()));
                });
                logger.info("Initializing push channels ["+minRoomPoolSizePerZone+"]");
            }
        }
        gameZoneIndex.put(gameZone.distributionKey(),index);
    }

    @Override
    public <T extends Configurable> void release(T t) {
        GameZoneIndex index = gameZoneIndex.remove(t.distributionKey());
        if(dedicated){
            UDPChannel udpChannel;
            do{
                udpChannel = index.pendingPushChannels.poll();
                if(udpChannel!=null){
                    udpChannel.close();
                }
            }while (udpChannel!=null);
        }
        else{
            GameRoom gameRoom;
            do{
                gameRoom = index.pendingRooms.poll();
                if(gameRoom!=null){
                    gameRoom.close();
                    gameRoomIndex.remove(gameRoom.roomId());
                }
            }while (gameRoom!=null);
        }
    }


    @Override
    public String typeId() {
        return this.typeLobby;
    }

    @Override
    public OnAccess onConnection(Connection connection) {
        GameZoneIndex index = gameZoneIndex(connection.configurationName());
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
        GameZoneIndex index = gameZoneIndex(connectionStub.configurationName());
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
        GameZoneIndex index = gameZoneIndex(connectionStub.configurationName());
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
        //logger.warn("Connection pinged on ["+connectionStub.configurationName()+"]");
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
        GameZoneIndex index = gameZoneIndex(connectionStub.configurationName());
        boolean removed = index.pendingConnections.remove(connectionStub);
        logger.warn("Connection released on ["+connection.configurationName()+"]["+removed+"]");
    }

    @Override
    public void reload(int partition, boolean localMember) {
        gameRoomIndex.clear();
    }

    public void onStart(EndPoint endPoint){
        if(endPoint.name().equals(EndPoint.UDP_ENDPOINT)){
            this.udpEndpoint = (UDPEndpoint)endPoint;
            if(pushChannelEnabled){
                gameZoneIndex.forEach((k,v)->{
                    if(dedicated){
                        for(int i=0;i<minRoomPoolSizePerZone;i++){
                            UDPChannel[] channels = udpEndpoint.createChannels(v.gameZone.capacity());
                            for(UDPChannel c : channels){
                                v.pendingPushChannels.offer(c);
                            }
                        }
                    }
                    else{
                        gameRoomIndex.forEach((rk,rv)->{
                            rv.setup(udpEndpoint.createChannels(v.gameZone.capacity()));
                        });
                        logger.warn("Total running/pending rooms ["+v.runningRooms.size()+"/"+v.pendingRooms.size()+"] on ["+typeLobby+"]["+v.gameZone.name()+"]");
                    }

                });
            }
        }
        started = true;
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
        if(!dedicated) {
            kickoff.clear();
            pendingReleaseRooms.forEach((k,p)->{
                p.room.onCountdown(timer);
                if(TimeUtil.expired(p.pendingSchedule)) kickoff.add(k);
            });
            kickoff.forEach(k->{
                PendingReleaseRoom pendingReleaseRoom = pendingReleaseRooms.remove(k);
                if(pendingReleaseRoom!=null) udpEndpoint.releaseChannel(pendingReleaseRoom.room.channelId());
            });
            return;
        }
        kickoff.clear();
        connectionIndex.forEach((k,v)->{
            if(v.onTimeout(timer)) kickoff.add(k);
        });
        kickoff.forEach(k->{
            logger.warn("Connection kickoff->"+k);
            onDisConnection(connectionIndex.get(k));
        });
    }

    private GameRoom newGameRoom(String type,int roomCapacity){
        return GameRoom.newGameRoom(type,roomCapacity);
    }

    private GameZoneIndex gameZoneIndex(String configurationName){
        GameZoneIndex[] gameZone ={null};
        gameZoneIndex.forEach((k,v)->{
            if(configurationName.trim().equals(v.gameZone.configurationTypeId()+"/"+v.gameZone.configurationName())){
                gameZone[0] = v;
            }
        });
        return gameZone[0];
    }

    private GameRoom loadGameRoom(GameZoneIndex zoneIndex,GameRoom gameRoom){
        gameRoom.dataStore(this.dataStore);
        gameRoom.load();
        gameRoom.setup(gameServiceProvider.gameServiceProvider(),zoneIndex.gameZone,dedicated);
        resetGameRoom(zoneIndex,gameRoom,true);
        gameRoomIndex.put(gameRoom.roomId(),gameRoom);
        return gameRoom;
    }

    private GameRoom loadGameRoom(GameZoneIndex zoneIndex,String roomId){
        return gameRoomIndex.computeIfAbsent(roomId,(k)->{
            GameRoom gameRoom = this.newGameRoom(zoneIndex.gameZone.playMode(),zoneIndex.gameZone.capacity());
            gameRoom.distributionId(Long.parseLong(roomId));
            if(!this.dataStore.load(gameRoom)) return null;
            gameRoom.dataStore(this.dataStore);
            gameRoom.load();
            gameRoom.setup(gameServiceProvider.gameServiceProvider(),zoneIndex.gameZone,dedicated);
            resetGameRoom(zoneIndex,gameRoom,true);
            return gameRoom;
        });
    }

    private GameRoom createGameRoom(GameZoneIndex zoneIndex,boolean queued){
        if(zoneIndex.maxRoomPoolSize.decrementAndGet() < 0) {
            logger.warn("Max room pool size overflow ->"+maxRoomPoolSizePerZone);
            return null;
        }
        GameZone gameZone = zoneIndex.gameZone;
        GameRoom gameRoom = this.newGameRoom(gameZone.playMode(),gameZone.capacity());
        gameRoom.ownerKey(SnowflakeKey.from(serviceContext.node().nodeId()));
        if(!this.dataStore.create(gameRoom)) return null;
        gameRoom.dataStore(this.dataStore);
        gameRoom.load();
        gameRoom.setup(gameServiceProvider.gameServiceProvider(),zoneIndex.gameZone,dedicated);
        gameRoomIndex.put(gameRoom.roomId(),gameRoom);
        resetGameRoom(zoneIndex,gameRoom,queued);
        return gameRoom;
    }

    private void resetGameRoom(GameZoneIndex index,GameRoom room,boolean queued){
        room.reset();
        if(queued) index.pendingRooms.offer(room);
        if(!started || !pushChannelEnabled) return;
        room.setup(udpEndpoint.createChannels(index.gameZone.capacity()));
    }

    private void localLeave(long systemId, GameZoneIndex index,String roomId, GameRoom.Listener listener){
        GameRoom gameRoom = loadGameRoom(index,roomId);
        if(gameRoom==null) {
            //logger.warn("Room Missed->"+index.gameZone.distributionKey()+">>"+roomId);
            return;
        }
        gameRoom.leave(systemId,listener);
    }

    private GameRoom joinGameRoom(GameZoneIndex index, GameRoom gameRoom, Stub stub){
        GameRoom joined = gameRoom.join(stub.distributionId(),(room,entry)->{
            if(room.available()){
                index.runningRooms.addFirst(room);
            }
        });
        //joined.setup(index.gameZone,null,rating);
        return joined;
    }
    private GameRoom localJoin(Stub stub, GameZoneIndex index){
        GameRoom gameRoom = index.runningRooms.poll();
        if(gameRoom != null) return joinGameRoom(index,gameRoom,stub);
        gameRoom = index.pendingRooms.poll();
        if(gameRoom != null) return joinGameRoom(index,gameRoom,stub);
        gameRoom = createGameRoom(index,false);
        if(gameRoom == null) return null;
        return joinGameRoom(index,gameRoom,stub);
    }

    private GameRoom remoteJoin(Stub stub, GameZoneIndex gameZoneIndex){
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
        joined.setup(channel);
        return joined;
    }

    private void releaseRoom(Room room) {
        GameZoneIndex index = gameZoneIndex.get(room.owner());
        if(index==null){
            logger.warn("Game lobby not available ["+room.owner()+"]");
            return;
        }
        if(pushChannelEnabled) udpEndpoint.releaseChannel(room.channelId());
        index.runningRooms.remove(room);
        resetGameRoom(index,gameRoomIndex.get(room.distributionKey()),true);
        //forcefully reset room
    }


    private ClusterProvider.ClusterStore channelStore(String serverId){
        return clusterProvider.clusterStore(ClusterProvider.ClusterStore.SMALL,serverId,false,false,true);
    }


    @Override
    public void onStarted(Room room) {
        //logger.warn("room started->"+room.channelId()+">>"+room.capacity());
        pendingReleaseRooms.put(room.distributionKey(),new PendingReleaseRoom(room,LocalDateTime.now().plus(room.duration()+room.overtime(), ChronoUnit.MILLIS)));
    }
    @Override
    public void onUpdated(Room room, byte[] payload) {
        //logger.warn("room updated->"+room.channelId()+">>"+new String(payload));
        GameRoom gameRoom = (GameRoom)room;
       //gameRoom.onUpdated(gameUpdateContext.gameServiceProvider(),payload);
    }

    @Override
    public void onEnded(Room room) {
        if(room.dedicated()) return;
        pendingReleaseRooms.remove(room.distributionKey());
        releaseRoom(room);
    }

    public boolean pushChannelEnabled(){
        return pushChannelEnabled;
    }

    public void onClosed(Room room){

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
        onAccessTrack.systemId(params[0]);
        onAccessTrack.command(params[1]);
        onAccessTrack.property(OnAccess.PAYLOAD,payload);
        gameServiceProvider.gameServiceProvider().onGameEvent(onAccessTrack);
    }
}
