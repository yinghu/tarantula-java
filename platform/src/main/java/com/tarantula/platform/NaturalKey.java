package com.tarantula.platform;

import com.tarantula.Recoverable;


import java.nio.charset.Charset;

/**
 * Updated by yinghu on 6/15/2018.
 */
public class NaturalKey extends RecoverableObject implements Recoverable.Key {


    public String key;

    public NaturalKey( String key){

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

