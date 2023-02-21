package com.tarantula.game.service;



public class ErrorCommand extends GameServiceProxyHeader {


    public final static ErrorCommand ERROR_COMMAND = new ErrorCommand((short) 0,true);

    public ErrorCommand(short serviceId,boolean exported){
        super(serviceId,exported,null);
    }

}
