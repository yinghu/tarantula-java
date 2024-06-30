package com.tarantula.platform.presence;

import com.icodesoftware.*;
import com.icodesoftware.util.ResponseHeader;

import java.util.List;

public class PresenceContext extends ResponseHeader {

    public Access access;
    public Account account;
    public Subscription subscription;
    public List<String> gameList;
    public List<Access.Role> roleList;
    public List<Lobby> lobbyList;
    public OnSession presence;

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
