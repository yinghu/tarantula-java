package com.icodesoftware.util;

import com.icodesoftware.Recoverable;

import java.io.IOException;
import java.io.OutputStream;

public class DataBufferOutputStream extends OutputStream {

    private Recoverable.DataBuffer dataBuffer;

    public DataBufferOutputStream(int size,boolean direct){
        this.dataBuffer = BufferProxy.buffer(size,direct);
    }

    @Override
    public void write(int b) throws IOException {
        if(!dataBuffer.full()){
            this.dataBuffer.writeByte((byte)b);
            return;
        }
        dataBuffer.flip();
        dataBuffer = BufferProxy.transfer(dataBuffer,BufferProxy.buffer(dataBuffer.size()*2, dataBuffer.direct()));
        dataBuffer.writeByte((byte)b);
    }

    @Override
    public void flush() throws IOException{
        this.dataBuffer.flip();
    }
    public Recoverable.DataBuffer src(){
        return dataBuffer;
    }
}
