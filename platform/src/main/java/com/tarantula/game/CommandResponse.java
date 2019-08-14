package com.tarantula.game;

import com.tarantula.Response;
import com.tarantula.game.casino.BetLine;

/**
 * Updated by yinghu lu on 4/30/2019.
 */
public class CommandResponse extends BetLine implements Response {
    public CommandResponse(String command,boolean successful){
        this.command = command;
        this.successful = successful;
    }
}
