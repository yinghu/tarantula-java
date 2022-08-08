package com.tarantula.platform;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.AccessIndex;
import com.icodesoftware.Distributable;
import com.icodesoftware.util.NaturalKey;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.util.Map;
import com.icodesoftware.util.RecoverableObject;

public class AccessIndexTrack extends RecoverableObject implements AccessIndex, Portable {

    private int referenceId;
    public AccessIndexTrack(){
    }
    public AccessIndexTrack(String owner,String bucket,String oid,int referenceId){
        this.owner = owner;
        this.bucket = bucket;
        this.oid = oid;
        this.referenceId = referenceId;
    }

    public AccessIndexTrack(String owner){
        this();
        this.owner = owner;
    }
    public int referenceId(){
        return referenceId;
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
        out.writeUTF("2",this.bucket);
        out.writeUTF("3",this.oid);
        out.writeInt("4",this.referenceId);
    }

    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.owner = in.readUTF("1");
        this.bucket = in.readUTF("2");
        this.oid = in.readUTF("3");
        this.referenceId = in.readInt("4");
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",bucket);
        this.properties.put("2",oid);
        this.properties.put("3",referenceId);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.bucket = (String)properties.get("1");
        this.oid = (String)properties.get("2");
        this.referenceId = ((Number)properties.get("3")).intValue();
    }

    @Override
    public String toString(){
        return "Access Index ["+owner+"]->"+bucket+"/"+oid+"] referenceID =>"+referenceId+"]";
    }
    public void distributionKey(String distributionKey){
       //skip the natural key
    }
    public Key key(){
        return new NaturalKey(this.owner);
    }
}
