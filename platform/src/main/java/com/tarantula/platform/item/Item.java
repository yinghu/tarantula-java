package com.tarantula.platform.item;

import com.google.gson.JsonObject;
import com.icodesoftware.Consumable;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.Map;

public class Item extends RecoverableObject implements Consumable {

    private String name;
    private String category;
    private String description;

    public Item(){
        this.onEdge = true;
        this.label = "Item";
    }

    @Override
    public String id() {
        return this.distributionKey();
    }

    @Override
    public String name() {
        return name;
    }
    public void name(String name){
        this.name = name;
    }

    @Override
    public String category() {
        return category;
    }
    public void category(String category){
        this.category = category;
    }

    @Override
    public String description() {
        return description;
    }

    public void description(String description){
        this.description = description;
    }
    @Override
    public Map<String,Object> toMap(){
        super.toMap();
        properties.put("name",name);
        properties.put("category",category);
        properties.put("description",description);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        super.fromMap(properties);
        name = (String) properties.get("name");
        category = (String) properties.get("category");
        description = (String) properties.get("description");
    }
    @Override
    public JsonObject toJson(){
        JsonObject json = new JsonObject();
        json.addProperty("id",distributionKey());
        properties.forEach((k,v)->{
            if(v!=null&& (v instanceof String)){
                json.addProperty(k,(String)v);
            }
            else if(v!=null&& (v instanceof Boolean)){
                json.addProperty(k,(Boolean)v);
            }
            else if(v!=null&& (v instanceof Number)){
                json.addProperty(k,(Number)v);
            }
        });
        return json;
    }
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }


    public int getClassId() {
        return PresencePortableRegistry.ITEM_CID;
    }
}
