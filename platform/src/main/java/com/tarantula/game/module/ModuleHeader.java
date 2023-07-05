package com.tarantula.game.module;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.tarantula.game.service.PlatformGameServiceProvider;

public class ModuleHeader implements Module {

    protected ApplicationContext context;
    protected PlatformGameServiceProvider gameServiceProvider;
    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        this.gameServiceProvider = gameServiceProvider();
        if(this.descriptor().accessMode()==Access.PRIVATE_ACCESS_MODE) this.gameServiceProvider.exportServiceModule(this.descriptor().tag(),this);
    }

    @Override
    public Descriptor descriptor(){
        return this.context.descriptor();
    }
    @Override
    public void clear() {
        this.gameServiceProvider.removeServiceModule(this.descriptor().tag());
    }

    protected PlatformGameServiceProvider gameServiceProvider(){
        if(this.descriptor().typeId().endsWith("data")){
            return this.context.serviceProvider(this.context.descriptor().typeId().replace("-data","-service"));
        }
        if(this.descriptor().typeId().endsWith("lobby")){
            return context.serviceProvider(context.descriptor().typeId().replace("-lobby","-service"));
        }
        return this.context.serviceProvider(this.descriptor().typeId());
    }
}
