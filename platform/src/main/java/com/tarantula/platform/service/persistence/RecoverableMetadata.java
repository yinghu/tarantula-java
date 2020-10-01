package com.tarantula.platform.service.persistence;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

import com.icodesoftware.Distributable;
import com.icodesoftware.service.Metadata;
import com.tarantula.platform.RecoverableObject;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;

/**
 * Updated by yinghu lu on 7/11/2020
 */
public class RecoverableMetadata extends RecoverableObject implements Metadata, Portable {

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
    }
    public RecoverableMetadata(int factoryId,int classId){
        this.factoryId = factoryId;
        this.classId = classId;
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
        out.writeInt("3",this.version);
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
        this.version = in.readInt("3");
        this.timestamp = in.readLong("4");
        this.partition = in.readInt("5");
        this.onEdge = in.readBoolean("6");
        this.scope = in.readInt("7");
        this.source = in.readUTF("8");
    }
    @Override
    public String toString(){
        return "Metadata ["+source+"/"+factoryId+"/"+classId+"/"+scope+"/"+partition+"/"+timestamp+"/"+onEdge+"/"+index+"]";
    }
    //METADATA CONTRACT METHODS
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
}
