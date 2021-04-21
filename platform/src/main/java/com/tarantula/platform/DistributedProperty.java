package com.tarantula.platform;
import com.icodesoftware.Property;
import com.tarantula.platform.service.cluster.PortableRegistry;
import com.icodesoftware.util.RecoverableObject;

import java.util.Map;

public class DistributedProperty extends RecoverableObject implements Property {
    public String name;
    public Object value;

    public DistributedProperty(){
    }

    public DistributedProperty(String name,Object value){
        this.name = name;
        this.value = value;
    }

    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    public int getClassId() {
        return PortableRegistry.PROPERTY_CID;
    }


    @Override
    public Map<String,Object> toMap(){
        this.properties.put("name",this.name);
        this.properties.put("value",this.value);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.name = (String)properties.get("name");
        this.value = (String)properties.get("value");//
    }

    public String name(){
        return name;
    }
    public Object value(){
        return value;
    }
    @Override
    public String toString(){
        return "["+this.name+"/"+this.value+"]";
    }
}
