package com.tarantula.platform.service;

import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.util.Base64;

public class PresenceKey extends RecoverableObject {

    private final static String _KEY = "_key";

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
        return ((String)properties.get(_KEY)).getBytes();
    }

    @Override
    public void fromBinary(byte[] payload) {
        properties.put(_KEY,Base64.getEncoder().encodeToString(payload));
    }

    @Override
    public Key key(){
        return new AssociateKey(this.bucket,this.oid, "presenceKey");
    }

    public byte[] toKey(){
        return Base64.getDecoder().decode(toBinary());
    }

}
