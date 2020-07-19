package com.tarantula.playlist;

import com.tarantula.ApplicationContext;
import com.tarantula.Module;
import com.tarantula.OnLog;
import com.tarantula.Session;

public class RecentlyPlayListModule implements Module {

    private ApplicationContext context;

    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate onUpdate) throws Exception {
        session.write(payload,label());
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.context.log("Recently Play List started on tag->"+context.descriptor().tag(), OnLog.WARN);
    }

    @Override
    public String label() {
        return "playlist";
    }
}
