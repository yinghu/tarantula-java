package com.tarantula.platform.leaderboard;

import com.tarantula.LeaderBoard;
import com.tarantula.platform.RecoverableObject;
import java.util.Map;

/**
 * Updated by yinghu on 8/24/19
 */
public class LeaderBoardEntry extends RecoverableObject implements LeaderBoard.Entry {

    private String header;
    private String category;
    private String classifier;
    private String systemId="--";
    private double value=0;

    public LeaderBoardEntry(){
        this.vertex ="LeaderBoardEntry";
        this.label = "E";
        this.onEdge = true;
    }
    public LeaderBoardEntry(String header,String category,String classifier,double value,long timestamp){
        this();
        this.header = header;
        this.category = category;
        this.classifier = classifier;
        this.value = value;
        this.timestamp = timestamp;
    }
    public void update(String systemId,double replace,long timestamp){
        this.systemId = systemId;
        this.value = replace;
        this.timestamp = timestamp;
    }
    public String header(){
        return this.header;
    }
    public String category(){
        return this.category;
    }
    public String classifier(){
        return this.classifier;
    }
    public String systemId() {
        return systemId;
    }

    public double value() {
        return value;
    }

    @Override
    public int getFactoryId() {
        return LeaderBoardPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return LeaderBoardPortableRegistry.LEADER_BOARD_ENTRY_CID;
    }
    @Override
    public String toString(){
        return this.header+","+category+","+classifier+","+this.systemId+","+value+","+timestamp;
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",this.systemId);
        this.properties.put("2",value);
        this.properties.put("3",timestamp);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.systemId = (String) properties.get("1");
        this.value = ((Number)properties.get("2")).doubleValue();
        this.timestamp = ((Number)properties.get("3")).longValue();
    }

    @Override
    public boolean equals(Object obj){
        LeaderBoardEntry lde = (LeaderBoardEntry)obj;
        return this.systemId.equals(lde.systemId());
    }
    @Override
    public int hashCode(){
        return this.systemId.hashCode();
    }
}
