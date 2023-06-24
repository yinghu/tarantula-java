package com.tarantula.platform.presence.saves;

import com.icodesoftware.Recoverable;
import com.icodesoftware.Session;
import com.icodesoftware.util.RecoverableObject;

public class SaveKey extends RecoverableObject implements Recoverable.Key {


    public SaveKey(String bucket,String oid, int  stub){
        this.bucket = bucket;
        this.oid = oid;
        this.routingNumber = stub;
    }
    public SaveKey(Session session){
        String[] query = session.systemId().split(Recoverable.PATH_SEPARATOR);
        this.bucket = query[0];
        this.oid = query[1];
        this.routingNumber = session.stub();
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
