package com.tarantula.game;

import com.icodesoftware.Configurable;
import com.icodesoftware.util.RecoverableObject;

import java.util.Map;

public class GameEntry extends RecoverableObject implements Configurable {
    public static final String LABEL = "GGE";
    public String systemId;

    public GameEntry(){
        this.label = LABEL;
        this.onEdge = true;
    }
    @Override
    public Map<String,Object> toMap(){
        properties.put("systemId",systemId);
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.systemId = (String)properties.getOrDefault("systemId",null);
    }

    @Override
    public int getFactoryId() {
        return GamePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return GamePortableRegistry.GAME_ENTRY_CID;
    }
}
