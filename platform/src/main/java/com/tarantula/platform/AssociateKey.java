package com.tarantula.platform;
import com.icodesoftware.Recoverable;
import com.icodesoftware.util.RecoverableObject;

public class AssociateKey extends RecoverableObject implements Recoverable.Key {

    public AssociateKey(String bucket, String oid, String suffix){
        this.bucket = bucket;
        this.oid = oid;
        this.label = suffix;
    }

    @Override
    public String asString() {
        if(bucket==null||oid==null){
            return null;
        }
        return new StringBuffer(bucket).append(Recoverable.PATH_SEPARATOR).append(oid).append(Recoverable.PATH_SEPARATOR).append(label).toString();
    }
    @Override
    public int hashCode(){
        return this.asString().hashCode();
    }
    @Override
    public boolean equals(Object obj){
        AssociateKey r = (AssociateKey)obj;
        return this.asString().equals(r.asString());
    }
}
