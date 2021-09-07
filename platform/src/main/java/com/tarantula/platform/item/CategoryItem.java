package com.tarantula.platform.item;

import com.google.gson.JsonObject;
import com.icodesoftware.util.JsonUtil;

import java.util.Map;

public class CategoryItem extends ConfigurableObject{


    public CategoryItem(){}
    public CategoryItem(String category,String name){
        this.configurationCategory = category;
        this.configurationName = name;
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put(CATEGORY_KEY, this.configurationCategory);
        this.properties.put(NAME_KEY, this.configurationName);
        this.properties.put(HEADER_KEY,header.toString());
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.configurationCategory = (String) properties.get(CATEGORY_KEY);
        this.configurationName = (String) properties.get(NAME_KEY);
        this.header = JsonUtil.parse((String) properties.getOrDefault(HEADER_KEY, "{}"));
    }
    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return ItemPortableRegistry.CATEGORY_ITEM_CID;
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("category",configurationCategory);
        jsonObject.addProperty("name",configurationName);
        header.entrySet().forEach((je)->jsonObject.add(je.getKey(),je.getValue()));
        return jsonObject;
    }

    @Override
    public boolean equals(Object obj){
        CategoryItem categoryItem =(CategoryItem)obj;
        return categoryItem.configurationCategory.equals(configurationCategory);
    }
    @Override
    public int hashCode(){
        return configurationCategory.hashCode();
    }

}
