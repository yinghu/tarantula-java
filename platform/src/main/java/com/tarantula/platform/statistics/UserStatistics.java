package com.tarantula.platform.statistics;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Statistics;
import com.icodesoftware.util.RecoverableObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class UserStatistics extends RecoverableObject implements Statistics {

    private Map<String,StatisticsEntry> mappings = new ConcurrentHashMap<>();

    private Listener listener;


    public UserStatistics(){

    }

    public void registerListener(Listener listener){
        this.listener = listener;
    }

    public Entry entry(String ename) {
        StatisticsEntry entry = this.mappings.computeIfAbsent(ename,(k)->{
            //new entry
            StatisticsEntry se = new StatisticsEntry(this.key(),ename);
            this.dataStore.create(se);
            this.dataStore.update(this);
            se.dataStore(this.dataStore);
            return se;
        });
        entry.listener(this.listener);
        return entry;
    }
    //memory copy list
    public List<Entry> summary(){
        ArrayList<Entry> elist = new ArrayList<>();
        mappings.forEach((k,v)->{
            //v.load();
            elist.add(v.duplicate());
        });
        return elist;
    }
    //original streaming
    public void summary(Stream query){
        mappings.forEach((k,v)->{
            //v.load();
            query.onEntry(v);
        });
    }
    @Override
    public int getFactoryId() {
        return StatisticsPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return StatisticsPortableRegistry.STATISTICS_CID;
    }


    @Override
    public JsonObject toJson() {
        JsonObject jo  = new JsonObject();
        JsonArray ja = new JsonArray();
        for(Statistics.Entry entry : summary()){
            ja.add(entry.toJson());
        }
        jo.addProperty("Successful",true);
        jo.add("_categories",ja);
        return jo;
    }

    public boolean read(DataBuffer buffer){
        //this.count = buffer.readInt();
        return true;
    }
    public boolean write(DataBuffer buffer) {
        //buffer.writeInt(count);
        return true;
    }

    public void load(){
        dataStore.list(new StatisticsEntryQuery(this.distributionId),e->{
            mappings.put(e.name(),e);
            return true;
        });
    }
}
