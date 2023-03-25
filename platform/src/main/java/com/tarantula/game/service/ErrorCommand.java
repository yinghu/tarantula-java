package com.tarantula.game.service;



public class ErrorCommand extends GameServiceProxyHeader {


    public final static ErrorCommand ERROR_COMMAND = new ErrorCommand((short) 0);

    public ErrorCommand(short serviceId){
        super(serviceId,null);
    }

}
