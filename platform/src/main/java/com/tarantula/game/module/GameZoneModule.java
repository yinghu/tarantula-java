package com.tarantula.game.module;

import com.google.gson.GsonBuilder;
import com.tarantula.ApplicationContext;
import com.tarantula.Module;
import com.tarantula.OnLog;
import com.tarantula.Session;
import com.tarantula.game.Arena;
import com.tarantula.game.GameObject;
import com.tarantula.game.Room;
import com.tarantula.game.Stub;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.game.service.Rating;
import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.util.ResponseSerializer;
import com.tarantula.platform.util.SystemUtil;

import java.util.concurrent.ConcurrentHashMap;
/**
 * Created by yinghu lu on 4/14/2020.
 */
public class GameZoneModule implements Module{

    private ApplicationContext context;
    private ConcurrentHashMap<Integer, Arena> mArena = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Stub> mStub = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Room> mRoom = new ConcurrentHashMap<>();
    private GsonBuilder builder;
    @Override
    public void onJoin(Session session,OnUpdate onUpdate) throws Exception{
        //match arena with service rank/xp
        Rating rating = new Rating();
        rating.fromMap(SystemUtil.toMap(session.payload()));
        Arena arena = mArena.get(rating.level);
        Stub stub = arena.room().join();
        stub.tag = this.context.descriptor().tag();
        stub.owner(session.systemId());
        GameObject gameObject = new GameObject();
        gameObject.successful(true);
        gameObject.stub = stub;
        mStub.put(session.systemId(),stub);
        session.write(gameObject.toJson().toString().getBytes(),label());
        //onUpdate.on(stub.roomId,"{}".getBytes());
    }
    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        if(session.action().equals("onMessage")){
            Stub stub = mStub.get(session.systemId());
            Room room = mRoom.get(stub.roomId);
            session.write("{}".getBytes(),label());
            update.on(room.oid()+"?onMessage",payload);
        }
        else if(session.action().equals("onLeave")){
            Stub stub = mStub.get(session.systemId());
            Room room = mRoom.get(stub.roomId);
            boolean left = room.leave(stub);
            session.instanceId(stub.roomId);
            ResponseHeader resp = new ResponseHeader("onLeave");
            resp.successful(left);
            session.write(builder.create().toJson(resp).getBytes(),label());
            return left;
        }
        return session.action().equals("onLeave");
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
        Arena arena = new Arena();
        arena.roomIndex = this.mRoom;
        arena.stubIndex = this.mStub;
        arena.deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        arena.descriptor = this.context.descriptor();
        arena.start();
        mArena.put(arena.level,arena);
        context.log(this.context.descriptor().tag()+"/"+this.context.descriptor().accessRank(),OnLog.WARN);
    }
    @Override
    public void onTimer(OnUpdate update){
        mArena.forEach((k,v)->{
            v.onTimer(update);
        });
    }
    @Override
    public String label() {
        return "game";
    }
}
