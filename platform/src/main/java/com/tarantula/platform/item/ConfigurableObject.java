package com.tarantula.platform.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.icodesoftware.Configuration;
import com.icodesoftware.Descriptor;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;

import java.util.Map;

public class ConfigurableObject extends RecoverableObject implements Configuration {

    protected static String TYPE_KEY = "1";
    protected static String TYPE_ID_KEY = "2";
    protected static String NAME_KEY = "3";
    protected static String CATEGORY_KEY = "4";
    protected static String VERSION_KEY = "5";
    protected static String DISABLED_KEY = "6";

    protected static String HEADER_KEY = "7";
    protected static String APPLICATION_KEY = "8";

    protected static String REFERENCE_KEY = "10";


    protected String configurationType;
    protected String configurationTypeId;
    protected String configurationName;
    protected String configurationCategory;
    protected String configurationVersion;

    protected JsonObject header = new JsonObject();
    protected JsonObject application = new JsonObject();
    protected JsonArray reference = new JsonArray();

    protected Configurable.Listener listener;

    public ConfigurableObject(){}
    public ConfigurableObject(ConfigurableObject configurableObject){
        this.configurationType = configurableObject.configurationType;
        this.configurationTypeId = configurableObject.configurationTypeId;
        this.configurationName = configurableObject.configurationName;
        this.configurationCategory = configurableObject.configurationCategory;
        this.configurationVersion = configurableObject.configurationVersion;
        this.disabled = configurableObject.disabled;
        this.header = configurableObject.header;
        this.application = configurableObject.application;
        this.reference = configurableObject.reference;
        this.listener = configurableObject.listener;
        this.distributionKey(configurableObject.distributionKey());
    }

    public <T extends Configurable> void registerListener(Listener<T> listener){
        this.listener = listener;
    }

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

    @Override
    public void registered(){
        this.disabled = false;
        this.dataStore.update(this);
    }
    @Override
    public void released(){
        this.disabled = true;
        this.dataStore.update(this);
    }

    @Override
    public Map<String, Object> toMap() {
        this.properties.put(TYPE_KEY, this.configurationType);
        this.properties.put(TYPE_ID_KEY, this.configurationTypeId);
        this.properties.put(NAME_KEY, this.configurationName);
        this.properties.put(CATEGORY_KEY, this.configurationCategory);
        this.properties.put(VERSION_KEY, this.configurationVersion);
        this.properties.put(DISABLED_KEY,this.disabled);
        this.properties.put(HEADER_KEY,header.toString());
        this.properties.put(APPLICATION_KEY,application.toString());
        this.properties.put(REFERENCE_KEY,reference.toString());
        return this.properties;
    }

    @Override
    public void fromMap(Map<String, Object> properties) {
        this.configurationType = (String) properties.get(TYPE_KEY);
        this.configurationTypeId = (String) properties.get(TYPE_ID_KEY);
        this.configurationName = (String) properties.get(NAME_KEY);
        this.configurationCategory = (String) properties.get(CATEGORY_KEY);
        this.configurationVersion = (String) properties.get(VERSION_KEY);
        this.disabled = (boolean)properties.getOrDefault(DISABLED_KEY,false);
        this.header = JsonUtil.parse((String) properties.getOrDefault(HEADER_KEY, "{}"));
        this.application = JsonUtil.parse((String) properties.getOrDefault(APPLICATION_KEY, "{}"));
        this.reference = JsonUtil.parseAsArray((String) properties.getOrDefault(REFERENCE_KEY, "[]"));
    }
    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return ItemPortableRegistry.CONFIGURABLE_OBJECT_CID;
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("configurationType",configurationType);
        jsonObject.addProperty("configurationTypeId",configurationTypeId);
        jsonObject.addProperty("configurationName",configurationName);
        jsonObject.addProperty("configurationCategory",configurationCategory);
        jsonObject.addProperty("configurationVersion",configurationVersion);
        jsonObject.addProperty("itemId", distributionKey());
        jsonObject.addProperty("disabled",disabled);
        jsonObject.add("header",header);
        jsonObject.add("application",application);
        jsonObject.add("reference",reference);
        return jsonObject;
    }

    @Override
    public boolean configureAndValidate(byte[] data) {
        JsonObject config = JsonUtil.parse(data);
        if(!config.has("configurationType")||!config.has("configurationTypeId")||!config.has("configurationName") ||!config.has("configurationCategory")||!config.has("configurationVersion")) return false;
        this.configurationType = config.get("configurationType").getAsString();
        this.configurationTypeId = config.get("configurationTypeId").getAsString();
        this.configurationName = config.get("configurationName").getAsString();
        this.configurationCategory = config.get("configurationCategory").getAsString();
        this.configurationVersion = config.get("configurationVersion").getAsString();
        return configureAndValidate(config);
    }

    @Override
    public boolean configureAndValidate(JsonObject config) {
        if(!config.has("configurationType")||!config.has("configurationTypeId")||!config.has("configurationName") ||!config.has("configurationCategory")||!config.has("configurationVersion")) return false;
        this.configurationType = config.get("configurationType").getAsString();
        this.configurationTypeId = config.get("configurationTypeId").getAsString();
        this.configurationName = config.get("configurationName").getAsString();
        this.configurationCategory = config.get("configurationCategory").getAsString();
        this.configurationVersion = config.get("configurationVersion").getAsString();
        if (!config.has("header") || !config.has("application") || !config.has("reference")) {
            return false;
        }
        this.header = config.getAsJsonObject("header");
        this.application = config.getAsJsonObject("application");
        this.reference = config.getAsJsonArray("reference");
        if(config.has("itemId")){
            this.distributionKey(config.get("itemId").getAsString());
        }
        return true;
    }

    @Override
    public boolean configureAndValidate() {
        return true;
    }

    @Override
    public  <T extends Configurable> T setup(){
        if(this.configurationType.equals(Configurable.COMPONENT_CONFIG_TYPE)){
            Component component = new Component(this);
            component.dataStore(dataStore);
            return component.setup();
        }
        if(this.configurationType.equals(Configurable.ASSET_CONFIG_TYPE)){
            Asset asset = new Asset(this);
            asset.dataStore(dataStore);
            return asset.setup();
        }
        if(this.configurationType.equals(Configurable.COMMODITY_CONFIG_TYPE)){
            Commodity commodity = new Commodity(this);
            commodity.dataStore(dataStore);
            return commodity.setup();
        }
        if(this.configurationType.equals(Configurable.ITEM_CONFIG_TYPE)){
            Item item = new Item(this);
            item.dataStore(dataStore);
            return item.setup();
        }
        if(this.configurationType.equals(Configurable.APPLICATION_CONFIG_TYPE)){
            Application application = new Application(this);
            application.dataStore(dataStore);
            return application.setup();
        }
        return null;
    }

    public Category category(Descriptor app){
        Category category = new Category();
        category.distributionKey(app.distributionKey());
        this.dataStore.createIfAbsent(category,true);
        category.dataStore(dataStore);
        return category;
    }
    public Index index(Descriptor app,String query){
        Index index = new Index(query);
        index.distributionKey(app.distributionKey());
        index.dataStore(dataStore);
        return index;
    }
    public <T extends Configurable> T configurableHeader(){
        ConfigurableHeader configurableHeader = new ConfigurableHeader(this);
        configurableHeader.distributionKey(this.distributionKey());
        return (T)configurableHeader;
    }
}