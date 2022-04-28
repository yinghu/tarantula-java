package com.tarantula.platform.item;

import com.google.gson.JsonObject;
import com.icodesoftware.util.JsonUtil;

import java.util.Map;

public class CategoryItem extends ConfigurableObject{


    public CategoryItem(){}
    public CategoryItem(String type,String category,String typeId){
        this.configurationType = type;
        this.configurationCategory = category;
        this.configurationTypeId = typeId;
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put(TYPE_KEY,this.configurationType);
        this.properties.put(CATEGORY_KEY, this.configurationCategory);
        this.properties.put(TYPE_ID_KEY, this.configurationTypeId);
        this.properties.put(HEADER_KEY,header.toString());
        this.properties.put(APPLICATION_KEY,application.toString());
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.configurationType  = (String) properties.get(TYPE_KEY);
        this.configurationCategory = (String) properties.get(CATEGORY_KEY);
        this.configurationTypeId = (String) properties.get(TYPE_ID_KEY);
        this.header = JsonUtil.parse((String) properties.getOrDefault(HEADER_KEY, "{}"));
        this.application = JsonUtil.parse((String) properties.getOrDefault(APPLICATION_KEY, "{}"));
    }

    @Override
    public int getClassId() {
        return ItemPortableRegistry.CATEGORY_ITEM_CID;
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type",configurationType);
        jsonObject.addProperty("category",configurationCategory);
        jsonObject.addProperty("typeId",configurationTypeId);
        header.entrySet().forEach((je)->jsonObject.add(je.getKey(),je.getValue()));
        return jsonObject;
    }

    @Override
    public boolean equals(Object obj){
        CategoryItem categoryItem =(CategoryItem)obj;
        return categoryItem.configurationType().equals(configurationType)&&categoryItem.configurationTypeId().equals(configurationTypeId);
    }
    @Override
    public int hashCode(){
        return (configurationType+configurationTypeId).hashCode();
    }

}
