package com.tarantula.platform.service.cluster.metrics;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

public class ServiceViewOperation extends Operation {

    private String serviceName;

    private String ret;

    public ServiceViewOperation() {
    }

    public ServiceViewOperation(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public void run() throws Exception {
        MetricsClusterService cis = this.getService();
        this.ret = cis.metricsPayload(serviceName);
    }

    @Override
    public Object getResponse() {
        return ret;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(serviceName);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
    }
}
