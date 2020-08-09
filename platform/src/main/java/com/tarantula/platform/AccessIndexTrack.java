package com.tarantula.platform;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.AccessIndex;
import com.tarantula.Distributable;
import com.tarantula.Recoverable;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.util.Map;

/**
 * updated 6/16/2020 yinghu lu
 */
public class AccessIndexTrack extends RecoverableObject implements AccessIndex, Portable {

    private int nodeId;

    public AccessIndexTrack(){
    }

    public AccessIndexTrack(String owner,String bucket,String label,int pid){
        this.owner = owner;
        this.bucket = bucket;
        this.label = label;
        this.nodeId = pid;
    }
    public AccessIndexTrack(String owner){
        this();
        this.owner = owner;
    }
    public int nodeId(){
        return this.nodeId;
    }
    public int scope(){
        return Distributable.INTEGRATION_SCOPE;
    }
    @Override
    public boolean backup(){
        return true;
    }
    @Override
    public boolean distributable(){return true;}
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.ACCESS_INDEX_CID;
    }

    @Override
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.owner);
        out.writeUTF("2",bucket);
        out.writeUTF("3",this.label);
        out.writeInt("4",this.nodeId);
    }

    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.owner = in.readUTF("1");
        this.bucket = in.readUTF("2");
        this.label  = in.readUTF("3");
        this.nodeId = in.readInt("4");
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",bucket);//lobby id
        this.properties.put("2",label);//game cluster id
        this.properties.put("3",nodeId);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.bucket = (String)properties.get("1");
        this.label = (String)properties.get("2");
        this.nodeId = ((Number)properties.getOrDefault("3",0)).intValue();
    }
    public String distributionKey(){
        return this.bucket+Recoverable.PATH_SEPARATOR+this.label+"-"+nodeId;
    }
    @Override
    public String toString(){
        return "Access Index ["+owner+"]->"+bucket+"/"+label+"/"+nodeId+"]";
    }
    public void distributionKey(String distributionKey){
       //skip the natural key
    }
    public Key key(){
        return new NaturalKey(this.owner);
    }
}
