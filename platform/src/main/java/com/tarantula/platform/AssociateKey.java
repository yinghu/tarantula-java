package com.tarantula.platform;
import com.icodesoftware.Recoverable;

import java.util.Objects;

public class AssociateKey implements Recoverable.Key {

    private long ownerId;
    private String label;
    public AssociateKey(long ownerId, String suffix){
        this.ownerId = ownerId;
        this.label = suffix;
    }

    @Override
    public String asString() {
        if(ownerId==0 || label==null) return null;
        return new StringBuffer().append(ownerId).append(Recoverable.PATH_SEPARATOR).append(label).toString();
    }
    @Override
    public int hashCode(){
        return Objects.hash(ownerId,label);
    }
    @Override
    public boolean equals(Object obj){
        AssociateKey r = (AssociateKey)obj;
        return this.ownerId == r.ownerId && label.equals(r.label);
    }

    public boolean read(Recoverable.DataBuffer buffer){
        ownerId = buffer.readLong();
        label = buffer.readUTF8();
        return true;
    }
    public boolean write(Recoverable.DataBuffer buffer){
        if(ownerId==0||label==null) return false;
        buffer.writeLong(ownerId);
        buffer.writeUTF8(label);
        return true;
    }
}
