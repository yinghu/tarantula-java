package com.tarantula.platform.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.tarantula.platform.IndexSet;

import java.util.ArrayList;
import java.util.Map;

public class Category extends IndexSet implements Configurable {

    private ArrayList<CategoryItem> itemList = new ArrayList<>();

    public Category(){
        this.label = "category";
    }

    @Override
    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }

    @Override
    public Map<String,Object> toMap(){
        super.toMap();
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        super.fromMap(properties);
    }

    @Override
    public int getClassId() {
        return ItemPortableRegistry.CATEGORY_CID;
    }

    public void list(){
        keySet.forEach((k)->{
            CategoryItem categoryItem = new CategoryItem();
            categoryItem.distributionKey(k);
            if(dataStore.load(categoryItem)){
                itemList.add(categoryItem);
            }
        });
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",true);
        jsonObject.addProperty("name",label);
        JsonArray items = new JsonArray();
        itemList.forEach((item)->items.add(item.toJson()));
        jsonObject.add("itemList",items);
        return jsonObject;
    }
}
