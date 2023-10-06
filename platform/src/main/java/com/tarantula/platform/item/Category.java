package com.tarantula.platform.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.icodesoftware.Descriptor;
import com.icodesoftware.util.RecoverableObject;

import java.util.HashSet;
import java.util.Set;

public class Category extends RecoverableObject implements Configurable {

    private Set<CategoryItem> itemList = new HashSet<>();
    private Descriptor app;
    public Category(Descriptor app){
        this.label = "inventory_category";
        this.app = app;
    }

    public void list(){
        list(ci->true);
    }
    public void list(Filter filter){
        CategoryItemQuery query = new CategoryItemQuery(app.key(),label);
        dataStore.list(query).forEach(c->{
            if(filter.onFilter(c)) itemList.add(c);
        });
    }
    public void addItem(CategoryItem item){
        if(itemList.add(item)){
            if(dataStore.create(item)){
                item.ownerKey(app.key());
                item.onEdge(true);
                item.label(label);
                dataStore.createEdge(item,label);
            }
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
