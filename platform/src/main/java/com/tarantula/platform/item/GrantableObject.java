package com.tarantula.platform.item;

import com.icodesoftware.Configurable;

public class GrantableObject extends Application implements Configurable.Listener<Commodity> {

    protected boolean validated;

    public GrantableObject(){}

    public GrantableObject(ConfigurableObject configurableObject){
        super(configurableObject);

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
            super.setup();
            return (T)this;
        }
        return null;
    }
    @Override
    public boolean configureAndValidate() {
        //Application app = new Application(this);
        //app.dataStore(this.dataStore);
        //app.registerListener(this);
        setup();
        return validated;
    }
    @Override
    public void onLoaded(Commodity commodity){
        this.validated = true;
    }


}
