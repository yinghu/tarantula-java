package com.icodesoftware.util;

import com.google.gson.JsonObject;


public class TROwnerObject extends RecoverableObject{

    @Override
    public String toString(){
        return "Owner ["+owner+"]";
    }

    public Key key(){
        return new NaturalKey(this.owner);
    }

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("owner",owner);
        return jsonObject;
    }

    @Override
    public boolean readKey(DataBuffer buffer){
        owner = buffer.readUTF8();
        return true;
    }
    @Override
    public boolean writeKey(DataBuffer buffer){
        if(owner==null) return false;
        buffer.writeUTF8(owner);
        return true;
    }

    @Override
    public boolean validate() {
        return owner!=null && owner.length()>4;
    }
}
