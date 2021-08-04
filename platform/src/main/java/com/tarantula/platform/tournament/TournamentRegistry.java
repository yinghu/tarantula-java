package com.tarantula.platform.tournament;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.AssociateKey;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TournamentRegistry extends RecoverableObject {

    private Set<String> players = new HashSet<>();
    private int maxSize;

    public TournamentRegistry(){
        this.label = "register";
    }
    public TournamentRegistry(int maxSize){
        this();
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
    //@Override
    //public Recoverable.Key key(){
        //return new AssociateKey(this.bucket,this.oid,this.label);
    //}
    @Override
    public String distributionKey() {
        if(this.bucket!=null&&this.oid!=null){
            return new StringBuffer(this.bucket).append(Recoverable.PATH_SEPARATOR).append(oid).append(Recoverable.PATH_SEPARATOR).append(label).toString();
        }
        else{
            return null;
        }
    }
    @Override
    public void distributionKey(String distributionKey) {
        String[] klist = distributionKey.split(Recoverable.PATH_SEPARATOR);
        this.bucket = klist[0];
        this.oid = klist[1];
    }
    public String tournamentInstanceId(){
        return new StringBuffer(this.bucket).append(Recoverable.PATH_SEPARATOR).append(oid).toString();
    }
}
