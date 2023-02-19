package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;
import com.icodesoftware.util.FIFOBuffer;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.AssociateKey;

import java.util.*;

public class PlayerTournamentHistory extends RecoverableObject {

    private FIFOBuffer<String> buffer;
    private int size;
    public PlayerTournamentHistory(){
        this.label = Tournament.HISTORY_LABEL;
    }
    public PlayerTournamentHistory(int maxHistoryRecords){
        this();
        this.size = maxHistoryRecords;
        this.buffer = new FIFOBuffer<>(this.size,new String[size]);
    }

    @Override
    public int getFactoryId() {
        return TournamentPortableRegistry.OID;
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("size",size);
        int[] i= {0};
        buffer.list(new ArrayList<>(size)).forEach(k->{
            properties.put("k"+i,k);
            i[0]++;
        });
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
       this.size = ((Number)properties.get("size")).intValue();
       this.buffer = new FIFOBuffer<>(size,new String[size]);
       int[] i={0};
       properties.forEach((k,v)->{
           String pv = (String)properties.get("k"+i);
           if(pv!=null) buffer.push(pv);
       });
    }

    @Override
    public int getClassId() {
        return TournamentPortableRegistry.PLAYER_TOURNAMENT_HISTORY_CID;
    }

    @Override
    public Key key(){
        return new AssociateKey(this.bucket,this.oid,this.label);
    }

    public void addKey(String key){
        buffer.push(key);
    }
    public List<String> keySet(){
        return buffer.list(new ArrayList<>(size));
    }
    public void reload(){
        this.dataStore.load(this);
    }

}
