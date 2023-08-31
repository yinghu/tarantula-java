package com.tarantula.platform;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.RecoverableObject;

public class AssociateObject extends RecoverableObject {

    @Override
    public boolean readKey(Recoverable.DataBuffer buffer){
        label = buffer.readUTF8();
        oid = buffer.readUTF8();
        return true;
    }
    @Override
    public boolean writeKey(Recoverable.DataBuffer buffer){
        if(oid==null && label ==null) return false;
        buffer.writeUTF8(label);
        buffer.writeUTF8(oid);
        return true;
    }

    @Override
    public Key key() {
        return new AssociateKey(oid,label);
    }
}
