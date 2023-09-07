package com.tarantula.platform;

import com.google.gson.JsonObject;
import com.icodesoftware.Access;
import com.icodesoftware.Descriptor;

import com.icodesoftware.service.DeployCode;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;

import java.util.Map;

public class DefaultDescriptor extends RecoverableObject implements Descriptor {

	protected String typeId;
    protected String type;
    protected String category;

    protected int accessControl;
    protected int accessMode;
    protected int accessRank;
    protected String tag;

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

    public DefaultDescriptor(){
        this.onEdge = true;
    }
    public String moduleId(){
        return this.moduleId==null?typeId:moduleId;
    }
    public void moduleId(String moduleId){
        this.moduleId = moduleId;
    }
    public String moduleArtifact(){
        return moduleArtifact;
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

    //Bufferable methods
    @Override
    public boolean read(DataBuffer buffer){
        this.typeId = buffer.readUTF8();
        this.type = buffer.readUTF8();
        this.name = buffer.readUTF8();
        this.category = buffer.readUTF8();
        this.disabled = buffer.readBoolean();
        this.logEnabled = buffer.readBoolean();
        this.resetEnabled = buffer.readBoolean();
        this.deployCode = buffer.readInt();
        this.deployPriority = buffer.readInt();
        this.accessControl = buffer.readInt();
        this.accessMode = buffer.readInt();
        return true;
    }
    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(this.typeId);
        buffer.writeUTF8(this.type);
        buffer.writeUTF8(this.name);
        buffer.writeUTF8(this.category);
        buffer.writeBoolean(this.disabled);
        buffer.writeBoolean(this.logEnabled);
        buffer.writeBoolean(this.resetEnabled);
        buffer.writeInt(this.deployCode);
        buffer.writeInt(this.deployPriority);
        buffer.writeInt(this.accessControl);
        buffer.writeInt(this.accessMode);
        return true;
    }

}
