package com.perfectday.games.earth8.data;

import com.google.gson.JsonObject;
import com.icodesoftware.DataStore;
import com.icodesoftware.protocol.GameContext;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.SnowflakeKey;
import com.perfectday.games.earth8.Earth8PortableRegistry;

public class PlayerDataTrack extends RecoverableObject {

    public static final String DATA_STORE = "player_data_track";

    public static final String LABEL = "data_track_index";

    public enum Type {Tournament,Analytics}

    public Type trackType;
    public long trackId;


    public PlayerDataTrack(){
        this.onEdge = true;
        this.label = LABEL;
    }

    private PlayerDataTrack(Type type,long trackId){
        this();
        this.trackType = type;
        this.trackId = trackId;
    }

    public PlayerDataTrack(long trackId){
        this(Type.Tournament,trackId);
    }

    @Override
    public int getFactoryId() {
        return Earth8PortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return Earth8PortableRegistry.PLAYER_DATA_TRACK_CID;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeInt(trackType.ordinal());
        buffer.writeLong(trackId);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        trackType = Type.values()[buffer.readInt()];
        trackId = buffer.readLong();
        return true;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("TrackType",trackType.name());
        jsonObject.addProperty("TrackId",trackId);
        return jsonObject;
    }


    public static PlayerDataTrack lookup(GameContext gameContext,long systemId,Type trackType){
        DataStore dataStore = gameContext.applicationSchema().applicationPreSetup().onDataStore(DATA_STORE);
        PlayerDataTrack[] pending = {null};
        dataStore.list(new PlayerDataTrackQuery(systemId),playerDataTrack -> {
            if(playerDataTrack.trackType == trackType){
                pending[0]=playerDataTrack;
                pending[0].dataStore = dataStore;
                return false;
            }
            return true;
        });
        if(pending[0]!=null) return pending[0];
        pending[0] = new PlayerDataTrack(trackType,0);
        pending[0].ownerKey(SnowflakeKey.from(systemId));
        dataStore.create(pending[0]);
        pending[0].dataStore = dataStore;
        return pending[0];
    }

}
