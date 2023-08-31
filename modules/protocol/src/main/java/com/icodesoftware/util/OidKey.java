package com.icodesoftware.util;
import com.icodesoftware.Recoverable;

public class OidKey implements Recoverable.Key {

    public String key;

    public OidKey(String key){
        this.key = key;
    }

    public String asString(){
        return this.key;
    }

    public boolean read(Recoverable.DataBuffer buffer){
        key = buffer.readUTF8();
        return true;
    }
    public boolean write(Recoverable.DataBuffer buffer){
        if(key==null) return false;
        buffer.writeUTF8(key);
        return true;
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
        OidKey r = (OidKey)obj;
        return key.equals(r.key);
    }
}

