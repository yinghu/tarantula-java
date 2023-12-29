package com.tarantula.platform.configuration;

import com.tarantula.platform.item.ItemPortableRegistry;
import com.tarantula.platform.presence.MappingObject;


public class ConfigurationObject extends MappingObject {

    public static final String LABEL = "configuration_object";

    public ConfigurationObject(){
        super();
        this.label = LABEL;
    }
    public ConfigurationObject(String name){
        this();
        this.name = name;
    }
    @Override
    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return ItemPortableRegistry.CONFIGURATION_OBJECT_CID;
    }


}
