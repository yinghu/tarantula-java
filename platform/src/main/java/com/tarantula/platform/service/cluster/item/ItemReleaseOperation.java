package com.tarantula.platform.service.cluster.item;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

public class ItemReleaseOperation extends Operation {

    private String gameServiceName;
    private String serviceName;
    private String category;
    private String itemId;
    private boolean ret;

    public ItemReleaseOperation() {
    }


    public ItemReleaseOperation(String gameServiceName, String serviceName, String category, String itemId) {
        this.gameServiceName = gameServiceName;
        this.serviceName = serviceName;
        this.category = category;
        this.itemId = itemId;
    }

    @Override
    public void run() throws Exception {
        ItemClusterService cis = this.getService();
        this.ret = cis.release(gameServiceName,serviceName,category,itemId);
    }

    @Override
    public Object getResponse() {
        return ret;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(gameServiceName);
        out.writeUTF(serviceName);
        out.writeUTF(category);
        out.writeUTF(itemId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        gameServiceName = in.readUTF();
        serviceName = in.readUTF();
        category = in.readUTF();
        itemId = in.readUTF();
    }
}
