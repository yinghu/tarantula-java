package com.icodesoftware.util;

import com.icodesoftware.Recoverable;

import java.io.IOException;
import java.io.Writer;

public class DataBufferWriter extends Writer {

    private final Recoverable.DataBuffer dataBuffer;

    public DataBufferWriter(Recoverable.DataBuffer dataBuffer){
        this.dataBuffer = dataBuffer;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        for(int i=off;i<len;i++){
            dataBuffer.writeByte((byte)cbuf[i]);
        }
    }

    @Override
    public void flush() throws IOException {
        dataBuffer.flip();
    }

    @Override
    public void close() throws IOException {

    }

    public Recoverable.DataBuffer src(){
        return dataBuffer;
    }
}
