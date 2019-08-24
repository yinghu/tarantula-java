package com.tarantula.platform;


import com.tarantula.Statistics;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Updated by yinghu lu on 8/23/19.
 */
public class StatisticsEntry extends RecoverableObject implements Statistics.Entry {

    private String name;
    private double value;

    public StatisticsEntry(){
        this.vertex = "StatisticsEntry";
        this.label = "SSE";
        this.binary = true;
        this.onEdge = true;
    }
    public StatisticsEntry(String name){
        this();
        this.name  = name;
    }
    @Override
    public String name() {
        return name;
    }
    @Override
    public double value() {
        return this.value;
    }
    @Override
    public void value(double delta) {
        this.value +=delta;
    }

    @Override
    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableRegistry.STATISTICS_ENTRY_CID;
    }
    public byte[] toByteArray(){
        ByteBuffer buffer = ByteBuffer.allocate(12+name.length());
        buffer.putDouble(this.value);
        buffer.putInt(name.length());
        buffer.put(name.getBytes(Charset.forName("UTF-8")));
        return buffer.array();
    }
    public void fromByteArray(byte[] data){
        ByteBuffer buffer = ByteBuffer.wrap(data);
        this.value = buffer.getDouble();
        int len = buffer.getInt();
        StringBuffer sb = new StringBuffer(len);
        for(int i=0;i<len;i++){
            sb.append((char) buffer.get());
        }
        this.name = sb.toString();
    }
    @Override
    public String toString(){
        return name+"/"+value;
    }
}
