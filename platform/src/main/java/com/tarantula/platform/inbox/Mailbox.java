package com.tarantula.platform.inbox;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;

import java.util.ArrayList;
import java.util.List;

public class Mailbox extends RecoverableObject {

    public List<Announcement> announcementList = new ArrayList<>();

    @Override
    public JsonObject toJson() {
        JsonObject resp = new JsonObject();
        resp.addProperty("Successful",true);
        JsonArray announcements = new JsonArray();
        if(announcementList!=null){
            announcementList.forEach(a->{
                announcements.add(a.toJson());
            });
        }
        resp.add("_announcements",announcements);
        return resp;
    }
}
