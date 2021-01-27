package com.tarantula.platform;

import com.icodesoftware.Descriptor;
import com.icodesoftware.service.DeployCode;
import com.tarantula.platform.util.SystemUtil;

import java.util.Map;

public class DefaultDescriptor extends DeploymentObject implements Descriptor {

	protected String typeId;
    protected String applicationId;
    protected String instanceId;
	protected String type;
    protected String subtypeId;
    protected String viewId;
    protected String category;
    protected boolean singleton;

    protected int accessControl;
    protected int accessMode;
    protected int accessRank;
    protected String tag;
    protected double entryCost;

	protected String name;
    protected String icon;
	protected String description;

	protected int capacity;
    protected String responseLabel;
    protected String moduleId;
    protected String moduleArtifact;
    protected String moduleVersion;
    protected String codebase;
    protected String moduleName;


    protected int deployPriority;
    protected int deployCode = DeployCode.SYSTEM_APPLICATION;

    protected long timerOnModule;
    protected boolean logEnabled =true;

    protected int maxInstancesPerPartition = 10;

    protected  String applicationClassName;
    protected  int instancesOnStartupPerPartition =1;

    protected int maxIdlesOnInstance;
    protected long runtimeDuration;
    protected long runtimeDurationOnInstance;

    protected boolean resetEnabled;

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
    public long timerOnModule(){
        return this.timerOnModule;
    }
    public void timerOnModule(long timerOnModule){
        this.timerOnModule = timerOnModule;
    }
    public boolean logEnabled(){
        return this.logEnabled;
    }
    public void logEnabled(boolean logEnabled){
        this.logEnabled = logEnabled;
    }

    public String typeId() {
		return this.typeId;
	}
    public String applicationId(){
        return this.applicationId;
    }
    public String instanceId() {
        return this.instanceId;
    }

    public String type() {
		return this.type;
	}


    public String subtypeId() {
        return this.subtypeId;
    }

    public String category(){
        return this.category;
    }
    public String responseLabel(){
        return this.responseLabel;
    }
	public String description() {
		
		return this.description;
	}


	public String name() {
		return this.name;
	}
    public String icon(){
        return this.icon;
    }
	public int capacity() {
		
		return this.capacity;
	}
	public void capacity(int capacity) {
		this.capacity = capacity;
	}
	
	public void description(String description) {
		this.description = description;
	}

    public String viewId(){
        return this.viewId;
    }
    public void viewId(String viewId){
        this.viewId = viewId;
    }
	public void typeId(String typeId) {
		this.typeId = typeId;
	}
    public void applicationId(String applicationId){
        this.applicationId = applicationId;
    }
    public void instanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public void name(String name) {
		this.name = name;
	}
	public void icon(String icon){
        this.icon = icon;
    }
	public void type(String type) {
		this.type = type;
	}

    public void subtypeId(String subtypeId) {
        this.subtypeId = subtypeId;
    }
    public void category(String category){
        this.category = category;
    }
    public void responseLabel(String responseLabel){
        this.responseLabel = responseLabel;
    }
    public boolean resetEnabled(){
        return this.resetEnabled;
    }
    public void resetEnabled(boolean resetEnabled){
        this.resetEnabled = resetEnabled;
    }
	@Override
	public String toString(){
		return new String(SystemUtil.toJson(this.toMap()));
	}

