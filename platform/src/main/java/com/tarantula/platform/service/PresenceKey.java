package com.tarantula.platform.service;

import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.service.cluster.PortableRegistry;

public class PresenceKey extends RecoverableObject {

    public byte[] key;

    public PresenceKey(){

    }

    @Override
    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableRegistry.PRESENCE_KEY_CID;
    }

    @Override
    public byte[] toBinary() {
        return key;
    }

    @Override
    public void fromBinary(byte[] payload) {
        key = payload;
    }

    @Override
    public Key key(){
        return new AssociateKey(this.bucket,this.oid, "presenceKey");
    }


}
