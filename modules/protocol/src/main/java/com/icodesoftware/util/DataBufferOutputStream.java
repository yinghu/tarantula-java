package com.icodesoftware.util;

import com.icodesoftware.Recoverable;

import java.io.IOException;
import java.io.OutputStream;

public class DataBufferOutputStream extends OutputStream {

    private final Recoverable.DataBuffer dataBuffer;

    public DataBufferOutputStream(Recoverable.DataBuffer dataBuffer){
        this.dataBuffer = dataBuffer;
    }

    @Override
    public void write(int b) throws IOException {
        this.dataBuffer.writeByte((byte)b);
    }
}
