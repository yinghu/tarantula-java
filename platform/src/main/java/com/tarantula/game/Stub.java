package com.tarantula.game;

import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.protocol.Channel;
import com.tarantula.platform.inbox.Inbox;
import com.tarantula.platform.presence.DailyLoginTrack;
import com.tarantula.platform.room.GameRoom;
import com.tarantula.platform.store.Shop;

import java.util.Map;

//per stub/game by playerId + lobby tag
public class Stub extends PlayerGameObject {

    public boolean joined;
    public boolean offline;
    public String zoneId;
    public String roomId;
    //public String tournamentId;
    public Shop shop;

    public GameRoom room;
    public Tournament.Instance tournament;
    public GameZone zone;
    //public String tag;
    public Rating rating;
    public DailyLoginTrack dailyLogin;
    public Statistics statistics;
    public Inbox inbox;
    public Channel pushChannel;
    public PlayerSavedGames playerSavedGames;

    public Stub(){
    }

    public Stub(String error){
        this.joined = false;
        this.message = error;
    }

    public JsonObject toJson(){
        JsonObject jo = new JsonObject();
        jo.addProperty("Successful",joined);
        if(!joined){
            jo.addProperty("Message",message==null?"failed to join":message);
            return jo;
        }
        jo.add("_zone",zone.toJson());
        jo.add("_arena", room.arena().toJson());
        jo.add("_room",room.toJson());
        if(shop!=null) jo.add("_shop",shop.toJson());
        if(tournament!=null) jo.add("_tournament",tournament.toJson());
        if(pushChannel!=null) jo.add("_pushChannel",pushChannel.toJson());
        if(inbox!=null) jo.add("_inbox",inbox.toJson());
        if(playerSavedGames!=null) jo.add("_playerSavedGames", playerSavedGames.toJson());
        jo.addProperty("Tag",tag);
        jo.addProperty("TournamentEnabled",tournament!=null);
        jo.addProperty("PlayMode",zone.playMode());
        jo.addProperty("Ticket",ticket);
        return jo;
    }
    @Override
    public Map<String,Object> toMap(){
        properties.put("joined",joined);
        properties.put("zoneId",zoneId);
        properties.put("roomId",roomId);
        properties.put("tournamentId",tournamentId);
        properties.put("trackId",trackId); //tournament instance id
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        joined = (boolean)properties.getOrDefault("joined",false);
        zoneId = ((String)properties.getOrDefault("zoneId",null));
        roomId = ((String)properties.getOrDefault("roomId",null));
        tournamentId = ((String)properties.getOrDefault("tournamentId",null));
        trackId = ((String)properties.getOrDefault("trackId",null));
    }
    @Override
    public Recoverable.Key key(){
        return new StubKey(this.bucket,this.oid,label,stub);
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
        //if(klist.length==3) this.label = klist[2];
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
        return key().toString();
    }

    @Override
    public void write(Session.Header messageHeader,byte[] payload){
        if(pushChannel == null) return;
        pushChannel.write(messageHeader,payload);
    }
}
