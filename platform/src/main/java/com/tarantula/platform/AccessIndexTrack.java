package com.tarantula.platform;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.AccessIndex;
import com.tarantula.Recoverable;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.util.Map;

/**
 * updated 6/16/2020 yinghu lu
 */
public class AccessIndexTrack extends IntegrationScopeObject implements AccessIndex, Portable {

    public AccessIndexTrack(){
    }

    public AccessIndexTrack(String owner,String bucket,String label,int pid){
        this.owner = owner;
        this.bucket = bucket;
        this.label = label;
        this.version = pid;
    }
    public AccessIndexTrack(String owner){
        this();
        this.owner = owner;
    }

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
        out.writeInt("4",this.version);
    }

    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.owner = in.readUTF("1");
        this.bucket = in.readUTF("2");
        this.label  = in.readUTF("3");
        this.version = in.readInt("4");
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",bucket);//lobby id
        this.properties.put("2",label);//game cluster id
        this.properties.put("3",version);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.bucket = (String)properties.get("1");
        this.label = (String)properties.get("2");
        this.version = ((Number)properties.getOrDefault("3",0)).intValue();
    }
    public String distributionKey(){
        return this.bucket+Recoverable.PATH_SEPARATOR+this.label+"-"+version;
    }
    @Override
    public String toString(){
        return "Access Index ["+bucket+"/"+owner+"/"+label+"/"+version+"]";
    }
    public void distributionKey(String distributionKey){
       //skip the natural key
    }
    public Key key(){
        return new NaturalKey(this.owner);
    }
}
