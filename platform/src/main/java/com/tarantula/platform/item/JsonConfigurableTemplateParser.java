package com.tarantula.platform.item;

import com.google.gson.JsonObject;
import com.icodesoftware.util.JsonUtil;

import java.io.InputStream;

public class JsonConfigurableTemplateParser {

    public static ConfigurableTemplate itemSet(InputStream json){
        try{
            ConfigurableTemplate itemSet = new ConfigurableTemplate();
            JsonObject temp = JsonUtil.parse(json);
            itemSet.type = temp.get("type").getAsString();
            itemSet.category = temp.get("category").getAsString();
            itemSet.version = temp.get("version").getAsString();
            itemSet.quantity = temp.get("quantity").getAsInt();
            itemSet.description = temp.get("description").getAsString();
            itemSet.property("itemList",temp.get("itemList").getAsJsonArray());
            return itemSet;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
