package com.perfectday.games.earth8.inbox;

import com.google.gson.JsonObject;
import com.icodesoftware.OnAccess;
import com.icodesoftware.util.TROnApplication;
import com.perfectday.games.earth8.Earth8PortableRegistry;

public class PlayerAction extends TROnApplication implements OnAccess {

    public static final String LABEL = "inbox";
    public boolean completed;
    public PlayerAction(){
        this.onEdge = true;
        this.label = LABEL;
    }

    public PlayerAction(String name,boolean completed){
        this();
        this.name = name;
        this.completed = completed;
    }
    @Override
    public int getFactoryId() {
        return Earth8PortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return Earth8PortableRegistry.PLAYER_ACTION_CID;
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
