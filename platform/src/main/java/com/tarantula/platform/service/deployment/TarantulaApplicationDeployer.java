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
		List<ServiceConfiguration> _slist = this.context.query(new String[]{this.context.masterDataStore().bucket()},new ServiceConfigurationQuery(this.context.masterDataStore().bucket()));
		Collections.sort(_slist,new ServiceConfigurationComparator());
		for(ServiceConfiguration c: _slist){
			this.context.configure(c); //setup configurations
		}
		List<LobbyDescriptor> bList = this.context.query(new String[]{this.context.masterDataStore().bucket()},new LobbyQuery(this.context.masterDataStore().bucket()));//this.context.tarantulaCluster.list(query);
		ArrayList<LobbyConfiguration> configurations = new ArrayList();
		bList.forEach((d)->{
			this.context.setLobby(d);//override the default one
			LobbyConfiguration lcx = new LobbyConfiguration();
			lcx.descriptor  = d;
			configurations.add(lcx);
		});
		Collections.sort(configurations,new LobbyComparator());
		for(LobbyConfiguration c:configurations){//may load from cluster or data store or local files
			c.configurations = this.context.query(new String[]{c.descriptor.distributionKey()},new ServiceConfigurationQuery(c.descriptor.distributionKey()));
			for(ServiceConfiguration scc : c.configurations){
				this.context.configure(scc);
			}
			c.views = this.context.query(new String[]{c.descriptor.distributionKey()},new OnViewQuery(c.descriptor.distributionKey()));
			this.context.configureViews(c);//deploy views
			c.applications = this.context.query(new String[]{c.descriptor.distributionKey()},new ApplicationQuery(c.descriptor.distributionKey()));
			OnLobby _ob = this.context.configure(c);
			if(c.descriptor.deployCode()>0){//system lobby if deploy code <=0, default 0
				this.onLobby(_ob);
			}
		}
	}

	public void shutdown() throws Exception {
		
	}
	private void onLobby(OnLobby deployed){
		this.context.deploymentService().deploy(deployed);
	}
}
