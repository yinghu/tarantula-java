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
    private long delta =1000;
    @Override
    public void onJoin(Session session,OnUpdate onUpdate) throws Exception{
        //match arena with rating rank/xp
        Rating rating = new Rating();
        rating.fromMap(SystemUtil.toMap(session.payload()));
        context.log("join on ["+this.context.descriptor().accessRank()+"/"+rating.level+"]",OnLog.WARN);
        Arena arena = mArena.get(rating.level);
        Stub stub = arena.join(session.systemId());
        stub.tag = this.context.descriptor().tag();
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
            session.instanceId(stub.roomId);
            session.write(payload,label());
            return true;
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        //delta = this.context.descriptor().timerOnModule();
        mArena.put(1,new Arena());
        context.log(this.context.descriptor().tag()+"/"+this.context.descriptor().accessRank(),OnLog.WARN);
    }
    @Override
    public void onTimer(OnUpdate update){
        delta = delta-this.context.descriptor().timerOnModule();
        if(delta<=0){
            mArena.forEach((k,v)->{
                v.onTimer(update);
            });
            delta = 1000;
        }
    }
    @Override
    public String label() {
        return "game";
    }
}
