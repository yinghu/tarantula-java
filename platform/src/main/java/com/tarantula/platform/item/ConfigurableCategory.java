package com.tarantula.platform.item;

import com.google.gson.JsonObject;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;

public class ConfigurableCategory extends RecoverableObject {

    public String header;
    public String application;

    public ConfigurableCategory(){
        this.onEdge = true;
    }

    public ConfigurableCategory(JsonObject category){
        this();
        header = category.get("header").toString();
        application = category.get("application").toString();
    }
    public boolean read(DataBuffer buffer){
        this.header = buffer.readUTF8();
        this.application = buffer.readUTF8();
        return true;
    }
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(header);
        buffer.writeUTF8(application);
        return true;
    }

    public JsonObject toJson(){
        JsonObject resp = new JsonObject();
        resp.add("header", JsonUtil.parse(header));
        resp.add("application", JsonUtil.parse(application));
        return resp;
    }
    @Override
    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return ItemPortableRegistry.CONFIGURABLE_CATEGORY_CID;
    }


}
