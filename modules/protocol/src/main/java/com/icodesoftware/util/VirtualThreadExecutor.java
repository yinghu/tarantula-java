package com.icodesoftware.util;

import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;

public class VirtualThreadExecutor implements Executor {

    private final Semaphore permission;
    private VirtualThreadExecutor(int concurrentSize){
        permission = new Semaphore(concurrentSize,true);
    }

    @Override
    public void execute(Runnable command) {
        try{
            permission.acquire();
            Thread.ofVirtual().name("tarantula-virtual-thread").start(command);
        }catch (Exception ex){
            //ignore
        }
        finally {
            permission.release();
        }
    }

    public static VirtualThreadExecutor create(){
        return new VirtualThreadExecutor(8);
    }

    public static VirtualThreadExecutor create(int concurrentSize){
        return new VirtualThreadExecutor(concurrentSize);
    }

}
