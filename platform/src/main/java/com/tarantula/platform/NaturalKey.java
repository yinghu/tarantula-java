package com.tarantula.platform;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.Recoverable;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Updated by yinghu on 6/15/2018.
 */
public class NaturalKey extends RecoverableObject implements Recoverable.Key, Portable {


    public String key;

    public NaturalKey(){}
    public NaturalKey( String key){

        this.key = key;
    }
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

    public int getClassId() {
        return PortableEventRegistry.NATURAL_KEY_CID;
    }

    public void writePortable(PortableWriter out) throws IOException {

        out.writeUTF("2",this.key);
    }

    public void readPortable(PortableReader in) throws IOException {

        this.key = in.readUTF("2");
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

