package com.tarantula.platform.item;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.NaturalKey;

public class ConfigurableCategoryQuery implements RecoverableFactory<ConfigurableCategory> {

    private String label;
    public ConfigurableCategoryQuery(String label){
        this.label = label;
    }
    @Override
    public ConfigurableCategory create() {
        return new ConfigurableCategory();
    }

    @Override
    public int registryId() {
        return 0;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public Recoverable.Key key() {
        return new NaturalKey("class/asset");
    }
}
