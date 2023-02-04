package com.tarantula.game.service;


import com.icodesoftware.Session;
import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.util.JsonUtil;

public class ErrorCommand extends ServiceCommandHeader{

    public ErrorCommand(short serviceId,boolean exported){
        super(serviceId,exported);
    }
    @Override
    public byte[] onService(Session stub, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        return JsonUtil.toSimpleResponse(false,"wrong command").getBytes();
    }
}
