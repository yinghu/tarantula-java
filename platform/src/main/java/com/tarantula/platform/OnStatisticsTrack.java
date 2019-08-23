package com.tarantula.platform;


import com.tarantula.OnStatistics;
import com.tarantula.Statistics;
import com.tarantula.platform.leveling.LevelingPortableRegistry;

import java.util.Map;

/**
 * Updated by yinghu lu on 8/23/19.
 */
public class OnStatisticsTrack extends OnApplicationHeader implements OnStatistics {

    private double xpDelta;
    private Statistics.Entry[] entryList;

    public OnStatisticsTrack(){}
    public OnStatisticsTrack(String name,String systemId){
        this.name = name;
        this.owner = systemId;
    }
    public double xpDelta() {
        return this.xpDelta;
    }


    public void xpDelta(double delta) {
        this.xpDelta = delta;
    }

    public Statistics.Entry[] entryList(){
        return entryList;
    }
    public void entryList(Statistics.Entry[] entryList){
        this.entryList = entryList;
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("owner",this.owner);
        this.properties.put("name",this.name);
        this.properties.put("xpDelta",this.xpDelta);
        this.properties.put("size",entryList.length);
        for(int i=0;i<entryList.length;i++){
            this.properties.put("n"+i,entryList[0].name());
            this.properties.put("v"+i,entryList[0].value());
        }
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.owner = (String)properties.get("owner");
        this.name = (String)properties.get("name");
        this.xpDelta = ((Number)properties.get("xpDelta")).doubleValue();
        int sz = ((Number)properties.get("size")).intValue();
        entryList = new Statistics.Entry[sz];
        for(int i=0;i<sz;i++){
            String n = (String)properties.get("n"+i);
            double v = ((Number)properties.get("v"+i)).doubleValue();
            entryList[i]=new StatisticsEntry(n,v);
        }
    }
    @Override
    public void distributionKey(String distributionKey) {
        //no key
    }

    @Override
    public int getFactoryId() {
        return LevelingPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return LevelingPortableRegistry.ON_STATS_CID;
    }
    @Override
    public String toString(){
        return "["+owner+"] on ["+name+"/"+xpDelta+"]";
    }
}
