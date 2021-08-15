package com.tarantula.game;

import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.IndexSet;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DynamicGameLobby extends IndexSet implements GameLobby, Configurable.Listener<GameZone> {

    private int levelMatchOffset;
    private JsonObject payload;
    private CopyOnWriteArrayList<GameZone> zoneList;
    private ConcurrentHashMap<Integer,GameZone> zoneIndex;
    private ApplicationContext context;
    private Descriptor application;
    private DeploymentServiceProvider deploymentServiceProvider;
    private GameServiceProvider gameServiceProvider;
    public DynamicGameLobby(){
        super("gameLobby");
        payload = new JsonObject();
        zoneList = new CopyOnWriteArrayList<>();
        zoneIndex = new ConcurrentHashMap<>();
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
        GameZone _zone = zoneIndex.get(rating.level);
        return _zone.join(session,rating);
    }

    public List<GameZone> list(){
        ArrayList<GameZone> list = new ArrayList<>();
        zoneList.forEach((v)->list.add(v));
        Collections.sort(zoneList,new GameZoneComparator());
        return list;
    }
    public void leave(String systemId){}
    public void update(String systemId){}
    public void onTimer(Module.OnUpdate onUpdate){}
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
            gameZone.registerListener(this.gameServiceProvider.roomServiceProvider());
            gameZone.registerListener(this);
            gameZone.setup(this.context);
            deploymentServiceProvider.register(gameZone);
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
        zoneIndex.forEach((k,v)->{
            context.log("Level ["+k+"] registered on ["+v.levelMatch()+"]",OnLog.WARN);
        });
    }

    @Override
    public void shutdown() throws Exception{
        for(GameZone gameZone : zoneList){
            this.deploymentServiceProvider.release(gameZone);
        }
    }
    public  void onLoaded(GameZone loaded){
        context.log("zone loaded in room service provider->"+loaded.distributionKey(),OnLog.WARN);
        //gameRoomRegistryManagers.put(loaded.distributionKey(),new GameRoomRegistryManager(dataStore,loaded));
        //gameRoomManagers.put(loaded.distributionKey(),new GameRoomManager(dataStore,loaded));
    }
    public void onUpdated(GameZone updated){
        //logger.warn("zone updated in room service provider->"+updated.distributionKey());
        context.log("zone updated in room service provider->"+updated.distributionKey(),OnLog.WARN);

    }
    public void onRemoved(GameZone remoted){
        context.log("zone removed in room service provider->"+remoted.distributionKey(),OnLog.WARN);
        //logger.warn("zone removed in room service provider->"+remoted.distributionKey());
        //gameRoomRegistryManagers.remove(remoted.distributionKey());
        //gameRoomManagers.remove(remoted.distributionKey());
    }
    public boolean configureGameZone(byte[] payload){
        GameZone gameZone = new DynamicZone();
        JsonObject jsonObject = JsonUtil.parse(payload);
        String zoneId = jsonObject.get("zoneId").getAsString();
        gameZone.distributionKey(zoneId);
        this.dataStore.load(gameZone);
        this.context.log(gameZone.toString(),OnLog.WARN);
        return gameZone.configureAndValidate(jsonObject);
    }
    public boolean configureArena(byte[] payload){
        Arena arena = new Arena();
        JsonObject jsonObject = JsonUtil.parse(payload);
        String arenaId = jsonObject.get("arenaId").getAsString();
        arena.distributionKey(arenaId);
        this.dataStore.load(arena);
        this.context.log(arena.toString(),OnLog.WARN);
        return arena.configureAndValidate(jsonObject);
    }
    public void reload(){
        this.context.log(this.toString(),OnLog.WARN);
    }
}
