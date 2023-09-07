package com.tarantula.platform;

import com.icodesoftware.util.FIFOBuffer;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FIFOIndexSet extends RecoverableObject {

    protected FIFOBuffer<String> buffer;
    private int size;
    public FIFOIndexSet(){
    }
    public FIFOIndexSet(String label,int maxRecords){
        this();
        this.label = label;
        this.size = maxRecords;
        this.buffer = new FIFOBuffer<>(this.size,new String[size]);
    }

    @Override
    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("size",size);
        int[] i= {0};
        buffer.list(new ArrayList<>(size)).forEach(k->{
            properties.put("k"+i[0],k);
            i[0]++;
        });
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
       this.size = ((Number)properties.get("size")).intValue();
       this.buffer = new FIFOBuffer<>(size,new String[size]);
       for(int i=0;i<size;i++){
           String pv = (String)properties.get("k"+i);
           if(pv == null) break;
           buffer.push(pv);
       }
    }

    @Override
    public int getClassId() {
        return PortableRegistry.FIFO_INDEX_SET_CID;
    }

    @Override
    public Key key(){
        return new AssociateKey(this.distributionId,this.label);
    }

    public void addKey(String key){
        buffer.push(key);
    }
    public List<String> keySet(){
        return buffer.list(new ArrayList<>(size));
    }
    public void reload(){
        this.dataStore.load(this);
    }

}
