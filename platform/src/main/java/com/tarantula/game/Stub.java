package com.tarantula.game;

import com.google.gson.JsonObject;
import com.icodesoftware.Connection;
import com.icodesoftware.Recoverable;
import com.icodesoftware.Tournament;
import com.tarantula.platform.AssociateKey;

import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.statistics.StatsDelta;

import java.time.format.DateTimeFormatter;
import java.util.Map;


public class Stub extends ResponseHeader {

    public boolean joined;

    public GameRoom room;
    public boolean offline;
    public String serverKey;
    public String ticket;
    public String roomId;
    public int seat;
    public String tag;
    public boolean tournamentEnabled;
    public Arena arena;
    public int rank; //rank of game 1 basis
    public double pxp; //percentage of game xp 100 basis
    public int rankUpBase;
    public int levelUpBase;
    public StatsDelta stats;
    public Rating rating;
    public Connection connection;
    public Tournament.Instance instance; //

    public Stub(){}

    public JsonObject toJson(){
        JsonObject jo = new JsonObject();
        jo.addProperty("successful",successful);
        if(!successful){
            jo.addProperty("message",message);
            return jo;
        }
        jo.addProperty("owner",owner);
        jo.addProperty("seat",seat);
        if(room!=null){
            jo.add("room",room.toJson());
        }
        if(arena!=null){
            jo.add("arena", arena.toJson());
        }
        jo.addProperty("tag",tag);
        jo.addProperty("tournamentEnabled",tournamentEnabled);
        jo.addProperty("offline",offline);
        if(ticket!=null){
            jo.addProperty("ticket",ticket);
        }
        if(serverKey!=null){
            jo.addProperty("serverKey",serverKey);
        }
        if(instance!=null){
            JsonObject jp = new JsonObject();
            jp.addProperty("id",instance.distributionKey());
            jp.addProperty("maxEntries",instance.maxEntries());
            jp.addProperty("startTime",instance.startTime().format(DateTimeFormatter.ISO_DATE_TIME));
            jp.addProperty("closeTime",instance.closeTime().format(DateTimeFormatter.ISO_DATE_TIME));
            jp.addProperty("endTime",instance.endTime().format(DateTimeFormatter.ISO_DATE_TIME));
            jo.add("tournament",jp);
        }
        if(connection!=null){
            JsonObject jp = new JsonObject();
            jp.addProperty("type",connection.type());
            jp.addProperty("serverId",connection.serverId());
            jp.addProperty("secured",connection.secured());
            jp.addProperty("connectionId",connection.connectionId());
            jp.addProperty("host",connection.host());
            jp.addProperty("port",connection.port());
            jo.add("connection",jp);
        }
        return jo;
    }
    @Override
    public Map<String,Object> toMap(){
        properties.put("joined",joined);
        properties.put("roomId",roomId);

        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        joined = (boolean)properties.getOrDefault("joined",false);
        roomId = (String)properties.getOrDefault("roomId",null);
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
