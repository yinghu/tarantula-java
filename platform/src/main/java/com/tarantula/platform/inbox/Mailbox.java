package com.tarantula.platform.inbox;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;

import java.util.ArrayList;
import java.util.List;

public class Mailbox extends RecoverableObject {


    @Override
    public JsonObject toJson() {
        JsonObject resp = new JsonObject();
        resp.addProperty("Successful",true);
        JsonArray announcements = new JsonArray();

        resp.add("_announcements",announcements);
        return resp;
    }
}
