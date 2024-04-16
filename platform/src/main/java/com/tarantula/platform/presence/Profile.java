package com.tarantula.platform.presence;

import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.AssociateKey;

import java.util.Map;

public class Profile extends RecoverableObject implements Configurable {

    public static final String LABEL = "profile";
    public String displayName;
    public int iconIndex;
    private JsonObject payload = new JsonObject();

    public Profile(){
        this.onEdge = true;
        this.label = LABEL;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(displayName);
        buffer.writeInt(iconIndex);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        displayName = buffer.readUTF8();
        iconIndex = buffer.readInt();
        return true;
    }

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
        this.properties.put("2", iconIndex);
        this.properties.put("3",payload.toString());
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.displayName = (String)properties.getOrDefault("1","");
        this.iconIndex = (int)properties.getOrDefault("2","");
        this.payload = JsonUtil.parse((String)properties.getOrDefault("3","{}"));
    }



    @Override
    public boolean configureAndValidate(byte[] data) {
        JsonObject config = JsonUtil.parse(data);
        if(config.has("DisplayName")){
            displayName = config.get("DisplayName").getAsString();
        }
        if(config.has("IconIndex")){
            iconIndex = config.get("IconIndex").getAsInt();
        }

        return true;
    }

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("DisplayName",displayName);
        jsonObject.addProperty("IconIndex", iconIndex);
        return jsonObject;
    }

}
