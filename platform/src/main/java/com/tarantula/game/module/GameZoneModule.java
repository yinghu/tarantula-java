package com.tarantula.game.module;

import com.google.gson.GsonBuilder;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.game.*;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.util.ResponseSerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
/**
 * Created by yinghu lu on 4/14/2020.
 */
public class GameZoneModule implements Module,ZoneListener{

    private ApplicationContext context;
    private Zone mZone;
    private ConcurrentHashMap<String, Stub> mStub = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Room> mRoom = new ConcurrentHashMap<>();
    private GsonBuilder builder;
    private GameServiceProvider gameServiceProvider;
    private Connection connection;
    @Override
    public void onJoin(Session session,OnUpdate onUpdate) throws Exception{
        //match arena with service rank/xp
        Stub stub = mZone.room().join();
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
        if(session.action().equals("onMessage")){
            Stub stub = mStub.get(session.systemId());
            Room room = mRoom.get(stub.roomId);
            session.write("{}".getBytes(),label());
            update.on(connection.serverId(),room.roomId+"?onMessage",payload);
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
        else{
            throw new UnsupportedOperationException(session.action());
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
            //create arenas using capacity of descriptor
            mZone.capacity=1;
            mZone.roundDuration = 60*1000;
            mZone.overtime = 5000;
            mZone.playMode = Room.OFF_LINE_MODE;
            int sz = this.context.descriptor().capacity();
            mZone.arenas = new Arena[sz];
            for(int i=1;i<sz+1;i++){
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
        Arrays.sort(mZone.arenas,new ArenaComparator());
        mZone.roomIndex = this.mRoom;
        mZone.stubIndex = this.mStub;
        mZone.deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        mZone.gameServiceProvider = this.gameServiceProvider;
        mZone.descriptor = this.context.descriptor();
        mZone.start();
        this.gameServiceProvider.addZoneListener(this);
        context.log(this.mZone.descriptor.tag()+"/"+this.mZone.descriptor.accessRank()+"/"+this.mZone.descriptor.distributionKey(),OnLog.WARN);
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

    //@Override
    public void reset() {
        this.context.log("reset something",OnLog.WARN);
    }

    @Override
    public void updated(Zone zone) {
        for(Arena a : zone.arenas){
            this.context.log(a.toString(),OnLog.WARN);
        }
    }
}
