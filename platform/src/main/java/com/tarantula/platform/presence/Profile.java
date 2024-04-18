package com.tarantula.platform.presence;

import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;

public class Profile extends RecoverableObject implements Configurable {

    public static final String LABEL = "profile";
    public String displayName;
    public int iconIndex;
    public int profileSequence;

    public Profile(){
        this.onEdge = false;
        this.label = LABEL;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(displayName);
        buffer.writeInt(iconIndex);
        buffer.writeInt(profileSequence);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        displayName = buffer.readUTF8();
        iconIndex = buffer.readInt();
        profileSequence = buffer.readInt();
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
    public boolean configureAndValidate(byte[] data) {
        JsonObject config = JsonUtil.parse(data);
        if(!config.has("DisplayName") || !config.has("IconIndex")) return false;
        displayName = config.get("DisplayName").getAsString();
        iconIndex = config.get("IconIndex").getAsInt();
        return true;
    }

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("DisplayName",displayName);
        jsonObject.addProperty("IconIndex", iconIndex);
        jsonObject.addProperty("ProfileSequence", profileSequence);
        return jsonObject;
    }

}
