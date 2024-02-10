package com.tarantula.platform.service.cluster.accessindex;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.tarantula.platform.event.TransactionReplicationEvent;

import java.io.IOException;

public class AccessIndexReplicationOperation extends Operation {

    private TransactionReplicationEvent transactionReplicationEvent;
    public AccessIndexReplicationOperation(){

    }

    public AccessIndexReplicationOperation(TransactionReplicationEvent transactionReplicationEvent){
        this.transactionReplicationEvent = transactionReplicationEvent;
    }
    @Override
    public void run() throws Exception {
        AccessIndexClusterService ais = this.getService();
        ais.replicate(transactionReplicationEvent);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeObject(transactionReplicationEvent);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.transactionReplicationEvent = in.readObject(TransactionReplicationEvent.class);
    }
}
