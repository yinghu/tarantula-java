package com.tarantula.platform.room;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.protocol.GameServerListener;
import com.icodesoftware.service.*;

import com.tarantula.cci.udp.UDPChannel;
import com.tarantula.cci.udp.UDPEndpoint;
import com.tarantula.game.GameZone;
import com.tarantula.game.Rating;
import com.tarantula.game.Stub;
import com.tarantula.game.blackjack.BlackjackModule;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.OnAccessTrack;
import com.icodesoftware.util.ScheduleRunner;


import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PlatformRoomServiceProvider implements ConfigurationServiceProvider, GameServerListener, ReloadListener {

    private static final String CONFIG = "game-room-settings";
    private static final String DS_SUFFIX = "_room";

    public static final String NAME = "room";

    public static final byte[] ROOM_ID_QUEUED = "q".getBytes();

    private TarantulaLogger logger;
    private final String name;
    private final GameCluster gameCluster;
    private final PlatformGameServiceProvider gameServiceProvider;

    private ServiceContext serviceContext;
    private ClusterProvider clusterProvider;
    private DistributionRoomService distributionRoomService;

    private ClusterProvider.ClusterStore serverClusterStore;
    private DataStore dataStore;

    private ConcurrentHashMap<String,GameZoneIndex> gameZoneIndex;
    private ConcurrentHashMap<String, GameRoom> gameRoomIndex;
    private ArrayBlockingQueue<RoomStatus>  pendingRooms;

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

    private ArrayList<String> kickoff = new ArrayList<>();
    private boolean timerEnabled = false;
    private int timer;
    private SchedulingTask schedulingTask;
    private boolean started;

    public PlatformRoomServiceProvider(PlatformGameServiceProvider gameServiceProvider){
        this.gameServiceProvider = gameServiceProvider;
        this.gameCluster = gameServiceProvider.gameCluster();
        this.name = this.gameCluster.serviceType();
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
            this.pendingRooms = new ArrayBlockingQueue<>(maxRoomPoolSizePerZone);
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
        this.serverClusterStore = this.serviceContext.clusterProvider().clusterStore(ClusterProvider.ClusterStore.SMALL,gameCluster.typeId()+"."+NAME);
        this.logger = serviceContext.logger(PlatformRoomServiceProvider.class);
    }

    @Override
    public void start() throws Exception {
        Collection<byte[]> cb = serverClusterStore.indexGet(typeLobby);
        cb.forEach(b->{
            byte[] data = serverClusterStore.mapGet(b);
            ConnectionStub c = new ConnectionStub();
            c.fromBinary(data);
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

    public Channel registerChannel(Stub stub,Session.TimeoutListener timeoutListener){
        GameZoneIndex index = gameZoneIndex.get(stub.zoneId);
        UDPChannel channel = index.pendingChannels.poll();
        UDPEndpoint udp = (UDPEndpoint) this.serviceContext.serviceProvider(EndPoint.UDP_ENDPOINT);
        if(channel == null){
            UDPChannel[] channels = udp.createChannels(index.gameZone.capacity());
            if(channels.length == 0) return null;
            for(int i=0;i<channels.length;i++){
                if(i==0){
                    channel = channels[i];
                }
                else{
                    index.pendingChannels.offer(channels[i]);
                }
            }
        }
        GameRoom room = gameRoomIndex.get(stub.roomId);
        BlackjackModule module = new BlackjackModule();
        module.setup(room,gameServiceProvider);
        room.setup(module);
        channel.register(stub,room,room,room,timeoutListener);
        udp.registerChannel(channel);
        return channel;
    }

    public GameZone gameZoneFromZoneId(String zoneId){
        return gameZoneIndex.get(zoneId).gameZone;
    }

    public GameRoom join(Rating rating,GameZone gameZone){
        GameZoneIndex index = gameZoneIndex.get(gameZone.distributionKey());
        return dedicated?remoteJoin(rating,gameZone):localJoin(rating,index);
    }

    public void leave(Stub stub){
        if(dedicated){
            this.distributionRoomService.onLeaveRoom(name,stub.zoneId,stub.roomId,stub.systemId());
            return;
        }
        GameZoneIndex index = gameZoneIndex.get(stub.zoneId);
        localLeave(stub.systemId(),index,stub.roomId,(room,entry)-> {
            if(room.empty()) {
                room.reset();
                index.pendingRooms.offer(room.roomId());
            }
        });
    }

    public GameRoom onRoomViewed(String zoneId,String roomId){
        GameZoneIndex gameZone = gameZoneIndex.get(zoneId);
        GameRoom gameRoom = gameRoom(gameZone,roomId);
        return gameRoom!=null?gameRoom.view():null;
    }

    public GameRoom onRoomJoined(String zoneId,String roomId, String systemId){
        GameZoneIndex index = gameZoneIndex.get(zoneId);
        GameRoom gameRoom = gameRoom(index,roomId);
        if(gameRoom==null) return null;
        return gameRoom.join(systemId,(room,entry) -> {
            pendingRooms.offer(new RoomStatus(zoneId,roomId,room.full()?RoomStatus.FULL:RoomStatus.JOINED));
        });
    }

    public void onRoomLeft(String zoneId,String roomId,String systemId){
        GameZoneIndex index = gameZoneIndex.get(zoneId);
        localLeave(systemId,index,roomId,(room,entry)->{
            boolean empty = room.started() && room.empty();
            if(empty) room.reset();
            pendingRooms.offer(new RoomStatus(zoneId,roomId,empty?RoomStatus.EMPTY:RoomStatus.LEFT));//reuse room id
        });
    }

    public void onRoomReset(String zoneId,String roomId){
        GameZoneIndex index = gameZoneIndex.get(zoneId);
        GameRoom gameRoom = gameRoom(index,roomId);
        gameRoom.reset();
        //logger.warn("room reset->"+gameRoom);
    }

    @Override
    public <T extends Configurable> void register(T t) {
        logger.warn("Game Zone Registered With ["+t.configurationTypeId()+"/"+t.configurationName()+"]["+minRoomPoolSizePerZone+"]");
        GameZone gameZone = (GameZone)t;
        GameZoneIndex index = new GameZoneIndex();
        index.gameZone = gameZone;
        index.maxRoomPoolSize = new AtomicInteger(maxRoomPoolSizePerZone);
        index.pendingChannels = new ArrayBlockingQueue<>(maxRoomPoolSizePerZone*gameZone.capacity());
        if(dedicated) {
            index.roomStore = this.clusterProvider.clusterStore(ClusterProvider.ClusterStore.SMALL,gameZone.oid());
        }
        else{
            index.pendingRooms = new ArrayBlockingQueue<>(maxRoomPoolSizePerZone*gameZone.capacity());
        }
        index.roomIndex = new IndexSet(gameZone.configurationTypeId());
        index.roomIndex.distributionKey(serviceContext.node().nodeId());
        this.dataStore.createIfAbsent(index.roomIndex,true);
        int[] rooms = {0};
        index.roomIndex.keySet().forEach(k->{
            GameRoom room = gameRoom(index,k);
            room.reset();
            rooms[0]++;
            if(dedicated){
                byte[] roomId = k.getBytes();
                //make sure no other nodes are using the roomId
                index.roomStore.mapLock(roomId);
                if(!index.roomStore.mapExists(roomId)){
                    index.roomStore.mapSet(roomId,ROOM_ID_QUEUED);
                    index.roomStore.queueOffer(roomId);
                }
                else{
                    logger.warn(room.roomId()+" : <In Use>");
                }
                index.roomStore.mapUnlock(roomId);
            }
            else{
                resetRoom(index,k);
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
                        index.roomStore.mapSet(rkey,ROOM_ID_QUEUED);
                        index.roomStore.queueOffer(rkey);
                    }
                    else{
                        resetRoom(index,gameRoom.roomId());
                    }
                }
            }
            this.dataStore.update(index.roomIndex);
        }
        int roomPoolRemaining = index.maxRoomPoolSize.addAndGet((-1)*rooms[0]);
        logger.warn(gameZone+" Remaining Room Pool Size ["+roomPoolRemaining+"] Capacity ["+gameZone.capacity()+"]");
        if(started){
            UDPEndpoint udp = (UDPEndpoint)serviceContext.serviceProvider(UDPEndpoint.UDP_ENDPOINT);
            for(int i=0;i<minRoomPoolSizePerZone;i++){
                UDPChannel[] channels = udp.createChannels(index.gameZone.capacity());
                for(UDPChannel c : channels){
                    index.pendingChannels.offer(c);
                }
            }
            logger.warn("Initializing push channels ["+minRoomPoolSizePerZone+"]");
        }
        gameZoneIndex.put(gameZone.distributionKey(),index);
    }

    @Override
    public <T extends Configurable> void release(T t) {
        GameZoneIndex index = gameZoneIndex.remove(t.distributionKey());
        UDPChannel udpChannel;
        do{
            udpChannel = index.pendingChannels.poll();
            if(udpChannel!=null){
                udpChannel.close();
            }
        }while (udpChannel!=null);
    }


    @Override
    public String typeId() {
        return this.typeLobby;
    }

    @Override
    public OnAccess onConnection(Connection connection) {
        GameZoneIndex index = gameZoneIndex(connection.configurationName());
        if(index==null || !gameCluster.dedicated()) return null;
        UDPEndpoint udpEndpoint = (UDPEndpoint) this.serviceContext.serviceProvider(UDPEndpoint.UDP_ENDPOINT);
        int timeout = udpEndpoint.sessionTimeout();
        connection.timeout(timeout);
        byte[] serverId = connection.serverId().getBytes();
        byte[] data = connection.toBinary();
        serverClusterStore.mapSet(serverId,data);
        serverClusterStore.indexSet(typeLobby,serverId);
        this.clusterProvider.deployService().onRegisterConnection(connection);
        OnAccess onAccess = new OnAccessTrack();
        onAccess.property("sessionTimeout",timeout);
        onAccess.property("capacity",index.gameZone.capacity());
        onAccess.property("duration",index.gameZone.roundDuration());
        onAccess.property("overtime",index.gameZone.roundOvertime());
        onAccess.property("joinsOnStart",index.gameZone.joinsOnStart());
        return onAccess;
    }

    @Override
    public boolean onChannel(Channel channel) {
        ChannelStub channelStub = (ChannelStub)channel;
        ConnectionStub connectionStub = connectionIndex.get(channelStub.serverId);
        if(connectionStub==null){
            logger.warn("no server connection ["+channelStub.serverId);
            return false;
        }
        GameZoneIndex index = gameZoneIndex(connectionStub.configurationName());
        if(index==null) throw new RuntimeException("no lobby available");
        byte[] roomId = index.roomStore.queuePoll();
        if(roomId==null && index.maxRoomPoolSize.decrementAndGet() < 0){
            logger.warn("No room id available on game zone->"+connectionStub.configurationName());
            return false;
        }
        if(roomId==null){
            logger.warn("Creating new room on game zone->"+connectionStub.configurationName());
            GameRoom gameRoom = this.gameRoom(index,null);
            if(gameRoom==null) return false;
            roomId = gameRoom.roomId().getBytes();
        }
        index.roomStore.indexSet(connectionStub.serverId(),roomId);
        channelStub.roomId = new String(roomId);
        for(int i=0;i<index.gameZone.capacity();i++){
            channelStub.sessionId(channelStub.sessionId()+i);
            //logger.warn(channelStub.toString());
            byte[] channelData = channelStub.toBinary();
            ClusterProvider.ClusterStore channelStore = channelStore(channelStub.serverId);
            channelStore.queueOffer(channelData);
        }
        return true;
    }

    public void onStartConnection(Connection connection){
        ConnectionStub connectionStub = connectionIndex.get(connection.serverId());
        if(connectionStub==null) return;
        this.clusterProvider.deployService().onStartConnection(connection);
    }

    public void onDisConnection(Connection connection){
        this.clusterProvider.deployService().onReleaseConnection(connection);//clean node local cache
        serverClusterStore.mapRemove(connection.serverId().getBytes());
        serverClusterStore.indexRemove(typeLobby,connection.toBinary());
        cleanConnection(connection);
    }

    public void onConnectionStarted(Connection connection){
        ConnectionStub connectionStub = connectionIndex.get(connection.serverId());
        if(connectionStub==null) return;
        connectionStub.started.set(true);
        pendingConnections.offer(connectionStub);
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
        connectionIndex.put(connection.serverId(),connectionStub);
    }

    public void onConnectionReleased(Connection connection){
        pendingConnections.remove(connection);
        connectionIndex.remove(connection.serverId());
    }

    @Override
    public void reload(int partition, boolean localMember) {
        gameRoomIndex.clear();
    }

    public void onStart(EndPoint endPoint){
        if(endPoint.name().equals(EndPoint.UDP_ENDPOINT)){
            UDPEndpoint udp = (UDPEndpoint)endPoint;
            gameZoneIndex.forEach((k,v)->{
                for(int i=0;i<minRoomPoolSizePerZone;i++){
                    UDPChannel[] channels = udp.createChannels(v.gameZone.capacity());
                    for(UDPChannel c : channels){
                        v.pendingChannels.offer(c);
                    }
                }
            });
            logger.warn("Initializing push channels ["+typeLobby+"]["+minRoomPoolSizePerZone+"]");
        }
        started = true;
    }


    private void cleanConnection(Connection connection){
        ClusterProvider.ClusterStore channelStore = this.clusterProvider.clusterStore(ClusterProvider.ClusterStore.SMALL,connection.serverId(),false,false,true);
        channelStore.destroy();
        GameZoneIndex index = gameZoneIndex(connection.configurationName());
        Collection<byte[]> ids = index.roomStore.indexGet(connection.serverId());
        ids.forEach(k->{
            //kickoff all remote room
            this.distributionRoomService.onResetRoom(name,index.gameZone.distributionKey(),new String(k));
            index.roomStore.mapRemove(k);
            index.roomStore.queueOffer(k);
        });
        index.roomStore.indexRemove(connection.serverId());
    }

    private void onSchedule() {
        kickoff.clear();
        connectionIndex.forEach((k,v)->{
            if(v.onTimeout(timer)) kickoff.add(k);
        });
        kickoff.forEach(k->{
            logger.warn("Connection kickoff->"+k);
            onDisConnection(connectionIndex.get(k));
        });
        RoomStatus roomStatus = pendingRooms.poll();
        if(roomStatus==null) return;
        //logger.warn("Reset room->"+roomStatus);
        GameZoneIndex index = gameZoneIndex.get(roomStatus.zoneId);
        byte[] roomId = roomStatus.roomId.getBytes();
        index.roomStore.mapLock(roomId);
        if(roomStatus.status == RoomStatus.FULL){
            index.roomStore.mapRemove(roomId);
        }
        else if(roomStatus.status == RoomStatus.EMPTY){
            if(!index.roomStore.mapExists(roomId)){
                index.roomStore.mapSet(roomId,ROOM_ID_QUEUED);
                index.roomStore.queueOffer(roomId);
            }
            else{
                logger.warn(roomStatus.roomId+" : <In Use>");
            }
        }
        index.roomStore.mapUnlock(roomId);

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

    private GameZoneIndex gameZoneIndex(String configurationName){
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
        synchronized (zoneIndex.roomIndex){
            zoneIndex.roomIndex.addKey(gameRoom.roomId());
            this.dataStore.update(zoneIndex.roomIndex);
        }
        gameRoomIndex.put(gameRoom.roomId(),gameRoom);
        return gameRoom;
    }

    private void localLeave(String systemId, GameZoneIndex index, String roomId, GameRoom.Listener listener){
        GameRoom gameRoom = gameRoom(index,roomId);
        if(gameRoom==null) {
            logger.warn("Room Missed->"+index.gameZone.distributionKey()+">>"+roomId);
            return;
        }
        gameRoom.leave(systemId,listener);
    }

    private GameRoom localJoin(Rating rating, GameZoneIndex index){
        String roomId = index.pendingRooms.poll();
        if(roomId==null && index.maxRoomPoolSize.decrementAndGet() < 0) return null;
        GameRoom gameRoom = gameRoom(index,roomId);
        if(gameRoom==null) return null;
        GameRoom joined = gameRoom.join(rating.systemId(),(room,entry)->{
            if(!room.full()) index.pendingRooms.offer(roomId);
        });
        joined.setup(index.gameZone,null,rating);

        return joined;
    }

    private GameRoom remoteJoin(Rating rating,GameZone gameZone){
        ConnectionStub connectionStub = pendingConnections.poll();
        if(connectionStub==null){
            logger.warn("no game server connection for ["+gameZone.configurationTypeId()+"/"+gameZone.configurationName()+"]");
            return null;
        }
        pendingConnections.offer(connectionStub);
        ClusterProvider.ClusterStore channelStore = this.channelStore(connectionStub.serverId());
        byte[] ret = channelStore.queuePoll();
        if(ret == null){
            logger.warn("no channel available for connection ["+connectionStub.serverId()+"]");
            return null;
        }
        ChannelStub channelStub = new ChannelStub();
        channelStub.fromBinary(ret);
        GameRoom room = this.distributionRoomService.onJoinRoom(name,gameZone.distributionKey(),channelStub.roomId,rating.systemId());
        Channel channel = channelStub.toChannel(connectionStub.clientConnection(),connectionStub.serverKey,connectionStub.timeout());
        room.setup(gameZone,channel,rating);
        return room;
    }

    private void resetRoom(GameZoneIndex index,String roomId){
        for(int i=0; i<index.gameZone.capacity(); i++){
            index.pendingRooms.offer(roomId);
        }
    }

    private ClusterProvider.ClusterStore channelStore(String serverId){
        return clusterProvider.clusterStore(ClusterProvider.ClusterStore.SMALL,serverId,false,false,true);
    }

}
