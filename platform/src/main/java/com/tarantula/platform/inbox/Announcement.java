package com.tarantula.platform.inbox;

import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;

public class Announcement extends RecoverableObject {

    //gridly loc string
    public String subject;
    public String body;

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Subject",subject);
        jsonObject.addProperty("Body",body);
        return jsonObject;
    }
}
