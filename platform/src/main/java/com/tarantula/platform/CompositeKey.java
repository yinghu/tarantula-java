package com.tarantula.platform;
import com.icodesoftware.Recoverable;
import com.icodesoftware.util.RecoverableObject;

public class CompositeKey extends RecoverableObject implements Recoverable.Key {

    public String key;

    public CompositeKey(String owner, String key){
        this.owner = owner;
        this.key = key;
    }

    public String asString(){
        return this.owner+"#"+this.key;
    }
    @Override
    public String toString(){
        return "Owner access key ["+owner+","+key+"]";
    }
    @Override
    public int hashCode(){
        return this.owner.hashCode()+this.key.hashCode();
    }
    @Override
    public boolean equals(Object obj){
        CompositeKey r = (CompositeKey)obj;
        return this.asString().equals(r.asString());
    }
}

