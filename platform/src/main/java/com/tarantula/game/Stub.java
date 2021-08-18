package com.tarantula.game;

import com.google.gson.JsonObject;
import com.icodesoftware.Recoverable;
import com.icodesoftware.Statistics;
import com.icodesoftware.Tournament;
import com.tarantula.platform.AssociateKey;

import java.util.Map;

//per stub/game by playerId + lobby tag
public class Stub extends PlayerGameObject {

    public boolean joined;

    public GameZone zone;
    public Arena arena;
    public GameRoom room;
    public Tournament.Instance tournament; //

    public boolean offline;
    public String tag;
    public boolean tournamentEnabled;

    public Rating rating;
    public Statistics statistics;

    public Stub(){
    }

    public JsonObject toJson(){
        JsonObject jo = new JsonObject();
        jo.addProperty("successful",joined);
        if(!joined){
            jo.addProperty("message","failed to join");
            return jo;
        }
        if(zone!=null){
            jo.add("zone",zone.toJson());
        }
        if(arena!=null){
            jo.add("arena", arena.toJson());
        }
        if(room!=null){
            jo.add("room",room.toJson());
        }
        if(tournament!=null){
            jo.add("tournament",tournament.toJson());
        }
        if(rating!=null){
            jo.add("rating",rating.toJson());
        }
        jo.addProperty("tag",tag);
        jo.addProperty("tournamentEnabled",tournamentEnabled);
        jo.addProperty("offline",offline);
        return jo;
    }
    @Override
    public Map<String,Object> toMap(){
        properties.put("joined",joined);
        properties.put("roomId",room!=null?room.roomId():null);
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        joined = (boolean)properties.getOrDefault("joined",false);
        GameRoom room = new GameRoom();
        room.distributionKey((String)properties.getOrDefault("roomId",null));
    }
    @Override
    public Recoverable.Key key(){
        return new AssociateKey(this.bucket,this.oid,label);
    }
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
        if(klist.length==3) this.label = klist[2];
    }
    @Override
    public int getFactoryId() {
        return GamePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return GamePortableRegistry.STUB_CID;
    }

    @Override
    public String toString(){
        return label+"=>"+super.toString();
    }
}
