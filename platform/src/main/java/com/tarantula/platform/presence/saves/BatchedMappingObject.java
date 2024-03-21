package com.tarantula.platform.presence.saves;

import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;


public class BatchedMappingObject extends RecoverableObject {

    //key length 4 + value length 4 + datastore header size 16 => 24
    public static final int MAP_OBJECT_HEADER_SIZE = 24;
    private byte[] value;
    public int batch;
    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.BATCHED_MAPPING_OBJECT_CID;
    }



    public BatchedMappingObject(){
        this.onEdge = true;
    }
    public BatchedMappingObject(String savedKey){
        this();
        this.label = savedKey;
    }

    public void value(byte[] json){
        value = json;
    }

    public byte[] value(){
        return value;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        batch = buffer.readInt();
        int size = buffer.readInt();
        if(size==0) return true;
        value = new byte[size];
        for(int i=0;i<size;i++){
            value[i]=buffer.readByte();
        }
        return true;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeInt(batch);
        if(value==null || value.length==0){
            buffer.writeInt(0);
            return true;
        }
        buffer.writeInt(value.length);
        for(byte b : value){
            buffer.writeByte(b);
        }
        return true;
    }

    @Override
    public boolean validate() {
        return true;
    }
}
