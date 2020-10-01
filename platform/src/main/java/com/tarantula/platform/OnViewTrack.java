package com.tarantula.platform;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

import com.icodesoftware.OnView;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.io.IOException;
import java.util.Map;

/**
 * Updated by yinghu lu on 7/11/2020
 */
public class OnViewTrack extends RecoverableObject implements OnView, Portable {

    protected String contentBaseUrl;
    protected String moduleFile;
    protected String moduleName;
    protected String moduleResourceFile;
    protected String viewId;

    protected String flag;

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
    public String flag(){
        return this.flag;
    }
    public void flag(String flag){
        this.flag = flag;
    }

    public String contentBaseUrl(){
        return this.contentBaseUrl;
    }
    public void contentBaseUrl(String contentBaseUrl){
        this.contentBaseUrl = contentBaseUrl;
    }

    public String moduleFile(){
        return this.moduleFile;
    }
    public void moduleFile(String moduleFile){
        this.moduleFile = moduleFile;
    }

    public String moduleName(){
        return this.moduleName;
    }
    public void moduleName(String moduleName){
        this.moduleName = moduleName;
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
        this.properties.put("2",flag);
        this.properties.put("3",contentBaseUrl);
        this.properties.put("5",moduleFile);
        this.properties.put("6",moduleName);
        this.properties.put("7",moduleResourceFile);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.viewId = (String)properties.get("1");
        this.flag = (String)properties.get("2");
        this.contentBaseUrl = (String)properties.get("3");
        this.moduleFile = (String)properties.get("5");
        this.moduleName = (String)properties.get("6");
        this.moduleResourceFile = (String)properties.get("7");
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
        portableWriter.writeUTF("2",flag);
        portableWriter.writeUTF("3",this.contentBaseUrl);
        portableWriter.writeUTF("5",this.moduleFile);
        portableWriter.writeUTF("6",this.moduleName);
        portableWriter.writeUTF("7",moduleResourceFile);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        viewId = portableReader.readUTF("1");
        flag = portableReader.readUTF("2");
        contentBaseUrl = portableReader.readUTF("3");
        moduleFile = portableReader.readUTF("5");
        moduleName = portableReader.readUTF("6");
        moduleResourceFile= portableReader.readUTF("7");
    }
    public void distributionKey(String distributionKey){
        //skip the natural key
    }
    public Key key(){
        return new NaturalKey(this.viewId);
    }
}
