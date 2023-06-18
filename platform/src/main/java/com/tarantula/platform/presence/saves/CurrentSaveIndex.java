package com.tarantula.platform.presence.saves;

import com.google.gson.JsonObject;
import com.icodesoftware.Recoverable;
import com.icodesoftware.Session;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.Map;

public class CurrentSaveIndex extends RecoverableObject {

    //systemId <> deviceId mapping for current save selection or default save
    public CurrentSaveIndex(){

    }

    public CurrentSaveIndex(Session session,String index){
        String[] query = session.systemId().split(Recoverable.PATH_SEPARATOR);
        this.bucket = query[0];
        this.oid = query[1];
        this.routingNumber = session.stub();
        this.index = index;
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",index);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.index = ((String) properties.get("1"));
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
        jsonObject.addProperty("SaveId",this.key().asString());
        jsonObject.addProperty("GameId",index);
        return jsonObject;
    }

}
