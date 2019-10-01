package com.tarantula.demo.quest;

import com.google.gson.JsonObject;
import com.tarantula.platform.OnApplicationHeader;
import com.tarantula.platform.util.SystemUtil;

import java.util.Map;
/**
 * Created by yinghu 9/30/2019
 */
public class RobotQuest extends OnApplicationHeader {

    static String LABEL = "RQ";
    static long START_COUNT_DOWN = 5000;
    private String[] playerIndex;
    private boolean started;
    private long startCountdown;

    public RobotQuest(){
        this.label = LABEL;
        this.onEdge = true;
        this.playerIndex = new String[]{"-","-"};
    }
    public RobotQuest(int index){
        this();
        this.stub = index;
    }
    public synchronized RobotQuest onTimer(long delta){
        if(started&&startCountdown>0){
            startCountdown = startCountdown-delta;
            return _snapshot();
        }
        else{
            return null;
        }
    }
    //return 1 one player 2 two players 0 fully joined
    public synchronized int join(String systemId){
        if(started){
            return 0;
        }
        int ix = playerIndex[0].equals("--")?0:1;
        playerIndex[ix]=systemId;
        started = ix==1;
        if(started){//
            startCountdown = START_COUNT_DOWN;
        }
        return ix+1;
    }
    public synchronized void leave(String systemId){
        if(systemId.equals(playerIndex[0])){
            playerIndex[0]="-";
        }
        else if(systemId.equals(playerIndex[1])){
            playerIndex[1]="-";
        }
        started = !(this.playerIndex[0]+this.playerIndex[1]).equals("--");
    }
    public synchronized RobotQuest snapshot(){
        return _snapshot();
    }
    @Override
    public Map<String,Object> toMap(){
        properties.put("index",this.stub);
        properties.put("p0",this.playerIndex[0]);
        properties.put("p1",this.playerIndex[1]);
        properties.put("scc",this.startCountdown);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.stub = ((Number)properties.get("index")).intValue();
        this.playerIndex[0]=(String) properties.get("p0");
        this.playerIndex[1]=(String) properties.get("p1");
        this.startCountdown = ((Number)properties.get("scc")).longValue();
        this.started = !(this.playerIndex[0]+this.playerIndex[1]).equals("--");
    }

    @Override
    public int getFactoryId() {
        return RobotQuestPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return RobotQuestPortableRegistry.ROBOT_QUEST_OID;
    }
    public String toString(){
        return new String(SystemUtil.toJson(this.toMap()));
    }
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("gameId",this.distributionKey());
        jsonObject.addProperty("player1",playerIndex[0]);
        jsonObject.addProperty("player2",playerIndex[1]);
        jsonObject.addProperty("started",started);
        jsonObject.addProperty("startCountDown",startCountdown);
        return jsonObject;
    }
    private RobotQuest _snapshot(){
        RobotQuest robotQuest = new RobotQuest();
        robotQuest.distributionKey(this.distributionKey());
        robotQuest.stub = this.stub;
        robotQuest.started = this.started;
        robotQuest.startCountdown = this.startCountdown;
        robotQuest.playerIndex[0]=this.playerIndex[0];
        robotQuest.playerIndex[1]=this.playerIndex[1];
        return robotQuest;
    }
}
