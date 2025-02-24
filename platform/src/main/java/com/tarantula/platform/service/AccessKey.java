package com.tarantula.platform.service;

import com.icodesoftware.OnAccess;
import com.icodesoftware.util.TROnApplication;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.util.Map;

public class AccessKey extends TROnApplication implements OnAccess {

    public static String LABEL = "accessKey";
    public AccessKey(){
        this.label = LABEL;
        this.onEdge = true;
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("typeId",typeId);
        this.properties.put("timestamp",timestamp);
        this.properties.put("index",index);
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

    @Override
    public boolean read(DataBuffer buffer){
        this.typeId = buffer.readUTF8();
        this.timestamp = buffer.readLong();
        this.index = buffer.readUTF8();
        this.disabled = buffer.readBoolean();
        return true;
    }
    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(typeId);
        buffer.writeLong(timestamp);
        buffer.writeUTF8(index);
        buffer.writeBoolean(disabled);
        return true;
    }
}
