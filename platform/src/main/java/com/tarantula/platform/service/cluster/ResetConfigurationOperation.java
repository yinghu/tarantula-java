package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.tarantula.Configuration;
import com.tarantula.platform.ApplicationConfiguration;
import com.tarantula.platform.util.SystemUtil;

import java.io.IOException;

/**
 * created by yinghu lu on 5/15/2020.
 */
public class ResetConfigurationOperation extends Operation {



    private Configuration configuration;

    public ResetConfigurationOperation() {
    }


    public ResetConfigurationOperation(Configuration configuration) {
        this.configuration = configuration;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        //cds.resetConfiguration(configuration);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(configuration.distributionKey());
        out.writeByteArray(SystemUtil.toJson(configuration.toMap()));
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        configuration = new ApplicationConfiguration();
        configuration.distributionKey(in.readUTF());
        configuration.fromMap(SystemUtil.toMap(in.readByteArray()));
    }
}
