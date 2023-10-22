package com.tarantula.platform;
import com.icodesoftware.Recoverable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Base64;
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
        if(ownerId==0 || label==null) return "key not available";
        //byte[] data = ByteBuffer.allocate(13+label.length()).order(ByteOrder.nativeOrder()).putLong(ownerId).put(Recoverable.PATH_SEPARATOR.getBytes()).putInt(label.length()).put(label.getBytes()).flip().array();
        return Base64.getEncoder().encodeToString(asBinary());//new StringBuffer().append(ownerId).append(Recoverable.PATH_SEPARATOR).append(label).toString();
    }

    @Override
    public byte[] asBinary() {
        return ByteBuffer.allocate(12+label.length()).order(ByteOrder.nativeOrder()).putLong(ownerId).putInt(label.length()).put(label.getBytes()).flip().array();
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
