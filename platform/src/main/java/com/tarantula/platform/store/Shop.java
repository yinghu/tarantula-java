package com.tarantula.platform.store;

import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.item.ItemPortableRegistry;


import java.util.List;

public class Shop extends Application {



    public Shop(){
    }
    public Shop(ConfigurableObject configurableObject){
        super(configurableObject);
    }
    public Shop(String name){
        this();
        configurationName = name;
    }

    public String name(){
        return configurationName;
    }


    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return ItemPortableRegistry.SHOP_CID;
    }

    public List<ConfigurableObject> list(){
        return _reference;
    }


}
