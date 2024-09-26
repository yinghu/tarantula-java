package com.icodesoftware.lmdb.test;

import com.google.gson.JsonObject;

import com.icodesoftware.AccessIndex;
import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.util.NaturalKey;
import com.icodesoftware.util.RecoverableObject;


public class TestAccessIndex extends RecoverableObject implements AccessIndex {

    public int referenceId;
    public TestAccessIndex(){
        this.label = "access";
        this.onEdge = true;
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
    public int getFactoryId() {
        return 10;
    }

    @Override
    public int getClassId() {
        return 100;
    }


    public boolean read(DataBuffer buffer){
        this.referenceId = buffer.readInt();
        this.distributionId = buffer.readLong();
        return true;
    }
    public boolean write(DataBuffer buffer) {
        buffer.writeInt(referenceId);
        buffer.writeLong(distributionId);
        return true;
    }

    public boolean readKey(Recoverable.DataBuffer buffer){
        owner = buffer.readUTF8();
        return true;
    }
    public boolean writeKey(Recoverable.DataBuffer buffer){
        if(owner==null) return false;
        buffer.writeUTF8(owner);
        return true;
    }

    @Override
    public String toString(){
        return "Access Index ["+owner+"]->"+"/"+distributionId+"] referenceID =>"+referenceId+"]";
    }

    public Key key(){
        return new NaturalKey(this.owner);
    }

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("login",owner);
        jsonObject.addProperty("distributionId",distributionId());
        jsonObject.addProperty("referenceId",referenceId);
        return jsonObject;
    }
}
