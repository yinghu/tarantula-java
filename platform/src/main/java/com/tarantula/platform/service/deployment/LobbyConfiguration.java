package com.tarantula.platform.service.deployment;

import java.util.ArrayList;
import java.util.List;

import com.icodesoftware.Configuration;
import com.icodesoftware.OnView;
import com.tarantula.platform.DeploymentDescriptor;
import com.tarantula.platform.LobbyDescriptor;


public class LobbyConfiguration{
	
	public LobbyDescriptor descriptor = new LobbyDescriptor();
	public List<DeploymentDescriptor> applications = new ArrayList();
	public List<OnView> views = new ArrayList<>();
	public List<Configuration> configurations = new ArrayList<>();

	public LobbyConfiguration(){}
	public LobbyConfiguration(LobbyDescriptor lobbyDescriptor){
		this.descriptor = lobbyDescriptor;
	}

}
