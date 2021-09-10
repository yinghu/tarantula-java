package com.tarantula.platform.service.cluster.item;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.tarantula.platform.item.Item;

import java.io.IOException;

public class ItemRegisterOperation extends Operation {

    private String serviceName;
    private String category;
    private String itemId;
    private boolean ret;

    public ItemRegisterOperation() {
    }


    public ItemRegisterOperation(String serviceName,String category,String itemId) {
        this.serviceName = serviceName;
        this.category = category;
        this.itemId = itemId;
    }

    @Override
    public void run() throws Exception {
        ItemClusterService cis = this.getService();
        this.ret = cis.register(serviceName,category,itemId);
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
        out.writeUTF(itemId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
        category = in.readUTF();
        itemId = in.readUTF();
    }
}
