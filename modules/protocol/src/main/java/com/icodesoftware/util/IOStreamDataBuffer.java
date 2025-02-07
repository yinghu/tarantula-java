package com.icodesoftware.util;

import com.icodesoftware.Recoverable;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class IOStreamDataBuffer implements Recoverable.DataBuffer {
    @Override
    public Recoverable.DataBuffer writeHeader(Recoverable.DataHeader header) {
        throw new RuntimeException("must override on subclass");
    }

    @Override
    public Recoverable.DataBuffer writeInt(int i) {
        throw new RuntimeException("must override on subclass");
    }

    @Override
    public Recoverable.DataBuffer writeLong(long l) {
        throw new RuntimeException("must override on subclass");
    }

    @Override
    public Recoverable.DataBuffer writeFloat(float f) {
        throw new RuntimeException("must override on subclass");
    }

    @Override
    public Recoverable.DataBuffer writeDouble(double d) {
        throw new RuntimeException("must override on subclass");
    }

    @Override
    public Recoverable.DataBuffer writeShort(short s) {
        throw new RuntimeException("must override on subclass");
    }

    @Override
    public Recoverable.DataBuffer writeBoolean(boolean b) {
        throw new RuntimeException("must override on subclass");
    }

    @Override
    public Recoverable.DataBuffer writeByte(byte b) {
        throw new RuntimeException("must override on subclass");
    }

    @Override
    public Recoverable.DataBuffer writeUTF8(String utf) {
        throw new RuntimeException("must override on subclass");
    }

    @Override
    public Recoverable.DataHeader readHeader() {
        throw new RuntimeException("must override on subclass");
    }

    @Override
    public int readInt() {
        throw new RuntimeException("must override on subclass");
    }

    @Override
    public boolean readBoolean() {
        throw new RuntimeException("must override on subclass");
    }

    @Override
    public long readLong() {
        throw new RuntimeException("must override on subclass");
    }

    @Override
    public float readFloat() {
        throw new RuntimeException("must override on subclass");
    }

    @Override
    public double readDouble() {
        throw new RuntimeException("must override on subclass");
    }

    @Override
    public short readShort() {
        throw new RuntimeException("must override on subclass");
    }

    @Override
    public byte readByte() {
        throw new RuntimeException("must override on subclass");
    }

    @Override
    public String readUTF8() {
        throw new RuntimeException("must override on subclass");
    }

    @Override
    public byte[] array() {
        throw new RuntimeException("must override on subclass");
    }

    @Override
    public ByteBuffer src() {
        throw new RuntimeException("must override on subclass");
    }

    @Override
    public ByteBuffer flip() {
        throw new RuntimeException("must override on subclass");
    }

    @Override
    public ByteBuffer rewind() {
        throw new RuntimeException("must override on subclass");
    }

    @Override
    public ByteBuffer clear() {
        throw new RuntimeException("must override on subclass");
    }

    @Override
    public boolean hasRemaining() {
        throw new RuntimeException("must override on subclass");
    }

    @Override
    public int remaining() {
        throw new RuntimeException("must override on subclass");
    }

    @Override
    public void position(int position) {
        throw new RuntimeException("must override on subclass");
    }

    @Override
    public boolean full() {
        return false;
        //throw new RuntimeException("must override on subclass");
    }

    @Override
    public int size() {
        throw new RuntimeException("must override on subclass");
    }

    @Override
    public boolean direct() {
        return true;
        //throw new RuntimeException("must override on subclass");
    }

    public Recoverable.DataBuffer write(Recoverable.DataBuffer src){
        throw new RuntimeException("must override on subclass");
    }
    public void read(Recoverable.DataBuffer dest){
        throw new RuntimeException("must override on subclass");
    }
    public Recoverable.DataBuffer write(byte[] src){
        return this;
    }
    public void read(byte[] dest){

    }
    public static Recoverable.DataBuffer reader(InputStream src){
        return InputStreamDataBufferProxy.proxy(src);
    }
    public static Recoverable.DataBuffer writer(OutputStream dest){
        return OutputStreamDataBufferProxy.proxy(dest);
    }

}
