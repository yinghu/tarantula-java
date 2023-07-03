package com.tarantula.platform.presence;

import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.AssociateKey;

import java.util.Map;

public class Profile extends RecoverableObject implements Configurable {

    public String displayName;
    public String iconUrl;
    private JsonObject payload = new JsonObject();

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PresencePortableRegistry.PROFILE_CID;
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",displayName);
        this.properties.put("2",iconUrl);
        this.properties.put("3",payload.toString());
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.displayName = (String)properties.getOrDefault("1","");
        this.iconUrl = (String)properties.getOrDefault("2","");
        this.payload = JsonUtil.parse((String)properties.getOrDefault("3","{}"));
    }

    @Override
    public Key key(){
        return new AssociateKey(this.bucket,this.oid, "profile");
    }

    @Override
    public boolean configureAndValidate(byte[] data) {
        JsonObject config = JsonUtil.parse(data);
        if(config.has("displayName")){
            displayName = config.get("displayName").getAsString();
        }
        if(config.has("iconUrl")){
            iconUrl = config.get("iconUrl").getAsString();
        }
        if(config.has("payload")){
            return configureAndValidate(config.getAsJsonObject("payload"));
        }
        else{
            return configureAndValidate(config);
        }
    }
    @Override
    public boolean configureAndValidate(JsonObject data) {
        this.payload = data;
        this.dataStore.update(this);
        return true;
    }
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("DisplayName",displayName);
        jsonObject.addProperty("IconUrl",iconUrl);
        return jsonObject;
    }

}
