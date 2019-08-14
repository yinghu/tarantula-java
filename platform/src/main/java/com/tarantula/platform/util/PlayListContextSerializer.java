package com.tarantula.platform.util;

import com.google.gson.*;
import com.tarantula.OnPlay;
import com.tarantula.platform.playlist.PlayListContext;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Updated by yinghu on 4/24//2018.
 */
public class PlayListContextSerializer implements JsonSerializer<PlayListContext> {

    public JsonElement serialize(PlayListContext playListContext, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        try{
            jo.addProperty("successful", playListContext.successful());
            JsonObject rlist = new JsonObject();
            if(playListContext.recentPlayList!=null){
                rlist.addProperty("owner",playListContext.recentPlayList.owner());
                rlist.addProperty("name",playListContext.recentPlayList.name());
                rlist.addProperty("size",playListContext.recentPlayList.size());
                //rlist.addProperty("editable", playListContext.recentPlayList.editable());
                List<OnPlay> list = playListContext.recentPlayList.onPlay();
                JsonArray ja = new JsonArray();
                OnPlaySerializer ser = new OnPlaySerializer();
                for(OnPlay o: list) {
                    if(o!=null){
                        ja.add(ser.serialize(o,type,jsonSerializationContext));
                    }
                }
                rlist.add("onList",ja);
                jo.add("recentPlayList",rlist);
            }
            if(playListContext.myPlayList!=null){
                rlist.addProperty("owner",playListContext.myPlayList.owner());
                rlist.addProperty("name",playListContext.myPlayList.name());
                rlist.addProperty("size",playListContext.myPlayList.size());
                //rlist.addProperty("editable",playListContext.myPlayList.editable());
                List<OnPlay> list = playListContext.myPlayList.onPlay();
                JsonArray ja = new JsonArray();
                OnPlaySerializer ser = new OnPlaySerializer();
                for(OnPlay o: list) {
                    if(o!=null){
                        ja.add(ser.serialize(o,type,jsonSerializationContext));
                    }
                }
                rlist.add("onList",ja);
                jo.add("playList",rlist);
            }
            if(playListContext.availableList!=null){
                JsonArray ja = new JsonArray();
                playListContext.availableList.forEach((String a)->{
                    ja.add(a);
                });
                jo.add("availableList",ja);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return jo;
    }
}
