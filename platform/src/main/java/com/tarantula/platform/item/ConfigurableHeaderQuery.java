package com.tarantula.platform.item;

import com.icodesoftware.RecoverableFactory;

public class ConfigurableHeaderQuery implements RecoverableFactory<ConfigurableHeader> {

    public String label;


    public ConfigurableHeaderQuery(String query){
        this.label = query;
    }

    @Override
    public ConfigurableHeader create() {
        return new ConfigurableHeader();
    }

    @Override
    public int registryId() {
        return ItemPortableRegistry.CONFIGURABLE_HEADER_CID;
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
