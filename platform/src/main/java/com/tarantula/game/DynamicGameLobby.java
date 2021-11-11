package com.tarantula.game;

import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.room.GameRoom;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DynamicGameLobby extends IndexSet implements GameLobby, Configurable.Listener<GameZone>,GameRoomListener {

    private int levelMatchOffset;
    private JsonObject payload;
    private CopyOnWriteArrayList<GameZone> zoneList;
    private ConcurrentHashMap<Integer,GameZone> zoneIndex;
    private ApplicationContext context;
    private Descriptor application;
    private DeploymentServiceProvider deploymentServiceProvider;
    private GameServiceProvider gameServiceProvider;
    private ConcurrentHashMap<String,Stub> stubIndex;

    public DynamicGameLobby(){
        super("gameLobby");
        payload = new JsonObject();
        zoneList = new CopyOnWriteArrayList<>();
        zoneIndex = new ConcurrentHashMap<>();
        stubIndex = new ConcurrentHashMap<>();
    }
    public int levelMatchOffset(){
        return levelMatchOffset;
    }
    public void levelMatchOffset(int levelMatchOffset){
        this.levelMatchOffset =levelMatchOffset;
    }
    public void addGameZone(GameZone gameZone){
        zoneList.add(gameZone);
    }

    public Stub join(Session session, Rating rating){
        Stub stub = stubIndex.get(session.systemId());
        if(stub!=null&&stub.joined) return stub;
        GameZone _zone = zoneIndex.get(rating.level);
        Stub _stub = _zone.join(session,rating);
        stubIndex.put(session.systemId(),_stub);
        return _stub;
    }

    public List<GameZone> list(){
        ArrayList<GameZone> list = new ArrayList<>();
        zoneList.forEach((v)->list.add(v));
        Collections.sort(zoneList,new GameZoneComparator());
        return list;
    }
    public void leave(Session session){
        Stub stub = stubIndex.remove(session.systemId());
        if(stub==null) return;
        stub.zone.leave(stub);
    }
    public void update(Session session, byte[] payload){
        Stub stub = stubIndex.get(session.systemId());
        if(stub==null){
            session.write(JsonUtil.toSimpleResponse(false,"no access token").getBytes());
            return;
        }
        stub.zone.update(session,stub,payload);
    }
    public void list(Session session){
        Stub stub = stubIndex.get(session.systemId());
        if(stub==null){
            session.write(JsonUtil.toSimpleResponse(false,"no access token").getBytes());
            return;
        }
        stub.zone.list(session,stub);
    }


    @Override
    public Map<String,Object> toMap(){
        this.properties.put("levelMatchOffset",levelMatchOffset);
        this.properties.put("payload",this.payload.toString());
        this.properties.put("disabled",this.disabled);
        return super.toMap();
    }

    @Override
    public void fromMap(Map<String,Object> properties){
        this.payload = JsonUtil.parse((String)properties.remove("payload"));
        this.disabled = (boolean)properties.remove("disabled");
        this.levelMatchOffset = ((Number)properties.remove("levelMatchOffset")).intValue();
        super.fromMap(properties);
    }

    @Override
    public int getFactoryId() {
        return GamePortableRegistry.OID;
    }

    @Override
    public int getClassId() { return GamePortableRegistry.GAME_LOBBY_CID; }

    @Override
    public boolean configureAndValidate(byte[] data){
        payload = JsonUtil.parse(data);
        return true;
    }

    public Descriptor descriptor(){
        return application;
    }

    public void descriptor(Descriptor descriptor){

    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.application = context.descriptor();
        this.deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        this.gameServiceProvider = this.context.serviceProvider(application.typeId().replace("lobby","service"));
    }

    @Override
    public void start() throws Exception{
        Collections.sort(zoneList,new GameZoneComparator());
        int levelEnd = application.accessRank()*levelMatchOffset;
        int levelStart = levelEnd-(levelMatchOffset-1);
        this.context.log("game start on level match from ["+levelStart+" to "+levelEnd+"]",OnLog.WARN);
        for(GameZone gameZone : zoneList){
            if(gameZone.disabled()) continue;
            gameZone.registerListener(this);
            gameZone.setup(this.context,this);
            deploymentServiceProvider.register(gameZone);
            gameServiceProvider.roomServiceProvider().register(gameZone);
            for(int i=levelStart;i<gameZone.levelMatch();i++){
                zoneIndex.put(i,gameZone);
            }
            zoneIndex.put(gameZone.levelMatch(),gameZone);
            levelStart = gameZone.levelMatch()+1;
        }
        if(levelStart<levelEnd){
            GameZone lastZone = zoneIndex.get(levelStart-1);
            for(int i=levelStart;i<=levelEnd;i++){
                zoneIndex.put(i,lastZone);
            }
        }
        //zoneIndex.forEach((k,v)->{
            //context.log("Level ["+k+"] registered on ["+v.levelMatch()+"]",OnLog.WARN);
        //});
    }

    @Override
    public void shutdown() throws Exception{
        for(GameZone gameZone : zoneList){
            gameZone.close();
            this.deploymentServiceProvider.release(gameZone);
            this.gameServiceProvider.roomServiceProvider().release(gameZone);
        }
    }
    public  void onLoaded(GameZone loaded){
        //context.log("zone loaded in room service provider->"+loaded.distributionKey(),OnLog.WARN);
        //gameRoomRegistryManagers.put(loaded.distributionKey(),new GameRoomRegistryManager(dataStore,loaded));
        //gameRoomManagers.put(loaded.distributionKey(),new GameRoomManager(dataStore,loaded));
    }
    public void onUpdated(GameZone updated){
        //logger.warn("zone updated in room service provider->"+updated.distributionKey());
        //context.log("zone updated in room service provider->"+updated.distributionKey(),OnLog.WARN);

    }
    public void onRemoved(GameZone remoted){
        //context.log("zone removed in room service provider->"+remoted.distributionKey(),OnLog.WARN);
        //logger.warn("zone removed in room service provider->"+remoted.distributionKey());
        //gameRoomRegistryManagers.remove(remoted.distributionKey());
        //gameRoomManagers.remove(remoted.distributionKey());
    }
    public boolean configureGameZone(byte[] payload){
        GameZone gameZone = new DynamicZone();
        Map<String,Object> data = JsonUtil.toMap(payload);
        String zoneId = (String)data.get("zoneId");
        gameZone.distributionKey(zoneId);
        this.dataStore.load(gameZone);
        this.context.log(gameZone.toString(),OnLog.WARN);
        return gameZone.configureAndValidate(data);
    }
    public boolean configureArena(byte[] payload){
        Arena arena = new Arena();
        Map<String,Object> data = JsonUtil.toMap(payload);
        String arenaId = (String) data.get("arenaId");
        arena.distributionKey(arenaId);
        this.dataStore.load(arena);
        this.context.log(arena.toString(),OnLog.WARN);
        return arena.configureAndValidate(data);
    }
    public void reload(){
        this.context.log(this.toString(),OnLog.WARN);
    }

    @Override
    public void timeout(GameRoom room) {
        this.context.log("Room->timeout>"+room.toString(),OnLog.WARN);
        //stubIndex.remove(stub.systemId());
    }
}
