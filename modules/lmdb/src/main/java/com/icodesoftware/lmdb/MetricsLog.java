package com.icodesoftware.lmdb;

import com.icodesoftware.Statistics;
import com.icodesoftware.protocol.statistics.StatisticsEntry;
import com.icodesoftware.util.RecoverableObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MetricsLog extends RecoverableObject {
    public String node;
    public List<Statistics.Entry> updates;
    public MetricsLog(){

    }
    private MetricsLog(String node,String name, List<Statistics.Entry> updates){
        this.node = node;
        this.name = name;
        this.updates = updates;
    }
    @Override
    public void fromBinary(byte[] payload) {
        DataBuffer buffer = BufferProxy.wrap(payload);
        node = buffer.readUTF8();
        name = buffer.readUTF8();
        updates = new ArrayList<>();
        int size = buffer.readInt();
        for(int i=0;i<size;i++){
            updates.add(StatisticsEntry.simpleValue(buffer.readUTF8(),buffer.readDouble()));
        }
    }

    @Override
    public byte[] toBinary() {
        DataBuffer buffer = BufferProxy.buffer(2000,false);
        buffer.writeUTF8(node);
        buffer.writeUTF8(name);
        buffer.writeInt(updates.size());
        for(Statistics.Entry e : updates){
            buffer.writeUTF8(e.name());
            buffer.writeDouble(e.total());
        }
        return Arrays.copyOf(buffer.array(),buffer.remaining());
    }

    public static MetricsLog metricsLog(String node,String name, List<Statistics.Entry> updates){
        if(node==null || name==null || updates.isEmpty()) throw new RuntimeException("node/name or updates cannot null or empty");
        return new MetricsLog(node,name,updates);
    }
}
