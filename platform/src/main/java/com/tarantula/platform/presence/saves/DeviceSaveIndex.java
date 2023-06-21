package com.tarantula.platform.presence.saves;

import com.google.gson.JsonObject;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class DeviceSaveIndex extends IndexSet {


    public DeviceSaveIndex(){
        this.label = "deviceSaveIndex";
    }

    public DeviceSaveIndex(String accessId){
        this();
        this.distributionKey(accessId);
    }

    @Override
    public int getClassId() {
        return PresencePortableRegistry.DEVICE_SAVE_INDEX_CID;
    }

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        //keySet().forEach();
        //jsonObject.addProperty("SessionId",this.key().asString());
        //jsonObject.addProperty("GameId",index);
        //jsonObject.addProperty("DeviceId",name);
        return jsonObject;
    }

}
