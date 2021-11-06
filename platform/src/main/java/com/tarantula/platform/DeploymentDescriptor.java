package com.tarantula.platform;

import java.io.IOException;
import java.util.Map;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Descriptor;
import com.tarantula.platform.event.PortableEventRegistry;

public class DeploymentDescriptor extends DefaultDescriptor implements Portable {


    public DeploymentDescriptor(){
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
		this.typeId = in.readUTF("1");
        this.codebase = in.readUTF("2");
        this.moduleArtifact = in.readUTF("3");
		this.moduleVersion = in.readUTF("4");
    }
    @Override
	public void writePortable(PortableWriter out) throws IOException {
		out.writeUTF("1",this.typeId);
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

    public Descriptor copy() {
        DeploymentDescriptor _copy = new DeploymentDescriptor();
        _copy.moduleId(this.moduleId);
        _copy.typeId(this.typeId);
        _copy.name(this.name);
        _copy.type(this.type);
        _copy.category(this.category);
        _copy.tag(this.tag);
        _copy.applicationClassName(this.applicationClassName);
        _copy.moduleName(this.moduleName);
        _copy.accessRank(this.accessRank);
        _copy.tournamentEnabled(this.tournamentEnabled);
        return _copy;
    }
}
