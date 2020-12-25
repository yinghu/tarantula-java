package com.tarantula.platform;

import com.tarantula.platform.service.cluster.PortableRegistry;

import java.util.Map;

/**
 * Created by yinghu lu on 12/25/2020.
 */
public class ModuleIndex extends RecoverableObject{

    public int getFactoryId() {
        return PortableRegistry.OID;
    }
    public int getClassId() {
        return PortableRegistry.MODULE_INDEX_CID;
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",index);//lobby id
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.index = (String)properties.get("1");
    }
}
