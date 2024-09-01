package com.tarantula.platform.item;

import com.google.gson.JsonObject;
import com.icodesoftware.Recoverable;
import com.icodesoftware.util.IntegerKey;

import java.util.List;


public class ConfigurableEdit extends ConfigurableObject {

    public int configurationId;

    @Override
    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return ItemPortableRegistry.CONFIGURABLE_EDIT_CID;
    }

    public String configurationScope() {
        return configurationScope;
    }

    public void configurationScope(String configurationScope) {
        this.configurationScope = configurationScope;
    }


    public boolean read(DataBuffer buffer){
        this.configurationType = buffer.readUTF8();
        this.configurationTypeId = buffer.readUTF8();
        this.configurationName = buffer.readUTF8();
        this.configurationCategory = buffer.readUTF8();
        this.configurationVersion = buffer.readUTF8();
        this.configurationScope = buffer.readUTF8();
        return true;
    }
    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(this.configurationType);
        buffer.writeUTF8(this.configurationTypeId);
        buffer.writeUTF8(this.configurationName);
        buffer.writeUTF8(this.configurationCategory);
        buffer.writeUTF8(this.configurationVersion);
        buffer.writeUTF8(this.configurationScope);
        return true;
    }

    public boolean readKey(Recoverable.DataBuffer buffer){
        configurationId = buffer.readInt();
        return true;
    }
    public boolean writeKey(Recoverable.DataBuffer buffer){
        if(configurationId <=0 ) return false;
        buffer.writeInt(configurationId);
        return true;
    }

    @Override
    public Key key() {
        return IntegerKey.from(configurationId);
    }

    @Override
    public JsonObject toJson() {
        JsonObject resp = new JsonObject();
        resp.addProperty("configurationId",configurationId);
        resp.addProperty("configurationName",configurationName);
        resp.addProperty("configurationType",configurationType);
        resp.addProperty("configurationTypeId",configurationTypeId);
        resp.addProperty("configurationCategory",configurationCategory);
        resp.addProperty("configurationVersion",configurationVersion);
        resp.addProperty("configurationScope",configurationScope);
        return resp;
    }

    public JsonObject assembly(){
        JsonObject resp = toJson();
        List<PropertyEdit> props = dataStore.list(new PropertyEditQuery(this.key()));
        props.forEach(prop->{
            if(prop.type.equals("number")){
                resp.addProperty(prop.name(),prop.edit.getAsNumber());
            }
            else if(prop.type.equals("enum")){
                resp.addProperty(prop.name(),prop.edit.getAsInt());
            }
            else if(prop.type.equals("string")){
                resp.addProperty(prop.name(),prop.edit.getAsString());
            }
            else if(prop.type.equals("category") || prop.type.equals("list")){
                resp.add(prop.name(),prop.edit.getAsJsonArray());
            }
        });
        return resp;
    }
}
