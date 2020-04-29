package com.tarantula.platform;

import java.io.IOException;
import java.util.Map;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.platform.event.PortableEventRegistry;

public class DeploymentDescriptor extends DefaultDescriptor implements Portable {


    public DeploymentDescriptor(){
        this.vertex = "DeploymentApplication";
        this.onEdge = true;
    }

    @Override
	public int getClassId() {
		return PortableEventRegistry.APPLICATION_DESCRIPTOR_CID;
	}
    @Override
	public int getFactoryId() {
		return PortableEventRegistry.OID;
	}
    @Override
	public void readPortable(PortableReader in) throws IOException {
		this.subtypeId = in.readUTF("1");
        this.codebase = in.readUTF("2");
        this.moduleArtifact = in.readUTF("3");
		this.moduleVersion = in.readUTF("4");
    }
    @Override
	public void writePortable(PortableWriter out) throws IOException {
		out.writeUTF("1",this.subtypeId);
        out.writeUTF("2",this.codebase);
		out.writeUTF("3",this.moduleArtifact);
		out.writeUTF("4",this.moduleVersion);
    }
    @Override
    public Map<String,Object> toMap(){
        Map<String,Object> _props = super.toMap();
        return _props;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        super.fromMap(properties);
    }

    public DeploymentDescriptor deploy(String instanceId){
        DeploymentDescriptor ins = new DeploymentDescriptor();
        ins.instanceId(instanceId);
        ins.type(this.type+"/instance");
        ins.category(this.category);
        ins.bucket(this.bucket);
        ins.oid(this.oid);
        ins.typeId(this.typeId);
        ins.subtypeId(this.subtypeId);
        ins.capacity(this.capacity);
        ins.accessControl(this.accessControl);
        ins.accessMode(this.accessMode);
        ins.accessRank(this.accessRank);
        ins.entryCost(this.entryCost);
        ins.tag(this.tag);
        ins.singleton(this.singleton);
        ins.name(this.name);
        ins.description(this.description);
        ins.viewId(this.viewId);
        ins.codebase(this.codebase);
        ins.moduleArtifact(this.moduleArtifact);
        ins.moduleVersion(this.moduleVersion);
        ins.moduleName(this.moduleName);
        ins.timerOnModule(this.timerOnModule);
        ins.applicationClassName(this.applicationClassName);
        ins.configurationName(this.configurationName);
        ins.configurationType(this.configurationType);
        ins.responseLabel(this.responseLabel);
        ins.instancesOnStartupPerPartition(this.instancesOnStartupPerPartition);
        ins.maxInstancesPerPartition(this.maxInstancesPerPartition);
        ins.deployPriority(this.deployPriority);
        ins.deployCode(this.deployCode);
        ins.logEnabled(this.logEnabled);
        ins.maxIdlesOnInstance(this.maxIdlesOnInstance);
        ins.runtimeDuration(this.runtimeDuration);
        ins.runtimeDurationOnInstance(this.runtimeDurationOnInstance);
        ins.resetEnabled(this.resetEnabled);
        return ins;
    }
}
