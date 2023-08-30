package com.tarantula.platform.item;

import com.google.gson.JsonObject;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;

public class ConfigurableType extends RecoverableObject {

    private String application;

    public ConfigurableType(){
        this.onEdge = true;
    }
    public ConfigurableType(JsonObject jsonObject){
        this();
        name = jsonObject.get("name").getAsString();
        application = jsonObject.toString();
    }
    public boolean read(DataBuffer buffer){
        name = buffer.readUTF8();
        this.application = buffer.readUTF8();
        return true;
    }
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(name);
        buffer.writeUTF8(application);
        return true;
    }

    public JsonObject toJson(){
        JsonObject jsonObject = JsonUtil.parse(application);
        return jsonObject;
    }
    @Override
    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return ItemPortableRegistry.CONFIGURABLE_TYPES_CID;
    }

}
