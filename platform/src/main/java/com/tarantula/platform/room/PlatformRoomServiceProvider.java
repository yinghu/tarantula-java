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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledFuture;

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
    private int roomPoolSizePerZone;
    private ConcurrentHashMap<String,GameZoneIndex> gameZoneIndex;
    private ConcurrentHashMap<String, GameRoom> gameRoomIndex;
    private ConcurrentLinkedDeque<ConnectionStub>  pendingConnections;
    private ConcurrentHashMap<String,ConnectionStub> connectionIndex;
    private ConcurrentHashMap<String,GameChannelIndex> gameChannelIndex;

    private String type;
    private String registerKey;
    private String reloadKey;
    private String typeLobby;
    private ScheduledFuture scheduledFuture;
    private ClusterProvider.ClusterStore clusterStore;
    ArrayList<String> kickoff = new ArrayList<>();

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
        this.pendingConnections = new ConcurrentLinkedDeque<>();
        this.connectionIndex = new ConcurrentHashMap<>();
        this.configuration = serviceContext.configuration(CONFIG);
        this.type = (String) gameCluster.property(GameCluster.MODE);
        JsonObject jsonObject = ((JsonElement)configuration.property(type)).getAsJsonObject();
        this.roomPoolSizePerZone = jsonObject.get("roomPoolSizePerZone").getAsInt();
        this.typeLobby = (String) this.gameCluster.property(GameCluster.GAME_LOBBY);
        this.registerKey = this.serviceContext.deploymentServiceProvider().registerGameServerListener(this);
        this.reloadKey = this.serviceContext.clusterProvider().registerReloadListener(this);
        this.scheduledFuture = this.serviceContext.schedule(this);
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
            onConnection(c);
            Collection<byte[]> cc = clusterStore.indexGet(c.serverId());
            cc.forEach(bb->{
                ChannelStub cs = new ChannelStub();
                cs.fromBinary(bb);
                cs.serverId = c.serverId();
                tryOnChannel(cs);
            });
        });
        logger.warn("Room service provider started for ["+gameCluster.property(GameCluster.NAME)+"]["+typeLobby+"]["+roomPoolSizePerZone+"]["+this.type+"]");
    }

    @Override
    public void shutdown() throws Exception {
        this.serviceContext.deploymentServiceProvider().unregisterGameServerListener(registerKey);
        this.serviceContext.clusterProvider().unregisterReloadListener(reloadKey);
        scheduledFuture.cancel(true);
    }

    public GameRoom join(GameZone gameZone, Rating rating){
        if(gameZone.playMode().equals(GameZone.PLAY_MODE_PVE)){
            String roomId = serviceContext.node().bucketName()+"/"+ SystemUtil.oid();
            GameRoom gameRoom =gameRoomIndex.computeIfAbsent(roomId,k-> this.createGameRoom(gameZone.playMode(),gameZone.capacity()));
            gameRoom.join(rating.systemId(),room->true);
            gameRoom.setup(gameZone,rating);
            gameRoom.distributionKey(roomId);
            return gameRoom;
        }
        RoomJoinStub roomRegistry = this.distributionRoomService.onRegisterRoom(name,gameZone.distributionKey(),rating);
        if(!roomRegistry.joined) return null;
        GameRoom room = this.distributionRoomService.onJoinRoom(name,gameZone.distributionKey(),roomRegistry.roomId,rating.systemId());
        if(room==null) return null;
        room.setup(gameZone,rating);
        return room;
    }
    public void leave(Stub stub){
        if(stub.playMode.equals(GameZone.PLAY_MODE_PVE)) {
            GameRoom gameRoom = gameRoomIndex.remove(stub.roomId);
            if(gameRoom==null) return;
            gameRoom.leave(stub.systemId(),room->true);
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
        return gameRoom.join(systemId,room->{
            ConnectionStub connectionStub = pendingConnections.poll();
            if(connectionStub==null){
                logger.warn("no connection->"+typeLobby);
                return false;
            }
            logger.warn("connection->"+connectionStub.serverId());
            ClusterProvider.ClusterStore channelStore = this.clusterProvider.clusterStore(connectionStub.serverId());
            byte[] ret = channelStore.queuePoll();
            if(ret==null) return false;
            logger.warn(new String(ret));
            GameChannel gameChannel;
            if(connectionStub==null || (gameChannel=connectionStub.gameChannel())==null) {
                this.serviceContext.schedule(new OneTimeRunner(100,()->this.distributionRoomService.release(name,gameRoom.index(),roomId,systemId)));
                return false;
            }
            gameRoom.channel(gameChannel);
            pendingConnections.offer(connectionStub);
            return true;
        });

    }
    public void onRoomLeft(String roomId,String systemId){
        GameRoom gameRoom = gameRoomIndex.get(roomId);
        gameRoom.leave(systemId,room-> {
                room.resetIfEmpty();
                return true;
            }
        );
        this.serviceContext.schedule(new OneTimeRunner(100,()->this.distributionRoomService.release(name,gameRoom.index(),roomId,systemId)));
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
        GameZone gameZone = (GameZone)t;
        String zkey = t.distributionKey();
        GameZoneIndex clusterIndex = this.distributionRoomService.localManaged(zkey);
        clusterIndex.gameZone = gameZone;
        gameZoneIndex.put(zkey,clusterIndex);
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
        }
    }

    @Override
    public <T extends Configurable> void release(T t) {
        gameZoneIndex.remove(t.distributionKey());
    }


    private void onDisConnection(String serverId){
        ConnectionStub connectionStub = connectionIndex.remove(serverId);
        if(connectionStub==null) return;
        pendingConnections.remove(connectionStub);
        connectionStub.close();
        Collection<byte[]> _cb = clusterStore.indexGet(typeLobby);
        Collection<byte[]> _cc = clusterStore.indexGet(serverId);
        logger.warn("cb->"+_cb.size()+">>cc->"+_cc.size()+">>>"+gameChannelIndex.size());
        //clusterProvider.removeIndex(typeLobby,connectionStub.toBinary());
        //clusterProvider.removeIndex(serverId);
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
    public void onConnection(Connection connection) {
        ConnectionStub connectionStub = (ConnectionStub)connection;
        //connectionStub.maxCapacity = roomCapacity;
        connectionStub.init();
        logger.warn("queued->"+pendingConnections.offer(connectionStub));
        connectionIndex.put(connection.serverId(),connectionStub);
        logger.warn("Connection->"+connection.serverId()+">>"+typeLobby);
    }

    private void tryOnChannel(ChannelStub channelStub){
        GameChannelIndex index = distributionRoomService.localManaged(channelStub.channelId());
        if(!index.localManaged) {
            index.channelStub = channelStub;
            index.serverId = channelStub.serverId;
            gameChannelIndex.put(index.toString(),index);
            return;
        }
        onChannel(channelStub);
    }

    @Override
    public void onChannel(Channel channel) {
        ChannelStub channelStub = (ChannelStub)channel;
        String serverId = channelStub.serverId;
        ConnectionStub connectionStub = connectionIndex.get(serverId);
        connectionStub.addChannel(channelStub);
        GameChannelIndex index = distributionRoomService.localManaged(channelStub.channelId());
        index.channelStub = channelStub;
        index.serverId = serverId;
        gameChannelIndex.put(index.toString(),index);
        logger.warn("on channel->"+index);
    }
    public void onDisConnection(Connection connection){
        onDisConnection(connection.serverId());
    }
    public void onPing(String serverId){
        //logger.warn("Ping from server id->"+serverId);
        ConnectionStub connectionStub = connectionIndex.get(serverId);
        if(connectionStub==null) return;
        connectionStub.ping();
    }
    @Override
    public boolean oneTime() {
        return false;
    }

    @Override
    public long initialDelay() {
        return UDPEndpointServiceProvider.GAME_SESSION_TIME_OUT;
    }

    @Override
    public long delay() {
        return UDPEndpointServiceProvider.GAME_SESSION_TIME_OUT+1000;
    }

    @Override
    public void run() {
        kickoff.clear();
        connectionIndex.forEach((k,v)->{
            if(v.timeout()) kickoff.add(k);
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
        }
        return gameRoom;
    }
    private Map<String,GameChannelIndex> reload(){
        HashMap<String,GameChannelIndex> tem = new HashMap<>();
        Collection<byte[]> cb = clusterStore.indexGet(typeLobby);
        cb.forEach(b->{
            ConnectionStub c = new ConnectionStub();
            c.fromBinary(b);
            c.serverKey = this.serviceContext.deploymentServiceProvider().serverKey(typeLobby);
            Collection<byte[]> cc = clusterStore.indexGet(c.serverId());
            cc.forEach(bb->{
                ChannelStub cs = new ChannelStub();
                cs.fromBinary(bb);
                cs.serverId = c.serverId();
                GameChannelIndex index = this.distributionRoomService.localManaged(cs.channelId());
                index.serverId = c.serverId();
                index.channelStub = cs;
                if(!gameChannelIndex.containsKey(index.toString())) tem.put(index.toString(),index);
            });
        });
        return tem;
    }

    @Override
    public void reload(int partition,boolean localMember) {
        //if(type.equals(GameZone.PLAY_MODE_PVE)) return;
        //reload local zone rooms
        gameZoneIndex.forEach((k,v)->{
            if(!v.gameZone.playMode().equals(GameZone.PLAY_MODE_PVE)){
                if(v.partitionId==partition){
                    if(v.localManaged && !localMember){
                        logger.warn("release zone ->"+k+">>"+v.gameZone.name());
                        v.gameZone.roomRegistryQueue().clear();
                        v.gameZone.roomRegistry().clear();
                        v.localManaged = false;
                    }
                    else if(!v.localManaged && localMember){
                        logger.warn("take over zone->"+k+">>"+v.gameZone.name());
                        v.localManaged = true;
                        this.dataStore.list(new GameRoomRegistryQuery(k),r->{
                            v.gameZone.roomRegistry().put(r.instanceId(),r);
                            distributionRoomService.onLoadRoom(name,v.gameZone.distributionKey(),r.instanceId());
                            return true;
                        });
                    }
                }
            }
        });
        //reload connections
        reload().forEach((k,v)->{
            if(v.localManaged) onChannel(v.channelStub);
        });
        gameChannelIndex.forEach((k,v)->{
            if(v.partitionId==partition){
                ConnectionStub cb = connectionIndex.get(v.serverId);
                if(v.localManaged && !localMember){
                    logger.warn("release game channel->"+k+">>"+cb.removeChannel(v.channelStub));
                    v.localManaged = false;
                }
                else if(!v.localManaged && localMember){
                    logger.warn("take over game channel->"+k);
                    v.localManaged = true;
                    cb.addChannel(v.channelStub);
                }
            }
        });

    }

}
