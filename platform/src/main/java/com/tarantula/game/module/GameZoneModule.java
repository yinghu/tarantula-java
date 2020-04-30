package com.tarantula.game.module;

import com.google.gson.GsonBuilder;
import com.tarantula.ApplicationContext;
import com.tarantula.Module;
import com.tarantula.OnLog;
import com.tarantula.Session;
import com.tarantula.game.*;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.util.ResponseSerializer;

import java.util.concurrent.ConcurrentHashMap;
/**
 * Created by yinghu lu on 4/14/2020.
 */
public class GameZoneModule implements Module{

    private ApplicationContext context;
    private Zone mZone;
    private ConcurrentHashMap<String, Stub> mStub = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Room> mRoom = new ConcurrentHashMap<>();
    private GsonBuilder builder;
    private GameServiceProvider gameServiceProvider;
    @Override
    public void onJoin(Session session,OnUpdate onUpdate) throws Exception{
        //match arena with service rank/xp
        Stub stub = mZone.room().join();
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
        String gz = this.context.descriptor().typeId().replace("-lobby","-service");
        this.gameServiceProvider = this.context.serviceProvider(gz);
        mZone = this.gameServiceProvider.zone(this.context.descriptor().distributionKey());
        if(mZone.arenas.length==0) {
            mZone.arenas = new Arena[]{
                    new Arena(1, 100, "Amber 1"),
                    new Arena(2, 200, "Amber 2"),
                    new Arena(3, 300, "Amber 3"),
                    new Arena(4, 400, "Amber 4"),
                    new Arena(5, 500, "Amber 5")};
            mZone.update();
        }
        for(Arena a : mZone.arenas){
            context.log(a.toString(),OnLog.WARN);
        }
        mZone.roomIndex = this.mRoom;
        mZone.stubIndex = this.mStub;
        mZone.deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        mZone.descriptor = this.context.descriptor();
        mZone.start();
        context.log(this.mZone.descriptor.tag()+"/"+this.mZone.descriptor.accessRank()+"/"+this.mZone.descriptor.distributionKey(),OnLog.WARN);
    }
    @Override
    public void onTimer(OnUpdate update){
        mZone.onTimer(update);
    }
    @Override
    public String label() {
        return "game";
    }
}
