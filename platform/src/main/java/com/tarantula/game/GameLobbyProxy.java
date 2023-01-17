package com.tarantula.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.game.service.*;
import com.tarantula.platform.lobby.LobbyItem;
import com.tarantula.platform.service.metrics.GameClusterMetrics;

import java.util.concurrent.ConcurrentHashMap;

public class GameLobbyProxy extends RecoverableObject implements GameLobby,Configurable.Listener<LobbyItem>{

    private ConcurrentHashMap<String,Stub> stubIndex;
    private ConcurrentHashMap<Integer,GameZone> zoneIndex;
    private GameServiceProvider gameServiceProvider;
    private ApplicationContext context;
    private Descriptor application;

    private boolean started;

    private ConcurrentHashMap<Short,ServiceMessageListener> listeners = new ConcurrentHashMap<>();

    public GameLobbyProxy(){
        this.stubIndex = new ConcurrentHashMap<>();
        this.zoneIndex = new ConcurrentHashMap<>();
    }

    @Override
    public Stub join(Session session, Rating rating) {
        if(!started) return new Stub("lobby not started");
        StubKey stubKey = new StubKey(session.systemId(),application.tag(),session.stub());
        Stub stub = stubIndex.get(stubKey.asString());
        if(stub!=null&&stub.joined) {
            stub.ticket = this.context.validator().ticket(session.systemId(),session.stub());
            stub.inbox = this.gameServiceProvider.inboxServiceProvider().inbox(stub.systemId());
            PlayerSavedGames playerSavedGames = new PlayerSavedGames(session.systemId(),session.clientId(),this.gameServiceProvider.presenceServiceProvider().listSaves(session.systemId(),session.clientId(),session.name()));
            playerSavedGames.gameServiceProvider = gameServiceProvider;
            stub.playerSavedGames = playerSavedGames;
            return stub;
        }
        GameZone _zone = gameZone(rating);
        stub = _zone.join(session,rating);
        stubIndex.put(stub.key().asString(),stub);
        return stub;
    }

    @Override
    public void leave(Session session) {
        if(!started) return;
        StubKey stubKey = new StubKey(session.systemId(),application.tag(),session.stub());
        Stub stub = stubIndex.remove(stubKey.asString());
        if(stub==null) return;
        stub.zone.leave(stub);
        stub.pushChannel.close();
    }

    @Override
    public void update(Session session, byte[] payload){
        if(!started) return;
        StubKey stubKey = new StubKey(session.systemId(),application.tag(),session.stub());
        Stub stub = stubIndex.get(stubKey.asString());
        if(stub==null){
            session.write(JsonUtil.toSimpleResponse(false,"no access token").getBytes());
            return;
        }
        stub.zone.update(session,stub,payload);
    }

