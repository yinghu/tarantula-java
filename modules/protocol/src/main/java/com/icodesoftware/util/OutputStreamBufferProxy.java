package com.icodesoftware.util;


import com.icodesoftware.Recoverable;

import java.io.DataOutputStream;
import java.io.OutputStream;

public class OutputStreamBufferProxy {

    private final DataOutputStream dest;

    private OutputStreamBufferProxy(OutputStream outputStream){
        this.dest = new DataOutputStream(outputStream);
    }

    public OutputStreamBufferProxy writeHeader(Recoverable.DataHeader header){
        try{
            dest.writeLong(header.revision());
            dest.writeInt(header.factoryId());
            dest.writeInt(header.classId());
            return this;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public OutputStreamBufferProxy writeUTF(String utf){
        try{
            dest.writeUTF(utf);
            return this;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }


    public static OutputStreamBufferProxy proxy(OutputStream dest){
        return new OutputStreamBufferProxy(dest);
    }

}
