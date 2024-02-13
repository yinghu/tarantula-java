package com.tarantula.platform.service.metrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;

import com.icodesoftware.service.Metrics;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class MetricsSnapshot extends RecoverableObject  {


    private MetricsProperty[] metrics;

    public MetricsSnapshot(String category,String classifier){
        this();
        this.name = classifier;
        this.label = Metrics.SNAPSHOT_LABEL_PREFIX +category;

    }

    public MetricsSnapshot(){
        this.onEdge = true;
        this.metrics = new MetricsProperty[Metrics.SNAPSHOT_TRACKING_SIZE];
        LocalDateTime _cur = LocalDateTime.now();
        for(int i=0;i<Metrics.SNAPSHOT_TRACKING_SIZE;i++){
            this.metrics[i]=new MetricsProperty("m"+i,0d,_cur);
        }
    }

    @Override
    public int scope() {
        return Distributable.LOCAL_SCOPE;
    }


    public boolean read(DataBuffer buffer){
        this.name = buffer.readUTF8();
        this.timestamp = buffer.readLong();
        for(int i=0; i<Metrics.SNAPSHOT_TRACKING_SIZE;i++){
            metrics[i].value = buffer.readDouble();
            metrics[i].timestamp(buffer.readLong());
        }
        return true;
    }
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(name);
        buffer.writeLong(timestamp);
        for(int i=0; i<Metrics.SNAPSHOT_TRACKING_SIZE;i++){
            buffer.writeDouble(metrics[i].value());
            buffer.writeLong(metrics[i].timestamp());
        }
        return true;
    }

    public MetricsProperty[] metrics(){
        return metrics;
    }

    @Override
    public int getFactoryId() {
        return StatisticsPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return StatisticsPortableRegistry.METRICS_SNAPSHOT_CID;
    }


    public void initialize(int index,MetricsProperty property,LocalDateTime timeUpdated){
        metrics[index].name(property.name());
        this.timestamp = TimeUtil.toUTCMilliseconds(timeUpdated);
    }
    public MetricsSnapshot update(double currentData){
        metrics[Metrics.SNAPSHOT_TRACKING_SIZE-1].value = currentData;//
        this.timestamp = TimeUtil.toUTCMilliseconds(LocalDateTime.now());
        return this;
    }
    public MetricsProperty push(MetricsProperty property,LocalDateTime dateTime){
        MetricsProperty toHistory = metrics[Metrics.SNAPSHOT_TRACKING_SIZE-1];
        for(int i=0;i<Metrics.SNAPSHOT_TRACKING_SIZE-1;i++){
            metrics[i]=metrics[i+1];
        }
        metrics[Metrics.SNAPSHOT_TRACKING_SIZE-1] = property;
        this.timestamp = TimeUtil.toUTCMilliseconds(dateTime);
        return toHistory;
    }

    public static String hourlyLabel(LocalDateTime dateTime){
        int hrs = dateTime.getHour();
        LocalDateTime labelTime = dateTime.toLocalDate().atTime(hrs,0,0,0);
        return labelTime.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }
    public static String dailyLabel(LocalDateTime dateTime){
        return dateTime.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
    }
    public static String weeklyLabel(LocalDateTime dateTime){
        return dateTime.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
    }
    public static String monthlyLabel(LocalDateTime dateTime){
        return dateTime.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
    }
    public static String yearlyLabel(LocalDateTime dateTime){
        return dateTime.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
    }

    public void reset(DataStore.Stream<MetricsProperty> reset){
        for(MetricsProperty p : metrics){
            reset.on(p);
        }
    }

    @Override
    public JsonObject toJson() {
        JsonObject resp = new JsonObject();
        resp.addProperty("name",name);
        resp.addProperty("timestamp",timestamp);
        JsonArray spot = new JsonArray();
        for(MetricsProperty m : metrics){
            spot.add(m.toJson());
        }
        resp.add("data",spot);
        return resp;
    }

}
