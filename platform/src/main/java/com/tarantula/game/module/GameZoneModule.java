package com.tarantula.game.module;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.tarantula.game.*;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.game.Rating;
import com.tarantula.platform.statistics.StatsDelta;
import com.tarantula.platform.util.OnAccessDeserializer;

import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
/**
 * updated by yinghu lu on 6/9/2020.
 */
public class GameZoneModule implements Module,Configurable.Listener,Connection.OnConnectionListener{

    private ApplicationContext context;
    private Zone mZone;
    private ConcurrentHashMap<String, Stub> mStub = new ConcurrentHashMap<>();
    private GsonBuilder builder;
    private GameServiceProvider gameServiceProvider;

    private int DEFAULT_LEVEL_COUNT = 3;
    private int DEFAULT_LEVEL_UP_BASE = 1000;
    private DeploymentServiceProvider deploymentServiceProvider;
    @Override
    public void onJoin(Session session, Module.OnUpdate onUpdate) throws Exception{
        //match arena with service rank/xp or offline play mode
        //this.context.log(new String(session.payload()),OnLog.WARN);
        if(mZone.descriptor.tournamentEnabled()&&(!gameServiceProvider.available(session.instanceId()))){
            session.write(toMessage("no tournament available,please try later",false).toString().getBytes(),label());
            return;
        }
        Rating rating = new Rating();
        rating.fromBinary(session.payload());
        Stub stub = mStub.get(session.systemId());
        Room room =null;
        if(stub!=null){
            room = gameServiceProvider.getRoom(stub.roomId);
            if(!room.rejoin(stub)){
                mStub.remove(session.systemId());
                stub = null;
            }
        }
        if(stub==null){
            room = mZone.playMode==Room.OFF_LINE_MODE?mZone.solo(rating):mZone.match(rating);
            stub = room.join(rating);
            if(stub==null){
                session.write(toMessage("no room available,please try later",false).toString().getBytes(),label());
                return;
            }
        }
        stub.tag = this.context.descriptor().tag();
        stub.owner(session.systemId());
        GameJoinObject gameObject = new GameJoinObject();
        gameObject.successful(true);
        gameObject.offline = mZone.playMode==Room.OFF_LINE_MODE;
        if(!gameObject.offline){
            gameObject.connection = room.connection();
            Connection connection = room.connection();
            gameObject.ticket = this.context.validator().ticket(session.systemId(),session.stub());
            byte[] key = this.deploymentServiceProvider.serverKey(connection);
            gameObject.serverKey = Base64.getEncoder().encodeToString(key);
        }
        gameObject.stub = stub;
        if(mZone.descriptor.tournamentEnabled()){
            Tournament.Entry  e = gameServiceProvider.join(session.instanceId(),session.systemId());
            stub.entry = e;
        }
        mStub.put(session.systemId(),stub);
        session.write(gameObject.toJson().toString().getBytes(),label());
    }
    @Override
    public boolean onRequest(Session session, byte[] payload, Module.OnUpdate update) throws Exception {
        if(session.action().equals("onUpdated")){
            Stub stub = mStub.get(session.systemId());
            if(stub!=null){
                session.write(toMessage(session.action(),true).toString().getBytes(),label());
                StatsDelta delta = toDelta(payload);
                mZone.onStatistics(session.systemId(),delta.name,delta.value);
            }
            else{
                session.write(toMessage("no room joined",false).toString().getBytes(),label());
            }
        }
        else if(session.action().equals("onScore")){
            Stub stub = mStub.get(session.systemId());
            if(stub!=null){
                Tournament.Entry _e = gameServiceProvider.score(stub.entry.owner(),session.systemId(),100);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("systemId",session.systemId());
                jsonObject.addProperty("score",_e.score(0));
                jsonObject.addProperty("rank",_e.rank());
                jsonObject.addProperty("timestamp",_e.timestamp());
                session.write(jsonObject.toString().getBytes(),label());
            }
            else{
                session.write(toMessage("no room joined",false).toString().getBytes(),label());
            }
        }
        else if(session.action().equals("onEnded")){
            Stub stub = mStub.get(session.systemId());
            if(stub!=null){
                //this.context.log(new String(payload),OnLog.WARN);
                session.write(toMessage(session.action(),true).toString().getBytes(),label());
            }
            else{
                session.write(toMessage("no room joined",false).toString().getBytes(),label());
            }
        }
        else if(session.action().equals("onLeave")){
            Stub stub = mStub.get(session.systemId());
            if(stub!=null){
                Room room = gameServiceProvider.getRoom(stub.roomId);
                boolean left = room.leave(stub);
                session.instanceId(stub.roomId);
                session.write(toMessage("onLeave",left).toString().getBytes(),label());
                return left;
            }
            else{
                session.write(toMessage("no room joined",false).toString().getBytes(),label());
                return true;
            }
        }
        else if(session.action().equals("onPlay")){
            //by passing match-making routing
            onJoin(session,update);
        }
        else{
            throw new UnsupportedOperationException(session.action());
        }
        return session.action().equals("onLeave");
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
        this.deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        String gz = this.context.descriptor().typeId().replace("-lobby","-service");
        this.gameServiceProvider = this.context.serviceProvider(gz);
        mZone = this.gameServiceProvider.zone(this.context.descriptor());
        mZone.levelUpBase = DEFAULT_LEVEL_UP_BASE;
        if(mZone.arenas.size()==0) {
            //create arenas using capacity of descriptor
            mZone.capacity=1;
            mZone.roundDuration = 60*1000;
            mZone.overtime = 5000;
            mZone.playMode = Room.OFF_LINE_MODE;
            mZone.levelLimit = this.context.descriptor().capacity();
            for(int i=1;i<DEFAULT_LEVEL_COUNT+1;i++){
                Arena arena = new Arena(mZone.bucket(),mZone.oid(),i);
                arena.name("level"+i);
                arena.level = i;
                arena.xp = i*DEFAULT_LEVEL_UP_BASE;
                arena.disabled(false);
                mZone.arenas.add(arena);
            }
            mZone.update();
        }
        mZone.stubIndex = this.mStub;
        mZone.deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        mZone.gameServiceProvider = this.gameServiceProvider;
        mZone.descriptor = this.context.descriptor();
        mZone.start();
        //mZone.aMap.forEach((k,v)-> context.log("Add level ->"+k+" ->/level:"+v.level+"/name:"+v.name()+"/xp:"+v.xp,OnLog.WARN));
        mZone.registerListener(this);
        deploymentServiceProvider.register(mZone);
        this.deploymentServiceProvider.registerOnConnectionListener(this);
        context.log("Game lobby started with tournament enabled ["+context.descriptor().tournamentEnabled()+"] on tag=>"+this.mZone.descriptor.tag(),OnLog.WARN);
    }
    @Override
    public void onConnection(Connection connection){
        if(connection.disabled()){
            this.gameServiceProvider.onClosed(connection);
        }
        else {
            //mPush.put(connection.serverId(),connection);
        }
    }
    @Override
    public void onTimer(com.icodesoftware.Module.OnUpdate update){
        mZone.onTimer((connection,label,data)->update.on(connection,label,data));
    }
    @Override
    public String label() {
        return this.context.descriptor().typeId();
    }


