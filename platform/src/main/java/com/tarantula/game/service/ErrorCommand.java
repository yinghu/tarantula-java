package com.tarantula.game.service;



public class ErrorCommand extends GameServiceProxyHeader {

    public static final ErrorCommand ERROR_COMMAND = new ErrorCommand((short)0,false);

    public ErrorCommand(short serviceId,boolean exported){
        super(serviceId,exported);
    }

}
