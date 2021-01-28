package com.tarantula.platform;


import com.icodesoftware.Recoverable;
import com.icodesoftware.util.RecoverableObject;
/**
 * Updated by yinghu on 8/23/19
 */
public class NaturalKey extends RecoverableObject implements Recoverable.Key {


    public String key;

    public NaturalKey( String key){

        this.key = key;
    }
    public String asString(){
        return this.key;
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

