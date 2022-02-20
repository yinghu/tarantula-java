package com.tarantula.platform.store;

import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.item.ItemPortableRegistry;

import java.util.Map;

public class Transaction extends RecoverableObject{

    public String originalPayload;
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("originalPayload",originalPayload);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
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
    @Override
    public Key key() {
        return new AssociateKey(this.bucket,this.oid,this.label);
    }
}
