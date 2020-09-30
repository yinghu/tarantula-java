package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.icodesoftware.Recoverable;
import com.tarantula.platform.util.SystemUtil;

import java.io.IOException;

/**
 * created by yinghu lu on 5/15/2020.
 */
public class RecoverableDistributionOperation extends Operation {


    private Recoverable recoverable;
    private int factoryId;
    private int classId;
    private String key;
    private byte[] value;

    public RecoverableDistributionOperation() {
    }


    public RecoverableDistributionOperation(Recoverable recoverable) {
        this.recoverable = recoverable;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        //result = cds.updateConfiguration(configuration);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeInt(recoverable.getFactoryId());
        out.writeInt(recoverable.getClassId());
        out.writeUTF(recoverable.distributionKey());
        out.writeByteArray(SystemUtil.toJson(recoverable.toMap()));
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        factoryId = in.readInt();
        classId = in.readInt();
        key = in.readUTF();
        value = in.readByteArray();
    }
}
