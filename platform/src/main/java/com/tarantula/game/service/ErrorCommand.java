package com.tarantula.game.service;


import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.Stub;

public class ErrorCommand extends ServiceCommandHeader{

    public ErrorCommand(short serviceId,boolean exported){
        super(serviceId,exported);
    }
    @Override
    public byte[] onService(Stub stub, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        return JsonUtil.toSimpleResponse(false,"wrong command").getBytes();
    }
}
