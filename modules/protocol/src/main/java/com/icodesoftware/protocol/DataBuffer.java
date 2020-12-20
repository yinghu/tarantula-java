package com.icodesoftware.protocol;

import java.nio.ByteBuffer;

/**
 * Created by yinghu lu on 10/15/2020.
 */
public class DataBuffer {
    private ByteBuffer byteBuffer;
    private boolean writeMode;
    public DataBuffer(){
        byteBuffer = ByteBuffer.allocate(OutboundMessage.MESSAGE_SIZE- InboundMessage.PAYLOAD_POS);
        this.writeMode = true;
    }
    public DataBuffer(int size){
        byteBuffer = ByteBuffer.allocate(size);
        this.writeMode = true;
    }
    public DataBuffer(byte[] payload){
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
    public void putByte(byte b){
        if(!writeMode){
            throw new UnsupportedOperationException();
        }
        byteBuffer.put(b);
    }
    public int getByte(){
        return byteBuffer.get();
    }

    public void putByteArray(byte[] b){
        if(!writeMode){
            throw new UnsupportedOperationException();
        }
        byteBuffer.putInt(b.length);
        byteBuffer.put(b);
    }
    public byte[] getByteArray(){
        byte[] ret = new byte[byteBuffer.getInt()];
        byteBuffer.get(ret);
        return ret;
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
    public void putShort(short i){
        if(!writeMode){
            throw new UnsupportedOperationException();
        }
        byteBuffer.putShort(i);
    }
    public short getShort(){
        return byteBuffer.getShort();
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
    public void putDouble(double i){
        if(!writeMode){
            throw new UnsupportedOperationException();
        }
        byteBuffer.putDouble(i);
    }
    public double getDouble(){
        return byteBuffer.getDouble();
    }
    public void putVector3(Vector3 vector3){
        if(!writeMode){
            throw new UnsupportedOperationException();
        }
        byteBuffer.putFloat(vector3.x);
        byteBuffer.putFloat(vector3.y);
        byteBuffer.putFloat(vector3.z);
    }
    public Vector3 getVector3(){
        return new Vector3(byteBuffer.getFloat(),byteBuffer.getFloat(),byteBuffer.getFloat());
    }
    public byte[] toArray(){
        byteBuffer.flip();
        byte[] _payload = new byte[byteBuffer.limit()];
        byteBuffer.position(0);
        byteBuffer.get(_payload);
        return _payload;
    }
}
