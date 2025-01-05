package com.tarantula.platform.service.metrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.util.BufferProxy;
import com.icodesoftware.util.FIFOBuffer;
import com.icodesoftware.util.RecoverableObject;


import java.time.LocalDateTime;


public class MetricsSnapshotRequest extends RecoverableObject{

    public String name;
    public String category;
    public String classifier;
    public LocalDateTime endTime;
    private JsonArray metrics;
    private boolean loaded;
    public boolean archived;

    public FIFOBuffer<LocalDateTime> lastViewed = new FIFOBuffer<>(2,new LocalDateTime[2]);
    private Runnable stop;
    public MetricsSnapshotRequest(String name,String category,String classifier,LocalDateTime endTime,Runnable runnable){
        this.name = name;
        this.category = category;
        this.classifier = classifier;
        this.endTime = endTime;
        this.archived = this.endTime != null;
        LocalDateTime init = LocalDateTime.now();
        for(int i=0;i<2;i++){
            lastViewed.push(init);
        }
        this.stop = runnable;
    }

    public synchronized void reset(){
        metrics = new JsonArray();
        loaded = false;
    }
    public synchronized void snapshot(JsonObject snapshot){
        metrics.add(snapshot);
    }
    public synchronized void loaded(){
        loaded = true;
    }

    @Override
    public synchronized JsonObject toJson() {
        lastViewed.push(LocalDateTime.now());
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",loaded);
        if(!loaded) return jsonObject;
        jsonObject.add("metrics",metrics);
        return jsonObject;
    }

    public String toString(){
        return (name+"_"+category+"_"+classifier+"_"+archived);
    }
    @Override
    public void fromBinary(byte[] payload) {
        JsonObject snapshot = new JsonObject();
        DataBuffer dataBuffer = BufferProxy.wrap(payload);
        snapshot.addProperty("memberId",dataBuffer.readUTF8());
        int sz = dataBuffer.readInt();
        JsonArray data = new JsonArray();
        for(int i=0;i<sz;i++){
            JsonObject m = new JsonObject();
            m.addProperty("x",dataBuffer.readUTF8());
            m.addProperty("y",dataBuffer.readDouble());
            data.add(m);
        }
        snapshot.add("data",data);
        snapshot(snapshot);
    }

    public void stop(){
        stop.run();
    }

    public static String queryId(String name,String category,String classifier,boolean archived){
        return (name+"_"+category+"_"+classifier+"_"+archived);
    }

}
