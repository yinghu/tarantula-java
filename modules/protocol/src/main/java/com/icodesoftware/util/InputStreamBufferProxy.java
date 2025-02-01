package com.icodesoftware.util;

import com.icodesoftware.Recoverable;

import java.io.DataInputStream;
import java.io.InputStream;

public class InputStreamBufferProxy {

    private final DataInputStream src;

    private InputStreamBufferProxy(InputStream inputStream){
        this.src = new DataInputStream(inputStream);
    }

    public Recoverable.DataHeader readHeader(){
        try{
            return new LocalHeader(src.readLong(),src.readInt(),src.readInt());
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public String readUTF(){
        try{
            return src.readUTF();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public static InputStreamBufferProxy proxy(InputStream src){
        return new InputStreamBufferProxy(src);
    }

}
