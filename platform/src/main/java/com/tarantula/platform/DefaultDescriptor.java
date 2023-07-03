package com.tarantula.platform;

import com.google.gson.JsonObject;
import com.icodesoftware.Access;
import com.icodesoftware.Descriptor;
import com.icodesoftware.service.DeployCode;
import com.icodesoftware.util.JsonUtil;
import java.util.Map;

public class DefaultDescriptor extends DeploymentObject implements Descriptor {

	protected String typeId;
    protected String type;
    protected String category;

    protected int accessControl;
    protected int accessMode;
    protected int accessRank;
    protected String tag;
    protected double entryCost;

    protected String moduleId;
    protected String moduleArtifact;
    protected String moduleVersion;
    protected String codebase;
    protected String moduleName;

    protected int deployPriority;
    protected int deployCode = DeployCode.SYSTEM_APPLICATION;

    protected boolean logEnabled =true;

    protected  String applicationClassName;

    protected boolean resetEnabled;
    protected boolean tournamentEnabled;

    public DefaultDescriptor(){}
    public String moduleId(){
        return this.moduleId!=null?moduleId:typeId;
    }
    public void moduleId(String moduleId){
        this.moduleId = moduleId;
    }
    public String moduleArtifact(){
        return this.moduleArtifact;
    }
    public void moduleArtifact(String moduleArtifact){
        this.moduleArtifact = moduleArtifact;
    }
    public String moduleVersion(){
        return this.moduleVersion;
    }
    public void moduleVersion(String moduleVersion){
        this.moduleVersion = moduleVersion;
    }
    public String codebase(){
        return this.codebase;
    }
    public void codebase(String codebase){
        this.codebase = codebase;
    }
    public int deployCode(){
        return this.deployCode;
    }
    public void deployCode(int deployCode){
        this.deployCode = deployCode;
    }
    public int deployPriority(){
        return this.deployPriority;
    }
    public void deployPriority(int deployPriority){
        this.deployPriority = deployPriority;
    }
    public String moduleName(){return this.moduleName;}
    public void moduleName(String moduleName){ this.moduleName = moduleName;}

    public boolean logEnabled(){
        return this.logEnabled;
    }
    public void logEnabled(boolean logEnabled){
        this.logEnabled = logEnabled;
    }

    public String typeId() {
		return this.typeId;
	}

    public String category(){
        return this.category;
    }

	public void typeId(String typeId) {
		this.typeId = typeId;
	}

    public String type(){
        return this.type;
    }
    public void type(String type){
        this.type = type;
    }
    public void category(String category){
        this.category = category;
    }

    public boolean resetEnabled(){
        return this.resetEnabled;
    }
    public void resetEnabled(boolean resetEnabled){
        this.resetEnabled = resetEnabled;
    }
	@Override
	public String toString(){
		return new String(JsonUtil.toJson(this.toMap()));
	}



    public int accessControl(){
        return this.accessControl;
    }
    public void accessControl(int accessControl){
        this.accessControl = accessControl;
    }

    public int accessMode(){
        return this.accessMode;
    }
    public void accessMode(int accessMode){
        this.accessMode = accessMode;
    }

    public int accessRank(){
        return this.accessRank;
    }
    public void accessRank(int accessRank){
        this.accessRank = accessRank;
    }

    public String tag(){
        return this.tag;
    }
    public void tag(String tag){
        this.tag = tag;
    }

    public double entryCost(){
        return this.entryCost;
    }
    public void entryCost(double entryCost){
        this.entryCost = entryCost;
    }


    @Override
    public Map<String, Object> toMap() {
        Map<String,Object> _props = this.properties;
        _props.put("typeId",this.typeId);
        _props.put("type",this.type);
        _props.put("name",this.name);
        _props.put("tag",this.tag);
        _props.put("entryCost",this.entryCost);
        _props.put("category",this.category);
        _props.put("disabled",this.disabled);
        _props.put("accessControl",this.accessControl);
        _props.put("accessMode",this.accessMode);
        _props.put("accessRank",this.accessRank);
        _props.put("deployCode",this.deployCode);
        _props.put("deployPriority",this.deployPriority);
        _props.put("moduleId",this.moduleId);
        _props.put("codebase",this.codebase);
        _props.put("moduleArtifact",this.moduleArtifact);
        _props.put("moduleVersion",this.moduleVersion);
        _props.put("moduleName",this.moduleName);
        _props.put("logEnabled",this.logEnabled);
        _props.put("applicationClassName",this.applicationClassName);
        _props.put("resetEnabled",this.resetEnabled);
        _props.put("tournamentEnabled",this.tournamentEnabled);
        //_props.put("index",this.index);
        return _props;
    }

    @Override
    public void fromMap(Map<String, Object> properties) {
        this.typeId=(String)properties.get("typeId");
        this.type=(String)properties.get("type");
        this.name=(String)properties.get("name");
        this.tag=properties.get("tag")!=null?(String)properties.get("tag"):null;
        this.entryCost=properties.get("entryCost")!=null?((Number)properties.get("entryCost")).doubleValue():0;
        this.category=properties.get("category")!=null?(String)properties.get("category"):null;
        this.disabled = properties.get("disabled")!=null?(boolean)properties.get("disabled"):false;
        this.accessControl  = properties.get("accessControl")!=null?((Number)properties.get("accessControl")).intValue():0;
        this.accessMode  = properties.get("accessMode")!=null?((Number)properties.get("accessMode")).intValue():12;
        this.accessRank = properties.get("accessRank")!=null?((Number)properties.get("accessRank")).intValue():0;
        this.moduleArtifact  = properties.get("moduleArtifact")!=null?(String) properties.get("moduleArtifact"):null;
        this.moduleVersion  = properties.get("moduleVersion")!=null?(String) properties.get("moduleVersion"):null;
        this.deployCode = properties.get("deployCode")!=null?((Number)properties.get("deployCode")).intValue():0;
        this.deployPriority = properties.get("deployPriority")!=null?((Number)properties.get("deployPriority")).intValue():0;
        this.moduleId = properties.get("moduleId")!=null?(String)properties.get("moduleId"):typeId;//
        this.codebase =properties.get("codebase")!=null?(String)properties.get("codebase"):null;
        this.moduleName = properties.get("moduleName")!=null?(String)properties.get("moduleName"):null;
        this.logEnabled = properties.get("logEnabled")!=null?(Boolean)properties.get("logEnabled"):true;
        this.applicationClassName = properties.get("applicationClassName")!=null?(String)properties.get("applicationClassName"):null;
        this.resetEnabled = properties.get("resetEnabled")!=null?(boolean)properties.get("resetEnabled"):false;
        this.tournamentEnabled = (boolean)properties.getOrDefault("tournamentEnabled",false);
        //this.index = properties.get("index")!=null?(String)properties.get("index"):null;
    }

    public String applicationClassName() {
        return applicationClassName;
    }

    public void applicationClassName(String applicationClassName) {
        this.applicationClassName = applicationClassName;
    }

    public boolean tournamentEnabled(){
        return this.tournamentEnabled;
    }
    public void tournamentEnabled(boolean tournamentEnabled){
        this.tournamentEnabled = tournamentEnabled;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name",name);
        jsonObject.addProperty("tag",tag);
        jsonObject.addProperty("typeId",typeId);
        jsonObject.addProperty("category",category);
        jsonObject.addProperty("resetEnabled",resetEnabled);
        jsonObject.addProperty("privateAccess",accessMode== Access.PRIVATE_ACCESS_MODE);
        return jsonObject;
    }
}
