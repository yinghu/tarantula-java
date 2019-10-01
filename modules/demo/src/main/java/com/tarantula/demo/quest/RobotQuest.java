package com.tarantula.demo.quest;

import com.google.gson.JsonObject;
import com.tarantula.DataStore;
import com.tarantula.Updatable;
import com.tarantula.platform.OnApplicationHeader;
import com.tarantula.platform.util.SystemUtil;

import java.util.Map;
/**
 * Created by yinghu 9/30/2019
 */
public class RobotQuest extends OnApplicationHeader {

    static String LABEL = "RQ";
    static long START_COUNT_DOWN = 5000; // 5 SECONDS
    static long ROUND_COUNT_DOWN = 3000*60; // 3 MINUTES PER ROUND
    private String[] playerIndex;
    private boolean started;
    private long startCountdown;
    private long roundCountdown;
    private int round;
    private DataStore dataStore;
    public RobotQuest(){
        this.label = LABEL;
        this.onEdge = true;
        this.playerIndex = new String[]{"-","-"};
    }
    public RobotQuest(int index){
        this();
        this.stub = index;
    }
    public void dataStore(DataStore dataStore){
        this.dataStore = dataStore;
    }
    public synchronized RobotQuest onTimer(long delta){
        if(!started){
            return null;
        }
        if(startCountdown>0){
            startCountdown = startCountdown-delta;
        }
        else{
            roundCountdown = roundCountdown-delta;
        }
        this.dataStore.update(this);
        return _snapshot();
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
            roundCountdown = ROUND_COUNT_DOWN;
        }
        this.dataStore.update(this);
        return ix+1;
    }
    public synchronized RobotQuest leave(String systemId){
        if(systemId.equals(playerIndex[0])){
            playerIndex[0]="-";
        }
        else if(systemId.equals(playerIndex[1])){
            playerIndex[1]="-";
        }
        started = !(this.playerIndex[0]+this.playerIndex[1]).equals("--");
        this.dataStore.update(this);
        return _snapshot();
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
        properties.put("rcc",this.roundCountdown);
        properties.put("round",this.round);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.stub = ((Number)properties.get("index")).intValue();
        this.playerIndex[0]=(String) properties.get("p0");
        this.playerIndex[1]=(String) properties.get("p1");
        this.startCountdown = ((Number)properties.get("scc")).longValue();
        this.roundCountdown = ((Number)properties.get("rcc")).longValue();
        this.round = ((Number)properties.get("round")).intValue();
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
        jsonObject.addProperty("round",this.round);
        jsonObject.addProperty("player1",playerIndex[0]);
        jsonObject.addProperty("player2",playerIndex[1]);
        jsonObject.addProperty("started",started);
        jsonObject.addProperty("startCountdown",startCountdown);
        jsonObject.addProperty("roundCountdown",roundCountdown);
        return jsonObject;
    }
    private RobotQuest _snapshot(){
        RobotQuest robotQuest = new RobotQuest();
        robotQuest.distributionKey(this.distributionKey());
        robotQuest.stub = this.stub;
        robotQuest.round = this.round;
        robotQuest.started = this.started;
        robotQuest.startCountdown = this.startCountdown;
        robotQuest.roundCountdown = this.roundCountdown;
        robotQuest.playerIndex[0]=this.playerIndex[0];
        robotQuest.playerIndex[1]=this.playerIndex[1];
        return robotQuest;
    }
}
