package com.tarantula.platform.service.cluster.tournament;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;


public class ScanTournamentOperation extends Operation {

    private String serviceName;
    private byte[] response;

    public ScanTournamentOperation() {
    }

    public ScanTournamentOperation(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public void run() throws Exception {
        TournamentClusterService ais = this.getService();
        response = ais.scan(serviceName);
    }

    @Override
    public Object getResponse() {
        return response;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(this.serviceName);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
    }
}
