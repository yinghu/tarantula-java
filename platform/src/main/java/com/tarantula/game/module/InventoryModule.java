package com.tarantula.game.module;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.inventory.Inventory;
import com.tarantula.platform.item.Category;

public class InventoryModule implements Module {

    private ApplicationContext context;
    private GameServiceProvider gameServiceProvider;


    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        if(session.action().equals("onCategory")){
            Category category = gameServiceProvider.inventoryServiceProvider().category();
            session.write(category.toJson().toString().getBytes());
        }
        else if(session.action().equals("onInventory")){
            String[] query = session.name().split("#");
            Inventory inventory = gameServiceProvider.inventoryServiceProvider().inventory(session.systemId(),query[0],query[1]);
            if(inventory!=null){
                session.write(inventory.toJson().toString().getBytes());
            }else{
                session.write(JsonUtil.toSimpleResponse(false,"no inventory").getBytes());
            }
        }
        else if(session.action().equals("onCommodity")){
            String[] query = session.name().split("#");
            Inventory inventory = gameServiceProvider.inventoryServiceProvider().inventory(session.systemId(),query[0],query[1]);
            Configurable commodity;
            if(inventory!=null&&(commodity=inventory.load(query[1]))!=null){
                session.write(commodity.toJson().toString().getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,session.name()).getBytes());
            }
        }
        else{
            throw new UnsupportedOperationException(session.action()+" not support");
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.gameServiceProvider = this.context.serviceProvider(context.descriptor().typeId());
        this.gameServiceProvider.exportServiceModule(this.context.descriptor().tag(),this);
        this.context.log("Inventory module started on tag ->"+this.context.descriptor().tag(), OnLog.WARN);
    }
    @Override
    public void clear(){

    }

}
