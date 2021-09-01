package com.tarantula.platform.item;

public class Asset extends ConfigurableObject{


    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return ItemPortableRegistry.ASSET_CID;
    }

    public boolean configureAndValidate(byte[] data){
        return super.configureAndValidate(data);
    }

}
