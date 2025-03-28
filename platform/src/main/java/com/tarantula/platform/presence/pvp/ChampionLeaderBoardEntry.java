package com.tarantula.platform.presence.pvp;

import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.time.format.DateTimeFormatter;

public class ChampionLeaderBoardEntry extends RecoverableObject {

    public static final String LABEL = "champion_ldb_entry";

    public long playerId;
    public int elo;

    public ChampionLeaderBoardEntry(){
        this.label = LABEL;
        this.onEdge = true;
    }

    private ChampionLeaderBoardEntry(long playerId,int elo,long timestamp){
        this();
        this.playerId = playerId;
        this.elo = elo;
        this.timestamp = timestamp;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeLong(playerId);
        buffer.writeInt(elo);
        buffer.writeLong(timestamp);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        playerId = buffer.readLong();
        elo = buffer.readInt();
        this.timestamp = buffer.readLong();
        return true;
    }

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PresencePortableRegistry.CHAMPION_LEADER_BOARD_ENTRY_CID;
    }


    public ChampionLeaderBoardEntry duplicate(){
        return new ChampionLeaderBoardEntry(this.playerId,this.elo,this.timestamp);
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("playerId",playerId);
        jsonObject.addProperty("elo",elo);
        jsonObject.addProperty("lastUpdated", TimeUtil.fromUTCMilliseconds(timestamp).format(DateTimeFormatter.ISO_DATE_TIME));
        return jsonObject;
    }
}
