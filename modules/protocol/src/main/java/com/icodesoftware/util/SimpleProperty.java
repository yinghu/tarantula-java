package com.icodesoftware.util;

import com.google.gson.JsonObject;
import com.icodesoftware.Property;

public class SimpleProperty extends RecoverableObject implements Property {

    protected Object value;

    public SimpleProperty(String name,Object value){
        this.name = name;
        this.value = value;
    }

    @Override
    public Object value() {
        return this.value;
    }

    @Override
    public boolean equals(Object obj) {
        SimpleProperty p = (SimpleProperty)obj;
        return p.name().equals(name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name",name);
        jsonObject.addProperty("value",value.toString());
        return jsonObject;
    }
}
