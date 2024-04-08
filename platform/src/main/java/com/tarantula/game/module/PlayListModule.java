package com.tarantula.game.module;

import com.icodesoftware.*;
import com.tarantula.game.util.ListSerializer;

import java.util.List;

public class PlayListModule extends ModuleHeader{

    @Override
    public boolean onRequest(Session session, byte[] bytes) throws Exception {
        if(session.action().equals("onRecentlyList")){
            List<Long> plist = gameServiceProvider.presenceServiceProvider().recentlyPlayList();
            session.write(ListSerializer.toJson(plist).toString().getBytes());
        }
        else if(session.action().equals("onFriendList")){
            List<Long> plist = gameServiceProvider.presenceServiceProvider().friendList(session.systemId());
            session.write(ListSerializer.toJson(plist).toString().getBytes());
        }
        else{
            throw new UnsupportedOperationException(session.action()+" not support");
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        super.setup(applicationContext);
        this.context.log("Play list module started", OnLog.WARN);
    }

}
