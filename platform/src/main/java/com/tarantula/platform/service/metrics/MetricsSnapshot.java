package com.tarantula.platform.service.metrics;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;

import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.statistics.StatisticsPortableRegistry;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class MetricsSnapshot extends RecoverableObject  {

    public final static int TRACKING_NUMBER = 12;
    private MetricsProperty[] metrics;

    public MetricsSnapshot(String category,String classifier){
        this();
        this.name = classifier;
        this.label = "snapshot_"+category;

    }

    public MetricsSnapshot(){
        this.onEdge = true;
        this.metrics = new MetricsProperty[TRACKING_NUMBER];
        for(int i=0;i<TRACKING_NUMBER;i++){
            this.metrics[i]=new MetricsProperty(i,"m"+i,0d,0l);
        }
    }

    @Override
    public int scope() {
        return Distributable.LOCAL_SCOPE;
    }


    public boolean read(DataBuffer buffer){
        this.name = buffer.readUTF8();
        this.timestamp = buffer.readLong();
        for(int i=0; i<TRACKING_NUMBER;i++){
            metrics[i].value = buffer.readDouble();
            metrics[i].timestamp(buffer.readLong());
        }
        return true;
    }
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(name);
        buffer.writeLong(timestamp);
        for(int i=0; i<TRACKING_NUMBER;i++){
            buffer.writeDouble((double)metrics[i].value());
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


    public void initialize(MetricsProperty property,LocalDateTime timeUpdated){
        metrics[property.routingNumber()].name = property.name;
        this.timestamp = TimeUtil.toUTCMilliseconds(timeUpdated);
    }
    public MetricsSnapshot update(double currentData){
        metrics[TRACKING_NUMBER-1].value = currentData;//
        this.timestamp = TimeUtil.toUTCMilliseconds(LocalDateTime.now());
        return this;
    }
    public MetricsProperty push(MetricsProperty property,LocalDateTime dateTime){
        MetricsProperty toHistory = metrics[TRACKING_NUMBER-1];
        for(int i=0;i<TRACKING_NUMBER-1;i++){
            metrics[i]=metrics[i+1];
        }
        metrics[TRACKING_NUMBER-1] = property;
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

}
