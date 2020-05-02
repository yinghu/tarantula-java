package com.tarantula.platform.leaderboard;

import com.tarantula.LeaderBoard;
import com.tarantula.Recoverable;
import com.tarantula.platform.NaturalKey;
import com.tarantula.platform.RecoverableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.Map;

/**
 * Updated by yinghu on 5/1/2020
 */
public class EntryImpl extends RecoverableObject implements LeaderBoard.Entry {

    private double value=0;
    private String classifier;
    private String category;
    public EntryImpl(){
        this.vertex ="Entry";
        this.owner="--";
        this.value=0;
        this.timestamp=0;
    }
    //query entry
    public EntryImpl(String classifier,String category,int version){
        this();
        this.classifier = classifier;
        this.category = category;
        this.version = version;
    }
    public EntryImpl(String systemId,double value,long timestamp){
        this();
        this.owner = systemId;
        this.value = value;
        this.timestamp = timestamp;
    }
    //value entry
    public EntryImpl(String classifier,String category,int version,String systemId,double value,long timestamp){
        this();
        this.classifier = classifier;
        this.category = category;
        this.version = version;
        this.owner = systemId;
        this.value = value;
        this.timestamp = timestamp;
    }
    public LeaderBoard.Entry update(LeaderBoard.Entry entry){
        this.owner = entry.owner();
        this.value = entry.value();
        this.timestamp = entry.timestamp();
        return this;
    }
    public String category(){
        return this.category;
    }
    public String classifier(){
        return this.classifier;
    }

    public double value() {
        return value;
    }

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PresencePortableRegistry.LEADER_BOARD_ENTRY_CID;
    }
    @Override
    public String toString(){
        return classifier+"/"+category+"/"+this.owner+"/"+value+"/"+timestamp;
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",this.owner);
        this.properties.put("2",value);
        this.properties.put("3",timestamp);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.owner = (String) properties.get("1");
        this.value = ((Number)properties.get("2")).doubleValue();
        this.timestamp = ((Number)properties.get("3")).longValue();
    }
    public void distributionKey(String distributionKey){
        //parse key
        String[] k = distributionKey.split(Recoverable.PATH_SEPARATOR);
        classifier = k[0];
        category = k[1];
        version = Integer.parseInt(k[2]);
    }
    public Key key(){
        return new NaturalKey(this.classifier+ Recoverable.PATH_SEPARATOR+category+Recoverable.PATH_SEPARATOR+version);
    }

    @Override
    public boolean equals(Object obj){
        EntryImpl lde = (EntryImpl)obj;
        return this.owner.equals(lde.owner());
    }
    @Override
    public int hashCode(){
        return this.owner.hashCode();
    }
}
