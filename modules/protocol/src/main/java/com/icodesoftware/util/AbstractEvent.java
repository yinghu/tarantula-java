package com.icodesoftware.util;

import com.icodesoftware.Event;
import com.icodesoftware.EventListener;
import com.icodesoftware.service.EventService;

public class AbstractEvent extends TROnApplication implements Event {

    protected String tag;
    protected String destination;
    protected int retries;
    protected int streamingBatchSize = 512;

    protected EventService eventService;
    protected EventListener eventListener;
    @Override
    public String tag() {
        return tag;
    }

    @Override
    public void tag(String tag) {
        this.tag = tag;
    }

    @Override
    public String destination() {
        return destination;
    }

    @Override
    public void destination(String destination) {
        this.destination = destination;
    }

    @Override
    public void eventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public int retries() {
        return retries;
    }

    @Override
    public void retries(int retries) {
        this.retries = retries;
    }

    public void streaming(Streaming next){
        if(dataBuffer==null) return;
        if(dataBuffer.type()==DataBuffer.MEMORY){
            next.on(dataBuffer);
            return;
        }
        if(dataBuffer.type()==DataBuffer.RAW_INPUT_STREAM){
            while (true){
                DataBuffer buffer = BufferProxy.buffer(streamingBatchSize,true);
                dataBuffer.read(buffer);
                if(!buffer.full()){
                    buffer.flip();
                    next.on(buffer);
                    break;
                }
                buffer.flip();
                next.on(buffer);
            }
            return;
        }
        if(dataBuffer.type()==DataBuffer.BATCH_TCP){
            while (true){
                int sz  = dataBuffer.readInt();
                if(sz==0){
                    break;
                }
                DataBuffer buffer = BufferProxy.buffer(sz,true);
                dataBuffer.read(buffer);
                buffer.flip();
                next.on(buffer);
            }
        }
    }
    
    public void eventListener(EventListener eventListener){
        this.eventListener = eventListener;
    }

}
