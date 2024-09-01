package com.tarantula.platform.item;

import com.google.gson.JsonElement;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;

public class PropertyEdit extends RecoverableObject {

    public static final String LABEL = "property_edit";

    public JsonElement edit;

    public PropertyEdit(){
        this.label = LABEL;
        this.onEdge = true;
    }

    @Override
    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return ItemPortableRegistry.PROPERTY_EDIT_CID;
    }

    public boolean read(DataBuffer buffer){
        edit = JsonUtil.parseAsJsonElement(buffer.readUTF8().getBytes());
        return true;
    }
    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(edit.toString());
        return true;
    }
}
