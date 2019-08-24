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


    private String leaderBoardHeader ;

    private Map<String,Entry> mappings = new ConcurrentHashMap<>();

    public DeltaStatistics(){
        this.vertex = "Statistics";
        this.label = "STAT";
    }

    public String leaderBoardHeader() {
        return leaderBoardHeader;
    }


    public void leaderBoardHeader(String header) {
        this.leaderBoardHeader = header;
    }

    public void entry(Entry entry){
        this.mappings.put(entry.name(),entry);
    }
    public OnStatistics value(String key, double value) {
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
        return new OnStatisticsTrack(this.leaderBoardHeader);
    }
    public Map<String,Double> list(){
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
        properties.put("leaderBoardHeader",this.leaderBoardHeader);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.leaderBoardHeader = (String)properties.get("leaderBoardHeader");
    }
    @Override
    public Key key(){
        return new AssociateKey(this.bucket,this.oid,this.vertex);
    }
    @Override
    public String toString(){
        return "On Statistics ["+this.vertex+"]";
    }
    public void onUpdate(){
        this.dataStore.update(this);
        mappings.forEach((k,v)->{
            //v.distributable(this.distributable);
            //v.index(SystemUtil.toString(new String[]{v.owner(),v.label()}));
            dataStore.update(v);
        });
    }

}
