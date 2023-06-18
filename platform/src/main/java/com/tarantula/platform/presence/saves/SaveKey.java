package com.tarantula.platform.presence.saves;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.RecoverableObject;

public class SaveKey extends RecoverableObject implements Recoverable.Key {


    public SaveKey(String bucket,String oid, int  stub){
        this.bucket = bucket;
        this.oid = oid;
        this.routingNumber = stub;
    }
    @Override
    public String asString() {
        return new StringBuffer(bucket).append(Recoverable.PATH_SEPARATOR).append(oid).append(Recoverable.PATH_SEPARATOR).append(routingNumber).toString();
    }
    @Override
    public int hashCode(){
        return this.asString().hashCode();
    }
    @Override
    public boolean equals(Object obj){
        SaveKey r = (SaveKey)obj;
        return this.asString().equals(r.asString());
    }
}
