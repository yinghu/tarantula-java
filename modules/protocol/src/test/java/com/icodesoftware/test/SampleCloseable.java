package com.icodesoftware.test;

public class SampleCloseable implements AutoCloseable{

    public boolean closed;

    @Override
    public void close() throws Exception {
        //System.out.println("Close now");
        closed = true;
    }

    public void doSome(){
        //System.out.println("doing things");
    }
}
