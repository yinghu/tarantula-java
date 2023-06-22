package com.tarantula.platform.presence.saves;

import com.google.gson.JsonObject;
import com.icodesoftware.Recoverable;
import com.icodesoftware.Session;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class CurrentSaveIndex extends RecoverableObject {

    //session  <> save Id mapping for current save selection or default save

    public int version;

    public CurrentSaveIndex(){

    }

    public CurrentSaveIndex(Session session){
        String[] query = session.systemId().split(Recoverable.PATH_SEPARATOR);
        this.bucket = query[0];
        this.oid = query[1];
        this.routingNumber = session.stub();
        this.timestamp = TimeUtil.toUTCMilliseconds(LocalDateTime.now());
    }

    public CurrentSaveIndex(Session session,SavedGame selected){
        this(session);
        this.index = selected.distributionKey();
        this.name = selected.name();
        this.version = selected.version;
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",index);
        this.properties.put("2",name);
        this.properties.put("3",version);
        this.properties.put("4",timestamp);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.index = ((String) properties.get("1"));
        this.name = ((String) properties.get("2"));
        this.version =  ((Number)properties.getOrDefault("3",0)).intValue();
        this.timestamp =  ((Number)properties.getOrDefault("4",0)).longValue();
    }

    @Override
    public int getClassId() {
        return PresencePortableRegistry.CURRENT_SAVE_INDEX_CID;
    }

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public Recoverable.Key key(){
        return new SaveKey(this.bucket,this.oid,routingNumber);
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Successful",true);
        jsonObject.addProperty("SessionId",this.key().asString());
        jsonObject.addProperty("GameId",index);
        jsonObject.addProperty("SaveName",name);
        jsonObject.addProperty("Version",version);
        jsonObject.addProperty("StartTime", TimeUtil.fromUTCMilliseconds(timestamp).format(DateTimeFormatter.BASIC_ISO_DATE));
        return jsonObject;
    }

}
