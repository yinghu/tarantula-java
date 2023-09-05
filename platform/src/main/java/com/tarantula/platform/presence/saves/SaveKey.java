package com.tarantula.platform.presence.saves;

import com.icodesoftware.Recoverable;


public class SaveKey implements Recoverable.Key {

    private String ownerId;

    private int stub;

    public SaveKey(String oid, int  stub){
        this.ownerId = oid;
        this.stub = stub;
    }

    @Override
    public String asString() {
        return new StringBuffer().append(ownerId).append(Recoverable.PATH_SEPARATOR).append(stub).toString();
    }
    @Override
    public int hashCode(){
        return this.asString().hashCode();
    }
    @Override
    public boolean equals(Object obj){
        SaveKey r = (SaveKey)obj;
        return this.asString().equals(r.asString());
    }

    public boolean read(Recoverable.DataBuffer buffer){
        ownerId = buffer.readUTF8();
        stub = buffer.readInt();
        return true;
    }
    public boolean write(Recoverable.DataBuffer buffer){
        if(ownerId==null) return false;
        buffer.writeUTF8(ownerId);
        buffer.writeInt(stub);
        return true;
    }
}
