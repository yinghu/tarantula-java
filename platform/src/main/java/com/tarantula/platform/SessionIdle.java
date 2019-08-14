package com.tarantula.platform;

import com.tarantula.platform.OnApplicationHeader;

/**
 * Created by yinghu lu on 3/20/2019.
 */
public class SessionIdle extends OnApplicationHeader {


    public SessionIdle(String label,String systemId,int stub){
        this.label = label;
        this.owner = systemId;
        this.stub = stub;
    }
}
