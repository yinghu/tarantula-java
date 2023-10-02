package com.tarantula.platform.item;

import com.icodesoftware.util.NaturalKey;
import com.icodesoftware.util.RecoverableObject;

public class InstanceReference extends RecoverableObject {

    public InstanceReference(){

    }
    public InstanceReference(String name,long instanceId){
        this.name = name;
        this.distributionId = instanceId;
    }
    @Override
    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return ItemPortableRegistry.INSTANCE_REFERENCE;
    }

    @Override
    public Key ownerKey() {
        return new NaturalKey(name);
    }
}
