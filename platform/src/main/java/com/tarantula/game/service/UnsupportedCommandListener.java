package com.tarantula.game.service;

import com.icodesoftware.protocol.MessageBuffer;
import com.tarantula.game.GameLobby;
import com.tarantula.game.Stub;

public class UnsupportedCommandListener extends ServiceCommandHeader implements GameLobby.ServiceMessageListener {


    @Override
    public byte[] update(Stub stub, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
      return "{}".getBytes();
    }
}
