package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

/**
 * Created by yinghu lu on 7/25/2020
 */
public class AccessIndexServiceUpdateOperation extends Operation {

    private boolean enabled;
    public AccessIndexServiceUpdateOperation() {
    }
    public AccessIndexServiceUpdateOperation(boolean enabled) {
        this.enabled = enabled;
    }
    @Override
    public void run() throws Exception {
        AccessIndexClusterService cds = this.getService();
        if(enabled){
            cds.enable();
        }else {
            cds.disable();
        }
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeBoolean(enabled);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        enabled = in.readBoolean();
    }
}
