package com.tarantula.platform.presence;

import com.icodesoftware.util.RecoverableObject;



public class MappingObject extends RecoverableObject {

    //key length 4 + value length 4 + datastore header size 16 => 24
    public static final int MAP_OBJECT_HEADER_SIZE = 24;
    private byte[] value;
    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.MAPPING_OBJECT_CID;
    }



    public MappingObject(){
        this.onEdge = true;
    }


    public void value(byte[] json){
        value = json;
    }

    public byte[] value(){
        return value;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        name = buffer.readUTF8();
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
        buffer.writeUTF8(name);
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
