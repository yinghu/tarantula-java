package com.icodesoftware.util;

import com.icodesoftware.Recoverable;

import java.io.IOException;
import java.io.InputStream;


public class DataBufferInputStream extends InputStream {

    private final Recoverable.DataBuffer dataBuffer;

    public DataBufferInputStream(Recoverable.DataBuffer dataBuffer){
        this.dataBuffer = dataBuffer;
    }


    @Override
    public int read() throws IOException {
        if(!dataBuffer.hasRemaining()) return -1;
        return dataBuffer.readByte();
    }
}
