package com.tarantula.game.module;

import com.icodesoftware.*;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.inbox.Inbox;


public class GameInboxModule extends ModuleHeader{

    private TokenValidatorProvider tokenValidatorProvider;
    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        if(session.action().equals("onInbox")){
            Inbox inbox = this.gameServiceProvider.inboxServiceProvider().inbox(session);
            session.write(inbox.toJson().toString().getBytes());
        }
        else if(session.action().equals("onFile")){
            TokenValidatorProvider.AuthVendor download = tokenValidatorProvider.authVendor(OnAccess.DOWNLOAD_CENTER);
            byte[] payload = download.download(gameServiceProvider.gameCluster().typeId()+"#"+session.name());
            session.write(payload);
        }
        else if(session.action().equals("onRedeem")){
            boolean suc = this.gameServiceProvider.inboxServiceProvider().redeem(session,session.name());
            session.write(JsonUtil.toSimpleResponse(suc,session.name()).getBytes());
        }
        else if(session.action().equals("onInventory")){
            Inventory inventory = this.gameServiceProvider.inventoryServiceProvider().inventory(session);
            if(inventory==null) session.write(JsonUtil.toSimpleResponse(false,"Inventory ["+session.name()+"] no existed").getBytes());
            session.write(inventory.toJson().toString().getBytes());
        }
        else{
            throw new UnsupportedOperationException(session.action()+" not support");
        }

        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        super.setup(applicationContext);
        tokenValidatorProvider = applicationContext.serviceProvider(TokenValidatorProvider.NAME);
        this.context.log("Game inbox module started -"+this.context.descriptor().tag(), OnLog.WARN);
    }

}
