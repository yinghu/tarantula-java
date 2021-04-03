package com.tarantula.platform.service.cluster.recover;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;


public class DataStoreQueryStartOperation extends Operation {

    private String memberId;
    private String source;
    private String dataStore;
    private int factoryId;
    private int classId;
    private String[] params;
    private boolean suc;
    public DataStoreQueryStartOperation() {
    }


    public DataStoreQueryStartOperation(String memberId,String source,String dataStore,int factoryId,int classId,String[] params) {
        this.memberId = memberId;
        this.source = source;
        this.dataStore = dataStore;
        this.factoryId = factoryId;
        this.classId = classId;
        this.params = params;
    }
    @Override
    public void run() throws Exception {
        ClusterRecoverService cds = this.getService();
        suc = cds.queryStart(memberId,source,dataStore,factoryId,classId,params);
    }

    @Override
    public Object getResponse() {
        return suc;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(memberId);
        out.writeUTF(source);
        out.writeUTF(dataStore);
        out.writeInt(factoryId);
        out.writeInt(classId);
        out.writeUTFArray(params);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.memberId = in.readUTF();
        this.source = in.readUTF();
        this.dataStore = in.readUTF();
        this.factoryId = in.readInt();
        this.classId = in.readInt();
        this.params = in.readUTFArray();
    }
}
