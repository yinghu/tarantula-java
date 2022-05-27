package com.tarantula.platform.service;

import com.icodesoftware.OnAccess;
import com.tarantula.platform.OnApplicationHeader;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.util.Map;

public class AccessKey extends OnApplicationHeader implements OnAccess {

    public static String LABEL = "accessKey";
    public AccessKey(){
        this.label = LABEL;
        this.onEdge = true;
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("typeId",typeId);
        this.properties.put("timestamp",timestamp);
        this.properties.put("index",owner);
        this.properties.put("disabled",disabled);
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.typeId = (String) properties.get("typeId");
        this.timestamp = ((Number)properties.getOrDefault("timestamp",0)).longValue();
        this.index = (String) properties.get("index");
        this.disabled = (boolean)properties.getOrDefault("disabled",false);
    }

    public int getFactoryId() {
        return PortableRegistry.OID;
    }


    public int getClassId() {
        return PortableRegistry.ACCESS_KEY;
    }
}
