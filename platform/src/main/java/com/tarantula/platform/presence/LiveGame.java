package com.tarantula.platform.presence;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tarantula.Descriptor;
import com.tarantula.Lobby;
import com.tarantula.platform.util.DescriptorSerializer;

import java.util.ArrayList;

public class LiveGame {
    public int index;
    public String name;
    public ArrayList<Lobby> lobbyList;
    public LiveGame(int index,String name){
        this.index = index;
        this.name = name;
    }
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",true);
        jsonObject.addProperty("name",name);
        jsonObject.addProperty("index",index);
        if(this.lobbyList!=null){
            DescriptorSerializer ser = new DescriptorSerializer();
            JsonArray blist = new JsonArray();
            for(Lobby lobby : this.lobbyList){
                JsonObject jlobby = new JsonObject();
                jlobby.add("descriptor",fromDescriptor(lobby.descriptor()));
                JsonArray jlist = new JsonArray();
                for(Descriptor d : lobby.entryList()){
                    //add application list
                    jlist.add(fromDescriptor(d));
                }
                jlobby.add("applications",jlist);
                blist.add(jlobby);
            }
            jsonObject.add("lobbyList",blist);
        }
        return jsonObject;
    }
    private JsonObject fromDescriptor(Descriptor descriptor){
        JsonObject jo = new JsonObject();
        jo.addProperty("type",descriptor.type());
        jo.addProperty("typeId",descriptor.typeId());
        jo.addProperty("name",descriptor.name());
        if(!descriptor.type().equals("lobby")){
            jo.addProperty("singleton",descriptor.singleton());
            jo.addProperty("tag",descriptor.tag());
            jo.addProperty("accessRank",descriptor.accessRank());
            jo.addProperty("applicationId",descriptor.distributionKey());
        }
        return jo;
    }
}
