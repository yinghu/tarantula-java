package com.tarantula.platform.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Configuration;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;

import java.util.Map;

public class ConfigurableObject extends RecoverableObject implements Configuration {

    protected static String HEADER_KEY = "6";
    protected static String APPLICATION_KEY = "7";
    protected static String PAYLOAD_KEY = "8";
    protected static String REFERENCE_KEY = "9";


    protected String configurationType;
    protected String configurationTypeId;
    protected String configurationName;
    protected String configurationCategory;
    protected String configurationVersion;

    protected JsonObject header = new JsonObject();
    protected JsonObject payload = new JsonObject();
    protected JsonObject application = new JsonObject();
    protected JsonArray reference = new JsonArray();

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
    public Map<String, Object> toMap() {
        this.properties.put("1", this.configurationType);
        this.properties.put("2", this.configurationTypeId);
        this.properties.put("3", this.configurationName);
        this.properties.put("4", this.configurationCategory);
        this.properties.put("5", this.configurationVersion);
        return this.properties;
    }

    @Override
    public void fromMap(Map<String, Object> properties) {
        this.configurationType = (String) properties.get("1");
        this.configurationTypeId = (String) properties.get("2");
        this.configurationName = (String) properties.get("3");
        this.configurationCategory = (String) properties.get("4");
        this.configurationVersion = (String) properties.get("5");
        this.header = JsonUtil.parse((String) properties.getOrDefault(HEADER_KEY, "{}"));
        this.application = JsonUtil.parse((String) properties.getOrDefault(APPLICATION_KEY, "{}"));
        this.payload = JsonUtil.parse((String) properties.getOrDefault(PAYLOAD_KEY, "{}"));
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
        jsonObject.addProperty("itemId", distributionKey());
        jsonObject.addProperty("configurationType", configurationType);
        jsonObject.addProperty("configurationTypeId", configurationTypeId);
        jsonObject.addProperty("configurationName", configurationName);
        jsonObject.addProperty("configurationCategory", configurationCategory);
        jsonObject.addProperty("configurationVersion", configurationVersion);
        jsonObject.add("header", header);
        jsonObject.add("application", application);
        jsonObject.add("payload", payload);
        jsonObject.add("reference", reference);
        return jsonObject;
    }

    @Override
    public boolean configureAndValidate(byte[] data) {
        JsonObject config = JsonUtil.parse(data);
        this.configurationType = config.get("configurationType").getAsString();
        this.configurationTypeId = config.get("configurationTypeId").getAsString();
        this.configurationName = config.get("configurationName").getAsString();
        this.configurationCategory = config.get("configurationCategory").getAsString();
        this.configurationVersion = config.get("configurationVersion").getAsString();
        return configureAndValidate(config);
    }

    @Override
    public boolean configureAndValidate(JsonObject config) {
        if (!config.has("header") || !config.has("payload") || !config.has("application") || !config.has("reference")) {
            return false;
        }
        this.header = config.getAsJsonObject("header");
        this.payload = config.getAsJsonObject("payload");
        this.application = config.getAsJsonObject("application");
        this.reference = config.getAsJsonArray("reference");
        return true;
    }

    @Override
    public boolean configureAndValidate() {
        return true;
    }
}