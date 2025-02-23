package com.icodesoftware.protocol.configuration;

import com.google.gson.JsonElement;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;

public class TRPropertyEdit extends RecoverableObject {

    public static final String LABEL = "property_edit";

    public String type;
    public JsonElement edit;

    public TRPropertyEdit(){
        this.label = LABEL;
        this.onEdge = true;
    }

    public TRPropertyEdit(String type,String name,JsonElement edit){
        this();
        this.type = type;
        this.name = name;
        this.edit = edit;
    }

    public boolean read(DataBuffer buffer){
        name = buffer.readUTF8();
        type = buffer.readUTF8();
        edit = JsonUtil.parseAsJsonElement(buffer.readUTF8().getBytes());
        return true;
    }
    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(name);
        buffer.writeUTF8(type);
        buffer.writeUTF8(edit.toString());
        return true;
    }

    public String toString(){
        return type+"<:> "+name+"<:>"+edit;
    }
}
