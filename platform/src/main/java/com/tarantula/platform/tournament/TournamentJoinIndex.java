package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;
import com.tarantula.platform.AssociateKey;


import java.time.LocalDateTime;
import java.util.Map;

public class TournamentJoinIndex extends TournamentInstance{

    public TournamentJoinIndex(){
        this.onEdge = true;
        this.label = "TJI";
    }
    public TournamentJoinIndex(int maxEntries,LocalDateTime start,LocalDateTime close,LocalDateTime end){
        this();
        this.maxEntries = maxEntries;
        this.start =start;
        this.close = close;
        this.end = end;
    }
    @Override
    public String id() {
        return distributionKey();
    }


    @Override
    public void update(String s, Tournament.OnEntry onEntry) {

    }
    @Override
    public Map<String,Object> toMap(){
        super.toMap();
        entryIndex.forEach((k,v)->{
            properties.put(k,"_e");
        });
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        super.fromMap(properties);
        properties.forEach((k,v)->{
            if(v.toString().equals("_e")){
                entryIndex.put(k,new TournamentEntry());
            }
        });
    }


    public Key key(){
        return new AssociateKey(this.bucket,this.oid,this.label);
    }
    @Override
    public int getFactoryId() {
        return TournamentPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return TournamentPortableRegistry.TOURNAMENT_JOIN_INDEX_CID;
    }

}
