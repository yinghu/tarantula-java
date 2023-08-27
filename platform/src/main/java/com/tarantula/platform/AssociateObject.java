package com.tarantula.platform;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.RecoverableObject;

public class AssociateObject extends RecoverableObject {




    @Override
    public boolean readKey(Recoverable.DataBuffer buffer){
        label = buffer.readUTF8();
        id = buffer.readLong();
        return true;
    }
    @Override
    public boolean writeKey(Recoverable.DataBuffer buffer){
        if(id==0 && label ==null) return false;
        buffer.writeUTF8(label);
        buffer.writeLong(id);
        return true;
    }

    @Override
    public Key key() {
        return new AssociateKey(id,label);
    }
}
