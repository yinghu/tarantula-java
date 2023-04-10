package com.tarantula.game;

import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;


public class GameUpdateObject extends RecoverableObject {
    private final static String _KEY = "_key";

    @Override
    public int getFactoryId() {
        return GamePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return GamePortableRegistry.GAME_UPDATE_OBJECT_CID;
    }


    public void value(byte[] json){
        properties.put(_KEY, JsonUtil.parseAsJsonElement(json));
    }

    public byte[] value(){
        return properties.get(_KEY).toString().getBytes();
    }

}
