package com.tarantula.platform.store;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.tarantula.platform.item.*;

import java.util.ArrayList;


public class ShoppingItem extends ConfigurableObject implements Configurable.Listener<Commodity>{

    private ArrayList<ConfigurableObject> _reference;
    private boolean validated;
    public ShoppingItem(){

    }

    public ShoppingItem(ConfigurableObject configurableObject){
        super(configurableObject);
    }

    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return ItemPortableRegistry.SHOPPING_ITEM_CID;
    }

    public String name(){
        return configurationName();
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = super.toJson();
        _setup();
        _reference.forEach((cob)-> json.add(cob.distributionKey(),cob.toJson()));
        return json;
    }
    @Override
    public boolean configureAndValidate() {
        Application app = new Application(this);
        app.dataStore(this.dataStore);
        app.registerListener(this);
        app.setup();
        return validated;
    }
    public  <T extends Configurable> T _setup(){
        _reference = new ArrayList<>();
        for(JsonElement je : reference){
            ConfigurableObject cob = new ConfigurableObject();
            cob.distributionKey(je.getAsString());
            cob.dataStore(dataStore);
            if(this.dataStore.load(cob)){
                _reference.add(cob.setup());
            }
        }
        return (T)this;
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
            this.registerListener(this.listener);
            return (T)this;
        }
        return null;
    }

    @Override
    public void onLoaded(Commodity commodity){
        this.validated = true;
    }
}
