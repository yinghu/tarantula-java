package com.tarantula.platform.item;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;


public class ConfigurableObjectQuery implements RecoverableFactory<ConfigurableObject> {

    public String label;
    public Recoverable.Key key;

    public ConfigurableObjectQuery(Recoverable.Key key, String query){
        this.key = key;
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
    public Recoverable.Key key() {
        return key;
    }
}
