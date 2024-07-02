package com.tarantula.platform.inbox;

import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;

import java.util.List;

public class Mailbox extends RecoverableObject {

    public List<Announcement> announcementList;

    @Override
    public JsonObject toJson() {

        return null;
    }
}
