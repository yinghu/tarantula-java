package com.tarantula.test.integration;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class RequestResult {
    public final String name;
    public AtomicInteger totalSuccess = new AtomicInteger(0);
    public AtomicInteger totalFailure = new AtomicInteger(0);
    public AtomicLong totalTimed = new AtomicLong(0);

    public RequestResult(String name){
        this.name = name;
    }

    public String toString(){
        return "["+name+"] : total success : ["+totalSuccess.get()+"] average request timed (ms) : ["+(totalTimed.get()/totalSuccess.get())+"] total failure : ["+totalFailure.get()+"]\n";
    }
}
