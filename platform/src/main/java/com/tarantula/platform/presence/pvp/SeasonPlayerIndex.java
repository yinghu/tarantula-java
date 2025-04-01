package com.tarantula.platform.presence.pvp;

import com.icodesoftware.util.LongCompositeKey;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class SeasonPlayerIndex extends RecoverableObject {

    public static final String LABEL = "season_player";

    public long playerId;
    public long seasonId;

    public int elo;
    public boolean onSeason;

    public SeasonPlayerIndex(){
        this.label = LABEL;
        this.onEdge = true;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeLong(timestamp);
        try{
            buffer.writeBoolean(onSeason);
        }catch (Exception ex){

        }
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        this.timestamp = buffer.readLong();
        try{
            onSeason = buffer.readBoolean();
        }catch (Exception ex){

        }
        return true;
    }

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PresencePortableRegistry.SEASON_PLAYER_INDEX_CID;
    }

    @Override
    public boolean readKey(DataBuffer buffer) {
        playerId = buffer.readLong();
        seasonId = buffer.readLong();
        return true;
    }

    @Override
    public boolean writeKey(DataBuffer buffer) {
        if(playerId==0 || seasonId ==0) return false;
        buffer.writeLong(playerId);
        buffer.writeLong(seasonId);
        return true;
    }

    @Override
    public Key key() {
        return new LongCompositeKey(playerId,seasonId);
    }
}
