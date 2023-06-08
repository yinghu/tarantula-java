package com.tarantula.platform.presence;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tarantula.platform.IndexSet;

import java.util.Set;

public class PersonalDataIndex extends IndexSet {


    public PersonalDataIndex(){
        this.label = "personalDataIndex";
    }

    @Override
    public int getClassId() {
        return PresencePortableRegistry.PERSONAL_DATA_INDEX_CID;
    }

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        JsonArray keys = new JsonArray();
        keySet().forEach(k->keys.add(k));
        jsonObject.add("_keys",keys);
        return jsonObject;
    }
    public String dataKey(String key){
        Set<String> keys = this.keySet();
        for (String k : keys){
            if(k.startsWith(key)) return k;
        }
        return null;
    }

}
