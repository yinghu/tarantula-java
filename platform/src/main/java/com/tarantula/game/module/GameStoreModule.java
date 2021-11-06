package com.tarantula.game.module;

import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.store.ShoppingItem;
import com.tarantula.platform.store.ShoppingItemContext;
import com.tarantula.platform.store.StoreServiceProvider;

public class GameStoreModule implements Module,Configurable.Listener<ShoppingItem>{
    private ApplicationContext context;
    private StoreServiceProvider storeServiceProvider;

    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        if(session.action().equals("onList")){
            session.write(new ShoppingItemContext(true,"shop list",this.storeServiceProvider.list()).toJson().toString().getBytes());
        }
        if(session.action().equals("onBuy")){
            if(this.storeServiceProvider.buy(session.systemId(),session.name())){
                session.write(JsonUtil.toSimpleResponse(true,"item purchased successfully").getBytes());
            }else{
                session.write(JsonUtil.toSimpleResponse(false,"item not existed->"+session.name()).getBytes());
            }
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        GameServiceProvider gameServiceProvider = this.context.serviceProvider(this.context.descriptor().typeId());
        this.storeServiceProvider = gameServiceProvider.storeServiceProvider();
        this.storeServiceProvider.registerConfigurableListener(this.context.descriptor(),this);
        this.context.log("Game store module started", OnLog.WARN);
    }
}
