package com.tarantula.game;

import com.icodesoftware.util.RecoverableObject;


public class MappingObject extends RecoverableObject {

    private byte[] value;
    @Override
    public int getFactoryId() {
        return GamePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return GamePortableRegistry.MAPPING_OBJECT_CID;
    }



    @Override
    public void label(String label){
        if(label.startsWith("mo_")){
            this.label = label;
            return;
        }
        this.label = "mo_"+label;
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
