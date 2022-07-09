package com.tarantula.platform.util;

import com.google.gson.JsonObject;
import com.icodesoftware.OnSession;
import com.icodesoftware.Session;
import com.icodesoftware.util.HttpCaller;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.OnSessionTrack;

import javax.crypto.Cipher;

public class PresenceFetcher extends HttpCaller {

    public Cipher encrypt;

    public PresenceFetcher(String host){
        super(host);
    }


    public OnSession login(String loginName, String password) throws Exception{
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
    }
    public byte[] presenceKey(String token,String clusterNameSuffix) throws Exception{
        String[] headers = new String[]{
                Session.TARANTULA_TAG,"role/sudo",
                Session.TARANTULA_ACTION,"onPresenceKey",
                Session.TARANTULA_NAME,clusterNameSuffix,
                Session.TARANTULA_TOKEN,token,
        };
        String resp = super.get("service/action",headers);
        JsonObject jsonObject = JsonUtil.parse(resp);
        if(!jsonObject.get("successful").getAsBoolean()) return null;
        String skey = jsonObject.get("accessKey").getAsString();
        return SystemUtil.fromBase64String(skey);
    }
    public OnSession presence(String token) {
        try {
            String[] headers = new String[]{
                    Session.TARANTULA_TAG,"presence/lobby",
                    Session.TARANTULA_ACTION,"onSession",
                    Session.TARANTULA_TOKEN,token,
            };
            String resp = super.get("service/action",headers);
            JsonObject jsonObject = JsonUtil.parse(resp).get("presence").getAsJsonObject();
            OnSession presence = new OnSessionTrack();
            presence.systemId(jsonObject.get("systemId").getAsString());
            presence.stub(jsonObject.get("stub").getAsInt());
            presence.balance(jsonObject.get("balance").getAsDouble());
            return presence;
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public void play(String token) {
        try {
            String[] headers = new String[]{
                    Session.TARANTULA_TAG,"robotquest/lobby",
                    Session.TARANTULA_ACTION,"onPlay",
                    Session.TARANTULA_TOKEN,token,
                    Session.TARANTULA_CLIENT_ID,"SAMPLE-1",
                    Session.TARANTULA_NAME,"Sample111"
            };
            String resp = super.get("service/action",headers);
            System.out.println(resp);
            //JsonObject jsonObject = JsonUtil.parse(resp).get("presence").getAsJsonObject();
            //OnSession presence = new OnSessionTrack();
            //presence.systemId(jsonObject.get("systemId").getAsString());
            //presence.stub(jsonObject.get("stub").getAsInt());
            //presence.balance(jsonObject.get("balance").getAsDouble());
            //return presence;
        }catch (Exception ex){
            ex.printStackTrace();
            //return null;
        }
    }
}
