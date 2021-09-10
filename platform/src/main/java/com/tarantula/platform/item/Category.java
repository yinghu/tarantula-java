package com.tarantula.platform.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.tarantula.platform.IndexSet;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Category extends IndexSet implements Configurable {

    private Set<CategoryItem> itemList = new HashSet<>();

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
        list(ci->true);
    }
    public void list(Filter filter){
        keySet.forEach((k)->{
            CategoryItem categoryItem = new CategoryItem();
            categoryItem.distributionKey(k);
            if(dataStore.load(categoryItem)){
                if(filter.onFilter(categoryItem)) itemList.add(categoryItem);
            }
        });
    }
    public void addItem(CategoryItem item){
        if(itemList.add(item)){
            dataStore.create(item);
            keySet.add(item.distributionKey());
            dataStore.update(this);
        }
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
    public interface Filter{
        boolean onFilter(CategoryItem categoryItem);
    }
}
