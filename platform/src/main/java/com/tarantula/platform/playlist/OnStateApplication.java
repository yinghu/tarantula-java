package com.tarantula.platform.playlist;

import com.tarantula.*;
import com.tarantula.platform.*;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Updated by yinghu on 6/17/2019
 */
public class OnStateApplication extends TarantulaApplicationHeader {

    private ConcurrentHashMap<String,OnSession> oMap = new ConcurrentHashMap<>();

    @Override
    public void callback(Session session, byte[] payload) throws Exception {

    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        this.context.dataStore("session").registerRecoverableListener(new SessionPortableRegistry()).addRecoverableFilter(SessionPortableRegistry.ON_SESSION_CID,(r)->{
            OnSession os = (OnSession)r;
            if(os.online()){
                oMap.put(os.distributionKey(),os);
            }else{
                oMap.remove(os.distributionKey());
            }
            //this.context.log(os.toString(),OnLog.WARN);
        });
        this.context.log("On State Application Started", OnLog.INFO);
    }
}
