package com.tarantula.platform.item;

import com.google.gson.JsonObject;
import com.icodesoftware.util.JsonUtil;

import java.io.InputStream;

public class JsonItemParser {

    public static ItemSet itemSet(InputStream json){
        try{
            ItemSet itemSet = new ItemSet();
            JsonObject temp = JsonUtil.parse(json);
            itemSet.property("description",temp.get("description").getAsString());
            itemSet.property("category",temp.get("category").getAsString());
            itemSet.property("itemList",temp.get("itemList").getAsJsonArray());
            return itemSet;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
