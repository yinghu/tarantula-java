package com.tarantula.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.ResponseHeader;

import java.util.List;

public class GameClusterContext extends ResponseHeader {
    public List<GameCluster> gameClusterList;
    public int index;
    public int pageSize=1;
    public GameClusterContext(){
        this.successful = true;
    }
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",successful);
        JsonArray glist = new JsonArray();
        if(index>=gameClusterList.size()) index=0;
        int len = pageSize;
        for(int i=index;i<gameClusterList.size();i++){
            GameCluster g = gameClusterList.get(i);
            len--;
            glist.add(g.toJson());
            if(len==0) break;
        }
        jsonObject.addProperty("index",index+(pageSize-len));
        jsonObject.add("gameClusterList",glist);
        return jsonObject;
    }


}
