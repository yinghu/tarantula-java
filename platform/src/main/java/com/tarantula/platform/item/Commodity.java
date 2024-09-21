package com.tarantula.platform.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.tarantula.platform.inventory.InventoryItem;

import java.util.ArrayList;
import java.util.List;


public class Commodity extends ConfigurableObject{

    public Commodity(){}

    public Commodity(ConfigurableObject configurableObject){
        super(configurableObject);
    }

    public Commodity(JsonObject payload){
        this.application = payload;
        this.distributionId = payload.get("ConfigurationId").getAsInt();
        this.configurationName(payload.get("ConfigurationName").getAsString());
        this.configurationType(payload.get("ConfigurationType").getAsString());
        this.configurationTypeId(payload.get("ConfigurationTypeId").getAsString());
        this.configurationVersion(payload.get("ConfigurationVersion").getAsString());
        this.configurationCategory(payload.get("ConfigurationCategory").getAsString());
    }


    @Override
    public int getClassId() {
        return ItemPortableRegistry.COMMODITY_CID;
    }

    @Override
    public boolean configureAndValidate(JsonObject config){
        return super.configureAndValidate(config) && _validate();
    }
    @Override
    public boolean configureAndValidate(){
        boolean passed = true;
        for(JsonElement je : this.reference){
            ConfigurableObject cob = new ConfigurableObject();
            cob.distributionId(je.getAsLong());
            if(!dataStore.load(cob)){
                passed = false;
                break;
            }
        }
        return passed;
    }
    @Override
    public JsonObject toJson() {
        JsonObject json = super.toJson(super.toJson());
        json.addProperty("Successful",true);
        return json;
    }
    @Override
    public  <T extends Configurable> T setup(){
        if(this.listener!=null) listener.onLoaded(this);
        _reference = new ArrayList<>();
        for(JsonElement je : reference){
            ConfigurableObject cob = new ConfigurableObject();
            cob.distributionId(je.getAsLong());
            cob.dataStore(dataStore);
            if(this.dataStore.load(cob)){
                cob.registerListener(this.listener);
                _reference.add(cob.setup());
            }
        }
        return (T)this;
    }

    private boolean _validate(){
        if(this.configurationType.equals(Configurable.COMMODITY_CONFIG_TYPE)) return true;
        if(this.configurationType.endsWith(".")) return false;
        String[] comp = this.configurationType.split("\\.");
        if(comp.length != 2) return false; //asset.xxx
        return comp[0].equals(Configurable.COMMODITY_CONFIG_TYPE);
    }

    public double amount(){
        return application.get("Amount").getAsDouble();
    }


    public InventoryItem inventoryItem(long itemId){
        InventoryItem inventoryItem = new InventoryItem(itemId,distributionId);
        inventoryItem.configurationName = configurationName;
        inventoryItem.configurationTypeId = configurationTypeId;
        return inventoryItem;
    }
    public List<PropertyEdit> stock(){
        ArrayList<PropertyEdit> edits = new ArrayList<>();
        for (JsonElement jsonElement : application.get("template").getAsJsonObject().get("application").getAsJsonObject().get("properties").getAsJsonArray()) {
            JsonObject prop = jsonElement.getAsJsonObject();
            PropertyEdit edit = new PropertyEdit();
            edit.type = prop.get("type").getAsString();
            edit.name(prop.get("name").getAsString());
            edit.edit = application.get(edit.name());
            edits.add(edit);
        }
        return edits;
    }

    public ConfigurableCategory configurableCategory(){
        ConfigurableCategory category = new ConfigurableCategory(application.get("template").getAsJsonObject());
        category.parse();
        return category;
    }

}
