package com.tarantula.platform;

import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.AccessIndex;
import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.util.NaturalKey;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import com.icodesoftware.util.RecoverableObject;

public class AccessIndexTrack extends RecoverableObject implements AccessIndex, Portable {

    private int referenceId;
    public AccessIndexTrack(){
    }
    public AccessIndexTrack(String owner,int referenceId,long distributionId){
        this.owner = owner;
        this.referenceId = referenceId;
        this.distributionId = distributionId;
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
        out.writeInt("2",this.referenceId);
        out.writeLong("3",distributionId);
    }

    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.owner = in.readUTF("1");
        this.referenceId = in.readInt("2");
        this.distributionId = in.readLong("3");
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
    public boolean readKey(Recoverable.DataBuffer buffer){
        owner = buffer.readUTF8();
        return true;
    }
    @Override
    public boolean writeKey(Recoverable.DataBuffer buffer){
        if(owner==null) return false;
        buffer.writeUTF8(owner);
        return true;
    }

    @Override
    public boolean validate() {
        return owner!=null && owner.length()>4;
    }
}
