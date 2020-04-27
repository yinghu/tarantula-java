package com.tarantula.platform;


import com.tarantula.OnStatistics;
import com.tarantula.Statistics;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Updated by yinghu lu on 8/23/19.
 */
public class OnStatisticsTrack extends OnApplicationHeader implements OnStatistics {

    private double xpDelta;
    private HashMap<String, Statistics.Entry> eMap = new HashMap<>();

    public OnStatisticsTrack(){}
    public OnStatisticsTrack(String name){
        this.name = name;
    }
    public double xpDelta() {
        return this.xpDelta;
    }


    public void xpDelta(double delta) {
        this.xpDelta = delta;
    }

    public List<Statistics.Entry> entryList(){
        List<Statistics.Entry> elist = new ArrayList<>();
        eMap.forEach((k,v)->elist.add(v));
        return elist;
    }
    public void onEntry(String name,double value){
        Statistics.Entry e = eMap.computeIfAbsent(name,(k)-> new StatisticsEntry(name));
        e.value(value);
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("owner",this.owner);
        this.properties.put("name",this.name);
        this.properties.put("xpDelta",this.xpDelta);
        final int[] i={0};
        eMap.forEach((k,v)->{
            this.properties.put("n"+i[0],k);
            this.properties.put("v"+i[0],v.value());
            i[0]++;
        });
        this.properties.put("size",i[0]);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.owner = (String)properties.get("owner");
        this.name = (String)properties.get("name");
        this.xpDelta = ((Number)properties.get("xpDelta")).doubleValue();
        int sz = ((Number)properties.get("size")).intValue();
        for(int i=0;i<sz;i++){
            String n = (String)properties.get("n"+i);
            double v = ((Number)properties.get("v"+i)).doubleValue();
            eMap.computeIfAbsent(n,(k)->new StatisticsEntry(n)).value(v);
        }
    }
    @Override
    public void distributionKey(String distributionKey) {
        //no key
    }

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PresencePortableRegistry.ON_STATISTICS_CID;
    }
    @Override
    public String toString(){
        return "["+owner+"] on ["+name+"/"+xpDelta+"]";
    }
}
