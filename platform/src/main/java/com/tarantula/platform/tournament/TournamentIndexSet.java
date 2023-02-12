package com.tarantula.platform.tournament;

import com.tarantula.platform.IndexSet;

import java.util.Map;

public class TournamentIndexSet extends IndexSet {

    private int totalInstanceCountRemaining;

    public TournamentIndexSet(String label,int maxInstanceCount){
        super(label);
        this.totalInstanceCountRemaining = maxInstanceCount;
    }

    public TournamentIndexSet(){
    }


    @Override
    public int getFactoryId() {
        return TournamentPortableRegistry.OID;
    }

    @Override
    public Map<String,Object> toMap(){
        properties.put("totalInstanceCountRemaining",totalInstanceCountRemaining);
        super.toMap();
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        totalInstanceCountRemaining = ((Number)properties.remove("totalInstanceCountRemaining")).intValue();
        super.fromMap(properties);
    }

    @Override
    public int getClassId() {
        return TournamentPortableRegistry.TOURNAMENT_INDEX_SET_CID;
    }

    public int remaining(){
        synchronized (keySet){
            return totalInstanceCountRemaining;
        }
    }

    @Override
    public boolean addKey(String key){
        synchronized (keySet){
            if(totalInstanceCountRemaining-1<0) return false;
            if(!keySet.add(key)) return false;
            totalInstanceCountRemaining--;
            return true;
        }
    }

}
