package com.tarantula.platform.presence;


import com.tarantula.*;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.leveling.XPLevel;

import java.util.List;

/**
 * Developer: YINGHU LU
 * Updated by yinghu lu on 8/29/2019.
 */
public class PresenceContext extends ResponseHeader {

    public List<Lobby> lobbyList;
    public XPLevel level;
    public List<XP> xp;
    public OnSession presence;
    public OnView view;
    public Configuration connection;
    public Profile profile;
    public PresenceContext(){

    }
    public PresenceContext(String command){
        this.command = command;
        this.code = 200;
        this.successful=true;
    }
}
