package com.tarantula.game;

import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.AssociateKey;
import com.icodesoftware.util.RecoverableObject;


public class MappingObject extends RecoverableObject {

    private final static String _KEY = "_key";

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


    public void value(byte[] json){
        properties.put(_KEY, JsonUtil.parseAsJsonElement(json));
    }

    public byte[] value(){
        return properties.get(_KEY).toString().getBytes();
    }

}
