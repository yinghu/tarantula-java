package com.tarantula.platform.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.icodesoftware.Configurable;
import com.icodesoftware.Configuration;
import com.icodesoftware.Descriptor;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;

import java.util.ArrayList;
import java.util.HashMap;
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

    protected static String REFERENCE_KEY = "9";

    protected static String SETTINGS_KEY = "10";

    protected static String SCOPE_KEY = "11";

    protected static String COMMODITY_PAYLOAD = "12";


    protected String configurationType;
    protected String configurationTypeId;
    protected String configurationName;
    protected String configurationCategory;
    protected String configurationVersion;

    protected String configurationScope;

    protected JsonObject header = new JsonObject();
    protected JsonObject application = new JsonObject();
    protected JsonArray reference = new JsonArray();

    protected Configurable.Listener listener;
    protected ArrayList<ConfigurableObject> _reference;
    protected ConfigurableCategories _configurableSetting;

    public ConfigurableObject(){}
    public ConfigurableObject(ConfigurableObject configurableObject){
        this.configurationType = configurableObject.configurationType;
        this.configurationTypeId = configurableObject.configurationTypeId;
        this.configurationName = configurableObject.configurationName;
        this.configurationCategory = configurableObject.configurationCategory;
        this.configurationVersion = configurableObject.configurationVersion;
        this.configurationScope = configurableObject.configurationScope;
        this.disabled = configurableObject.disabled;
        this.header = configurableObject.header;
        this.application = configurableObject.application;
        this.reference = configurableObject.reference;
        this.listener = configurableObject.listener;
        this._reference = configurableObject._reference;
        this._configurableSetting = configurableObject._configurableSetting;
        this.distributionId(configurableObject.distributionId());
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

    public boolean read(DataBuffer buffer){
        this.configurationType = buffer.readUTF8();
        this.configurationTypeId = buffer.readUTF8();
        this.configurationName = buffer.readUTF8();
        this.configurationCategory = buffer.readUTF8();
        this.configurationVersion = buffer.readUTF8();
        this.configurationScope = buffer.readUTF8();
        this.disabled = buffer.readBoolean();
        this.header = JsonUtil.parse(buffer.readUTF8());
        this.application = JsonUtil.parse(buffer.readUTF8());
        this.reference = JsonUtil.parseAsJsonArray(buffer.readUTF8());
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
        buffer.writeBoolean(disabled);
        buffer.writeUTF8(header.toString());
        buffer.writeUTF8(application.toString());
        buffer.writeUTF8(reference.toString());
        return true;
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
        if(distributionId()==0) return jsonObject;
        jsonObject.addProperty("ItemId",distributionKey());
        header.entrySet().forEach(e->{
            String k  = e.getKey();
            JsonPrimitive pv = e.getValue().getAsJsonPrimitive();
            if(pv.isString()) jsonObject.addProperty(k,pv.getAsString());
            if(pv.isNumber()) jsonObject.addProperty(k,pv.getAsNumber());
            if(pv.isBoolean()) jsonObject.addProperty(k,pv.getAsBoolean());
        });
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
        this.revision = Long.parseLong(config.get("revision").getAsString());
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
            this.distributionId(Long.parseLong(config.get("itemId").getAsString()));
            this.revision = Long.parseLong(config.get("revision").getAsString());
        }
        return true;
    }

    @Override
    public boolean configureAndValidate() {
        return true;
    }

    @Override
    public  <T extends Configurable> T setup(){
        if(this.configurationType.equals(Configurable.ASSET_CONFIG_TYPE)){
            Asset asset = new Asset(this);
            asset.dataStore(dataStore);
            return asset.setup();
        }
        if(this.configurationType.equals(Configurable.COMPONENT_CONFIG_TYPE)){
            Component component = new Component(this);
            component.dataStore(dataStore);
            return component.setup();
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
        if(this.configurationType.startsWith(Configurable.APPLICATION_CONFIG_TYPE)){
            Item application = new Item(this);
            application.dataStore(dataStore);
            return application.setup();
        }
        return null;
    }

    public Category category(Descriptor app){
        Category category = new Category(app);
        category.dataStore(dataStore);
        return category;
    }

    public void configurableSetting(ConfigurableCategories configurableSetting){
        ConfigurableCategory category = configurableSetting.configurableSetting(configurationCategory);
        category.parse();
        configurationScope = category.scope;
        _configurableSetting = configurableSetting;
    }

    public JsonObject header(){
        return header;
    }
    public JsonObject application(){
        return application;
    }
    public JsonArray reference(){
        return reference;
    }

    protected JsonObject toJson(JsonObject json){
        if(_configurableSetting==null) return json;
        ConfigurableCategory category = _configurableSetting.configurableSetting(configurationCategory);
        category.toJson();
        HashMap<String,ConfigurableObject> _ref = new HashMap<>();
        if(_reference==null) return json;
        _reference.forEach(cob-> {
            cob._configurableSetting = _configurableSetting;
            _ref.put(cob.distributionKey(),cob);
        });
        application.entrySet().forEach(e->{
            String k = e.getKey();
            JsonObject _type = category.properties.get(k);
            String f = k.substring(0,1);
            String fk = k.replaceFirst(f,"_"+f.toLowerCase());
            String cat = _type.get("type").getAsString();
            if(cat.equals("category")){
                JsonArray keys = e.getValue().getAsJsonArray();
                if(keys.size()==1) {
                    json.add(fk,_ref.get(keys.get(0).getAsString()).toJson());
                }
            }
            else if(cat.equals("list") || cat.equals("set")){
                if(!json.has(fk)) json.add(fk,new JsonArray());
                JsonArray arr = json.get(fk).getAsJsonArray();
                String refType = _type.get("reference").getAsString();
                if(refType.startsWith("category")){
                    e.getValue().getAsJsonArray().forEach(key->{
                        arr.add(_ref.get(key.getAsString()).toJson());
                    });
                }
                else{
                    e.getValue().getAsJsonArray().forEach(key->{
                        arr.add(key);
                    });
                }
            }
        });
        return json;
    }

}