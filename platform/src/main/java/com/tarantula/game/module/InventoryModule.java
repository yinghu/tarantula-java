package com.tarantula.game.module;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.inventory.Inventory;
import com.tarantula.platform.item.Category;

import java.util.Map;


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
            Inventory inventory = gameServiceProvider.inventoryServiceProvider().inventory(session.systemId(),session.name());
            if(inventory!=null){
                session.write(inventory.toJson().toString().getBytes());
            }else{
                session.write(JsonUtil.toSimpleResponse(false,"no inventory").getBytes());
            }
        }
        else if(session.action().equals("onCommodity")){
            String[] query = session.name().split("#");
            Inventory inventory = gameServiceProvider.inventoryServiceProvider().inventory(session.systemId(),query[0]);
            Configurable commodity;
            if(inventory!=null&&(commodity=inventory.load(query[1]))!=null){
                session.write(commodity.toJson().toString().getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,session.name()).getBytes());
            }
        }
        else if(session.action().equals("onValidate")){
            Map<String,Object> params = JsonUtil.toMap(session.payload());
            if(this.context.validator().validateToken(params)){
                session.write(JsonUtil.toSimpleResponse(true,"receipt validated").getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"receipt not validated").getBytes());
            }
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.gameServiceProvider = this.context.serviceProvider(context.descriptor().typeId());
        this.context.log("Inventory module started", OnLog.WARN);
    }
    @Override
    public void clear(){

    }

}
