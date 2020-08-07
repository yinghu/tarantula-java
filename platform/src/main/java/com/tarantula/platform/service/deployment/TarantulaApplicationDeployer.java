package com.tarantula.platform.service.deployment;

import java.util.*;
import com.tarantula.platform.*;
import com.tarantula.platform.service.OnLobby;
import com.tarantula.platform.service.Serviceable;

public class TarantulaApplicationDeployer implements Serviceable {


	private final TarantulaContext context;
	public TarantulaApplicationDeployer(final TarantulaContext context ){
		this.context = context;
	}

	public void start() throws Exception {
		this.context._registerNode();
		String bucketId = this.context.bucketId();
		List<LobbyDescriptor> bList = this.context.query(new String[]{bucketId},new LobbyQuery(bucketId));
		ArrayList<LobbyConfiguration> configurations = new ArrayList();
		bList.forEach((d)->{
			this.context.setLobby(d);//override the default one
			LobbyConfiguration lcx = new LobbyConfiguration();
			lcx.descriptor  = d;
			configurations.add(lcx);
		});
		Collections.sort(configurations,new LobbyComparator());
		for(LobbyConfiguration c:configurations){//may load from cluster or data store or local files
			c.configurations = this.context.query(new String[]{c.descriptor.distributionKey()},new ApplicationConfigurationQuery(c.descriptor.distributionKey()));
			this.context.configureConfigurations(c);
			c.views = this.context.query(new String[]{c.descriptor.distributionKey()},new OnViewQuery(c.descriptor.distributionKey()));
			this.context.configureViews(c);//deploy views
			c.applications = this.context.query(new String[]{c.descriptor.distributionKey()},new ApplicationQuery(c.descriptor.distributionKey()));
			OnLobby _ob = this.context.configure(c);
			this.context.deploymentService().register(_ob);
		}
	}

	public void shutdown() throws Exception {
		
	}
}
