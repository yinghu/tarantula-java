package com.tarantula.platform.presence;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


import java.util.List;

public class ProfilePayload {

    public List<Profile> profileList;

    public ProfilePayload(List<Profile> profileList){
        this.profileList = profileList;
    }

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();

        JsonArray profiles = new JsonArray();
        profileList.forEach((profile -> profiles.add(profile.toJson())));
        jsonObject.add("_profileList",profiles);

        return jsonObject;
    }
}
