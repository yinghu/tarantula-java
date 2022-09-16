package com.tarantula.platform.service.metrics;

import com.google.gson.JsonObject;
import com.icodesoftware.Property;
import com.icodesoftware.Recoverable;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.IndexKey;
import com.tarantula.platform.statistics.StatisticsPortableRegistry;

import java.time.LocalDateTime;
import java.util.Map;

public class MetricsProperty extends RecoverableObject implements Property {

    public String name;
    public Object value;


    public MetricsProperty(){
    }

    public MetricsProperty(int index, String name, Object value,LocalDateTime dateCreated){
        this(index,name,value,TimeUtil.toUTCMilliseconds(dateCreated));
    }
    public MetricsProperty(int index,String name, Object value,long timestamp){
        this.routingNumber = index;
        this.name = name;
        this.value = value;
        this.timestamp = timestamp;
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
        this.properties.put("timestamp",timestamp);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.name = (String)properties.get("name");
        this.value = Double.parseDouble((String)properties.get("value"));//
        this.timestamp = ((Number)properties.get("timestamp")).longValue();
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
        jsonObject.addProperty("timestamp",timestamp);
        return jsonObject;
    }


    public Key key(){
        return new IndexKey(this.bucket,oid,routingNumber);
    }

    public static String historyPropertyLabel(LocalDateTime current){
        return new StringBuffer().append(current.getYear()).append(Recoverable.PATH_SEPARATOR).append(current.getDayOfYear()).append(Recoverable.PATH_SEPARATOR).append(current.getHour()).toString();
    }
}
