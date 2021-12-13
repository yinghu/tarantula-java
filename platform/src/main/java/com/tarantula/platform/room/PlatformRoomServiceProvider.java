package com.tarantula.platform.room;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.protocol.GameChannelListener;
import com.icodesoftware.protocol.UDPEndpointServiceProvider;
import com.icodesoftware.service.ConfigurationServiceProvider;
import com.icodesoftware.service.ReloadListener;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.cci.udp.GameChannel;
import com.tarantula.game.Arena;
import com.tarantula.game.GameZone;
import com.tarantula.game.Rating;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.RoomRegistry;
import com.tarantula.platform.service.SystemValidatorProvider;
import com.tarantula.platform.service.cluster.OneTimeRunner;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledFuture;

public class PlatformRoomServiceProvider implements ConfigurationServiceProvider, GameChannelListener,SchedulingTask, ReloadListener {

    private static final String CONFIG = "game-room-settings";
    private static final String DS_SUFFIX = "_room";

    private TarantulaLogger logger;
    private final String name;
    private final GameCluster gameCluster;
    private ServiceContext serviceContext;
    private DistributionRoomService distributionRoomService;
    private SystemValidatorProvider systemValidatorProvider;
    private DataStore dataStore;
    private Configuration configuration;
    private int roomCapacity;
    private int roomPoolSizePerZone;
    private ConcurrentHashMap<String,GameZoneIndex> gameZoneIndex;
    private ConcurrentHashMap<String, GameRoom> gameRoomIndex;
    private ConcurrentLinkedDeque<ConnectionStub>  pendingConnections;
    private ConcurrentHashMap<String,ConnectionStub> connectionIndex;

    private String type;
    private String registerKey;
    private String reloadKey;
    private String typeLobby;
    private ScheduledFuture scheduledFuture;
    ArrayList<String> kickoff = new ArrayList<>();

    public PlatformRoomServiceProvider(GameCluster gameCluster){
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
        this.systemValidatorProvider  = (SystemValidatorProvider)serviceContext.serviceProvider(SystemValidatorProvider.NAME);
        this.distributionRoomService = this.serviceContext.clusterProvider(Distributable.INTEGRATION_SCOPE).serviceProvider(DistributionRoomService.NAME);
        this.dataStore = serviceContext.dataStore(name.replace("-","_")+DS_SUFFIX,serviceContext.partitionNumber());
        this.gameZoneIndex = new ConcurrentHashMap<>();
        this.gameRoomIndex = new ConcurrentHashMap<>();
        this.pendingConnections = new ConcurrentLinkedDeque<>();
        this.connectionIndex = new ConcurrentHashMap<>();
        this.configuration = serviceContext.configuration(CONFIG);
        this.type = (String) gameCluster.property(GameCluster.MODE);
        JsonObject jsonObject = ((JsonElement)configuration.property(type)).getAsJsonObject();
        this.roomCapacity = jsonObject.get("roomCapacity").getAsInt();
        this.roomPoolSizePerZone = jsonObject.get("roomPoolSizePerZone").getAsInt();
        this.typeLobby = (String) this.gameCluster.property(GameCluster.GAME_LOBBY);
        this.registerKey = this.serviceContext.deploymentServiceProvider().registerGameChannelListener(this);
        this.reloadKey = this.serviceContext.clusterProvider(Distributable.INTEGRATION_SCOPE).registerReloadListener(this);
        this.scheduledFuture = this.serviceContext.schedule(this);
        this.logger = serviceContext.logger(PlatformRoomServiceProvider.class);
    }
    @Override
    public void start() throws Exception {
        logger.warn("Room service provider started for ["+gameCluster.property(GameCluster.NAME)+"] Mode ["+type+"]["+typeLobby+"]");
    }

    @Override
    public void shutdown() throws Exception {
        this.serviceContext.deploymentServiceProvider().unregisterGameChannelListener(registerKey);
        this.serviceContext.clusterProvider(Distributable.INTEGRATION_SCOPE).unregisterReloadListener(reloadKey);
        scheduledFuture.cancel(true);
    }

