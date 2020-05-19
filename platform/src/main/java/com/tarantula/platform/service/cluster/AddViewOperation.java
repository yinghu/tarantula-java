package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.tarantula.OnView;
import com.tarantula.platform.OnViewTrack;
import com.tarantula.platform.util.SystemUtil;

import java.io.IOException;

/**
 * updated by yinghu lu on 5/29/2019.
 */
public class AddViewOperation extends Operation {

    private String typeId;
    private OnView view;

    private boolean result;

    public AddViewOperation() {
    }


    public AddViewOperation(OnView view) {
        this.typeId = view.owner();
        this.view = view;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        this.view.owner(typeId);
        this.result = cds.addView(this.view);
    }

    @Override
    public Object getResponse() {
        return this.result;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(typeId);
        out.writeByteArray(SystemUtil.toJson(this.view.toMap()));
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.typeId = in.readUTF();
        this.view = new OnViewTrack();
        this.view.fromMap(SystemUtil.toMap(in.readByteArray()));
    }
}
