package com.tarantula.game;

import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.inbox.Inbox;
import com.tarantula.platform.item.Category;
import com.tarantula.platform.presence.DailyLoginTrack;
import com.tarantula.platform.room.GameRoom;
import com.tarantula.platform.store.Shop;
import com.tarantula.platform.store.ShoppingItem;

import java.util.List;
import java.util.Map;

//per stub/game by playerId + lobby tag
public class Stub extends PlayerGameObject {

    public boolean joined;
    public boolean offline;
    public String roomId;
    public String ticket;
    public Shop shop;

    public GameRoom room;
    public Tournament.Instance tournament;
    public GameZone zone;
    public String tag;
    public Rating rating;
    public DailyLoginTrack dailyLogin;
    public Statistics statistics;
    public Inbox inbox;
    public Channel pushChannel;

    public Stub(){
    }

    public JsonObject toJson(){
        JsonObject jo = new JsonObject();
        jo.addProperty("successful",joined);
        if(!joined){
            jo.addProperty("message","failed to join");
            return jo;
        }
        jo.add("zone",zone.toJson());
        jo.add("arena", room.arena().toJson());
        jo.add("room",room.toJson());
        jo.add("rating",rating.toJson());

        if(dailyLogin!=null) jo.add("dailyLogin",dailyLogin.toJson());
        if(shop!=null) jo.add("shop",shop.toJson());
        if(tournament!=null) jo.add("tournament",tournament.toJson());
        if(pushChannel!=null) jo.add("pushChannel",pushChannel.toJson());
        if(inbox!=null) jo.add("inbox",inbox.toJson());
        jo.addProperty("tag",tag);
        jo.addProperty("tournamentEnabled",tournament!=null);
        jo.addProperty("offline",offline);
        jo.addProperty("ticket",ticket);
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
        roomId = ((String)properties.getOrDefault("roomId",null));
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
