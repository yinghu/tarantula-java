package com.icodesoftware.test;

public class SampleCloseable implements AutoCloseable{
    @Override
    public void close() throws Exception {
        System.out.println("Close now");
    }

    public void doSome(){
        System.out.println("doing things");
    }
}
