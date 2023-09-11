package com.tarantula.platform.presence.saves;

import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.icodesoftware.Session;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.time.LocalDateTime;

public class SavedGame extends RecoverableObject implements Configurable {

    //index -- device id
    //version -- game latest update mark

    public final static String USER_SAVE = "save";

    public int version;
    public long stub;

    private boolean loaded;

    public SavedGame(){
        this.onEdge = true;
        this.label = USER_SAVE;
    }
    public SavedGame(String owner,String saveName){
        this.owner = owner;
        this.name = saveName;
    }

    public boolean read(DataBuffer buffer){
        this.name = buffer.readUTF8();
        this.version = buffer.readInt();
        this.timestamp = buffer.readLong();
        this.stub = buffer.readLong();
        return true;
    }
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(name);
        buffer.writeInt(version);
        buffer.writeLong(timestamp);
        buffer.writeLong(stub);
        return true;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.SAVED_GAME_CID;
    }

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("GameId",this.distributionKey());
        jsonObject.addProperty("SaveName",name);
        jsonObject.addProperty("Version",version);
        jsonObject.addProperty("Timestamp",Long.toString(timestamp));
        jsonObject.addProperty("Stub",Long.toString(stub));
        return jsonObject;
    }

    @Override
    public boolean equals(Object obj){
        SavedGame savedGame =(SavedGame) obj;
        return savedGame.distributionId()==(this.distributionId());
    }
    @Override
    public int hashCode(){
        return Long.hashCode(distributionId);
    }

    @Override
    public void update(){
        this.dataStore.update(this);
    }

    public void load(){
        if(loaded) return;
        loaded = true;
        this.dataStore.load(this);
    }
    public boolean onSession(Session session){
        if(stub==0){
            //stub = session.stub();
            timestamp = TimeUtil.toUTCMilliseconds(LocalDateTime.now());
            this.update();
            return true;
        }
        return stub == session.stub();
    }
    public void offSession(Session session){
        if(stub != session.stub()) return;
        stub = 0;
        timestamp = TimeUtil.toUTCMilliseconds(LocalDateTime.now());
        this.update();
    }

    public void expireSession(int expired){
        if(stub != expired) return;
        stub = 0;
        timestamp = TimeUtil.toUTCMilliseconds(LocalDateTime.now());
        this.update();
    }

}