    @Override
    public void list(Session session){
        if(!started) return;
        StubKey stubKey = new StubKey(session.systemId(),application.tag(),session.stub());
        Stub stub = stubIndex.get(stubKey.asString());
        if(stub==null){
            session.write(JsonUtil.toSimpleResponse(false,"no access token").getBytes());
            return;
        }
        stub.zone.list(session,stub);
    }
    public void validate(Session session){
        StubKey stubKey = new StubKey(session.systemId(),application.tag(),session.stub());
        session.write(JsonUtil.toSimpleResponse(stubIndex.get(stubKey.asString())!=null,"").getBytes());
    }
    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        DeploymentServiceProvider deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        Configuration config = deploymentServiceProvider.configuration("service-listener-settings");
        JsonArray cmds = ((JsonElement)config.property("listeners")).getAsJsonArray();
        cmds.forEach((cmd->{
            JsonObject cc = cmd.getAsJsonObject();
            listeners.put(cc.get("command").getAsShort(),toServiceMessageListener(cc.get("className").getAsString()));
        }));
        this.application = applicationContext.descriptor();
        this.gameServiceProvider = this.context.serviceProvider(context.descriptor().typeId().replace("lobby","service"));
    }

    @Override
    public boolean timeout(String systemId,int stub) {
        StubKey stubKey = new StubKey(systemId,application.tag(),stub);
        Stub removed = stubIndex.remove(stubKey.asString());
        if(removed==null) return false;
        removed.zone.leave(removed);
        gameServiceProvider.onUpdated(GameClusterMetrics.GAME_TIMEOUT_COUNT,1);
        return  true;
    }


    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {
        //defaultLobby.shutdown();
    }
    private GameZone gameZone(Rating rating){
        if(rating.level>0 && rating.level<101) return zoneIndex.get(1);
        if(rating.level>100 && rating.level<201) return zoneIndex.get(2);
        if(rating.level>200 && rating.level<301) return zoneIndex.get(3);
        if(rating.level>300 && rating.level<401) return zoneIndex.get(4);
        if(rating.level>400 && rating.level<501) return zoneIndex.get(5);
        if(rating.level>500 && rating.level<601) return zoneIndex.get(6);
        if(rating.level>600 && rating.level<701) return zoneIndex.get(7);
        if(rating.level>700 && rating.level<801) return zoneIndex.get(8);
        if(rating.level>800 && rating.level<901) return zoneIndex.get(9);
        return zoneIndex.get(10);
    }

    @Override
    public void onLoaded(LobbyItem lobbyItem){
        this.context.log("configurable lobby item loaded->"+lobbyItem.configurationName(), OnLog.WARN);
        if(configure(lobbyItem)) this.started = true;
    }
    @Override
    public void onUpdated(LobbyItem lobbyItem){
        this.context.log("configurable lobby item updated->"+lobbyItem.configurationName(),OnLog.WARN);
        if(configure(lobbyItem)) this.started = true;
    }
    @Override
    public void onRemoved(LobbyItem lobbyItem){
        this.context.log("configurable lobby item removed->"+lobbyItem.configurationName(),OnLog.WARN);
        this.started = false;
        zoneIndex.clear();
    }
    private boolean configure(LobbyItem lobbyItem){
        zoneIndex.clear();
        //this.gameServiceProvider.roomServiceProvider().register(lobbyItem);
        lobbyItem.zoneList().forEach(zoneItem -> {
            ConfigurableZone configurableZone = new ConfigurableZone(zoneItem);
            configurableZone.configurationTypeId(lobbyItem.configurationName());
            configurableZone.configurationName(zoneItem.configurationName());
            configurableZone.dataStore(gameServiceProvider.serviceDataStore());
            configurableZone.setup(context,this);
            GameZone.RoomProxy roomProxy = roomProxy(zoneItem.playMode());
            roomProxy.setup(context,this,configurableZone);
            configurableZone.roomProxy(roomProxy);
            zoneIndex.put(zoneItem.rank(),configurableZone);
            this.gameServiceProvider.roomServiceProvider().register(configurableZone);
        });
        if(zoneIndex.isEmpty()) return false;
        fillLobby();
        return true;
    }
    private GameZone.RoomProxy roomProxy(String playMode){
        GameZone.RoomProxy roomProxy = new PVERoomProxy();
        if(playMode.equals(GameZone.PLAY_MODE_PVP)){
            roomProxy = new PVPRoomProxy();
        }
        else if(playMode.equals(GameZone.PLAY_MODE_TVE)){
            roomProxy = new TVERoomProxy();
        }
        else if(playMode.equals(GameZone.PLAY_MODE_TVT)){
            roomProxy = new TVTRoomProxy();
        }
        return roomProxy;
    }
    private void fillLobby(){
        for(int i=1;i<11;i++){
            if(!zoneIndex.containsKey(i)){
                GameZone pre = zoneIndex.get(i-1);
                if(pre!=null) {
                    zoneIndex.put(i,pre);
                }
                else{
                    for(int j=i+1;j<11;j++){
                        GameZone next = zoneIndex.get(j);
                        if(next!=null){
                            zoneIndex.put(i,next);
                            break;
                        }
                    }
                }
            }
        }
    }

    public ServiceMessageListener serviceMessageListener(short serviceId){
        ServiceMessageListener listener = listeners.get(serviceId);
        if(listener==null) return new ErrorCommand();
        return listener;
    }
    private ServiceMessageListener toServiceMessageListener(String className){
        try {
            ServiceMessageListener serviceMessageListener = (ServiceMessageListener) Class.forName(className).getConstructor().newInstance();
            serviceMessageListener.setup(this.context);
            return serviceMessageListener;
        }catch (Exception ex){
            return new ErrorCommand();
        }
    }
}
