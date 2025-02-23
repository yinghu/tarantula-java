package com.icodesoftware.protocol.configuration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Configuration;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.StringUtil;

import java.util.List;

public class TRConfigurableEdit extends RecoverableObject implements Configuration {

    protected String configurationType;
    protected String configurationTypeId;
    protected String configurationName;
    protected String configurationCategory;
    protected String configurationVersion;
    protected String configurationScope;

    protected List<TRPropertyEdit> properties;

    protected JsonObject application;

    public String configurationType() {
        return this.configurationType;
    }

    public void configurationType(String configurationType) {
        this.configurationType = configurationType;
    }

    public String configurationTypeId() {
        return this.configurationTypeId;
    }

    public void configurationTypeId(String configurationTypeId) {
        this.configurationTypeId = configurationTypeId;
    }

    public String configurationName() {
        return configurationName;
    }

    public void configurationName(String configurationName) {
        this.configurationName = configurationName;
    }

    public String configurationCategory() {
        return configurationCategory;
    }

    public void configurationCategory(String configurationCategory) {
        this.configurationCategory = configurationCategory;
    }

    public String configurationVersion() {
        return configurationVersion;
    }

    public void configurationVersion(String configurationVersion) {
        this.configurationVersion = configurationVersion;
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


    @Override
    public JsonObject toJson() {
        JsonObject resp = new JsonObject();
        resp.addProperty("configurationId",Long.toString(distributionId));
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
        List<TRPropertyEdit> props = dataStore.list(new TRPropertyEditQuery(this.key()));
        properties.forEach(prop->{
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
                    TRConfigurableEdit edit = new TRConfigurableEdit();
                    edit.distributionId(pe.getAsLong());
                    edit.dataStore(dataStore);
                    list.add(edit.assembly());
                });
                resp.add(prop.name(),list);
            }
            else if(prop.type.equals("category")){
                JsonArray pes = prop.edit.getAsJsonArray();
                TRConfigurableEdit edit = new TRConfigurableEdit();
                edit.distributionId(pes.get(0).getAsLong());
                edit.dataStore(dataStore);
                resp.add(prop.name(),edit.assembly());
            }
        });
        return resp;
    }

    public long build(JsonObject payload){
        this.application = payload;
        this.configurationType = payload.get("ConfigurationType").getAsString();
        this.configurationTypeId = payload.get("ConfigurationTypeId").getAsString();
        this.configurationName = payload.get("ConfigurationName").getAsString();
        this.configurationCategory = payload.get("ConfigurationCategory").getAsString();
        this.configurationVersion = payload.get("ConfigurationVersion").getAsString();
        this.dataStore.create(this);
        list();
        return distributionId;
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
                    TRConfigurableEdit configurableEdit = new TRConfigurableEdit();
                    configurableEdit.dataStore(dataStore);
                    long editId = configurableEdit.build(category.getAsJsonObject());
                    pes.add(editId);
                }));
                TRPropertyEdit pe = new TRPropertyEdit("list",_name,pes);
                pe.ownerKey(this.key());
                dataStore.create(pe);
            }
            else if(type.equals("category")){
                String _name = StringUtil.toUnderScore(name);
                JsonObject category = application.get(_name).getAsJsonObject();
                TRConfigurableEdit configurableEdit = new TRConfigurableEdit();
                configurableEdit.dataStore(dataStore);
                long editId = configurableEdit.build(category.getAsJsonObject());
                JsonArray pes = new JsonArray();
                pes.add(editId);
                TRPropertyEdit pe = new TRPropertyEdit("category",_name,pes);
                pe.ownerKey(this.key());
                dataStore.create(pe);
            }
            else{
                TRPropertyEdit propertyEdit = new TRPropertyEdit();
                propertyEdit.name(name);
                propertyEdit.type = type;
                propertyEdit.edit = application.get(name);
                propertyEdit.ownerKey(this.key());
                dataStore.create(propertyEdit);
            }
        });
    }

}
