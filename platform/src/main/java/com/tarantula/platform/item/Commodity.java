package com.tarantula.platform.item;


import com.icodesoftware.util.JsonUtil;

import java.util.Map;

public class Commodity extends ConfigurableObject{


    @Override
    public Map<String,Object> toMap(){
        super.toMap();
        this.properties.put("6",this.header.toString());
        this.properties.put("7",this.application.toString());
        this.properties.put("8",this.payload.toString());
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        super.fromMap(properties);
        this.header = JsonUtil.parse((String)properties.get("6"));
        this.application = JsonUtil.parse((String)properties.get("7"));
        this.payload = JsonUtil.parse((String)properties.get("8"));
    }

    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return ItemPortableRegistry.COMMODITY_CID;
    }
}
