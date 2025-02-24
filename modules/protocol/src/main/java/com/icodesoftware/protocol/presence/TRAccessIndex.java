package com.icodesoftware.protocol.presence;

import com.google.gson.JsonObject;
import com.icodesoftware.AccessIndex;
import com.icodesoftware.Distributable;
import com.icodesoftware.util.TROwnerObject;

public class TRAccessIndex extends TROwnerObject implements AccessIndex {

    protected int referenceId;
    public TRAccessIndex(){
    }
    public TRAccessIndex(String owner, int referenceId, long distributionId){
        this.owner = owner;
        this.referenceId = referenceId;
        this.distributionId = distributionId;
    }

    public TRAccessIndex(String owner){
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
    public String toString(){
        return "Access ["+owner+"] Distribution ID ["+distributionId+"] ReferenceID ["+referenceId+"]";
    }

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("login",owner);
        jsonObject.addProperty("distributionId",Long.toString(distributionId));
        jsonObject.addProperty("referenceId",referenceId);
        return jsonObject;
    }

    //Bufferable methods
    @Override
    public boolean read(DataBuffer buffer){
        this.referenceId = buffer.readInt();
        this.distributionId = buffer.readLong();
        return true;
    }
    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeInt(referenceId);
        buffer.writeLong(distributionId);
        return true;
    }

}
