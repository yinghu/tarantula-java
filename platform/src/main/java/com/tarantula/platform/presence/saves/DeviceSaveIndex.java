package com.tarantula.platform.presence.saves;

import com.google.gson.JsonObject;
import com.icodesoftware.Recoverable;
import com.icodesoftware.Session;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.Map;

public class DeviceSaveIndex extends RecoverableObject {

    //systemId <> deviceId mapping for current save selection or default save
    public DeviceSaveIndex(){

    }

    public DeviceSaveIndex(Session session){
        String[] query = session.systemId().split(Recoverable.PATH_SEPARATOR);
        this.bucket = query[0];
        this.oid = query[1];
        this.routingNumber = session.stub();
    }

    public DeviceSaveIndex(Session session, SavedGame selected){
        this(session);
        this.index = selected.distributionKey();
        this.name = selected.index();
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",index);
        this.properties.put("2",name);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.index = ((String) properties.get("1"));
        this.name = ((String) properties.get("2"));
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
    public Key key(){
        return new SaveKey(this.bucket,this.oid,routingNumber);
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("SessionId",this.key().asString());
        jsonObject.addProperty("GameId",index);
        jsonObject.addProperty("DeviceId",name);
        return jsonObject;
    }

}
