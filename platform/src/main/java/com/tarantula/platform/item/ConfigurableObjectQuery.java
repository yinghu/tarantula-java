package com.tarantula.platform.item;

import com.icodesoftware.RecoverableFactory;

public class ConfigurableObjectQuery implements RecoverableFactory<ConfigurableObject> {

    public String label;
    public String itemId;

    public ConfigurableObjectQuery(String query){
        this.label = query;
    }

    public ConfigurableObjectQuery(String itemId,String query){
        this.itemId = itemId;
        this.label = query;
    }


    @Override
    public ConfigurableObject create() {
        return this.itemId==null?new ConfigurableObject():new VersionedConfigurableObject();
    }

    @Override
    public int registryId() {
        return ItemPortableRegistry.CONFIGURABLE_OBJECT_CID;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public String distributionKey() {
        return itemId;
    }
}
