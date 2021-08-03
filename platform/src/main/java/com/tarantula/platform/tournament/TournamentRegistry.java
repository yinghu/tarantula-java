package com.tarantula.platform.tournament;

import com.icodesoftware.util.RecoverableObject;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TournamentRegistry extends RecoverableObject {

    private Set<String> players = new HashSet<>();
    private int maxSize;

    public TournamentRegistry(){}
    public TournamentRegistry(int maxSize){
        this.maxSize = maxSize;
    }
    public void addPlayer(String systemId){
        synchronized (players){
            players.add(systemId);
        }
    }

    @Override
    public Map<String,Object> toMap(){
        players.forEach((k)->{
            properties.put(k,"1");
        });
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        properties.forEach((k,v)->{
            players.add(k);
        });
    }
    public int getFactoryId() {
        return TournamentPortableRegistry.OID;
    }

    public int getClassId() {
        return TournamentPortableRegistry.TOURNAMENT_REGISTRY_CID;
    }
}
