package com.tarantula.platform;

import com.tarantula.Recoverable;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.util.HashSet;
import java.util.Set;

public class IndexSet extends RecoverableObject {

    public Set<String> keySet = new HashSet<>();

    public IndexSet(){
        this.binary = true;
    }

    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    @Override
    public byte[] toByteArray(){
        StringBuilder sb = new StringBuilder();
        keySet.forEach((k)->sb.append(k).append(","));
        return sb.substring(0,sb.length()-1).getBytes();
    }
    @Override
    public void fromByteArray(byte[] data){
        String[] keys = new String(data).split(",");
        for(String k : keys){
            keySet.add(k);
        }
    }


    public int getClassId() {
        return PortableRegistry.KEY_INDEX_CID;
    }


    public Recoverable.Key key(){
        return new AssociateKey(this.bucket,this.oid,this.label);
    }

}
