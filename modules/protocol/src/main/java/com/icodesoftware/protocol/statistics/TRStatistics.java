package com.icodesoftware.protocol.statistics;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Statistics;
import com.icodesoftware.protocol.ProtocolPortableRegistry;
import com.icodesoftware.util.OnApplicationHeader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class TRStatistics extends OnApplicationHeader implements Statistics {

    private Map<String,StatisticsEntry> mappings = new ConcurrentHashMap<>();

    private Listener listener;


    public TRStatistics(){

    }

    public void registerListener(Listener listener){
        this.listener = listener;
    }

    public Entry entry(String ename) {
        StatisticsEntry entry = this.mappings.computeIfAbsent(ename,(k)->{
            //new entry
            StatisticsEntry se = new StatisticsEntry(this.key(),ename);
            this.dataStore.create(se);
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
            elist.add(v.duplicate());
        });
        return elist;
    }
    //original streaming
    public void summary(Stream query){
        mappings.forEach((k,v)->{
            query.onEntry(v);
        });
    }
    @Override
    public int getFactoryId() {
        return ProtocolPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return ProtocolPortableRegistry.STATISTICS_CID;
    }


    @Override
    public JsonObject toJson() {
        JsonObject jo  = new JsonObject();
        JsonArray ja = new JsonArray();
        for(Entry entry : summary()){
            ja.add(entry.toJson());
        }
        jo.addProperty("Successful",true);
        jo.add("_categories",ja);
        return jo;
    }

    public void load(){
        dataStore.list(new StatisticsEntryQuery(this.distributionId),e->{
            e.dataStore(dataStore);
            mappings.put(e.name(),e);
            return true;
        });
    }
}
