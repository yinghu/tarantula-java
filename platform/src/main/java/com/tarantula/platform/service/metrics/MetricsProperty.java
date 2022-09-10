package com.tarantula.platform.service.metrics;

import com.google.gson.JsonObject;
import com.icodesoftware.Property;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.IndexKey;
import com.tarantula.platform.statistics.StatisticsPortableRegistry;

import java.util.Map;

public class MetricsProperty extends RecoverableObject implements Property {

    public String name;
    public Object value;


    public MetricsProperty(){
    }

    public MetricsProperty(int index,String name, Object value){
        this.routingNumber = index;
        this.name = name;
        this.value = value;
    }
    public MetricsProperty(String name, Object value){
        this.name = name;
        this.value = value;
    }


    @Override
    public int getFactoryId() {
        return StatisticsPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return StatisticsPortableRegistry.METRICS_PROPERTY_CID;
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
        this.value = properties.get("value");//
    }

    public String name(){
        return name;
    }
    public Object value(){
        return value;
    }
    @Override
    public String toString(){
        return "["+routingNumber+":"+this.name+"/"+this.value+"]";
    }

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name",name);
        jsonObject.addProperty("value",value.toString());
        return jsonObject;
    }


    public Key key(){
        return new IndexKey(this.bucket,oid,routingNumber);
    }
}
