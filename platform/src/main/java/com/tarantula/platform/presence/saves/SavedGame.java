package com.tarantula.platform.presence.saves;

import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.icodesoftware.DataStore;
import com.icodesoftware.Session;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.time.LocalDateTime;
import java.util.List;

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


    public static List<SavedGame> list(Session session, DataStore dataStore,int saveSize){
        List<SavedGame> list = dataStore.list(new SavedGameQuery(session));
        if(list.size()==0){
            for(int i=0;i<saveSize;i++){
                SavedGame save = new SavedGame();
                save.name("save"+i);
                save.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
                save.version = 1;
                save.ownerKey(session.key());
                dataStore.create(save);
                list.add(save);
            }
        }
        return list;
    }

    public static SavedGame lookup(long saveId,DataStore dataStore){
        SavedGame savedGame = new SavedGame();
        savedGame.distributionId = saveId;
        if(!dataStore.load(savedGame)) return null;
        savedGame.dataStore(dataStore);
        return savedGame;
    }

}
