package com.tarantula.platform;


import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.Property;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.io.IOException;
import java.util.Map;

/**
 * Updated 5/23/2018 yinghu lu
 */
public class DistributedProperty extends RecoverableObject implements Property{
    public String name;
    public String value;

    public DistributedProperty(){
        this.vertex = "DeploymentProperty";
    }

    public DistributedProperty(String name,String value){
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
    @Override
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.name);
        out.writeUTF("2",this.value);
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.name = in.readUTF("1");
        this.value = in.readUTF("2");
    }
    public String name(){
        return name;
    }
    public String value(){
        return value;
    }
    @Override
    public String toString(){
        return "["+this.name+"/"+this.value+"]";
    }
}
