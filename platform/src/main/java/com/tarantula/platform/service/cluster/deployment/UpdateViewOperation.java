package com.tarantula.platform.service.cluster.deployment;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.icodesoftware.OnView;
import com.tarantula.platform.OnViewTrack;

import java.io.IOException;

public class UpdateViewOperation extends Operation {


    private OnView onView;

    public UpdateViewOperation() {
    }


    public UpdateViewOperation(OnView onView) {
        this.onView = onView;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        cds.onUpdateView(this.onView);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(onView.owner());
        out.writeByteArray(this.onView.toBinary());
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.onView = new OnViewTrack();
        this.onView.owner(in.readUTF());
        this.onView.fromBinary(in.readByteArray());
    }
}
