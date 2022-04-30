package com.tarantula.platform.presence.saves;

import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.Map;

public class SavedGame extends RecoverableObject {

    public SavedGame(){

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
        return PresencePortableRegistry.SAVED_GAME_CID;
    }

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }

}
