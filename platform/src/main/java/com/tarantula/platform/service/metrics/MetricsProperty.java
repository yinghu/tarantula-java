package com.tarantula.platform.service.metrics;

import com.google.gson.JsonObject;
import com.icodesoftware.Recoverable;
import com.icodesoftware.service.Metrics;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.statistics.StatisticsPortableRegistry;
import java.time.LocalDateTime;

public class MetricsProperty extends RecoverableObject implements Metrics.Spot {


    public double value;

    public MetricsProperty(){
    }
    public MetricsProperty(double value,LocalDateTime dateCreated){
        this.value = value;
        this.timestamp = TimeUtil.toUTCMilliseconds(dateCreated);
    }
    public MetricsProperty(String name, double value,LocalDateTime dateCreated){
        this(value,dateCreated);
        this.name = name;
    }

    @Override
    public int getFactoryId() {
        return StatisticsPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return StatisticsPortableRegistry.METRICS_PROPERTY_CID;
    }


    public double value(){
        return value;
    }
    public void value(double value){
        this.value = value;
    }

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name",name);
        jsonObject.addProperty("value",Double.toString(value));
        jsonObject.addProperty("timestamp",timestamp);
        return jsonObject;
    }


    public static String historyPropertyLabel(LocalDateTime current){
        return new StringBuffer().append(current.getYear()).append(Recoverable.PATH_SEPARATOR).append(current.getDayOfYear()).append(Recoverable.PATH_SEPARATOR).append(current.getHour()).toString();
    }
}
