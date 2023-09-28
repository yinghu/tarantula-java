package com.tarantula.game;

import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.protocol.Channel;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.room.GameRoom;

import java.time.LocalDateTime;


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
        jo.add("_zone",zone!=null?zone.toJson():new JsonObject());
        jo.add("_arena",room!=null?room.arena().toJson():new JsonObject());
        jo.add("_room",room!=null?room.toJson():new JsonObject());
        if(tournament!=null) jo.add("_tournament",tournament.toJson());
        if(pushChannel!=null) jo.add("_pushChannel",pushChannel.toJson());
        jo.addProperty("Tag",tag);
        jo.addProperty("TournamentEnabled",tournament!=null);
        jo.addProperty("PlayMode",zone!=null?zone.playMode():null);
        jo.addProperty("Ticket",ticket);
        jo.addProperty("DistributionId",distributionId);
        return jo;
    }


    public boolean read(DataBuffer buffer){
        this.joined = buffer.readBoolean();
        this.zoneId = buffer.readUTF8();
        this.roomId = buffer.readUTF8();
        this.tournamentId = buffer.readUTF8();
        this.trackId = buffer.readUTF8();
        this.timestamp = buffer.readLong();
        this.systemId = Long.toString(buffer.readLong());
        return true;
    }
    public boolean write(DataBuffer buffer) {
        buffer.writeBoolean(joined);
        buffer.writeUTF8(zoneId);
        buffer.writeUTF8(roomId);
        buffer.writeUTF8(tournamentId);
        buffer.writeUTF8(trackId);
        buffer.writeLong(timestamp);
        buffer.writeLong(Long.parseLong(systemId));
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
