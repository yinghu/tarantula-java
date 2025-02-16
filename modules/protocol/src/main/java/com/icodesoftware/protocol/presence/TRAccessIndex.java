package com.icodesoftware.protocol.presence;

import com.google.gson.JsonObject;
import com.icodesoftware.AccessIndex;
import com.icodesoftware.Distributable;
import com.icodesoftware.util.NaturalKey;
import com.icodesoftware.util.RecoverableObject;


public class TRAccessIndex extends RecoverableObject implements AccessIndex {

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

    public Key key(){
        return new NaturalKey(this.owner);
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
    @Override
    public boolean readKey(DataBuffer buffer){
        owner = buffer.readUTF8();
        return true;
    }
    @Override
    public boolean writeKey(DataBuffer buffer){
        if(owner==null) return false;
        buffer.writeUTF8(owner);
        return true;
    }

    @Override
    public boolean validate() {
        return owner!=null && owner.length()>4;
    }
}
