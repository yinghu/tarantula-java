package com.tarantula.platform.item;

import com.icodesoftware.RecoverableFactory;

public class CommodityQuery implements RecoverableFactory<Commodity> {

    public String label;

    public CommodityQuery(){}

    public CommodityQuery(String configurationType){
        this.label = configurationType;
    }

    @Override
    public Commodity create() {
        return new Commodity();
    }

    @Override
    public int registryId() {
        return ItemPortableRegistry.COMMODITY_CID;
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
