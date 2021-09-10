package com.tarantula.platform.tournament;

import com.tarantula.platform.RoomRegistry;

import java.util.Map;

public class TournamentRegistry extends RoomRegistry {


    public TournamentRegistry(){
        super();
    }
    public TournamentRegistry(int maxSize){
        super(maxSize);
    }

    @Override
    public Map<String,Object> toMap(){
        return super.toMap();
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        super.fromMap(properties);
    }
    public int getFactoryId() {
        return TournamentPortableRegistry.OID;
    }
    public int getClassId() {
        return TournamentPortableRegistry.TOURNAMENT_REGISTRY_CID;
    }

}
