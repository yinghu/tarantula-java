package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.tarantula.OnView;
import com.tarantula.platform.OnViewTrack;

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
        cds.updateView(onView);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(onView.viewId());
        out.writeUTF(onView.flag());
        out.writeUTF(onView.contentBaseUrl());
        out.writeUTF(onView.moduleFile());
        out.writeUTF(onView.moduleName());
        out.writeUTF(onView.moduleResourceFile());
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.onView = new OnViewTrack();
        this.onView.viewId(in.readUTF());
        this.onView.flag(in.readUTF());
        this.onView.contentBaseUrl(in.readUTF());
        this.onView.moduleFile(in.readUTF());
        this.onView.moduleName(in.readUTF());
        this.onView.moduleResourceFile(in.readUTF());
    }
}
