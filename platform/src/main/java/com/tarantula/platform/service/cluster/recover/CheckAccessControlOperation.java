package com.tarantula.platform.service.cluster.recover;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.icodesoftware.Access;
import com.tarantula.platform.AccessControl;

import java.io.IOException;

/**
 * updated by yinghu lu on 7/10/2020.
 */
public class CheckAccessControlOperation extends Operation {

    private String systemId;
    private Access.Role role;
    private int flag;

    public CheckAccessControlOperation() {
    }


    public CheckAccessControlOperation(String systemId, Access.Role role) {
        this.systemId = systemId;
        this.role = role;
    }

    @Override
    public void run() throws Exception {
        ClusterRecoverService cis = this.getService();
        flag = cis.checkAccessControl(systemId,role);
    }

    @Override
    public Object getResponse() {
        return flag;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(systemId);
        out.writeUTF(role.name());
        out.writeInt(role.accessControl());
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        systemId = in.readUTF();
        role = new AccessControl(in.readUTF(),in.readInt());
    }
}