    @Override
    public void clear() {
        this.deploymentServiceProvider.release(mZone);
        this.context.log("clear->"+this.context.descriptor().name(),OnLog.WARN);
    }
    @Override
    public void onBucket(int bucket,int state){
        this.context.log("Bucket->"+bucket+"/"+state,OnLog.WARN);
    }
    public void onUpdated(Configurable zone) {
        mZone.reset((Zone)zone);
        //this.context.log("Play mode->"+mZone.playMode,OnLog.WARN);
        //this.context.log("joinsOnStart->"+mZone.joinsOnStart,OnLog.WARN);
        //mZone.aMap.forEach((k,v)-> context.log("Add level ->"+k+" ->/level:"+v.level+"/name:"+v.name()+"/xp:"+v.xp,OnLog.WARN));
    }
    private JsonObject toMessage(String msg,boolean successful){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",successful);
        jsonObject.addProperty("message",msg);
        return jsonObject;
    }
    private StatsDelta toDelta(byte[] data){
        JsonParser parser = new JsonParser();
        JsonObject jo = parser.parse(new String(data)).getAsJsonObject();
        return new StatsDelta(jo.get("Name").getAsString(),jo.get("Delta").getAsDouble());
    }

    @Override
    public String lobbyTag() {
        return this.context.descriptor().tag();
    }

    @Override
    public void onConnection(Session session) {
        try{
            this.onJoin(session,(c,l,d)->{});
        }catch (Exception ex){
            session.write(toMessage(ex.getMessage(),false).toString().getBytes(),label());
        }
    }
}
