package com.tarantula.game;

import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.protocol.Channel;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.room.GameRoom;

import java.time.LocalDateTime;
import java.util.Map;

//per stub/game by playerId + lobby tag
public class Stub extends PlayerGameObject {

    public final static String LABEL = "stub";
    public boolean joined;
    public boolean offline;
    public String zoneId;
    public String roomId;

    public GameRoom room;
    public Tournament.Instance tournament;
    public GameZone zone;

    public Channel pushChannel;

    public Stub(){
        timestamp = TimeUtil.toUTCMilliseconds(LocalDateTime.now());
        this.onEdge = true;
        this.label = LABEL;
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
        if(tournament!=null) jo.add("_tournament",tournament.toJson());
        if(pushChannel!=null) jo.add("_pushChannel",pushChannel.toJson());
        jo.addProperty("Tag",tag);
        jo.addProperty("TournamentEnabled",tournament!=null);
        jo.addProperty("PlayMode",zone.playMode());
        jo.addProperty("Ticket",ticket);
        jo.addProperty("DistributionId",distributionId);
        return jo;
    }
    @Override
    public Map<String,Object> toMap(){
        properties.put("joined",joined);
        properties.put("zoneId",zoneId);
        properties.put("roomId",roomId);
        properties.put("tournamentId",tournamentId);
        properties.put("trackId",trackId); //tournament instance id
        properties.put("timestamp",timestamp);
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        joined = (boolean)properties.getOrDefault("joined",false);
        zoneId = ((String)properties.getOrDefault("zoneId",null));
        roomId = ((String)properties.getOrDefault("roomId",null));
        tournamentId = ((String)properties.getOrDefault("tournamentId",null));
        trackId = ((String)properties.getOrDefault("trackId",null));
        timestamp = ((Number)properties.getOrDefault("timestamp",0)).longValue();
    }

    public boolean read(DataBuffer buffer){
        this.joined = buffer.readBoolean();
        this.zoneId = buffer.readUTF8();
        this.roomId = buffer.readUTF8();
        this.tournamentId = buffer.readUTF8();
        this.trackId = buffer.readUTF8();
        this.timestamp = buffer.readLong();
        this.stub = buffer.readLong();
        return true;
    }
    public boolean write(DataBuffer buffer) {
        buffer.writeBoolean(joined);
        buffer.writeUTF8(zoneId);
        buffer.writeUTF8(roomId);
        buffer.writeUTF8(tournamentId);
        buffer.writeUTF8(trackId);
        buffer.writeLong(timestamp);
        buffer.writeLong(stub);
        return true;
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
