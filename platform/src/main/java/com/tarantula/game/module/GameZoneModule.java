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

import java.util.ArrayList;
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
        //match arena with service rank/xp
        Rating rating = this.gameServiceProvider.rating(session.systemId());
        Stub stub = mZone.room(rating).join(rating);
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
        if(session.action().equals("onCommit")){
            Stub stub = mStub.get(session.systemId());
            Room room = mRoom.get(stub.roomId);
            if(room.offline()){
                OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
                session.write(toMessage(session.action(),true).toString().getBytes(),label());
                update.on(connection.serverId(),room.roomId+"?onCommit",payload);
            }
            else{
                session.write(toMessage("only offline mode can commit by player",false).toString().getBytes(),label());
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
        mZone = this.gameServiceProvider.zone(this.context.descriptor().distributionKey());
        if(mZone.arenas.length==0) {
            //create arenas using capacity of descriptor
            mZone.capacity=1;
            mZone.roundDuration = 60*1000;
            mZone.overtime = 5000;
            mZone.playMode = Room.OFF_LINE_MODE;
            mZone.arenas = new Arena[DEFAULT_LEVEL_COUNT];
            for(int i=1;i<DEFAULT_LEVEL_COUNT+1;i++){
                mZone.arenas[i-1]=new Arena(i,i*100,"Level "+i,false);
            }
            mZone.update();
        }
        ArrayList<Arena> alist = new ArrayList<>();
        for(Arena a : mZone.arenas){
            if(!a.disabled()){
                alist.add(a);
            }
        }
        mZone.arenas = new Arena[alist.size()];
        for(int i=0;i<alist.size();i++){
            mZone.arenas[i]=alist.get(i);
        }
        mZone.roomIndex = this.mRoom;
        mZone.stubIndex = this.mStub;
        mZone.deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        mZone.gameServiceProvider = this.gameServiceProvider;
        mZone.descriptor = this.context.descriptor();
        mZone.start();
        //mZone.aMap.forEach((k,v)-> context.log("Add level ->"+k+" ->level ["+v.level+"/"+v.name()+"]",OnLog.WARN));
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
        mZone.reset(zone);
        //mZone.aMap.forEach((k,v)-> context.log("Add level ->"+k+" ->level ["+v.level+"/"+v.name()+"]",OnLog.WARN));
    }
    private JsonObject toMessage(String msg,boolean successful){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",successful);
        jsonObject.addProperty("message",msg);
        return jsonObject;
    }
}
