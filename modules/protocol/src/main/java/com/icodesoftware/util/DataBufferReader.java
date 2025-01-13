package com.icodesoftware.util;

import com.icodesoftware.Recoverable;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class DataBufferReader extends Reader {

    private Recoverable.DataBuffer dataBuffer;


    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        return 0;
    }

    @Override
    public void close() throws IOException {

    }
}
