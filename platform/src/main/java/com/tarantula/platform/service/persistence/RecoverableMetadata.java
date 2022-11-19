package com.tarantula.platform.service.persistence;

import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

import com.icodesoftware.Distributable;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.event.PortableEventRegistry;
import com.icodesoftware.util.RecoverableObject;

import java.io.IOException;


public class RecoverableMetadata extends RecoverableObject implements Metadata, Portable {

    private String typeId;
    private String source;
    private int factoryId;
    private int classId;

    private int scope = Distributable.DATA_SCOPE;
    private int partition;

    public RecoverableMetadata(){}

    public RecoverableMetadata(String source,int partition,int scope){
        this.source = source;
        this.partition = partition;
        this.scope = scope;
        _type();
    }

    public RecoverableMetadata(Metadata original,long revision){
        this.source = original.source();
        this.scope = original.scope();
        this.factoryId = original.factoryId();
        this.classId = original.classId();
        this.revision = revision;
        _type();
    }


    private void _type(){
        int ix = source.indexOf("_");
        if(ix<=0){
            typeId = source;
        }
        else{
            typeId = source.substring(0,ix);
        }
    }

    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.METADATA_CID;
    }

    @Override
    public void writePortable(PortableWriter out) throws IOException {
        out.writeInt("1",this.factoryId);
        out.writeInt("2",this.classId);
        out.writeLong("3",this.revision);
        out.writeLong("4",this.timestamp);
        out.writeInt("5",this.partition);
        out.writeBoolean("6",this.onEdge);
        out.writeInt("7",this.scope);
        out.writeUTF("8",this.source);
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.factoryId = in.readInt("1");
        this.classId = in.readInt("2");
        this.revision = in.readLong("3");
        this.timestamp = in.readLong("4");
        this.partition = in.readInt("5");
        this.onEdge = in.readBoolean("6");
        this.scope = in.readInt("7");
        this.source = in.readUTF("8");
        _type();
    }
    @Override
    public String toString(){
        return "Metadata ["+typeId+"/"+source+"/"+factoryId+"/"+classId+"/"+scope+"/"+partition+"/"+timestamp+"/"+onEdge+"/"+index+"]";
    }
    //METADATA CONTRACT METHODS
    public String typeId(){ return this.typeId;}
    public int factoryId(){
        return this.factoryId;
    }
    public int classId(){
        return this.classId;
    }
    public int scope(){
        return this.scope;
    }
    public String source(){return this.source;}
    public int partition(){return this.partition;}



    @Override
    public JsonObject toJson(){
        JsonObject json = new JsonObject();
        json.addProperty("typeId",typeId);
        json.addProperty("source",source);
        json.addProperty("factoryId",factoryId);
        json.addProperty("classId",classId);
        json.addProperty("scope",scope);
        json.addProperty("revision",String.valueOf(revision));
        return json;
    }

    public byte[] toBinary(){
        return toJson().toString().getBytes();
    }

    public void fromBinary(byte[] payload){
        JsonObject jsonObject = JsonUtil.parse(payload);
        this.typeId = jsonObject.get("typeId").getAsString();
        this.source = jsonObject.get("source").getAsString();
        this.factoryId = jsonObject.get("factoryId").getAsInt();
        this.classId = jsonObject.get("classId").getAsInt();
        this.scope = jsonObject.get("scope").getAsInt();
        this.revision = Long.parseLong(jsonObject.get("revision").getAsString());
    }

    public Metadata fromRevision(long revision){
        return new RecoverableMetadata(this,revision);
    }
}
