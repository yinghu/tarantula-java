package com.tarantula.platform;

import com.tarantula.OnView;
import com.tarantula.platform.service.cluster.PortableRegistry;
import java.util.Map;

/**
 * Updated by yinghu lu on 8/23/19.
 */
public class OnViewTrack extends RecoverableObject implements OnView {

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
    public Map<String,Object> toMap(){
        this.properties.put("1",viewId);
        this.properties.put("2",flag!=null?flag:"n/a");
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
