package com.tarantula.platform.item;

import com.icodesoftware.Configurable;
import com.icodesoftware.util.RecoverableObject;

public class Item extends RecoverableObject implements Configurable {

    protected String configurationType;
    protected String configurationName;
    protected String configurationCategory;

    public String configurationType(){return this.configurationType;}
    public void configurationType(String configurationType){
        this.configurationType = configurationType;
    }
    public String configurationName(){return configurationName;}
    public void configurationName(String configurationName){
        this.configurationName = configurationName;
    }
    public String configurationCategory(){return configurationCategory;}
    public void configurationCategory(String configurationCategory){
        this.configurationCategory = configurationCategory;
    }


    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return ItemPortableRegistry.ITEM_CID;
    }

    public boolean configureAndValidate(byte[] data){
        return true;
    }

}
