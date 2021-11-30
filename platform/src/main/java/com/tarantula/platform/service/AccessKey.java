package com.tarantula.platform.service;

import com.icodesoftware.OnAccess;
import com.tarantula.platform.OnApplicationHeader;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.util.Map;

public class AccessKey extends OnApplicationHeader implements OnAccess {

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("typeId",typeId);
        this.properties.put("timestamp",timestamp);
        this.properties.put("index",index);
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.typeId = (String) properties.get("typeId");
        this.timestamp = ((Number)properties.getOrDefault("timestamp",0)).longValue();
        this.index = (String) properties.get("index");
    }

    public int getFactoryId() {
        return PortableRegistry.OID;
    }


    public int getClassId() {
        return PortableRegistry.ACCESS_KEY;
    }
}
