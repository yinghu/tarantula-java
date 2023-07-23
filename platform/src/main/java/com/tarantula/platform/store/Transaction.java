package com.tarantula.platform.store;

import com.icodesoftware.util.NaturalKey;
import com.tarantula.platform.service.ServiceEventLog;



public class Transaction extends ServiceEventLog {

    public Transaction(){}
    public Transaction(String source,String message,String payload){
        super("transaction",Level.INFO,source,message,payload);
    }

    public void distributionKey(String distributionKey){
        this.index = distributionKey;
    }
    @Override
    public Key key(){
        return new NaturalKey(this.index);
    }
}
