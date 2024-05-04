package com.tarantula.platform.service.metrics;


import com.google.gson.JsonObject;
import com.icodesoftware.Distributable;

import com.icodesoftware.Statistics;
import com.icodesoftware.util.OnApplicationHeader;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;

import java.time.LocalDateTime;

public class SystemStatisticsEntry extends OnApplicationHeader implements Statistics.Entry {

    //private String name;
    private double total=0;
    private double hourly=0;
    private double daily=0;
    private double weekly=0;
    private double monthly=0;
    private double yearly=0;

    public SystemStatisticsEntry(){
        this.onEdge = true;
    }
    public SystemStatisticsEntry(String name,String label){
        this();
        this.name = name;
        this.label = label;
    }
    public SystemStatisticsEntry(Statistics.Entry entry){
        this();
        this.name = entry.name();
        this.hourly = entry.hourly();
        this.daily = entry.daily();
        this.weekly = entry.weekly();
        this.monthly = entry.monthly();
        this.yearly = entry.yearly();
        this.total = entry.total();
        this.timestamp = entry.timestamp();
    }

    @Override
    public int scope() {
        return Distributable.LOCAL_SCOPE;
    }

    @Override
    public String name() {
        return name;
    }
    @Override
    public synchronized double total() {
        return this.total;
    }

    void total(double total,LocalDateTime update){
        this.total = total;
        this.timestamp = TimeUtil.toUTCMilliseconds(update);
    }
    @Override
    public double hourly() {
        return hourly;
    }
    void hourly(double hourly,LocalDateTime update){
        this.hourly = hourly;
        this.timestamp = TimeUtil.toUTCMilliseconds(update);
    }
    @Override
    public synchronized double daily() {
        return daily;
    }

    void daily(double daily,LocalDateTime update){
        this.daily = daily;
        this.timestamp = TimeUtil.toUTCMilliseconds(update);
    }
    @Override
    public synchronized double weekly() {
        return weekly;
    }

    void weekly(double weekly,LocalDateTime update){
        this.weekly = weekly;
        this.timestamp = TimeUtil.toUTCMilliseconds(update);
    }
    @Override
    public synchronized double monthly() {
        return monthly;
    }

    void monthly(double monthly,LocalDateTime update){
        this.monthly = monthly;
        this.timestamp = TimeUtil.toUTCMilliseconds(update);
    }
    @Override
    public synchronized double yearly() {
        return yearly;
    }

    void yearly(double yearly,LocalDateTime update){
        this.yearly = yearly;
        this.timestamp = TimeUtil.toUTCMilliseconds(update);
    }
    @Override
    public synchronized Statistics.Entry update(double delta) {
        LocalDateTime _now = LocalDateTime.now();
        hourly += delta;
        daily += delta;
        weekly += delta;
        monthly += delta;
        yearly += delta;
        total += delta;
        timestamp = TimeUtil.toUTCMilliseconds(_now);
        return this;
    }
    @Override
    public int getFactoryId() {
        return StatisticsPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return StatisticsPortableRegistry.SYSTEM_STATISTICS_ENTRY_CID;
    }

    public boolean read(DataBuffer buffer){
        this.name = buffer.readUTF8();
        this.hourly = buffer.readDouble();
        this.daily = buffer.readDouble();
        this.weekly = buffer.readDouble();
        this.monthly = buffer.readDouble();
        this.yearly = buffer.readDouble();
        this.total = buffer.readDouble();
        this.timestamp = buffer.readLong();
        return true;
    }
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(name);
        buffer.writeDouble(hourly);
        buffer.writeDouble(daily);
        buffer.writeDouble(weekly);
        buffer.writeDouble(monthly);
        buffer.writeDouble(yearly);
        buffer.writeDouble(total);
        buffer.writeLong(timestamp);
        return true;
    }
    Statistics.Entry duplicate(){
        return new SystemStatisticsEntry(this);
    }

    @Override
    public JsonObject toJson() {
        JsonObject resp = new JsonObject();
        resp.addProperty("name",name);
        resp.addProperty("hourly",hourly);
        resp.addProperty("daily",daily);
        resp.addProperty("weekly",weekly);
        resp.addProperty("monthly",monthly);
        resp.addProperty("yearly",yearly);
        resp.addProperty("total",total);
        resp.addProperty("timestamp",timestamp);
        return resp;
    }
}
