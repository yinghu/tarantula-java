package com.tarantula.platform.presence;


import com.tarantula.*;
import com.tarantula.platform.ResponseHeader;

import java.util.List;

/**
 * Developer: YINGHU LU
 * Updated by yinghu lu on 8/29/2019.
 */
public class PresenceContext extends ResponseHeader {

    public List<Lobby> lobbyList;
    public OnSession presence;
    public OnView view;
    public Connection connection;
    public String googleClientId;
    public PresenceContext(){
        this.code = 200;
        this.successful=true;
    }
    public PresenceContext(String command){
        this.command = command;
        this.code = 200;
        this.successful=true;
    }
}
