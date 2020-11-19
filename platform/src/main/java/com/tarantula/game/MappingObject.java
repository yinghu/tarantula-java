package com.tarantula.game;

import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.RecoverableObject;


public class MappingObject extends RecoverableObject {

    private final static String _KEY = "_key";

    public byte[] toBinary(){
        return (byte[])this.properties.get(_KEY);
    }
    public void fromBinary(byte[] payload){
        this.properties.put(_KEY,payload);
    }
    @Override
    public int getFactoryId() {
        return GamePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return GamePortableRegistry.MAPPING_OBJECT_CID;
    }
    @Override
    public Key key(){
        return new AssociateKey(this.bucket,this.oid,this.label);
    }
}
