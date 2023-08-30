package com.tarantula.platform.item;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.NaturalKey;

public class ConfigurableTypeQuery implements RecoverableFactory<ConfigurableType> {

    public static final Recoverable.Key AssetKey = new NaturalKey("type/asset");
    public static final Recoverable.Key ComponentKey = new NaturalKey("type/component");

    public static final Recoverable.Key CommodityKey = new NaturalKey("type/commodity");
    public static final Recoverable.Key ItemKey = new NaturalKey("type/item");
    public static final Recoverable.Key ApplicationKey = new NaturalKey("category/application");


    private Recoverable.Key key;
    private String label;
    public ConfigurableTypeQuery(Recoverable.Key key, String label) {
        this.key = key;
        this.label = label;
    }
    @Override
    public ConfigurableType create() {
        return new ConfigurableType();
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
