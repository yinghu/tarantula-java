package com.tarantula.platform.presence;

import com.tarantula.Lobby;
import com.tarantula.OnView;
import com.tarantula.platform.ResponseHeader;

import java.util.List;

/**
 * Updated by yinghu lu on 8/25/19
 */
public class IndexContext extends ResponseHeader {

    public IndexContext(String command){
        this.successful = true;
        this.command = command;
    }

    public OnView view;
    public List<Lobby> lobbyList;
}
