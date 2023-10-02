package com.tarantula.platform.item;

import com.icodesoftware.util.NaturalKey;
import com.icodesoftware.util.RecoverableObject;

public class CategoryReference extends RecoverableObject {


    public CategoryReference(){

    }

    public CategoryReference(String name,String index){
        this.name = name;
        this.index = index;
    }

    @Override
    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return ItemPortableRegistry.CATEGORY_REFERENCE;
    }

    @Override
    public Key key() {
        return new NaturalKey(index);
    }

    @Override
    public Key ownerKey() {
        return new NaturalKey(name);
    }

}
