package com.icodesoftware.protocol.service;

import com.icodesoftware.protocol.ProtocolPortableRegistry;
import com.icodesoftware.service.AccessKey;
import com.icodesoftware.util.RecoverableObject;

public class AccessKeyTrack extends RecoverableObject implements AccessKey {

    public int getFactoryId(){
        return ProtocolPortableRegistry.OID;
    }
    public int getClassId(){
        return ProtocolPortableRegistry.ACCESS_KEY_TRACK_ID;
    }

    @Override
    public long keyId() {
        return this.distributionId;
    }

    @Override
    public boolean valid() {
        return !disabled;
    }

}
