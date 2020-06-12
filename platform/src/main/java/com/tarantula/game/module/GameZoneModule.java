package com.tarantula.game.module;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.game.*;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.game.Rating;
import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.util.OnAccessDeserializer;

import java.util.concurrent.ConcurrentHashMap;
/**
 * updated by yinghu lu on 6/9/2020.
 */
public class GameZoneModule implements Module,ZoneListener{

    private ApplicationContext context;
    private Zone mZone;
    private ConcurrentHashMap<String, Stub> mStub = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Room> mRoom = new ConcurrentHashMap<>();
    private GsonBuilder builder;
    private GameServiceProvider gameServiceProvider;
    private Connection connection;
    private int DEFAULT_LEVEL_COUNT = 3;
    @Override
    public void onJoin(Session session,OnUpdate onUpdate) throws Exception{
        //match arena with service rank/xp or offline play mode
        Rating rating = this.gameServiceProvider.rating(session.systemId());
        Room room = session.accessMode()==Session.OFF_LINE_MODE?mZone.solo(rating):mZone.match(rating);
        Stub stub = room.join(rating);
        stub.tag = this.context.descriptor().tag();
        stub.owner(session.systemId());
        GameObject gameObject = new GameObject();
        gameObject.successful(true);
        gameObject.ticket = this.context.validator().ticket(session.systemId(),session.stub());
        gameObject.stub = stub;
        gameObject.connection = connection;
        mStub.put(session.systemId(),stub);
        session.write(gameObject.toJson().toString().getBytes(),label());
        //onUpdate.on(stub.roomId,"{}".getBytes());
    }
    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        if(session.action().equals("onUpdated")){
            Stub stub = mStub.get(session.systemId());
            Room room = mRoom.get(stub.roomId);
            if(room.offline()){
                //this.context.log(new String(payload),OnLog.WARN);
                room.onUpdated(payload);
                session.write(toMessage(session.action(),true).toString().getBytes(),label());
            }
            else{
                session.write(toMessage("only offline mode can commit onUpdated by player",false).toString().getBytes(),label());
            }
        }
        else if(session.action().equals("onEnded")){
            Stub stub = mStub.get(session.systemId());
            Room room = mRoom.get(stub.roomId);
            if(room.offline()){
                //this.context.log(new String(payload),OnLog.WARN);
                room.onEnded(payload);
                session.write(toMessage(session.action(),true).toString().getBytes(),label());
            }
            else{
                session.write(toMessage("only offline mode can commit onEnded by player",false).toString().getBytes(),label());
            }
        }
        else if(session.action().equals("onLeave")){
            Stub stub = mStub.get(session.systemId());
            Room room = mRoom.get(stub.roomId);
            boolean left = room.leave(stub);
            session.instanceId(stub.roomId);
            session.write(toMessage("onLeave",left).toString().getBytes(),label());
            return left;
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
        String gz = this.context.descriptor().typeId().replace("-lobby","-service");
        this.gameServiceProvider = this.context.serviceProvider(gz);
        mZone = this.gameServiceProvider.zone(this.context.descriptor());
        if(mZone.arenas.size()==0) {
            //create arenas using capacity of descriptor
            mZone.capacity=1;
            mZone.roundDuration = 60*1000;
            mZone.overtime = 5000;
            mZone.playMode = Room.OFF_LINE_MODE;
            //mZone.arenas = new Arena[DEFAULT_LEVEL_COUNT];
            for(int i=1;i<DEFAULT_LEVEL_COUNT+1;i++){
                Arena arena = new Arena(mZone.bucket(),mZone.oid(),i);
                arena.name("level"+i);
                arena.level = i;
                arena.xp = i*100;
                arena.disabled(false);
                mZone.arenas.add(arena);
            }
            mZone.update();
        }
        mZone.roomIndex = this.mRoom;
        mZone.stubIndex = this.mStub;
        mZone.deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        mZone.gameServiceProvider = this.gameServiceProvider;
        mZone.descriptor = this.context.descriptor();
        mZone.start();
        mZone.aMap.forEach((k,v)-> context.log("Add level ->"+k+" ->level ["+v.level+"/"+v.name()+"]",OnLog.WARN));
        this.gameServiceProvider.addZoneListener(this.context.descriptor().distributionKey(),this);
        context.log("Game lobby started->"+this.mZone.descriptor.tag(),OnLog.WARN);
    }
    public void onConnection(Connection connection){
        if(this.connection==null){
            this.connection = connection.copy();
            return;
        }
        this.connection.reset(connection);
    }
    @Override
    public void onTimer(OnUpdate update){
        mZone.onTimer((c,u,d)->{
            if(connection!=null&&!connection.disabled()){
                update.on(connection.serverId(),u,d);
            }
        });
    }
    @Override
    public String label() {
        return "game";
    }


    @Override
    public void clear() {
        this.gameServiceProvider.removeZoneListener(this.context.descriptor().distributionKey());
        this.context.log("clear->"+this.context.descriptor().name(),OnLog.WARN);
    }

    @Override
    public void updated(Zone zone) {
        mZone.reset(this.gameServiceProvider.zone(this.context.descriptor()));
        mZone.aMap.forEach((k,v)-> context.log("Add level ->"+k+" ->level ["+v.level+"/"+v.name()+"]",OnLog.WARN));
    }
    private JsonObject toMessage(String msg,boolean successful){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",successful);
        jsonObject.addProperty("message",msg);
        return jsonObject;
    }
}
