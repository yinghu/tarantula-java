package com.tarantula.platform.service.cluster.metrics;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

public class MetricsViewOperation extends Operation {

    private String serviceName;
    private String category;
    private String classifier;

    private String ret;

    public MetricsViewOperation() {
    }

    public MetricsViewOperation(String serviceName,String category,String classifier) {
        this.serviceName = serviceName;
        this.category = category;
        this.classifier = classifier;
    }

    @Override
    public void run() throws Exception {
        MetricsClusterService cis = this.getService();
        this.ret = cis.metricsSnapshot(serviceName,category,classifier);
    }

    @Override
    public Object getResponse() {
        return ret;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(serviceName);
        out.writeUTF(category);
        out.writeUTF(classifier);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
        category = in.readUTF();
        classifier = in.readUTF();
    }
}
