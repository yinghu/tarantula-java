package com.tarantula.platform.presence.saves;

import com.icodesoftware.util.NaturalKey;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.presence.PresencePortableRegistry;


//device id => systemId index set
public class DeviceIndex extends IndexSet {

    public DeviceIndex(){

    }
    public DeviceIndex(String deviceId){
        this.index = deviceId;
    }

    @Override
    public int getClassId() {
        return PresencePortableRegistry.DEVICE_INDEX_CID;
    }

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    public Key key(){
        return new NaturalKey(this.index);
    }
}