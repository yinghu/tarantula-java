package com.tarantula.platform.store;

import com.icodesoftware.util.NaturalKey;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.item.ItemPortableRegistry;

import java.util.Map;

public class Transaction extends RecoverableObject{

    public String originalPayload;
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("owner",owner);
        this.properties.put("index",index);
        this.properties.put("originalPayload",originalPayload);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.owner = (String) properties.getOrDefault("owner","");
        this.index = (String) properties.getOrDefault("index","");
        this.originalPayload = (String) properties.getOrDefault("originalPayload","{}");
    }
    @Override
    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return ItemPortableRegistry.TRANSACTION_CID;
    }
    public void distributionKey(String distributionKey){
        this.index = distributionKey;
    }
    @Override
    public Key key(){
        return new NaturalKey(this.index);
    }
}
