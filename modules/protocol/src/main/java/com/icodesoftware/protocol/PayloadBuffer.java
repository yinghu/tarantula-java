package com.icodesoftware.protocol;

import java.nio.ByteBuffer;

/**
 * Created by yinghu lu on 10/15/2020.
 */
public class PayloadBuffer {
    private ByteBuffer byteBuffer;
    private boolean writeMode;
    public PayloadBuffer(){
        byteBuffer = ByteBuffer.allocate(PendingOutboundMessage.MESSAGE_SIZE);
        this.writeMode = true;
    }
    public PayloadBuffer(byte[] payload){
        byteBuffer = ByteBuffer.wrap(payload);
    }
    public void putUTF8(String str){
        if(!writeMode){
            throw new UnsupportedOperationException();
        }
        byte[] data = str.getBytes();
        byteBuffer.putInt(data.length);
        byteBuffer.put(data);
    }
    public String getUTF8(){
        byte[] str = new byte[byteBuffer.getInt()];
        byteBuffer.get(str);
        return new String(str);
    }
    public void putInt(int i){
        if(!writeMode){
            throw new UnsupportedOperationException();
        }
        byteBuffer.putInt(i);
    }
    public int getInt(){
        return byteBuffer.getInt();
    }
    public void putFloat(float i){
        if(!writeMode){
            throw new UnsupportedOperationException();
        }
        byteBuffer.putFloat(i);
    }
    public float getFloat(){
        return byteBuffer.getFloat();
    }
    public void putLong(long i){
        if(!writeMode){
            throw new UnsupportedOperationException();
        }
        byteBuffer.putLong(i);
    }
    public long getLong(){
        return byteBuffer.getLong();
    }
    public byte[] toArray(){
        byteBuffer.flip();
        byte[] _payload = new byte[byteBuffer.limit()];
        byteBuffer.position(0);
        byteBuffer.get(_payload);
        return _payload;
    }
}
