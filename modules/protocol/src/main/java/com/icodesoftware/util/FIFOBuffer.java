package com.icodesoftware.util;

import java.util.List;

public class FIFOBuffer<T> {

    private T[] buffer;

    private transient int header;
    private transient int tail;
    private transient int overflow;

    public FIFOBuffer(int size, T[] tlist){
        buffer =  tlist;
        header = 0;
        tail = 0;
        overflow = size;
    }
    public synchronized int push(T t){
        if(tail<overflow) {
            buffer[tail++] = t;
        }
        else{//overflow
            if(header>0){ //remove segment
                tail = overflow-header;
                for(int i=0;i<tail;i++){
                    buffer[i]=buffer[header++];
                }
                header = 0;
                buffer[tail++]=t;
            }
            else{//remove first
                for(int i=0;i<overflow-1;i++){
                    buffer[i]=buffer[i+1];
                }
                buffer[overflow-1]=t;
            }
        }
        return tail;
    }
    public synchronized T pop(){
        T pt = buffer[header];
        if(pt!=null){
            header++;
            header=header==overflow?0:header;
        }
        return pt;
    }
    public synchronized void reset(T[] tlist){
        for(int i=0;i<overflow;i++){
            buffer[i]= tlist[i];
        }
    }
    public synchronized T[] list(T[] tlist){
        for(int i=0;i<overflow;i++){
            tlist[i]=buffer[i];//clone origial one
        }
        return tlist;
    }
    public synchronized List<T> list(List<T> tlist){
        for(int i=0;i<overflow;i++){
            if(buffer[i]!=null){
                tlist.add(buffer[i]);
            }//clone origial one
        }
        return tlist;
    }
}
