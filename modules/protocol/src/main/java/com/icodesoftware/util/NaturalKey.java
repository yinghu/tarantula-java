package com.icodesoftware.util;
import com.icodesoftware.Recoverable;

public class NaturalKey implements Recoverable.Key {

    public String key;

    public NaturalKey(String key){
        this.key = key;
    }

    public String asString(){
        return this.key;
    }

    public byte[] asBinary(){
        return asString().getBytes();
    }
    @Override
    public String toString(){
        return "Owner access key ["+key+"]";
    }

    @Override
    public int hashCode(){
        return this.key.hashCode();
    }

    @Override
    public boolean equals(Object obj){
        NaturalKey r = (NaturalKey)obj;
        return key.equals(r.key);
    }
}

