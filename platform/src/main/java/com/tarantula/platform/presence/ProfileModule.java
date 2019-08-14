package com.tarantula.platform.presence;

import com.google.gson.GsonBuilder;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.platform.util.OnAccessDeserializer;
import com.tarantula.platform.util.ProfileContextSerializer;

public class ProfileModule implements Module {

    private GsonBuilder builder;
    private DataStore dataStore;

    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        if(session.action().equals("onProfile")){
            OnAccess cmd = this.builder.create().fromJson(new String((payload)).trim(),OnAccess.class);
            Profile u = this._load(cmd.systemId());
            ProfileContext pcx  = new ProfileContext();
            pcx.command("onProfile");
            pcx.profile = u;
            pcx.successful(true);
            session.write(builder.create().toJson(pcx).getBytes(),this.label());
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(OnAccess.class, new OnAccessDeserializer());
        this.builder.registerTypeAdapter(ProfileContext.class,new ProfileContextSerializer());
        this.dataStore = context.dataStore("profile");
    }

    @Override
    public String label() {
        return "profile";
    }
    private Profile _load(String systemId){
        Profile _px = new ProfileTrack();
        _px.distributionKey(systemId);
        if(this.dataStore.load(_px)){
            return _px;
        }
        else{
            return null;
        }
    }
}
