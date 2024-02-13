package com.tarantula.platform.service.cluster.recover;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.tarantula.platform.event.TransactionReplicationEvent;

import java.io.IOException;

public class DataReplicationOperation extends Operation {

    private TransactionReplicationEvent transactionReplicationEvent;
    public DataReplicationOperation(){

    }

    public DataReplicationOperation(TransactionReplicationEvent transactionReplicationEvent){
        this.transactionReplicationEvent = transactionReplicationEvent;
    }
    @Override
    public void run() throws Exception {
        ClusterRecoverService ais = this.getService();
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
        this.transactionReplicationEvent = in.readObject();
    }
}