    public GameRoom join(GameZone gameZone, Rating rating){
        if(type.equals(GameZone.PLAY_MODE_PVE)){
            GameRoom gameRoom =gameRoomIndex.computeIfAbsent(rating.systemId(),k-> this.createGameRoom(type,0));
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
        GameZone gameZone = gameZoneIndex.get(gameZoneId).gameZone;
        Arena arena = gameZone.arena(rating.arenaLevel);
        GameRoomRegistry pending = gameZone.roomRegistryQueue().poll();
        if(pending==null) return new RoomJoinStub();
        int ret = pending.addPlayer(rating.systemId(),room->{
            if(room.empty()) room.reset(arena);
            return true;
        });
        if(ret == RoomRegistry.NOT_JOINED) return new RoomJoinStub();
        if(ret == RoomRegistry.JOINED || ret == RoomRegistry.ALREADY_JOINED) gameZone.roomRegistryQueue().offerFirst(pending);
        this.dataStore.update(pending);
        return new RoomJoinStub(pending.arenaLevel,pending.instanceId(),systemValidatorProvider.hashJoinTicket(pending.instanceId(),rating.systemId()));
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
        if(joined.length>0) logger.warn("Sync->"+roomRegistry);
        roomRegistry.sync(joined,room->{
            if(!room.fullJoined())gameZone.roomRegistryQueue().offer(room);
            this.dataStore.update(room);
            return true;
        });
    }
    public GameRoom onView(String roomId){
        GameRoom gameRoom = gameRoomIndex.computeIfAbsent(roomId,(k)->{
            GameRoom _gameRoom = this.createGameRoom(type,0);
            _gameRoom.distributionKey(roomId);
            if(!this.dataStore.load(_gameRoom)) return null;
            _gameRoom.dataStore(this.dataStore);
            _gameRoom.load();
            return _gameRoom;
        });
        return gameRoom!=null?gameRoom.view():null;
    }
    public GameRoom onJoin(String ticket, String roomId, String systemId){
        if(!systemValidatorProvider.validHash(roomId,systemId,ticket)) return null;
        GameRoom gameRoom = gameRoomIndex.computeIfAbsent(roomId,(k)->{
            GameRoom _gameRoom = this.createGameRoom(type,0);
            _gameRoom.distributionKey(roomId);
            if(!this.dataStore.load(_gameRoom)) return null;
            _gameRoom.dataStore(this.dataStore);
            _gameRoom.load();
            return _gameRoom;
        });
        if(gameRoom==null) return null;
        return gameRoom.join(systemId,room->{
            ConnectionStub connectionStub = pendingConnections.poll();
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
        GameRoom gameRoom = this.createGameRoom(this.type,roomCapacity);
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
        GameRoom gameRoom = this.createGameRoom(type,0);
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
        String zkey = t.distributionKey();
        GameZoneIndex clusterIndex = this.distributionRoomService.localManaged(zkey);
        clusterIndex.gameZone = gameZone;
        gameZoneIndex.put(zkey,clusterIndex);
        if(type.equals(GameZone.PLAY_MODE_PVE)) return;
        if(!clusterIndex.localManaged) return;
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
            this.dataStore.create(gameRoomRegistry);
            gameZone.roomRegistry().put(gameRoomRegistry.instanceId(),gameRoomRegistry);
            distributionRoomService.create(name,gameZone.distributionKey(),gameRoomRegistry.instanceId());
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
    }

    @Override
    public String typeId() {
        return this.typeLobby;
    }

    @Override
    public void onConnection(Connection connection) {
        ConnectionStub connectionStub = (ConnectionStub)connection;
        connectionStub.maxCapacity = roomCapacity;
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
        onDisConnection(connection.serverId());
    }
    public void onPing(String serverId){
        ConnectionStub connectionStub = connectionIndex.get(serverId);
        connectionStub.ping();
    }
    @Override
    public boolean oneTime() {
        return false;
    }

    @Override
    public long initialDelay() {
        return UDPEndpointServiceProvider.SESSION_CHECK_INTERVAL;
    }

    @Override
    public long delay() {
        return UDPEndpointServiceProvider.SESSION_CHECK_INTERVAL+1000;
    }

    @Override
    public void run() {
        kickoff.clear();
        connectionIndex.forEach((k,v)->{
            if(!v.check()) kickoff.add(k);
        });
        kickoff.forEach(k->{
            logger.warn("Connection kickoff->"+k);
            onDisConnection(k);
        });
    }
    private GameRoom createGameRoom(String type,int roomCapacity){
        GameRoom gameRoom = null;
        if(type.equals(GameZone.PLAY_MODE_PVE)){
            gameRoom = new PVEGameRoom();
        }
        else if(type.equals(GameZone.PLAY_MODE_PVP)){
            gameRoom = roomCapacity>0?new PVPGameRoom(roomCapacity):new PVPGameRoom();
        }
        return gameRoom;
    }

    @Override
    public void reload(int partition,boolean localMember) {
        if(type.equals(GameZone.PLAY_MODE_PVE)) return;
        //reload local zone rooms
        gameZoneIndex.forEach((k,v)->{
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
                        distributionRoomService.load(name,r.instanceId());
                        return true;
                    });
                }
            }
        });
        //reload connections

    }

}
