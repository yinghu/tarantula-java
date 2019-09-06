package com.tarantula.platform.presence;

import com.tarantula.*;
import com.tarantula.platform.TarantulaApplicationHeader;
import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.util.PresenceContextSerializer;

/**
 * Updated by yinghu lu on 8/26/19
 */
public class ProfileApplication extends TarantulaApplicationHeader {

    private DataStore dataStore;

    private DeploymentServiceProvider deploymentServiceProvider;

    @Override
    public void callback(Session session, byte[] payload) throws Exception {
        if(session.action().equals("onProfile")){
            OnAccess cmd = this.builder.create().fromJson(new String((payload)).trim(),OnAccess.class);
            Profile u = this._load(cmd.systemId());
            PresenceContext pcx  = new PresenceContext("onProfile");
            pcx.profile = u;
            session.write(builder.create().toJson(pcx).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("content/avatar")){
            //this.context.log(session.trackId(),OnLog.INFO);
            Avatar avatar = new Avatar();
            avatar.distributionKey(session.trackId());
            if(this.dataStore.load(avatar)){
                byte[] icon = this.deploymentServiceProvider.resource(avatar.name(),null);
                session.write(icon,0,avatar.type(),this.descriptor.responseLabel(),true);
            }
            else{
                byte[] icon = this.deploymentServiceProvider.resource("assets/man.png",null);
                session.write(icon,0,"image/png",this.descriptor.responseLabel(),true);
                avatar.type("image/png");
                avatar.name("assets/man.png");
                this.dataStore.createIfAbsent(avatar,false);
            }
        }
        else{
            OnAccess cmd = this.builder.create().fromJson(new String((payload)).trim(),OnAccess.class);
            Profile u = this._load(cmd.systemId());
            PresenceContext pcx  = new PresenceContext("onProfile");
            pcx.profile = u;
            session.write(builder.create().toJson(pcx).getBytes(),this.descriptor.responseLabel());
        }
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        this.builder.registerTypeAdapter(PresenceContext.class,new PresenceContextSerializer());
        this.dataStore = this.context.dataStore("profile");
        this.deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        this.context.log("Profile application started on tag ["+descriptor.tag()+"]",OnLog.INFO);
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
