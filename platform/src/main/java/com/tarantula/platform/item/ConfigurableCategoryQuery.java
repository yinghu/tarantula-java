package com.tarantula.platform.item;

import com.icodesoftware.Configurable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.NaturalKey;

public class ConfigurableCategoryQuery implements RecoverableFactory<ConfigurableCategory> {

    public static final Recoverable.Key AssetKey = new NaturalKey("category/asset");
    public static final Recoverable.Key ComponentKey = new NaturalKey("category/component");

    public static final Recoverable.Key CommodityKey = new NaturalKey("category/commodity");
    public static final Recoverable.Key ItemKey = new NaturalKey("category/item");
    public static final Recoverable.Key ApplicationKey = new NaturalKey("category/application");


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
    public String label() {
        return label;
    }

    @Override
    public Recoverable.Key key() {
        return key;
    }

    public static ConfigurableCategoryQuery query(String type,String label){
        if(type.equals(Configurable.ASSET_CONFIG_TYPE)) return new ConfigurableCategoryQuery(AssetKey,label);
        if(type.equals(Configurable.COMPONENT_CONFIG_TYPE)) return new ConfigurableCategoryQuery(ComponentKey,label);
        if(type.equals(Configurable.COMMODITY_CONFIG_TYPE)) return new ConfigurableCategoryQuery(CommodityKey,label);
        if(type.equals(Configurable.ITEM_CONFIG_TYPE)) return new ConfigurableCategoryQuery(ItemKey,label);
        if(type.equals(Configurable.APPLICATION_CONFIG_TYPE)) return new ConfigurableCategoryQuery(ApplicationKey,label);
        return null;
    }
}
