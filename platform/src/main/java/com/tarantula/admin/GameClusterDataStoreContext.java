package com.tarantula.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tarantula.platform.ResponseHeader;

import java.util.ArrayList;
import java.util.List;

public class GameClusterDataStoreContext extends ResponseHeader {

    public String name;
    public String tag;
    //public String dataStore;
    //public long dataStoreCount;
    //public String serviceStore;
    //public long serviceStoreCount;
    //public String serviceRoomStore;
    //public long serviceRoomStoreCount;
    //public String serviceConfigurationStore;
    //public long serviceConfigurationStoreCount;
    //public String serviceTournamentStore="n/a";
    //public long serviceTournamentStoreCount;

    public List<GameDataStore> gameDataStoreList = new ArrayList<>();

    public GameClusterDataStoreContext(){
        this.successful = true;
    }

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",successful);
        jsonObject.addProperty("name",name);
        jsonObject.addProperty("tag",tag);
        //jsonObject.addProperty("dataStore",dataStore);
        //jsonObject.addProperty("dataStoreCount",dataStoreCount);
        //jsonObject.addProperty("serviceStore",serviceStore);
        //jsonObject.addProperty("serviceStoreCount",serviceStoreCount);
        //jsonObject.addProperty("serviceRoomStore",serviceRoomStore);
        //jsonObject.addProperty("serviceRoomStoreCount",serviceRoomStoreCount);
        //jsonObject.addProperty("serviceConfigurationStore",serviceConfigurationStore);
        //jsonObject.addProperty("serviceConfigurationStoreCount",serviceConfigurationStoreCount);
        //jsonObject.addProperty("serviceTournamentStore",serviceTournamentStore);
        //jsonObject.addProperty("serviceTournamentStoreCount",serviceTournamentStoreCount);
        JsonArray storeList = new JsonArray();
        gameDataStoreList.forEach(ds-> storeList.add(ds.toJson()));
        jsonObject.add("storeList",storeList);
        return jsonObject;
    }

    public static class GameDataStore{
        public String serviceName;
        public String dataStoreName;
        public long count;
        public GameDataStore(String serviceName,String dataStoreName,long count){
            this.serviceName = serviceName;
            this.dataStoreName = dataStoreName;
            this.count = count;
        }
        public JsonObject toJson(){
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name",serviceName);
            jsonObject.addProperty("dataStore",dataStoreName);
            jsonObject.addProperty("count",count);
            return jsonObject;
        }
    }
}
