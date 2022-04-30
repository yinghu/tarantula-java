package com.tarantula.platform.presence.saves;

import com.tarantula.platform.IndexSet;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.Map;

//device id => systemId index set
public class DeviceIndex extends IndexSet {


    @Override
    public Map<String,Object> toMap(){
        this.properties.put("3",timestamp);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.timestamp = ((Number) properties.get("3")).longValue();
    }

    @Override
    public int getClassId() {
        return PresencePortableRegistry.DEVICE_INDEX_CID;
    }

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
}