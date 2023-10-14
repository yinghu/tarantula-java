package com.tarantula.platform.service.persistence;

import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.service.cluster.PortableRegistry;

public class NodeOperation extends RecoverableObject {

    @Override
    public boolean write(DataBuffer buffer) {
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        return true;
    }

    @Override
    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableRegistry.NODE_OPERATION_CID;
    }

}
