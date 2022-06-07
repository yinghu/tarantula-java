package com.tarantula.game;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.RecoverableObject;

public class StubKey extends RecoverableObject implements Recoverable.Key {

    //private int index;

    public StubKey(String systemId,String label,int  stub){
        String[] query = systemId.split("/");
        this.bucket = query[0];
        this.oid = query[1];
        this.label = label;
        this.routingNumber = stub;
    }
    public StubKey(String bucket, String oid,String label,int  stub){
        this.bucket = bucket;
        this.oid = oid;
        this.label = label;
        this.routingNumber = stub;
    }

    @Override
    public String asString() {
        return new StringBuffer(bucket).append(Recoverable.PATH_SEPARATOR).append(oid).append(Recoverable.PATH_SEPARATOR).append(label).append(Recoverable.PATH_SEPARATOR).append(routingNumber).toString();
    }
    @Override
    public int hashCode(){
        return this.asString().hashCode();
    }
    @Override
    public boolean equals(Object obj){
        StubKey r = (StubKey)obj;
        return this.asString().equals(r.asString());
    }
}
