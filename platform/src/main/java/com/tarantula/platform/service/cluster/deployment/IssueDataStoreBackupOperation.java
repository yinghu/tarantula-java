package com.tarantula.platform.service.cluster.deployment;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

public class IssueDataStoreBackupOperation extends Operation {


    private int scope;

    public IssueDataStoreBackupOperation() {
    }

    public IssueDataStoreBackupOperation(int scope) {
        this.scope = scope;
    }

    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        cds.onIssueDataStoreBackup(scope);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeInt(scope);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        scope = in.readInt();
    }
}
