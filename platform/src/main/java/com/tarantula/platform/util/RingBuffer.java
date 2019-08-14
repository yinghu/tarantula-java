package com.tarantula.platform.util;

public class RingBuffer<T>{

    private T[] _buffer;
    private int index;
    private int limit;
    public RingBuffer(T[] buffer){
        _buffer = buffer;
        index = 0;
        limit = -1;
    }

    public synchronized T pop(){
        T t = _buffer[index];
        if((index+1)<=limit){
            index++;
        }
        else{
          index=0;
        }
        return t;
    }
    public synchronized void reset(Reset<T> reset){
        this._buffer = reset.reset(this._buffer,this.limit+1);
        this.limit = -1;
        for(int i=0;i<this._buffer.length;i++){
            if(this._buffer[i]!=null){
                this.limit = i;
            }
            else{
                break;
            }
        }
    }
    public synchronized boolean push(T t){
        if((limit+1)<_buffer.length){
            _buffer[++limit]=t;
            return true;
        }
        else{
            return false;
        }
    }

    public interface Reset<T>{
        T[] reset(T[] old,int limit);
    }

}
