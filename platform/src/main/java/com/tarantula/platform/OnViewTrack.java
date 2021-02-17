package com.tarantula.platform;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

import com.icodesoftware.OnView;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.io.IOException;
import java.util.Map;
import com.icodesoftware.util.RecoverableObject;
/**
 * Updated by yinghu lu on 7/11/2020
 */
public class OnViewTrack extends RecoverableObject implements OnView, Portable {


    private String moduleContext;
    private String moduleResourceFile;
    private String viewId;


    public OnViewTrack(){
        this.onEdge = true;
        this.label = LABEL;
    }
    public String viewId(){
        return this.viewId;
    }
    public void viewId(String viewId){
        this.viewId = viewId;
    }

    public String moduleContext(){
        return this.moduleContext;
    }
    public void moduleContext(String moduleContext){
        this.moduleContext = moduleContext;
    }

    public String moduleResourceFile(){
        return moduleResourceFile;
    }
    public void moduleResourceFile(String moduleResourceFile){
        this.moduleResourceFile = moduleResourceFile;
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",viewId);
        this.properties.put("4",moduleContext);
        this.properties.put("5",moduleResourceFile);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.viewId = (String)properties.get("1");
        this.moduleContext = (String)properties.get("4");
        this.moduleResourceFile = (String)properties.get("5");
    }
    @Override
    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableRegistry.ON_VIEW_OID;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeUTF("1",viewId);
        portableWriter.writeUTF("4",this.moduleContext);
        portableWriter.writeUTF("5",moduleResourceFile);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        viewId = portableReader.readUTF("1");
        moduleContext = portableReader.readUTF("4");
        moduleResourceFile= portableReader.readUTF("7");
    }
    public void distributionKey(String distributionKey){
        //skip the natural key
    }
    public Key key(){
        return new NaturalKey(this.viewId);
    }
}
