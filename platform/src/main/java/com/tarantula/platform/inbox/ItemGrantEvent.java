package com.tarantula.platform.inbox;

import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;

public class ItemGrantEvent extends RecoverableObject {
    public static final String LABEL = "inbox";
    public boolean completed;
    public ItemGrantEvent(){
        this.onEdge = true;
        this.label = LABEL;
    }

    public ItemGrantEvent(String name, boolean completed){
        this();
        this.name = name;
        this.completed = completed;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(name);
        buffer.writeBoolean(completed);
        return true;
    }

    //Data store read contract
    @Override
    public boolean read(DataBuffer buffer) {
        name = buffer.readUTF8();
        completed = buffer.readBoolean();
        return true;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Name",name);
        jsonObject.addProperty("Completed",completed);
        return jsonObject;
    }
}
