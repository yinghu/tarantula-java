package com.tarantula.platform.util;

import java.util.List;

/**
 * Updated by yinghu on 6/15/2018.
 */
public class DrainBuffer<T> {

    private T[] buffer;
    private int index;
    private int overflow;
    private boolean reset;

    public DrainBuffer(int size,T[] tlist){
        buffer =  tlist;
        index = -1;
        this.reset = false;
        overflow = size-1;
    }
    public DrainBuffer(int size,T[] tlist,boolean reset){
        buffer =  tlist;
        index = -1;
        this.reset = reset;
        overflow = size-1;
    }
    public int size(){
        return overflow+1;
    }
    public synchronized int push(T t){
        if(index==overflow){
            index =-1;//reset to oldest
        }
        buffer[++index]=t;
        return index;
    }
    public synchronized List<T> drain(List<T> olist){
        if(index!=-1){
            int p = index;
            for(int i=0;i<overflow+1;i++){
                int rx = p--;
                T t = buffer[rx];
                if(t!=null){
                    if(reset){
                        buffer[rx]=null;
                    }
                    olist.add(t);
                }
                if(p==-1){
                    p=overflow;
                }
            }
        }
        return olist;
    }
}