	public boolean singleton(){
        return this.singleton;
    }
    public void singleton(boolean singleton){
        this.singleton = singleton;
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

    public int maxIdlesOnInstance(){
        return this.maxIdlesOnInstance;
    }
    public void maxIdlesOnInstance(int maxIdlesOnInstance){
        this.maxIdlesOnInstance = maxIdlesOnInstance;
    }
    public int maxInstancesPerPartition(){
        return this.maxInstancesPerPartition;
    }
    public void maxInstancesPerPartition(int maxPoolSize){
        this.maxInstancesPerPartition = maxPoolSize;
    }
    @Override
    public Map<String, Object> toMap() {
        Map<String,Object> _props = this.properties;
        _props.put("typeId",this.typeId);
        _props.put("subtypeId",this.subtypeId);
        _props.put("type",this.type);
        _props.put("name",this.name);
        _props.put("icon",this.icon);
        _props.put("description",this.description);
        _props.put("viewId",this.viewId);
        _props.put("singleton",this.singleton);
        _props.put("tag",this.tag);
        _props.put("entryCost",this.entryCost);
        _props.put("capacity",this.capacity); //0 unlimited
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
        _props.put("timerOnModule",this.timerOnModule);
        _props.put("logEnabled",this.logEnabled);
        _props.put("maxInstancesPerPartition",this.maxInstancesPerPartition);
        _props.put("applicationClassName",this.applicationClassName);
        _props.put("instancesOnStartupPerPartition",this.instancesOnStartupPerPartition);
        _props.put("maxIdlesOnInstance",this.maxIdlesOnInstance);
        _props.put("responseLabel",this.responseLabel);
        _props.put("runtimeDuration",this.runtimeDuration);
        _props.put("runtimeDurationOnInstance",this.runtimeDurationOnInstance);
        _props.put("resetEnabled",this.resetEnabled);
        _props.put("index",this.index);
        return _props;
    }

    @Override
    public void fromMap(Map<String, Object> properties) {
        this.typeId=(String)properties.get("typeId");
        this.subtypeId=properties.get("subtypeId")!=null?(String)properties.get("subtypeId"):null;
        this.type=(String)properties.get("type");
        this.name=(String)properties.get("name");
        this.icon=properties.get("icon")!=null?(String)properties.get("icon"):null;
        this.description=properties.get("description")!=null?(String)properties.get("description"):null;
        this.viewId=properties.get("viewId")!=null?(String)properties.get("viewId"):null;
        this.singleton=properties.get("singleton")!=null?(boolean)properties.get("singleton"):false;
        this.tag=properties.get("tag")!=null?(String)properties.get("tag"):null;
        this.entryCost=properties.get("entryCost")!=null?((Number)properties.get("entryCost")).doubleValue():0;
        this.capacity=properties.get("capacity")!=null?((Number)properties.get("capacity")).intValue():0;
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
        this.timerOnModule =properties.get("timerOnModule")!=null?((Number)properties.get("timerOnModule")).longValue():50;
        this.logEnabled = properties.get("logEnabled")!=null?(Boolean)properties.get("logEnabled"):true;
        this.maxInstancesPerPartition = properties.get("maxInstancesPerPartition")!=null?((Number)properties.get("maxInstancesPerPartition")).intValue():1;
        this.applicationClassName = properties.get("applicationClassName")!=null?(String)properties.get("applicationClassName"):null;
        this.instancesOnStartupPerPartition = properties.get("instancesOnStartupPerPartition")!=null?((Number)properties.get("instancesOnStartupPerPartition")).intValue():10;
        this.maxIdlesOnInstance =properties.get("maxIdlesOnInstance")!=null?((Number)properties.get("maxIdlesOnInstance")).intValue():0;
        this.responseLabel =properties.get("responseLabel")!=null? (String)properties.get("responseLabel"):null;
        this.runtimeDuration = properties.get("runtimeDuration")!=null?((Number)properties.get("runtimeDuration")).longValue():0;
        this.runtimeDurationOnInstance = properties.get("runtimeDurationOnInstance")!=null?((Number)properties.get("runtimeDurationOnInstance")).longValue():0;
        this.resetEnabled = properties.get("resetEnabled")!=null?(boolean)properties.get("resetEnabled"):false;
        this.index = properties.get("index")!=null?(String)properties.get("index"):null;
    }

    public String applicationClassName() {
        return applicationClassName;
    }

    public void applicationClassName(String applicationClassName) {
        this.applicationClassName = applicationClassName;
    }
    public int instancesOnStartupPerPartition(){
        return this.instancesOnStartupPerPartition;
    }

    public void instancesOnStartupPerPartition(int instancesOnStartup){
        this.instancesOnStartupPerPartition = instancesOnStartup;
    }
    public long runtimeDuration(){
        return this.runtimeDuration;
    }
    public void runtimeDuration(long runtimeDuration){
        this.runtimeDuration = runtimeDuration;
    }
    public long runtimeDurationOnInstance(){
        return this.runtimeDurationOnInstance;
    }
    public void runtimeDurationOnInstance(long runtimeDurationOnInstance){
        this.runtimeDurationOnInstance = runtimeDurationOnInstance;
    }
}
