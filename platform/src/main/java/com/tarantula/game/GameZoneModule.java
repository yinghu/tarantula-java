package com.tarantula.game;

import com.tarantula.ApplicationContext;
import com.tarantula.Module;
import com.tarantula.OnLog;
import com.tarantula.Session;
import com.tarantula.platform.util.SystemUtil;

import java.util.concurrent.ConcurrentHashMap;
/**
 * Created by yinghu lu on 4/14/2020.
 */
public class GameZoneModule implements Module {

    private ApplicationContext context;
    private ConcurrentHashMap<Integer,Arena> mArena = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,Stub> mStub = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,Room> mRoom = new ConcurrentHashMap<>();

    @Override
    public void onJoin(Session session,OnUpdate onUpdate) throws Exception{
        //match arena with rating rank/xp
        Rating rating = new Rating();
        rating.fromMap(SystemUtil.toMap(session.payload()));
        context.log("join on ["+this.context.descriptor().accessRank()+"/"+rating.level+"]",OnLog.WARN);
        Arena arena = mArena.get(rating.level);
        Stub stub = arena.room().join();
        stub.tag = this.context.descriptor().tag();
        stub.owner(session.systemId());
        GameObject gameObject = new GameObject();
        gameObject.successful(true);
        gameObject.stub = stub;
        mStub.put(session.systemId(),stub);
        session.write(gameObject.toJson().toString().getBytes(),label());
        onUpdate.on(stub.roomId,"{}".getBytes());
    }
    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        if(session.action().equals("onLeave")){
            Stub stub = mStub.remove(session.systemId());
            Room room = mRoom.get(stub.roomId);
            room.leave(stub);
            session.instanceId(stub.roomId);
            session.write(payload,label());
            return true;
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        Arena arena = new Arena();
        arena.roomIndex = this.mRoom;
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
