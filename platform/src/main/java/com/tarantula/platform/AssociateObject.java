package com.tarantula.platform;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.RecoverableObject;

public class AssociateObject extends RecoverableObject {

    @Override
    public boolean readKey(Recoverable.DataBuffer buffer){
        distributionId = buffer.readLong();
        label = buffer.readUTF8();
        return true;
    }
    @Override
    public boolean writeKey(Recoverable.DataBuffer buffer){
        if(distributionId==0 && label ==null) return false;
        buffer.writeLong(distributionId);
        buffer.writeUTF8(label);
        return true;
    }

    @Override
    public Key key() {
        return new AssociateKey(distributionId,label);
    }
}
