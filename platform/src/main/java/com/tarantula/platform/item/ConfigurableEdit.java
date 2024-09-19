package com.tarantula.platform.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.util.SnowflakeKey;
import com.icodesoftware.util.StringUtil;

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

    public boolean read(DataBuffer buffer){
        this.configurationId = buffer.readInt();
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
        buffer.writeInt(this.configurationId);
        buffer.writeUTF8(this.configurationType);
        buffer.writeUTF8(this.configurationTypeId);
        buffer.writeUTF8(this.configurationName);
        buffer.writeUTF8(this.configurationCategory);
        buffer.writeUTF8(this.configurationVersion);
        buffer.writeUTF8(this.configurationScope);
        return true;
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
        this.dataStore.load(this);
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
            else if(prop.type.equals("list")){
                JsonArray pes = prop.edit.getAsJsonArray();
                JsonArray list = new JsonArray();
                pes.forEach(pe->{
                    ConfigurableEdit edit = new ConfigurableEdit();
                    edit.distributionId(pe.getAsLong());
                    edit.dataStore(dataStore);
                    list.add(edit.assembly());
                });
                resp.add(prop.name(),list);
            }
            else if(prop.type.equals("category")){
                JsonArray pes = prop.edit.getAsJsonArray();
                ConfigurableEdit edit = new ConfigurableEdit();
                edit.distributionId(pes.get(0).getAsLong());
                edit.dataStore(dataStore);
                resp.add(prop.name(),edit.assembly());
            }
        });
        return resp;
    }

    private void list(){
        JsonObject template = application.getAsJsonObject("template");
        JsonArray props = template.getAsJsonObject("application").getAsJsonArray("properties");
        props.forEach(prop->{
            JsonObject edit = prop.getAsJsonObject();
            String type = edit.get("type").getAsString();
            String name = edit.get("name").getAsString();
            if(type.equals("list")){
                String _name = StringUtil.toUnderScore(name);
                JsonArray list = application.get(_name).getAsJsonArray();
                JsonArray pes = new JsonArray();
                list.forEach((category->{
                    ConfigurableEdit configurableEdit = new ConfigurableEdit();
                    configurableEdit.dataStore(dataStore);
                    long editId = configurableEdit.build(category.getAsJsonObject());
                    pes.add(editId);
                }));
                PropertyEdit pe = new PropertyEdit("list",_name,pes);
                pe.ownerKey(this.key());
                dataStore.create(pe);
            }
            else if(type.equals("category")){
                String _name = StringUtil.toUnderScore(name);
                JsonObject category = application.get(_name).getAsJsonObject();
                ConfigurableEdit configurableEdit = new ConfigurableEdit();
                configurableEdit.dataStore(dataStore);
                long editId = configurableEdit.build(category.getAsJsonObject());
                JsonArray pes = new JsonArray();
                pes.add(editId);
                PropertyEdit pe = new PropertyEdit("category",_name,pes);
                pe.ownerKey(this.key());
                dataStore.create(pe);
            }
            else{
                PropertyEdit propertyEdit = new PropertyEdit();
                propertyEdit.name(name);
                propertyEdit.type = type;
                propertyEdit.edit = application.get(name);
                propertyEdit.ownerKey(this.key());
                dataStore.create(propertyEdit);
            }
        });
    }

    public long build(JsonObject payload){
        this.application = payload;
        this.configurationId = payload.get("ConfigurationId").getAsInt();
        this.configurationType = payload.get("ConfigurationType").getAsString();
        this.configurationTypeId = payload.get("ConfigurationTypeId").getAsString();
        this.configurationName = payload.get("ConfigurationName").getAsString();
        this.configurationCategory = payload.get("ConfigurationCategory").getAsString();
        this.configurationVersion = payload.get("ConfigurationVersion").getAsString();
        this.dataStore.create(this);
        list();
        return distributionId;
    }
}
