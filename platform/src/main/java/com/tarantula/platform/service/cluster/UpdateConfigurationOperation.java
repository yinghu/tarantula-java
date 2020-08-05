package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.tarantula.Configuration;
import com.tarantula.OnView;
import com.tarantula.platform.ApplicationConfiguration;
import com.tarantula.platform.OnViewTrack;
import com.tarantula.platform.util.SystemUtil;

import java.io.IOException;

/**
 * created by yinghu lu on 5/15/2020.
 */
public class UpdateConfigurationOperation extends Operation {



    private Configuration configuration;
    private boolean result;
    public UpdateConfigurationOperation() {
    }


    public UpdateConfigurationOperation(Configuration configuration) {
        this.configuration = configuration;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        //result = cds.updateConfiguration(configuration);
    }

    @Override
    public Object getResponse() {
        return result;
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
