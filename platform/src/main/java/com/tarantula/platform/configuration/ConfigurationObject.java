package com.tarantula.platform.configuration;

import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.item.ItemPortableRegistry;


public class ConfigurationObject extends RecoverableObject {

    private final static String _KEY = "_key";

    public ConfigurationObject(){
    }
    public ConfigurationObject(String label){
        this.label = label;
    }
    @Override
    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return ItemPortableRegistry.CONFIGURATION_OBJECT_CID;
    }

    @Override
    public Key key(){
        return new AssociateKey(this.bucket,this.oid,this.label);
    }

    public void value(byte[] json){
        properties.put(_KEY, JsonUtil.parseAsJsonElement(json));
    }

    public byte[] value(){
        return properties.getOrDefault(_KEY,"{}").toString().getBytes();
    }


}
