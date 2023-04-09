package com.tarantula.platform.room;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.protocol.Channel;
import com.icodesoftware.protocol.GameModule;
import com.icodesoftware.protocol.GameServerListener;
import com.icodesoftware.service.*;

import com.tarantula.cci.udp.UDPChannel;
import com.tarantula.cci.udp.UDPEndpoint;
import com.tarantula.game.GameZone;
import com.tarantula.game.Rating;
import com.tarantula.game.Stub;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.OnAccessTrack;
import com.icodesoftware.util.ScheduleRunner;
import com.tarantula.platform.util.SystemUtil;


import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PlatformRoomServiceProvider implements ConfigurationServiceProvider, GameServerListener, ReloadListener,RoomListener {

    private static final String CONFIG = "game-room-settings";
    private static final String DS_SUFFIX = "_room";

    public static final String NAME = "room";

    private static final String DEDICATED_GAME_MODULE_NAME = "com.tarantula.platform.room.DedicatedGameModule";

    private TarantulaLogger logger;
    private final String name;
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
        this.dataStore = serviceContext.dataStore(name.replace("-","_")+DS_SUFFIX,serviceContext.node().partitionNumber());
        this.gameZoneIndex = new ConcurrentHashMap<>();
        this.gameRoomIndex = new ConcurrentHashMap<>();
        Configuration configuration = serviceContext.configuration(CONFIG);
        JsonObject jsonObject = ((JsonElement)configuration.property(playMode)).getAsJsonObject();
        this.maxDedicatedServerConnections = jsonObject.get("maxDedicatedServerConnections").getAsInt();
        this.maxRoomPoolSizePerZone = jsonObject.get("maxRoomPoolSizePerZone").getAsInt();
        this.minRoomPoolSizePerZone = jsonObject.get("minRoomPoolSizePerZone").getAsInt();
        if(this.dedicated){
            this.connectionIndex = new ConcurrentHashMap<>();
            this.serverClusterStore = this.serviceContext.clusterProvider().clusterStore(ClusterProvider.ClusterStore.SMALL,gameCluster.typeId()+"."+NAME);
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
        this.logger = serviceContext.logger(PlatformRoomServiceProvider.class);
    }

    @Override
    public void start() throws Exception {
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
        logger.warn("Room service provider started for ["+gameCluster.property(GameCluster.NAME)+"]["+typeLobby+"]["+this.playMode+"]["+dedicated+"]["+maxRoomPoolSizePerZone+"]");
    }

    @Override
    public void shutdown() throws Exception {
        this.clusterProvider.unregisterReloadListener(reloadKey);
        this.serviceContext.deploymentServiceProvider().unregisterGameServerListener(registerKey);
        gameZoneIndex.forEach((k,z)->{
            if(dedicated) z.gameModule.close();
        });
    }

    public Channel registerChannel(Stub stub, Session.TimeoutListener timeoutListener){
        GameZoneIndex index = gameZoneIndex.get(stub.zoneId);
        UDPEndpoint udp = (UDPEndpoint) this.serviceContext.serviceProvider(EndPoint.UDP_ENDPOINT);
        if(this.dedicated){
            UDPChannel channel = index.pendingPushChannels.poll();
            if(channel == null){
                UDPChannel[] channels = udp.createChannels(index.gameZone.capacity());
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
            GameRoom room = index.gameRoom;
            channel.register(stub,room,room,room,timeoutListener);
            udp.registerChannel(channel);
            return channel;
        }
        else{//local join case
            GameRoom room = gameRoomIndex.get(stub.roomId);
            Channel channel = room.registerChannel(stub,timeoutListener);
            udp.registerChannel((UDPChannel)channel);
            //logger.warn("Using assigned channel ["+channel.channelId()+"/"+channel.sessionId()+"]");
            return channel;
        }
    }

    public GameZone gameZoneFromZoneId(String zoneId){
        return gameZoneIndex.get(zoneId).gameZone;
    }

    public GameRoom join(Rating rating,GameZone gameZone){
        GameZoneIndex index = gameZoneIndex.get(gameZone.distributionKey());
        return dedicated?remoteJoin(rating,index):localJoin(rating,index);
    }

    public void leave(Stub stub){
        if(dedicated) return; //close from channel close
        GameZoneIndex index = gameZoneIndex.get(stub.zoneId);
        localLeave(stub.systemId(),index,stub.roomId,(room,entry)-> {});
    }

    @Override
    public <T extends Configurable> void register(T t) {
        logger.warn("Game Zone Registered With ["+t.configurationTypeId()+"/"+t.configurationName()+"]["+minRoomPoolSizePerZone+"]");
        GameZone gameZone = (GameZone)t;
        this.logger.warn("Game zone is running on game module ["+gameZone.gameModule()+"]");
        GameZoneIndex index = new GameZoneIndex();
        index.gameZone = gameZone;
        index.maxRoomPoolSize = new AtomicInteger(maxRoomPoolSizePerZone);
        if(dedicated) {
            index.pendingPushChannels = new ArrayBlockingQueue<>(maxRoomPoolSizePerZone*gameZone.capacity());
            index.pendingConnections = new LinkedBlockingDeque<>(maxDedicatedServerConnections);
            index.gameRoom = this.newGameRoom(gameZone.playMode(),gameZone.capacity());
            GameModule gameModule = gameModule(DEDICATED_GAME_MODULE_NAME,index.gameRoom);
            index.gameModule = gameModule;
            index.gameRoom.setup(gameZone,gameModule,dedicated);
            if(started){
                UDPEndpoint udp = (UDPEndpoint)serviceContext.serviceProvider(UDPEndpoint.UDP_ENDPOINT);
                for(int i=0;i<minRoomPoolSizePerZone;i++){
                    UDPChannel[] channels = udp.createChannels(index.gameZone.capacity());
                    for(UDPChannel c : channels){
                        index.pendingPushChannels.offer(c);
                    }
                }
            }
            logger.warn("Initializing push channels ["+minRoomPoolSizePerZone+"]");
        }
        else{
            index.pendingRooms = new ArrayBlockingQueue<>(maxRoomPoolSizePerZone);
            index.runningRooms = new LinkedBlockingDeque<>(maxRoomPoolSizePerZone);
            index.roomIndex = new IndexSet(gameZone.configurationTypeId());
            index.roomIndex.distributionKey(serviceContext.node().nodeId());
            this.dataStore.createIfAbsent(index.roomIndex,true);
            int[] rooms = {0};
            index.roomIndex.keySet().forEach(k->{
                GameRoom room = loadGameRoom(index,k);
                if(room!=null) rooms[0]++;
            });
            if(rooms[0] < minRoomPoolSizePerZone){
                int remaining = minRoomPoolSizePerZone - rooms[0];
                logger.warn("Creating game room on node->"+serviceContext.node().nodeName()+" : "+remaining);
                for(int i=0; i<remaining;i++){
                    GameRoom gameRoom = this.createGameRoom(index,true);
                    if(gameRoom!=null) rooms[0]++;
                }
            }
            this.dataStore.update(index.roomIndex);
            int roomPoolRemaining = index.maxRoomPoolSize.addAndGet((-1)*rooms[0]);
            logger.warn(gameZone+" Remaining Room Pool Size ["+roomPoolRemaining+"] Capacity ["+gameZone.capacity()+"]");
            if(started) {
                UDPEndpoint udp = (UDPEndpoint) serviceContext.serviceProvider(UDPEndpoint.UDP_ENDPOINT);
                gameRoomIndex.forEach((rk, rv) -> {
                    rv.setup(udp.createChannels(gameZone.capacity()));
                });
                logger.warn("Initializing push channels ["+minRoomPoolSizePerZone+"]");
            }
        }
        gameZoneIndex.put(gameZone.distributionKey(),index);
    }

    @Override
    public <T extends Configurable> void release(T t) {
        GameZoneIndex index = gameZoneIndex.remove(t.distributionKey());
        index.gameModule.close();
        UDPChannel udpChannel;
        do{
            udpChannel = index.pendingPushChannels.poll();
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
        if(index==null || !this.dedicated) {
            logger.warn("No game lobby available for ["+connection.configurationName()+"]");
            return null;
        }
        byte[] lockKey = index.gameZone.distributionKey().getBytes();
        serverClusterStore.mapLock(lockKey);
        serverClusterStore.mapGet(lockKey);
        serverClusterStore.mapUnlock(lockKey);
        UDPEndpoint udpEndpoint = (UDPEndpoint) this.serviceContext.serviceProvider(UDPEndpoint.UDP_ENDPOINT);
        int timeout = udpEndpoint.sessionTimeout();
        connection.timeout(timeout);
        this.clusterProvider.deployService().onRegisterConnection(connection);
        OnAccess onAccess = new OnAccessTrack();
        onAccess.property("sessionTimeout",timeout);
        onAccess.property("capacity",index.gameZone.capacity());
        onAccess.property("duration",index.gameZone.roundDuration());
        onAccess.property("overtime",index.gameZone.roundOvertime());
        onAccess.property("joinsOnStart",index.gameZone.joinsOnStart());
        onAccess.property("gameModule",index.gameZone.gameModule());
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
            UDPEndpoint udp = (UDPEndpoint)endPoint;
            gameZoneIndex.forEach((k,v)->{
                if(dedicated){
                    for(int i=0;i<minRoomPoolSizePerZone;i++){
                        UDPChannel[] channels = udp.createChannels(v.gameZone.capacity());
                        for(UDPChannel c : channels){
                            v.pendingPushChannels.offer(c);
                        }
                    }
                }
                else{
                    gameRoomIndex.forEach((rk,rv)->{
                        rv.setup(udp.createChannels(v.gameZone.capacity()));
                    });
                    logger.warn("Total running/pending rooms ["+v.runningRooms.size()+"/"+v.pendingRooms.size()+"] on ["+typeLobby+"]["+v.gameZone.name()+"]");
                }

            });
            //logger.warn("Initializing push channels ["+typeLobby+"]["+minRoomPoolSizePerZone+"]["+dedicated+"]");
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

    private GameRoom loadGameRoom(GameZoneIndex zoneIndex,String roomId){
        GameZone gameZone = zoneIndex.gameZone;
        return gameRoomIndex.computeIfAbsent(roomId,(k)->{
            GameRoom _gameRoom = this.newGameRoom(gameZone.playMode(),gameZone.capacity());
            _gameRoom.distributionKey(roomId);
            if(!this.dataStore.load(_gameRoom)) return null;
            _gameRoom.dataStore(this.dataStore);
            _gameRoom.load();
            GameModule gameModule = gameModule(zoneIndex.gameZone.gameModule(),_gameRoom);
            _gameRoom.setup(zoneIndex.gameZone,gameModule,dedicated);
            resetGameRoom(zoneIndex,_gameRoom,true);
            return _gameRoom;
        });
    }

    private GameRoom createGameRoom(GameZoneIndex zoneIndex,boolean queued){
        if(zoneIndex.maxRoomPoolSize.decrementAndGet() < 0) {
            logger.warn("Max room pool size overflow ->"+maxRoomPoolSizePerZone);
            return null;
        }
        GameZone gameZone = zoneIndex.gameZone;
        GameRoom gameRoom = this.newGameRoom(gameZone.playMode(),gameZone.capacity());
        if(!this.dataStore.create(gameRoom)) return null;
        gameRoom.dataStore(this.dataStore);
        GameModule gameModule = gameModule(zoneIndex.gameZone.gameModule(),gameRoom);
        gameRoom.setup(zoneIndex.gameZone,gameModule,dedicated);
        synchronized (zoneIndex.roomIndex){
            zoneIndex.roomIndex.addKey(gameRoom.roomId());
            this.dataStore.update(zoneIndex.roomIndex);
        }
        gameRoomIndex.put(gameRoom.roomId(),gameRoom);
        resetGameRoom(zoneIndex,gameRoom,queued);
        return gameRoom;
    }

    private void resetGameRoom(GameZoneIndex index,GameRoom room,boolean queued){
        room.reset();
        if(queued) index.pendingRooms.offer(room);
        if(!started) return;
        UDPEndpoint udp = (UDPEndpoint)serviceContext.serviceProvider(UDPEndpoint.UDP_ENDPOINT);
        room.setup(udp.createChannels(index.gameZone.capacity()));
    }

    private void localLeave(String systemId, GameZoneIndex index, String roomId, GameRoom.Listener listener){
        GameRoom gameRoom = loadGameRoom(index,roomId);
        if(gameRoom==null) {
            logger.warn("Room Missed->"+index.gameZone.distributionKey()+">>"+roomId);
            return;
        }
        gameRoom.leave(systemId,listener);
    }

    private GameRoom joinGameRoom(GameZoneIndex index,GameRoom gameRoom,Rating rating){
        GameRoom joined = gameRoom.join(rating.systemId(),(room,entry)->{
            if(room.available()){
                index.runningRooms.addFirst(room);
            }
        });
        joined.setup(index.gameZone,null,rating);
        return joined;
    }
    private GameRoom localJoin(Rating rating, GameZoneIndex index){
        GameRoom gameRoom = index.runningRooms.poll();
        if(gameRoom != null) return joinGameRoom(index,gameRoom,rating);
        gameRoom = index.pendingRooms.poll();
        if(gameRoom != null) return joinGameRoom(index,gameRoom,rating);
        gameRoom = createGameRoom(index,false);
        if(gameRoom == null) return null;
        return joinGameRoom(index,gameRoom,rating);
    }

    private GameRoom remoteJoin(Rating rating,GameZoneIndex gameZoneIndex){
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
        joined.setup(gameZone,channel,rating);
        return joined;
    }


    private ClusterProvider.ClusterStore channelStore(String serverId){
        return clusterProvider.clusterStore(ClusterProvider.ClusterStore.SMALL,serverId,false,false,true);
    }

    private GameModule gameModule(String moduleName,Room room){
        GameModule gameModule = SystemUtil.gameModule(moduleName);
        gameModule.setup(room,gameServiceProvider.gameContext(gameModule.getClass()));
        gameModule.registerRoomListener(this);
        return gameModule;
    }

    @Override
    public void onStarted(Room room) {
        logger.warn("Room started->"+room.distributionKey());
        //logger.warn("RoomID B->"+room.roomId()+">>>"+index.pendingRooms.size()+">>>"+index.runningRooms.size());
    }
    @Override
    public void onUpdated(Room room, byte[] payload) {
        this.logger.warn("Room updated");
    }

    @Override
    public void onEnded(Room room) {
        if(room.dedicated()) return;
        this.logger.warn("Room ended");
        GameZoneIndex index = gameZoneIndex.get(room.owner());
        if(index==null){
            logger.warn("Game lobby not available ["+room.owner()+"]");
            return;
        }
        UDPEndpoint udpEndpoint = (UDPEndpoint) this.serviceContext.serviceProvider(UDPEndpoint.UDP_ENDPOINT);
        udpEndpoint.releaseChannel(room.channelId());
        index.runningRooms.remove(room);
        resetGameRoom(index,gameRoomIndex.get(room.distributionKey()),true);
        //forcefully reset room
    }
}
