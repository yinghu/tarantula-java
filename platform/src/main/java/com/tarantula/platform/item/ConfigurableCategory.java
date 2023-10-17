package com.tarantula.platform.item;

import com.google.gson.JsonObject;
import com.icodesoftware.Configuration;
import com.icodesoftware.Recoverable;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.NaturalKey;
import com.icodesoftware.util.RecoverableObject;

import java.util.HashMap;

public class ConfigurableCategory extends RecoverableObject implements Configuration {

    public String header;
    public String application;

    public String scope;
    public String version;
    public String description;

    public boolean rechargeable;
    public boolean constrained;

    public HashMap<String,JsonObject> properties = new HashMap<>();
    public ConfigurableCategory(){
        this.onEdge = true;
    }

    public ConfigurableCategory(JsonObject category){
        this();
        this.name = category.get("header").getAsJsonObject().get("type").getAsString();
        header = category.get("header").toString();
        application = category.get("application").toString();
    }
    public boolean read(DataBuffer buffer){
        this.name = buffer.readUTF8();
        this.header = buffer.readUTF8();
        this.application = buffer.readUTF8();
        return true;
    }
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(name);
        buffer.writeUTF8(header);
        buffer.writeUTF8(application);
        return true;
    }

    public JsonObject toJson(){
        JsonObject resp = new JsonObject();
        resp.addProperty("name",name);
        resp.add("header", JsonUtil.parse(header));
        JsonObject props = JsonUtil.parse(application);
        props.get("properties").getAsJsonArray().forEach(e->{
            JsonObject jo = e.getAsJsonObject();
            properties.put(jo.get("name").getAsString(),jo);
        });
        resp.add("application", props);
        return resp;
    }

    public void parse(){
        JsonObject h = JsonUtil.parse(header);
        scope = h.get("scope").getAsString();
        version = h.get("version").getAsString();
        description = h.get("description").getAsString();
        rechargeable = h.get("rechargeable").getAsBoolean();
        constrained = h.has("constrained")? h.get("constrained").getAsBoolean() : false;
    }

    public ConfigurableType configurableType(){
        JsonObject resp = new JsonObject();
        resp.addProperty("type","category");
        resp.addProperty("name",name);
        return new ConfigurableType(resp);
    }

    @Override
    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return ItemPortableRegistry.CONFIGURABLE_CATEGORY_CID;
    }

    public boolean readKey(Recoverable.DataBuffer buffer){
        name = buffer.readUTF8().split("/")[1];
        return true;
    }
    public boolean writeKey(Recoverable.DataBuffer buffer){
        if(name==null) return false;
        buffer.writeUTF8("category/"+name);
        return true;
    }
    @Override
    public Key key() {
        return new NaturalKey("category/"+this.name);
    }

    public void reset(JsonObject update){
        header = update.get("header").toString();
        application = update.get("application").toString();
    }

}
