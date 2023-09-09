package com.tarantula.game;

import com.icodesoftware.Recoverable;


public class StubKey implements Recoverable.Key {
    private String bucket;
    private String oid;

    private String label;

    private long stub;
    public StubKey(String systemId,String label,long  stub){
        String[] query = systemId.split("/");
        this.bucket = query[0];
        this.oid = query[1];
        this.label = label;
        this.stub = stub;
    }
    public StubKey(String bucket, String oid,String label,long  stub){
        this.bucket = bucket;
        this.oid = oid;
        this.label = label;
        this.stub = stub;
    }

    @Override
    public String asString() {
        return new StringBuffer(bucket).append(Recoverable.PATH_SEPARATOR).append(oid).append(Recoverable.PATH_SEPARATOR).append(label).append(Recoverable.PATH_SEPARATOR).append(stub).toString();
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
