package com.tarantula.platform;
import com.tarantula.*;
import com.tarantula.platform.service.cluster.PortableRegistry;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Updated by yinghu on 8/23/19.
 */
public class DeltaStatistics extends OnApplicationHeader implements Statistics {

    private Map<String,Entry> mappings = new ConcurrentHashMap<>();

    public DeltaStatistics(){
        this.vertex = "Statistics";
        this.label = "STAT";
    }

    public void entry(Entry entry){
        this.mappings.put(entry.name(),entry);
    }
    public void value(String key, double value) {
        this.mappings.compute(key,(k,v)->{
            if(v==null){
                v = new StatisticsEntry(key);
                v.owner(this.key().asString());
                dataStore.create(v);
            }
            if(value>0){//skip query value with 0 value
                v.value(value);
                dataStore.update(v);
            }
            return v;
        });
    }
    public Map<String,Double> summary(){
        Map<String,Double> _mv = new HashMap<>();
        this.mappings.forEach((k,v)->{
            _mv.put(k,v.value());
        });
        return _mv;
    }
    @Override
    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableRegistry.DELTA_STAT_CID;
    }

    @Override
    public Map<String,Object> toMap(){

        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){

    }
    @Override
    public Key key(){
        return new AssociateKey(this.bucket,this.oid,this.vertex);
    }
    @Override
    public String toString(){
        return "On Statistics ["+this.vertex+"]";
    }

    public void dataStore(DataStore dataStore){
        this.dataStore = dataStore;
    }
    public void update(){
        this.dataStore.update(this);
        mappings.forEach((k,v)->{
            dataStore.update(v);
        });
    }

}
