package com.tarantula.game;

import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.IndexSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DynamicGameLobby extends IndexSet implements GameLobby, Configurable.Listener<GameZone> {

    private JsonObject payload;
    private CopyOnWriteArrayList<GameZone> zoneList;
    private ConcurrentHashMap<Integer,GameZone> zoneIndex;
    private int[] levelMatches;
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

    public void addGameZone(GameZone gameZone){
        zoneList.add(gameZone);
    }

    public Stub join(Session session, Rating rating){
        return null;
    }

    public List<GameZone> list(){
        ArrayList<GameZone> list = new ArrayList<>();
        zoneList.forEach((v)->list.add(v));
        return list;
    }
    public void leave(String systemId){}
    public void update(String systemId){}
    public void onTimer(Module.OnUpdate onUpdate){}
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("payload",this.payload.toString());
        this.properties.put("disabled",this.disabled);
        return super.toMap();
    }

    @Override
    public void fromMap(Map<String,Object> properties){
        this.payload = JsonUtil.parse((String)properties.remove("payload"));
        this.disabled = (boolean)properties.remove("disabled");
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
        for(GameZone gameZone : zoneList){
            gameZone.registerListener(this.gameServiceProvider.roomServiceProvider());
            gameZone.registerListener(this);
            gameZone.setup(this.context);
            deploymentServiceProvider.register(gameZone);
        }
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
}
