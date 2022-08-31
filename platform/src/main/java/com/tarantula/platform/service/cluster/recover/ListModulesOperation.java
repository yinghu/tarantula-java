package com.tarantula.platform.service.cluster.recover;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;


public class ListModulesOperation extends Operation {

    private String[] moduleList;

    public ListModulesOperation() {
    }

    @Override
    public void run() throws Exception {
        ClusterRecoverService cds = this.getService();
        moduleList =  cds.onListModules();
    }

    @Override
    public Object getResponse() {
        return moduleList;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
    }
}
