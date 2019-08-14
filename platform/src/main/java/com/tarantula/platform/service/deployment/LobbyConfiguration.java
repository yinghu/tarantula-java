package com.tarantula.platform.service.deployment;

import java.util.ArrayList;
import java.util.List;

import com.tarantula.OnView;
import com.tarantula.platform.DeploymentDescriptor;


public class LobbyConfiguration{
	
	public DeploymentDescriptor descriptor = new DeploymentDescriptor();
	//public ClassLoader classLoader;
	public List<DeploymentDescriptor> applications = new ArrayList();
	public List<OnView> views = new ArrayList<>();
	public List<ServiceConfiguration> configurations = new ArrayList<>();

}
