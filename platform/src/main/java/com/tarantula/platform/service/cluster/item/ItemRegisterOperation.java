package com.tarantula.platform.service.cluster.item;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.tarantula.platform.item.Item;

import java.io.IOException;

public class ItemRegisterOperation extends Operation {

    private String serviceName;
    private Item item;
    private boolean ret;

    public ItemRegisterOperation() {
    }


    public ItemRegisterOperation(String serviceName, Item item) {
        this.serviceName = serviceName;
        this.item = item;
    }

    @Override
    public void run() throws Exception {
        ItemClusterService cis = this.getService();
        this.ret = cis.register(serviceName,item);
    }

    @Override
    public Object getResponse() {
        return ret;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(serviceName);
        out.writeByteArray(item.toBinary());
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
        item = new Item();
        item.fromBinary(in.readByteArray());
    }
}
