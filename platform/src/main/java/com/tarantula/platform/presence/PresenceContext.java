package com.tarantula.platform.presence;


import com.tarantula.*;
import com.tarantula.platform.ResponseHeader;

import java.util.List;

/**
 * Developer: YINGHU LU
 * Updated by yinghu lu on 6/16/2020
 */
public class PresenceContext extends ResponseHeader {

    public Access access;
    public Account account;
    public Subscription subscription;

    public List<Access.Role> roleList;
    public List<Lobby> lobbyList;
    public OnSession presence;
    public String googleClientId;
    public String stripeClientId;
    public Connection connection;
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
