package com.tarantula.platform;
import com.icodesoftware.Recoverable;

public class AssociateKey implements Recoverable.Key {

    private String ownerId;
    private String label;
    public AssociateKey(String ownerId, String suffix){
        this.ownerId = ownerId;
        this.label = suffix;
    }

    @Override
    public String asString() {
        if(ownerId==null || label==null) return null;
        return new StringBuffer().append(ownerId).append(Recoverable.PATH_SEPARATOR).append(label).toString();
    }
    @Override
    public int hashCode(){
        return this.asString().hashCode();
    }
    @Override
    public boolean equals(Object obj){
        AssociateKey r = (AssociateKey)obj;
        return this.asString().equals(r.asString());
    }

    public boolean read(Recoverable.DataBuffer buffer){
        ownerId = buffer.readUTF8();
        label = buffer.readUTF8();
        return true;
    }
    public boolean write(Recoverable.DataBuffer buffer){
        if(ownerId==null||label==null) return false;
        buffer.writeUTF8(ownerId);
        buffer.writeUTF8(label);
        return true;
    }
}
