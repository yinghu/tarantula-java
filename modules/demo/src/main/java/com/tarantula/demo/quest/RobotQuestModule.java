package com.tarantula.demo.quest;

import com.google.gson.GsonBuilder;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.platform.util.OnAccessDeserializer;

import java.util.List;
/**
 * Created by yinghu 9/30/2019
 */
public class RobotQuestModule implements Module {

    static String ROBOT_QUEST_DATA_STORE = "rbs";
    static String ROBOT_QUEST_RESPONSE_LABEL = "robotQuest";
    static long TIMER_DELTA = 500;

    private ApplicationContext context;
    private GsonBuilder builder;
    private RobotQuest[] gameList;
    private DataStore dataStore;
    private long delta = TIMER_DELTA;
    @Override
    public void onJoin(Session session, Connection connection,OnUpdate onUpdate) throws Exception{
        RobotQuest rq =null;
        for(int i=0; i<10;i++){
            if(gameList[i].join(session.systemId())>0){
                rq = gameList[i].snapshot();
                OnInstance ox = this.context.onRegistry().onInstance(session.systemId());
                ox.accessMode(i);
                break;
            }
        }
        onUpdate.on(rq.distributionKey(),this.builder.create().toJson(rq).getBytes());
        GameObject ret =  onJoin(connection,this.context.validator().ticket(session.systemId(),session.stub()),rq);
        session.write(this.builder.create().toJson(ret).getBytes(),label());
    }

    @Override
    public boolean onRequest(Session session, byte[] bytes, OnUpdate onUpdate) throws Exception {
        /** PARSE THE CLIENT JSON PAYLOAD
        OnAccess access = this.builder.create().fromJson(new String(bytes),OnAccess.class);
        **/
        //ADD MORE IF BLOCK TO PROCESS THE CLIENT REQUEST
        if(session.action().equals("onLeave")){
            OnInstance ox = this.context.onRegistry().onInstance(session.systemId());
            RobotQuest ret = gameList[ox.accessMode()].leave(session.systemId());
            onUpdate.on(gameList[ox.accessMode()].distributionKey(),this.builder.create().toJson(ret).getBytes());
            session.write(bytes,this.label());
        }
        else{
            throw new UnsupportedOperationException("Action["+session.action()+"] not supported");
        }
        return session.action().equals("onLeave");
    }
    @Override
    public void onTimeout(Session session){
        OnInstance ox = this.context.onRegistry().onInstance(session.systemId());
        gameList[ox.accessMode()].leave(session.systemId());
    }
    @Override
    public void onTimer(OnUpdate update){
        delta -= this.context.descriptor().timerOnModule();
        if(delta<=0){
            for(RobotQuest rq : gameList){
                RobotQuest up = rq.onTimer(TIMER_DELTA);
                if(up!=null){
                    update.on(rq.distributionKey(),this.builder.create().toJson(up).getBytes());
                }
            }
            delta = TIMER_DELTA;
        }
    }
    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
        this.builder.registerTypeAdapter(GameObject.class,new GameObjectSerializer());
        this.builder.registerTypeAdapter(RobotQuest.class,new RobotQuestSerializer());
        this.dataStore = this.context.dataStore(ROBOT_QUEST_DATA_STORE);
        this.dataStore.registerRecoverableListener(new RobotQuestPortableRegistry()).addRecoverableFilter(RobotQuestPortableRegistry.ROBOT_QUEST_OID,(t)->{
            this.context.log(t.toString(),OnLog.INFO);
        });
        this.gameList = new RobotQuest[]{new RobotQuest(0),new RobotQuest(1),new RobotQuest(2),new RobotQuest(3),new RobotQuest(4),
                new RobotQuest(5),new RobotQuest(6),new RobotQuest(7),new RobotQuest(8),new RobotQuest(9)};
        RobotQuestQuery query = new RobotQuestQuery(this.context.onRegistry().distributionKey());
        List<RobotQuest> rlist =  this.dataStore.list(query);
        if(rlist.isEmpty()){
            for(RobotQuest r : this.gameList){
                r.owner(query.distributionKey());
                if(this.dataStore.create(r)){
                    rlist.add(r);
                }
                else{
                    throw new RuntimeException("failed to create persistent robot quest object");
                }
            }
        }
        rlist.forEach((r)->{
            r.dataStore(this.dataStore);
            this.gameList[r.stub()]=r;
        });
        this.context.log("Robot Quest Game Instance started", OnLog.INFO);
    }

    @Override
    public String label() {
        return ROBOT_QUEST_RESPONSE_LABEL;
    }

    @Override
    public void clear(){
        for(RobotQuest r: gameList){
            this.dataStore.update(r);
        }
    }
    private GameObject onJoin(Connection connection,String ticket,RobotQuest robotQuest){
        GameObject gameObject = new GameObject(connection,ticket,robotQuest);
        gameObject.instanceId(this.context.onRegistry().distributionKey());
        return gameObject;
    }
}
