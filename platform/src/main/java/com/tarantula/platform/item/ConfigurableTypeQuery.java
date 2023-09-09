package com.tarantula.platform.item;

import com.icodesoftware.Configurable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.NaturalKey;

public class ConfigurableTypeQuery implements RecoverableFactory<ConfigurableType> {

    public static final Recoverable.Key AssetKey = new NaturalKey("type/asset");
    public static final Recoverable.Key ComponentKey = new NaturalKey("type/component");

    public static final Recoverable.Key CommodityKey = new NaturalKey("type/commodity");
    public static final Recoverable.Key ItemKey = new NaturalKey("type/item");
    public static final Recoverable.Key ApplicationKey = new NaturalKey("type/application");


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
    public String label() {
        return label;
    }

    @Override
    public Recoverable.Key key() {
        return key;
    }

    public static ConfigurableTypeQuery query(String type,String label){
        if(type.equals(Configurable.ASSET_CONFIG_TYPE)) return new ConfigurableTypeQuery(AssetKey,label);
        if(type.equals(Configurable.COMPONENT_CONFIG_TYPE)) return new ConfigurableTypeQuery(ComponentKey,label);
        if(type.equals(Configurable.COMMODITY_CONFIG_TYPE)) return new ConfigurableTypeQuery(CommodityKey,label);
        if(type.equals(Configurable.ITEM_CONFIG_TYPE)) return new ConfigurableTypeQuery(ItemKey,label);
        if(type.equals(Configurable.APPLICATION_CONFIG_TYPE)) return new ConfigurableTypeQuery(ApplicationKey,label);
        return null;
    }
}
