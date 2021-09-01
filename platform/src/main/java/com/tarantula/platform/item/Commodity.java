package com.tarantula.platform.item;

public class Commodity extends ConfigurableObject{

    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return ItemPortableRegistry.COMMODITY_CID;
    }
}
