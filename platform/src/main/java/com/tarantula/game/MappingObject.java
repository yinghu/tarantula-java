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

    @Override
    public void label(String label){
        this.label = "mo_"+label;
    }

    public void value(byte[] json){
        properties.put(_KEY, JsonUtil.parseAsJsonElement(json));
    }

    public byte[] value(){
        return properties.getOrDefault(_KEY,"{}").toString().getBytes();
    }

}
