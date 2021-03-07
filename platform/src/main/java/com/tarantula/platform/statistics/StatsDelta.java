package com.tarantula.platform.statistics;

import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.protocol.DataBuffer;



public class StatsDelta extends RecoverableObject {
    public String name;
    public double value;

    public StatsDelta(){

    }
    public StatsDelta(String name,double value){
        this.name = name;
        this.value = value;
    }
    public byte[] toBinary(){
        DataBuffer dataBuffer = new DataBuffer();
        dataBuffer.putUTF8(name);
        dataBuffer.putDouble(value);
        return dataBuffer.toArray();
    }
    public void fromBinary(byte[] payload){
        DataBuffer buffer = new DataBuffer(payload);
        name = buffer.getUTF8();
        value = buffer.getDouble();
    }
    @Override
    public int getFactoryId() {
        return StatisticsPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return StatisticsPortableRegistry.STATISTICS_DELTA_CID;
    }
}
