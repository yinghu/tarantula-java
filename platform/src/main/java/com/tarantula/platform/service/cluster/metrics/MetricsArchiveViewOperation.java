package com.tarantula.platform.service.cluster.metrics;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.icodesoftware.util.TimeUtil;

import java.io.IOException;
import java.time.LocalDateTime;

public class MetricsArchiveViewOperation extends Operation {

    private String serviceName;
    private String category;
    private String classifier;
    private LocalDateTime endTime;

    private String ret;

    public MetricsArchiveViewOperation() {
    }

    public MetricsArchiveViewOperation(String serviceName, String category, String classifier,LocalDateTime endTime) {
        this.serviceName = serviceName;
        this.category = category;
        this.classifier = classifier;
        this.endTime = endTime;
    }

    @Override
    public void run() throws Exception {
        MetricsClusterService cis = this.getService();
        this.ret = cis.metricsArchive(serviceName,category,classifier,endTime);
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
        out.writeLong(TimeUtil.toUTCMilliseconds(endTime));
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
        category = in.readUTF();
        classifier = in.readUTF();
        endTime = TimeUtil.fromUTCMilliseconds(in.readLong());
    }
}
