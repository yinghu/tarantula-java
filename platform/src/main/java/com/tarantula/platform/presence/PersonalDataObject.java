package com.tarantula.platform.presence;

import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;



public class PersonalDataObject extends RecoverableObject {

    private final static String _KEY = "_key";

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.PERSONAL_DATA_OBJECT_CID;
    }

    public void value(byte[] json){
        properties.put(_KEY, JsonUtil.parseAsJsonElement(json));
    }

    public byte[] value(){
        return properties.get(_KEY).toString().getBytes();
    }

}
