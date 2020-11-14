package com.tarantula.game.module;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.protocol.DataBuffer;
import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.tarantula.game.*;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.game.Rating;
import com.tarantula.platform.util.OnAccessDeserializer;

import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
/**
 * updated by yinghu lu on 6/9/2020.
 */
public class GameZoneModule implements Module,Configurable.Listener,Connection.InboundMessageListener{

    private ApplicationContext context;
    private Zone mZone;
    private ConcurrentHashMap<String, Stub> mStub = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Room> mRoom = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,Connection> mPush = new ConcurrentHashMap<>();
    private GsonBuilder builder;
    private GameServiceProvider gameServiceProvider;

    private int DEFAULT_LEVEL_COUNT = 3;
    private int DEFAULT_LEVEL_UP_BASE = 1000;
    private DeploymentServiceProvider deploymentServiceProvider;

    @Override
    public void onJoin(Session session, com.icodesoftware.Module.OnUpdate onUpdate) throws Exception{
        //match arena with service rank/xp or offline play mode
        Rating rating = this.gameServiceProvider.rating(session.systemId());
        Room room = session.accessMode()==Session.OFF_LINE_MODE?mZone.solo(rating):mZone.match(rating);
        Stub stub = room.join(rating);
        stub.tag = this.context.descriptor().tag();
        stub.owner(session.systemId());
        GameObject gameObject = new GameObject();
        gameObject.successful(false);
        Connection con = room.connection();
        if(con!=null){
            gameObject.connection = con;
            gameObject.successful(true);
        }
        if(gameObject.successful()){
            Connection connection = mPush.get(con.serverId());
            gameObject.ticket = this.context.validator().ticket(session.systemId(),session.stub());
            byte[] key = this.deploymentServiceProvider.serverKey(connection);
            gameObject.serverKey = Base64.getEncoder().encodeToString(key);
            gameObject.stub = stub;
            mStub.put(session.systemId(),stub);
            DataBuffer push = new DataBuffer();
            push.putLong(con.connectionId());
            push.putUTF8(session.systemId());
            onUpdate.on(connection, MessageHandler.JOIN+"/true",push.toArray());
        }
        session.write(gameObject.toJson().toString().getBytes(),label());
    }
    @Override
    public boolean onRequest(Session session, byte[] payload, com.icodesoftware.Module.OnUpdate update) throws Exception {
        if(session.action().equals("onUpdated")){
            Stub stub = mStub.get(session.systemId());
            Room room = mRoom.get(stub.roomId);
            if(room.offline()){
                //this.context.log(new String(payload),OnLog.WARN);
                room.onUpdated(10,payload);
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
                //room.onEnded(payload);
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
        this.deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        String gz = this.context.descriptor().typeId().replace("-lobby","-service");
        this.gameServiceProvider = this.context.serviceProvider(gz);
        mZone = this.gameServiceProvider.zone(this.context.descriptor());
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
        mZone.roomIndex = this.mRoom;
        mZone.stubIndex = this.mStub;
        mZone.deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        mZone.gameServiceProvider = this.gameServiceProvider;
        mZone.descriptor = this.context.descriptor();
        mZone.start();
        mZone.aMap.forEach((k,v)-> context.log("Add level ->"+k+" ->/level:"+v.level+"/name:"+v.name()+"/xp:"+v.xp,OnLog.WARN));
        mZone.registerListener(this);
        deploymentServiceProvider.register(mZone);
        context.log("Game lobby started->"+this.mZone.descriptor.tag(),OnLog.WARN);
    }
    @Override
    public void onConnection(Connection connection){
        if(connection.disabled()){
            mPush.remove(connection.serverId());
        }
        else {
            mPush.put(connection.serverId(),connection);
        }
    }
    @Override
    public void onTimer(com.icodesoftware.Module.OnUpdate update){
        mZone.onTimer((connection,label,data)->{
            if(mPush.containsKey(connection.serverId())){
                update.on(connection,label,data);
            }
        });
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

    public void onUpdated(Configurable zone) {
        mZone.reset((Zone)zone);
        this.context.log("Play mode->"+mZone.playMode,OnLog.WARN);
        mZone.aMap.forEach((k,v)-> context.log("Add level ->"+k+" ->/level:"+v.level+"/name:"+v.name()+"/xp:"+v.xp,OnLog.WARN));
    }
    private JsonObject toMessage(String msg,boolean successful){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",successful);
        jsonObject.addProperty("message",msg);
        return jsonObject;
    }

    @Override
    public void onUpdated(int code,byte[] updated) {
        this.context.log(new String(updated).trim(),OnLog.WARN);
    }
}
