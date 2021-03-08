package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;
import com.tarantula.platform.AssociateKey;


import java.time.LocalDateTime;
import java.util.Map;

public class TournamentJoinIndex extends TournamentInstance{

    public TournamentJoinIndex(){

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
        StringBuffer elist = new StringBuffer();
        entryIndex.forEach((k,v)->{
            elist.append(",").append(k);
        });
        properties.put("entryList",elist.toString());
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        super.fromMap(properties);
        String elist = (String) properties.get("entryList");
        for(String e : elist.split(",")){
            if(e!=null&&(!e.equals(""))){
                entryIndex.put(e,new TournamentEntry(e,id()));
            }
        }
    }

    @Override
    public Tournament.Entry enter(String systemId) {
        Tournament.Entry _e = new TournamentEntry(systemId);
        this.entryIndex.putIfAbsent(_e.systemId(),_e);
        _e.owner(id());
        return _e;
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
