package com.tarantula.platform.service;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Distributable;
import com.icodesoftware.service.KeyIndex;
import com.icodesoftware.util.NaturalKey;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.util.Map;

public class KeyIndexTrack extends RecoverableObject implements KeyIndex , Portable {

    private String masterNode;
    private String[] slaveNodes = new String[0];
    public String masterNode(){
        return masterNode;
    }
    public String[] slaveNodes(){
        return slaveNodes;
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
        return Distributable.LOCAL_SCOPE;
    }
    @Override
    public boolean backup(){
        return false;
    }
    @Override
    public boolean distributable(){return true;}
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.KEY_INDEX_CID;
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",masterNode);
        this.properties.put("2",slaveNodes.length);
        if(slaveNodes.length == 0) return this.properties;
        for(int i=0;i<slaveNodes.length;i++){
            properties.put("s"+i,slaveNodes[i]);
        }
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.masterNode = (String)properties.get("1");
        int slaves = ((Number)properties.get("2")).intValue();
        if(slaves == 0) return;
        slaveNodes = new String[slaves];
        for(int i=0;i<slaves;i++){
            slaveNodes[i] = (String)properties.get("s"+i);
        }
    }

    public Key key(){
        return new NaturalKey(this.index);
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
}
