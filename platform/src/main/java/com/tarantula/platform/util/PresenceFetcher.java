package com.tarantula.platform.util;

import com.google.gson.JsonObject;
import com.icodesoftware.OnSession;
import com.icodesoftware.Presence;
import com.icodesoftware.Session;
import com.icodesoftware.util.HttpCaller;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.OnSessionTrack;
import com.tarantula.platform.PresenceIndex;

public class PresenceFetcher extends HttpCaller {

    public PresenceFetcher(String host){
        super(host);
    }
    public OnSession login(String loginName, String password) {
        try {
            String[] headers = new String[]{
                    Session.TARANTULA_TAG,"index/user",
                    Session.TARANTULA_ACTION,"onLogin",
                    Session.TARANTULA_MAGIC_KEY, loginName
            };
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("login",loginName);
            jsonObject.addProperty("password",password);
            String resp = super.post("user/action",jsonObject.toString().getBytes(),headers);
            JsonObject json = JsonUtil.parse(resp);
            OnSession onSession = new OnSessionTrack();
            onSession.token(json.get("Token").getAsString());
            return onSession;
        }catch (Exception ex){
            return null;
        }
    }
    public Presence presence(String token) {
        try {
            String[] headers = new String[]{
                    Session.TARANTULA_TAG,"presence/lobby",
                    Session.TARANTULA_ACTION,"onPresence",
                    Session.TARANTULA_TOKEN,token,
            };
            String resp = super.get("service/action",headers);
            System.out.println(resp);
            Presence presence = new PresenceIndex();
            return presence;
        }catch (Exception ex){
            return null;
        }
    }
}
