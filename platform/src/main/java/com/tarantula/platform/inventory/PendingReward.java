package com.tarantula.platform.inventory;

import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.Map;

public class PendingReward extends RecoverableObject {


    //name -- the name of reward like daily giveaway , achievement , tournament
    //index -- this key of the reward
    //disabled -- rewarded already

    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.PENDING_REWARD_CID;
    }

    @Override
    public Map<String,Object> toMap(){
        properties.put("1",index);
        properties.put("2",name);
        properties.put("3",disabled);
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.index = (String) properties.get("1");
        this.name = (String) properties.get("2");
        this.disabled = (boolean)properties.get("3");
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name",name);
        jsonObject.addProperty("rewardKey",index);
        jsonObject.addProperty("rewarded",disabled);
        return jsonObject;
    }
}
