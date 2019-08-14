package com.tarantula.platform;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.OnView;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.io.IOException;
import java.util.Map;

/**
 * Updated by yinghu lu on 11/20/2018.
 */
public class OnViewTrack extends DeploymentObject implements OnView {

    protected String contentBaseUrl;
    protected String moduleFile;
    protected String moduleName;
    protected String moduleResourceFile;
    protected String icon;
    protected String category;
    protected String viewId;

    protected String flag;

    public OnViewTrack(){
        this.onEdge = true;
        this.label = "LVT";
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
    public void icon(String icon) {
        this.icon = icon;
    }
    public String category() {
        return this.category;
    }
    public void category(String category) {
        this.category = category;
    }
    public String icon() {
        return this.icon;
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
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.contentBaseUrl);
        out.writeUTF("2",this.moduleFile);
        out.writeUTF("3",this.moduleName);
        out.writeUTF("4",this.moduleResourceFile);
        out.writeUTF("5",this.icon);
        out.writeUTF("6",this.viewId);
        out.writeUTF("7",this.flag);
        out.writeUTF("8",this.category);
    }

    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.contentBaseUrl = in.readUTF("1");
        this.moduleFile = in.readUTF("2");
        this.moduleName = in.readUTF("3");
        this.moduleResourceFile = in.readUTF("4");
        this.icon = in.readUTF("5");
        this.viewId = in.readUTF("6");
        this.flag = in.readUTF("7");
        this.category = in.readUTF("8");
    }
    @Override

    public Map<String,Object> toMap(){
        this.properties.put("1",viewId);
        this.properties.put("2",flag);
        this.properties.put("3",contentBaseUrl!=null?contentBaseUrl:"n/a");
        this.properties.put("4",icon!=null?icon:"n/a");
        this.properties.put("5",moduleFile!=null?moduleFile:"n/a");
        this.properties.put("6",moduleName!=null?moduleName:"n/a");
        this.properties.put("7",moduleResourceFile!=null?moduleResourceFile:"n/a");
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.viewId = (String)properties.get("1");
        this.flag = (String)properties.get("2");
        this.contentBaseUrl = (String)properties.get("3");
        this.icon = (String)properties.get("4");
        this.moduleFile = (String)properties.get("5");
        this.moduleName = (String)properties.get("6");
        this.moduleResourceFile = (String)properties.get("7");
    }
    //@Override
    //public Key key(){
        //return new CompositeKey(this.viewId,flag);
    //}
    @Override
    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableRegistry.ON_VIEW_OID;
    }

    public String toString(){
        return "OnView->["+viewId+"/"+moduleName+"/"+moduleFile+"/"+moduleResourceFile+"]";
    }
}
