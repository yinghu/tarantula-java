package com.tarantula.platform;


import com.tarantula.Recoverable;

import java.nio.charset.Charset;

/**
 * Updated by yinghu on 8/23/19
 */
public class CompositeKey extends RecoverableObject implements Recoverable.Key {

    //public String owner;
    public String key;

    public CompositeKey(String owner, String key){
        this.owner = owner;
        this.key = key;
    }

    public byte[] toByteArray(){
        return this.key.getBytes(Charset.forName("UTF-8"));
    }
    public void fromByteArray(byte[] data){
        StringBuffer sb = new StringBuffer();
        for(byte b : data){
            sb.append((char)b);
        }
        this.key = sb.toString();
    }
    public String asString(){
        return this.owner+"_"+this.key;
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
        return owner.equals(r.owner)&&key.equals(r.key);
    }
}

