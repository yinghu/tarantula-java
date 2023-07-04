package com.tarantula.game.module;

import com.icodesoftware.Module;
import com.icodesoftware.*;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.game.util.ListSerializer;

import java.util.List;

public class PlayListModule implements Module{
    private ApplicationContext context;
    private PlatformGameServiceProvider gameServiceProvider;
    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        if(session.action().equals("onRecentlyList")){
            List<String> plist = gameServiceProvider.presenceServiceProvider().recentlyPlayList();
            session.write(ListSerializer.toJson(plist).toString().getBytes());
        }
        else if(session.action().equals("onFriendList")){
            List<String> plist = gameServiceProvider.presenceServiceProvider().friendList(session.systemId());
            session.write(ListSerializer.toJson(plist).toString().getBytes());
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
        if(this.descriptor().accessMode() == Access.PRIVATE_ACCESS_MODE) this.gameServiceProvider.exportServiceModule(this.context.descriptor().tag(),this);
        this.context.log("Play list module started", OnLog.WARN);
    }

    @Override
    public void clear() {
        this.gameServiceProvider.removeServiceModule(this.descriptor().tag());
    }

    public Descriptor descriptor(){
        return this.context.descriptor();
    }
}
