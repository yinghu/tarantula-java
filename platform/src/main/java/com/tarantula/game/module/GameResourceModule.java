package com.tarantula.game.module;

import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.GameResourceContext;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.resource.GameResource;
import com.tarantula.platform.resource.PlatformResourceServiceProvider;

import java.util.List;


public class GameResourceModule implements Module,Configurable.Listener<ConfigurableObject>{
    private ApplicationContext context;
    private PlatformResourceServiceProvider platformResourceServiceProvider;

    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        if(session.action().equals("onList")){
            List<GameResource> gameResources = this.platformResourceServiceProvider.list();
            session.write(new GameResourceContext(true,"game resource list",gameResources).toJson().toString().getBytes());
        }
        else if(session.action().equals("onResource")){
            GameResource gameResource = this.platformResourceServiceProvider.list(session.name());
            session.write(gameResource.toJson().toString().getBytes());
        }
        else if(session.action().equals("onGrant")){
            session.write(JsonUtil.toSimpleResponse(platformResourceServiceProvider.grant(session.systemId(),session.name()), session.name()).getBytes());
        }
        else{
            throw new UnsupportedOperationException(session.action()+" not support");
        }

        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        PlatformGameServiceProvider gameServiceProvider = this.context.serviceProvider(context.descriptor().typeId());
        this.platformResourceServiceProvider = gameServiceProvider.resourceServiceProvider();
        this.platformResourceServiceProvider.registerConfigurableListener(this.context.descriptor(),this);
        if(this.context.descriptor().accessMode()==Access.PRIVATE_ACCESS_MODE) gameServiceProvider.exportServiceModule(this.context.descriptor().tag(),this);
        this.context.log("Game resource module started", OnLog.WARN);
    }
    public void onCreated(ConfigurableObject item){
        this.context.log(item.toJson().toString(),OnLog.WARN);
    }

    public Descriptor descriptor(){
        return this.context.descriptor();
    }
}
