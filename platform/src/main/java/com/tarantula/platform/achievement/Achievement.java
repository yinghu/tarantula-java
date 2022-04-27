package com.tarantula.platform.achievement;

import com.icodesoftware.Configurable;
import com.tarantula.platform.item.*;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class Achievement extends ConfigurableObject {

    public Achievement(){}

    public Achievement(ConfigurableObject configurableObject){
        super(configurableObject);
    }

    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.ACHIEVEMENT_CID;
    }
    public String name(){
        return header.get("Name").getAsString();
    }
    public double goal(){
        return header.get("Goal").getAsDouble();
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
            return (T)this;
        }
        return null;
    }
}
