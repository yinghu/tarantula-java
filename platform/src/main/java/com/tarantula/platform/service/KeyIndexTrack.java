package com.tarantula.platform.service;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.service.KeyIndex;
import com.icodesoftware.util.BinaryKey;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;


public class KeyIndexTrack extends RecoverableObject implements KeyIndex , Portable {

    private String masterNode;
    private String[] slaveNodes = new String[0];
    public String masterNode(){
        return masterNode;
    }
    public String[] slaveNodes(){
        return slaveNodes;
    }


    public KeyIndexTrack(){

    }
    public KeyIndexTrack(String source,Key key){
        this.owner = source;
        this.ownerKey = key;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeUTF("1",masterNode);
        portableWriter.writeUTFArray("2",slaveNodes);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        masterNode = portableReader.readUTF("1");
        slaveNodes = portableReader.readUTFArray("2");
    }

    public int scope(){
        return Distributable.INDEX_SCOPE;
    }

    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.KEY_INDEX_CID;
    }

    @Override
    public boolean read(DataBuffer buffer){
        this.masterNode = buffer.readUTF8();
        this.slaveNodes = new String[buffer.readInt()];
        for(int i=0;i<slaveNodes.length;i++){
            slaveNodes[i]=buffer.readUTF8();
        }
        return true;
    }
    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(masterNode);
        buffer.writeInt(slaveNodes.length);
        for(int i=0;i<slaveNodes.length;i++){
            buffer.writeUTF8(slaveNodes[i]);
        }
        return true;
    }

    @Override
    public boolean readKey(Recoverable.DataBuffer buffer){
        if(ownerKey==null) ownerKey = new BinaryKey();
        ownerKey.read(buffer);
        return true;
    }
    @Override
    public boolean writeKey(Recoverable.DataBuffer buffer){
        if(ownerKey ==null) return false;
        ownerKey.write(buffer);
        return true;
    }


    public boolean placeMasterNode(String node){
        if(masterNode==null){
            masterNode = node;
            return true;
        }
        if(masterNode.equals(node)) return false;
        masterNode = node;
        return true;
    }
    public boolean placeSlaveNode(String node){
        if(slaveNodes.length==0){
            slaveNodes = new String[]{node};
            return true;
        }
        int index = -1;
        for(int i=0;i<slaveNodes.length;i++){
            if(slaveNodes[i].equals(node)){
                index = i;
                break;
            }
        }
        if(index==-1){
            String[] _slaveNodes = new String[slaveNodes.length+1];
            _slaveNodes[0]=node;
            for(int i=1;i<_slaveNodes.length;i++){
                _slaveNodes[i] = slaveNodes[i-1];
            }
            slaveNodes = _slaveNodes;
            return true;
        }
        if(index==0) return false;
        //move it to first
        for(int i=index;i>0;i--){
            slaveNodes[i] = slaveNodes[i-1];
        }
        return true;
    }

    @Override
    public Key key() {
        return ownerKey;
    }
}
