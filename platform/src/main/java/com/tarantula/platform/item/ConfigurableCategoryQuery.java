package com.tarantula.platform.item;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.NaturalKey;

public class ConfigurableCategoryQuery implements RecoverableFactory<ConfigurableCategory> {

    public static final Recoverable.Key AssetKey = new NaturalKey("class/asset");
    public static final Recoverable.Key ComponentKey = new NaturalKey("class/component");

    public static final Recoverable.Key CommodityKey = new NaturalKey("class/commodity");
    public static final Recoverable.Key ItemKey = new NaturalKey("class/item");
    public static final Recoverable.Key ApplicationKey = new NaturalKey("class/application");


    private Recoverable.Key key;
    private String label;
    public ConfigurableCategoryQuery(Recoverable.Key key,String label) {
        this.key = key;
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
        return key;
    }
}
