package com.tarantula.platform.item;

import com.google.gson.JsonObject;
import com.icodesoftware.util.JsonUtil;


public class CategoryItem extends ConfigurableObject{


    public CategoryItem(){}
    public CategoryItem(String type,String category,String typeId){
        this.configurationType = type;
        this.configurationCategory = category;
        this.configurationTypeId = typeId;
    }

    public boolean read(DataBuffer buffer){
        this.configurationType = buffer.readUTF8();
        this.configurationTypeId = buffer.readUTF8();
        this.configurationCategory = buffer.readUTF8();
        this.header = JsonUtil.parse(buffer.readUTF8());
        this.application = JsonUtil.parse(buffer.readUTF8());
        return true;
    }
    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(this.configurationType);
        buffer.writeUTF8(this.configurationTypeId);
        buffer.writeUTF8(this.configurationCategory);
        buffer.writeUTF8(header.toString());
        buffer.writeUTF8(application.toString());
        return true;
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
