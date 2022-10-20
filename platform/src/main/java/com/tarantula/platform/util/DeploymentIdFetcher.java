package com.tarantula.platform.util;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.util.HttpCaller;
import com.icodesoftware.util.JsonUtil;

public class DeploymentIdFetcher extends HttpCaller {


    public DeploymentIdFetcher(String host){
        super(host);
    }

    public String deploymentId(String accessKey){
        try{
            _init();
            String[] headers = new String[]{
                Session.TARANTULA_ACCESS_KEY, accessKey
            };
            String resp = super.get("backup/deployment",headers);
            JsonObject json = JsonUtil.parse(resp);
            if(!json.get("successful").getAsBoolean()) return null;
            return json.get("message").getAsString();
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }
}
