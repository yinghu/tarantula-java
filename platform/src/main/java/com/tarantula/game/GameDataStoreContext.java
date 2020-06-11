package com.tarantula.game;

import com.google.gson.JsonObject;
import com.tarantula.platform.ResponseHeader;

public class GameDataStoreContext extends ResponseHeader {

    public String name;
    public String tag;
    public String dataStore;
    public long dataStoreCount;
    public String serviceStore;
    public long serviceStoreCount;

    
    public GameDataStoreContext(){
        this.successful = true;
    }

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",successful);
        jsonObject.addProperty("name",name);
        jsonObject.addProperty("tag",tag);
        jsonObject.addProperty("dataStore",dataStore);
        jsonObject.addProperty("dataStoreCount",dataStoreCount);
        jsonObject.addProperty("serviceStore",serviceStore);
        jsonObject.addProperty("serviceStoreCount",serviceStoreCount);
        return jsonObject;
    }
}
