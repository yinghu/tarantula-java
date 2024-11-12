package com.icodesoftware.lmdb.test;

import com.icodesoftware.util.RecoverableObject;

public class TestObject extends RecoverableObject {

    public String type;
    public String name;

    public TestObject(){}

    public TestObject(String type,String name){
        this.type = type;
        this.name = name;
    }

    @Override
    public int getClassId() {
        return 113;
    }

    @Override
    public int getFactoryId() {
        return 1;
    }

    @Override
    public boolean read(DataBuffer buffer){
        type = buffer.readUTF8();
        name = buffer.readUTF8();
        return true;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(type);
        buffer.writeUTF8(name);
        return true;
    }
}
