package com.icodesoftware.lmdb.test;

import com.google.gson.JsonObject;

import com.icodesoftware.AccessIndex;
import com.icodesoftware.Distributable;
import com.icodesoftware.util.NaturalKey;
import com.icodesoftware.util.RecoverableObject;

import java.util.Map;
import java.util.UUID;

public class TestAccessIndex extends RecoverableObject implements AccessIndex {

    private int referenceId;
    public TestAccessIndex(){
        this.bucket = "DBS";
        this.oid = UUID.randomUUID().toString();
        this.referenceId = 1;
    }
    public TestAccessIndex(String owner){
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
        return 1;
    }

    @Override
    public int getClassId() {
        return 10;
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

    public void read(DataBuffer buffer){
        this.bucket = buffer.readUTF8();
        this.oid = buffer.readUTF8();
        this.referenceId = buffer.readInt();
    }
    public void write(DataBuffer buffer) {
        buffer.writeUTF8(bucket);
        buffer.writeUTF8(oid);
        buffer.writeInt(referenceId);
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

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("login",owner);
        jsonObject.addProperty("distributionKey",distributionKey());
        jsonObject.addProperty("referenceId",referenceId);
        return jsonObject;
    }
}
