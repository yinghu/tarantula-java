package com.tarantula.game.service;


import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.GameLobby;
import com.tarantula.game.Stub;

public class ErrorCommand extends ServiceCommandHeader implements GameLobby.ServiceMessageListener {

    @Override
    public byte[] update(Stub stub, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        return JsonUtil.toSimpleResponse(false,"wrong command").getBytes();
    }
}
