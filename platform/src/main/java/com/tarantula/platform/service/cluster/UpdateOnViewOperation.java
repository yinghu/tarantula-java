package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.tarantula.OnView;

import java.io.IOException;

/**
 * created by yinghu lu on 5/15/2020.
 */
public class UpdateOnViewOperation extends Operation {


    private OnView onView;
    public UpdateOnViewOperation() {
    }


    public UpdateOnViewOperation(OnView onView) {
        this.onView = onView;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        //cds.launchModule(typeId);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        //out.writeUTF(typeId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        //this.typeId = in.readUTF();
    }
}
