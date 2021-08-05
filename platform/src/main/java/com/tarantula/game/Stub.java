package com.tarantula.game;

import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Connection;
import com.icodesoftware.Consumable;
import com.icodesoftware.Tournament;
import com.icodesoftware.protocol.DataBuffer;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.event.PortableEventRegistry;
import com.tarantula.platform.statistics.StatsDelta;

import java.io.IOException;
import java.time.format.DateTimeFormatter;


public class Stub extends ResponseHeader implements Portable {

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
    public Consumable consumable;
    /**
     * pxp - performance xp percentage on 100 base points pxp*(100) 0.7*100 = 70 0.3*100 = 30
     * rank - final result 1,2 rank xp = (1/rank)*100  1 - 100 2 50 ..
     * xp-delta = (1/rank)*(100)+pxp*(100)+csw*(100); //cws only if last is cws
     * zxp = zxp +xp-delta
     * xp = xp + xp-delta
      */

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
            jp.addProperty("id",instance.id());
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
        if(consumable!=null){
            jo.add("configurations",consumable.toJson());
        }
        return jo;
    }

    @Override
    public byte[] toBinary(){
        DataBuffer dataBuffer = new DataBuffer();
        dataBuffer.putInt(rank);
        dataBuffer.putDouble(pxp);
        dataBuffer.putInt(rankUpBase);
        dataBuffer.putInt(levelUpBase);
        return dataBuffer.toArray();
    }
    @Override
    public void fromBinary(byte[] payload){
        DataBuffer buffer = new DataBuffer(payload);
        this.rank = buffer.getInt();
        this.pxp = buffer.getDouble();
        this.rankUpBase = buffer.getInt();
        this.levelUpBase = buffer.getInt();
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PortableEventRegistry.GAME_STUB_CID;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeUTF("roomId",roomId);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        roomId = portableReader.readUTF("roomId");
    }
}
