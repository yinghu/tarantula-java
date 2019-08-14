package com.tarantula.platform.service.persistence;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.Distributable;
import com.tarantula.Metadata;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.io.IOException;

/**
 * Updated by yinghu lu on 4/7/2019.
 */
public class RecoverableMetadata implements Metadata {

    private String source;
    private int factoryId;
    private int classId;
    private int version;
    private int scope = Distributable.DATA_SCOPE;
    private int partition;
    private long timestamp;
    private boolean onEdge;
    private boolean distributable;
    private String index;

    public RecoverableMetadata(){}
    public RecoverableMetadata(String source,int factoryId,int classId,int scope,boolean onEdge,boolean distributable,String index){
        this.source = source;
        this.factoryId = factoryId;
        this.classId = classId;
        this.scope = scope;
        this.onEdge = onEdge;
        this.distributable = distributable;
        this.index = index;
    }
    public RecoverableMetadata(int factoryId,int classId){
        this.factoryId = factoryId;
        this.classId = classId;
    }
    public RecoverableMetadata(String source,int scope,int factoryId,int classId,int partition,boolean onEdge){
        this.source = source;
        this.scope = scope;
        this.factoryId = factoryId;
        this.classId = classId;
        this.partition = partition;
        this.onEdge = onEdge;
    }
    public boolean distributable(){
        return this.distributable;
    }
    public String index(){
        return this.index;
    }
    @Override
    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableRegistry.METADATA_CID;
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
        return "Metadata ["+source+"/"+factoryId+"/"+classId+"/"+scope+"/"+partition+"/"+timestamp+"/"+onEdge+"/"+distributable+"/"+index+"]";
    }
    //METADATA CONTRACT METHODS
    public int factoryId(){
        return this.factoryId;
    }
    public int classId(){
        return this.classId;
    }
    public int version(){
        return this.version;
    }
    public int scope(){
        return this.scope;
    }
    public long timestamp(){
        return this.timestamp;
    }
    public boolean onEdge(){ return this.onEdge;}
    public String source(){return this.source;}
    public int partition(){return this.partition;}
}
