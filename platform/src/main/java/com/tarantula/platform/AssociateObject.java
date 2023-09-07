package com.tarantula.platform;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.RecoverableObject;

public class AssociateObject extends RecoverableObject {

    @Override
    public boolean readKey(Recoverable.DataBuffer buffer){
        label = buffer.readUTF8();
        distributionId = buffer.readLong();
        return true;
    }
    @Override
    public boolean writeKey(Recoverable.DataBuffer buffer){
        if(distributionId==0 && label ==null) return false;
        buffer.writeUTF8(label);
        buffer.writeLong(distributionId);
        return true;
    }

    @Override
    public Key key() {
        return new AssociateKey(distributionId,label);
    }
}
