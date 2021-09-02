package com.tarantula.platform.item;

import com.icodesoftware.RecoverableFactory;

public class ConfigurableObjectQuery implements RecoverableFactory<ConfigurableObject> {

    public String label;

    public ConfigurableObjectQuery(){}

    public ConfigurableObjectQuery(String query){
        this.label = query;
    }

    @Override
    public ConfigurableObject create() {
        return new ConfigurableObject();
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
        return null;
    }
}
