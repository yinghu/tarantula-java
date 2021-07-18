package com.tarantula.platform.service.deployment;

import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.service.cluster.PortableRegistry;

public class ConfigurationTemplate extends RecoverableObject {

    private final static String _KEY = "_template";

    public byte[] toBinary(){
        return (byte[])this.properties.get(_KEY);
    }
    public void fromBinary(byte[] payload){
        this.properties.put(_KEY,payload);
    }
    @Override
    public int getFactoryId() {
        return PortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PortableRegistry.CONFIGURATION_TEMPLATE_CID;
    }
    @Override
    public Key key(){
        return new AssociateKey(this.bucket,this.oid,this.label);
    }
}
