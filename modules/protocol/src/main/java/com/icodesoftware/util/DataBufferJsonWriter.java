package com.icodesoftware.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import com.icodesoftware.Recoverable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;

public class DataBufferJsonWriter extends Writer {

    private Recoverable.DataBuffer dataBuffer;
    private JsonWriter jsonWriter;

    public DataBufferJsonWriter(){
        this(1024,false);
    }
    public DataBufferJsonWriter(int size,boolean direct){
        this.dataBuffer = BufferProxy.buffer(size,direct);
    }
    public DataBufferJsonWriter(Recoverable.DataBuffer dataBuffer){
        this.dataBuffer = dataBuffer;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        for(int i=off;i<len;i++){
            if(!dataBuffer.full()){
                dataBuffer.writeByte((byte)cbuf[i]);
                continue;
            }
            dataBuffer.flip();
            dataBuffer = BufferProxy.transfer(dataBuffer,BufferProxy.buffer(dataBuffer.size()*2, dataBuffer.direct()));
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

    public DataBufferJsonWriter beginObject(){
        try{
            jsonWriter.beginObject();
            return this;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public DataBufferJsonWriter endObject(){
        try{
            jsonWriter.endObject();
            return this;
        }catch (Exception ex){
           throw new RuntimeException(ex);
        }
    }
    public DataBufferJsonWriter namedString(String key,String value){
        try{
            jsonWriter.name(key).value(value);
            return this;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public DataBufferJsonWriter namedNumber(String key,Number value){
        try{
            jsonWriter.name(key).value(value);
            return this;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public DataBufferJsonWriter namedBoolean(String key,boolean value){
        try{
            jsonWriter.name(key).value(value);
            return this;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public DataBufferJsonWriter namedLong(String key,long value){
        try{
            jsonWriter.name(key).value(value);
            return this;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public DataBufferJsonWriter namedDouble(String key,double value){
        try{
            jsonWriter.name(key).value(value);
            return this;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public DataBufferJsonWriter namedFloat(String key,float value){
        try{
            jsonWriter.name(key).value(value);
            return this;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public DataBufferJsonWriter start(){
        dataBuffer.clear();
        jsonWriter = new JsonWriter(this);
        return this;
    }
    public void end(){
        try{
            jsonWriter.flush();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public DataBufferJsonWriter namedArray(String name){
        try{
            jsonWriter.name(name);
            jsonWriter.beginArray();
            return this;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public DataBufferJsonWriter stringOfArray(String value){
        try{
            jsonWriter.value(value);
            return this;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public DataBufferJsonWriter numberOfArray(Number value){
        try{
            jsonWriter.value(value);
            return this;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public DataBufferJsonWriter longOfArray(long value){
        try{
            jsonWriter.value(value);
            return this;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public DataBufferJsonWriter doubleOfArray(double value){
        try{
            jsonWriter.value(value);
            return this;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public DataBufferJsonWriter floatOfArray(float value){
        try{
            jsonWriter.value(value);
            return this;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public DataBufferJsonWriter endArray(){
        try{
            jsonWriter.endArray();
            return this;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }


    public JsonObject toJson(){
        return JsonParser.parseReader(new InputStreamReader(new DataBufferInputStream(src()))).getAsJsonObject();
    }

}
