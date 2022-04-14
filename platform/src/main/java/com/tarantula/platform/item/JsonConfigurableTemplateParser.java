package com.tarantula.platform.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.util.JsonUtil;

import java.io.InputStream;

public class JsonConfigurableTemplateParser {

    public static ConfigurableTemplate itemSet(InputStream json){
        try{
            ConfigurableTemplate itemSet = new ConfigurableTemplate();
            JsonObject temp = JsonUtil.parse(json);
            itemSet.type = temp.has("type")?temp.get("type").getAsString():"";
            itemSet.category = temp.has("category")?temp.get("category").getAsString():"";
            itemSet.version = temp.has("version")?temp.get("version").getAsString():"";
            itemSet.description = temp.has("description")?temp.get("description").getAsString():"";
            itemSet.name = temp.has("name")?temp.get("name").getAsString():"";
            JsonArray items = temp.get("itemList").getAsJsonArray();
            JsonArray exposed = new JsonArray();
            items.forEach((item)->{
                JsonObject template = item.getAsJsonObject();
                if(!template.has("header")) template.add("header",new JsonObject());
                if(!template.has("application")) template.add("application",new JsonObject());
                if(!template.has("payload")) template.add("payload",new JsonObject());
                if(!template.has("reference")) template.add("reference",new JsonArray());
                JsonObject header = template.getAsJsonObject("header");
                if(!header.has("enabled")||header.get("enabled").getAsBoolean()){
                    exposed.add(template);
                    if(itemSet.type.equals("category")){
                        ConfigurableSetting setting = new ConfigurableSetting();
                        setting.type = header.get("type").getAsString();
                        setting.name = header.get("name").getAsString();
                        if(header.has("icon")) setting.icon = header.get("icon").getAsString();
                        setting.rechargeable = header.has("rechargeable")&&header.get("rechargeable").getAsBoolean();
                        setting.properties = template.getAsJsonObject("application").get("properties").getAsJsonArray();
                        itemSet.settings.put(setting.type,setting);
                    }
                }
            });
            itemSet.property("itemList",exposed);
            return itemSet;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
