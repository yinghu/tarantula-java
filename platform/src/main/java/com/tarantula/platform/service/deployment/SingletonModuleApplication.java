package com.tarantula.platform.service.deployment;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.protocol.GameServerListener;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.tarantula.platform.TarantulaApplicationHeader;
import com.tarantula.platform.event.FastPlayEvent;
import com.tarantula.platform.event.GameClusterSyncEvent;

public class SingletonModuleApplication extends TarantulaApplicationHeader{

    private Module module;
    private DeploymentServiceProvider serviceProvider;

    @Override
    public void callback(Session session, byte[] payload) throws Exception {
        this.module.onRequest(session,payload);
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        this.serviceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        this.module = this.serviceProvider.module(this.descriptor);
        module.setup(context);
    }

    public boolean onEvent(Event event){
        try{
            if(event instanceof FastPlayEvent){
                this.module.onJoin(event);
            }
            else if(event instanceof GameClusterSyncEvent){
                GameServerListener listener = this.serviceProvider.gameServerListener(event.typeId());
                if(listener!=null){
                    listener.onGameClusterEventUpdated(event.name(),event.payload());
                }
            }
        }catch (Exception ex){
            //write error to client
            this.onError(event,ex);
        }
        return false;
    }

    @Override
    public void clear(){
        this.module.clear();
    }


}
