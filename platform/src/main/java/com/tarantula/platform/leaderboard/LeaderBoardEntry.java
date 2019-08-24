package com.tarantula.platform.leaderboard;

import com.tarantula.LeaderBoard;
import com.tarantula.platform.RecoverableObject;
import com.tarantula.platform.util.SystemUtil;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Updated by yinghu on 8/24/19
 */
public class LeaderBoardEntry extends RecoverableObject implements LeaderBoard.Entry {

    private String header;
    private String category;
    private String classifier;
    private String name;
    private String systemId="--";
    private double value=0;
    public LeaderBoardEntry(){
        this.vertex ="LeaderBoardEntry";
        this.label = "E";
        this.onEdge = true;
    }
    public LeaderBoardEntry(String header,String category,String classifier,double value){
        this.header = header;
        this.category = category;
        this.classifier = classifier;
        this.value = value;
    }
    public LeaderBoardEntry(String name){
        this();
        this.name = name;
    }
    public void update(String systemId,double replace){
        this.systemId = systemId;
        this.value = replace;
        this.timestamp = SystemUtil.toUTCMilliseconds(LocalDateTime.now());
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
    public String name(){
        return name;
    }
    public String systemId() {
        return systemId;
    }

    public double value() {
        return value;
    }
    public void value(double value){
        this.value = value;
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
        return this.header+","+category+","+classifier+","+this.systemId+","+value+"/"+name;
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",this.name);
        this.properties.put("2",this.systemId);
        this.properties.put("3",value);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.name = (String) properties.get("1");
        this.systemId = (String) properties.get("2");
        this.value = ((Number)properties.get("3")).doubleValue();
    }

    @Override
    public byte[] toByteArray(){
        byte[] _sb = systemId.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(20+_sb.length);
        buffer.putDouble(this.value);
        buffer.putLong(this.timestamp);
        buffer.putInt(_sb.length);
        buffer.put(_sb);
        return buffer.array();
    }
    @Override
    public void fromByteArray(byte[] data){
        ByteBuffer buffer = ByteBuffer.wrap(data);
        this.value = buffer.getDouble();
        this.timestamp = buffer.getLong();
        int len = buffer.getInt();
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<len;i++){
            sb.append((char) buffer.get());
        }
        this.systemId = sb.toString();
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
