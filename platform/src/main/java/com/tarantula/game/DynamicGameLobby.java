package com.tarantula.game;

import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.service.metrics.GameClusterMetrics;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class DynamicGameLobby extends IndexSet implements GameLobby {

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
        GameZone _zone = zoneIndex.get(rating.level);
        Stub _stub = _zone.join(session,rating);
        stubIndex.put(_stub.key().asString(),_stub);
        return _stub;
    }

    public void leave(Session session){
        StubKey stubKey = new StubKey(session.systemId(),application.tag(),session.stub());
        Stub stub = stubIndex.get(stubKey.asString());
        if(stub==null) return;
        stub.zone.leave(stub);
    }
    public void update(Session session, byte[] payload){
        StubKey stubKey = new StubKey(session.systemId(),application.tag(),session.stub());
        Stub stub = stubIndex.get(stubKey.asString());
        if(stub==null){
            session.write(JsonUtil.toSimpleResponse(false,"no access token").getBytes());
            return;
        }
        stub.zone.update(session,stub,payload);
    }
    public void list(Session session){
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
        session.write(JsonUtil.toSimpleResponse(stubIndex.get(stubKey)!=null,"").getBytes());
    }

    public boolean timeout(String systemId,int stub){
        StubKey stubKey = new StubKey(systemId,application.tag(),stub);
        Stub removed = stubIndex.remove(stubKey.asString());
        removed.zone.leave(removed);
        gameServiceProvider.onUpdated(GameClusterMetrics.GAME_TIMEOUT_COUNT,1);
        return  removed!=null;
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
        this.context.log("default game lobby start on level match from ["+levelStart+" to "+levelEnd+"]",OnLog.WARN);
        for(GameZone gameZone : zoneList){
            if(gameZone.disabled()) continue;
            //gameZone.registerListener(this);
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
    }

    @Override
    public void shutdown() throws Exception{
        for(GameZone gameZone : zoneList){
            gameZone.close();
            this.deploymentServiceProvider.release(gameZone);
            this.gameServiceProvider.roomServiceProvider().release(gameZone);
        }
    }


}
