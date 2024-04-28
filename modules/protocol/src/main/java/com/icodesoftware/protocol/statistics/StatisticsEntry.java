package com.icodesoftware.protocol.statistics;

import com.google.gson.JsonObject;
import com.icodesoftware.Statistics;
import com.icodesoftware.protocol.ProtocolPortableRegistry;
import com.icodesoftware.util.OnApplicationHeader;
import com.icodesoftware.util.TimeUtil;

import java.time.LocalDateTime;


public class StatisticsEntry extends OnApplicationHeader implements Statistics.Entry {

    public static final String LABEL = "stats";
    //private String name;
    private double total=0;
    private double hourly = 0;
    private double daily=0;
    private double weekly=0;
    private double monthly=0;
    private double yearly=0;

    private Statistics.Listener listener;
    public StatisticsEntry(){
        this.onEdge = true;
        this.label = LABEL;
    }
    public StatisticsEntry(Key ownerKey, String name){
        this();
        this.ownerKey = ownerKey;
        this.name = name;
    }
    public StatisticsEntry(Statistics.Entry entry){
        this.name = entry.name();
        this.daily = entry.daily();
        this.weekly = entry.weekly();
        this.monthly = entry.monthly();
        this.yearly = entry.yearly();
        this.total = entry.total();
    }
    public void listener(Statistics.Listener listener){
        this.listener = listener;
    }
    @Override
    public String name() {
        return name;
    }
    @Override
    public double total() {
        return this.total;
    }

    @Override
    public double hourly() {
        return hourly;
    }


    @Override
    public double daily() {
        return daily;
    }

    @Override
    public double weekly() {
        return weekly;
    }

    @Override
    public double monthly() {
        return monthly;
    }

    @Override
    public double yearly() {
        return yearly;
    }

    @Override
    public synchronized Statistics.Entry update(double delta) {
        LocalDateTime lastUpdated = TimeUtil.fromUTCMilliseconds(timestamp);
        LocalDateTime _now = LocalDateTime.now();
        if(StatisticsUtil.validateDaily(lastUpdated,_now)){
            daily += delta;
            weekly += delta;
            monthly += delta;
            yearly += delta;
        }
        else{
            daily = delta; //daily reset reset daily
            if(_now.getDayOfWeek().getValue()==1){//weekly reset
                weekly = delta;
            }
            if(_now.getDayOfMonth() == 1){
                monthly = delta;
            }
            if(_now.getDayOfYear() == 1){
                yearly = delta;
            }
        }
        total += delta;
        if(listener==null) return this;
        listener.entryUpdated(this,delta);
        return this;
    }
    @Override
    public int getFactoryId() {
        return ProtocolPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return ProtocolPortableRegistry.STATISTICS_ENTRY_CID;
    }


    Statistics.Entry duplicate(){
        return new StatisticsEntry(this);
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Name",name);
        jsonObject.addProperty("Daily",daily);
        jsonObject.addProperty("Weekly",weekly);
        jsonObject.addProperty("Monthly",monthly);
        jsonObject.addProperty("Yearly",yearly);
        jsonObject.addProperty("Total",total);
        return jsonObject;
    }

    public boolean read(DataBuffer buffer){
        this.name = buffer.readUTF8();
        this.daily = buffer.readDouble();
        this.weekly = buffer.readDouble();
        this.monthly = buffer.readDouble();
        this.yearly = buffer.readDouble();
        this.total = buffer.readDouble();
        return true;
    }
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(name);
        buffer.writeDouble(daily);
        buffer.writeDouble(weekly);
        buffer.writeDouble(monthly);
        buffer.writeDouble(yearly);
        buffer.writeDouble(total);
        return true;
    }
}
