package com.tarantula.platform.service.deployment;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.service.OnLobby;
import com.icodesoftware.service.RecoverService;
import com.icodesoftware.service.Serviceable;
import com.tarantula.platform.*;
import com.tarantula.platform.service.cluster.PortableRegistry;

public class TarantulaApplicationDeployer implements Serviceable {

	private final TarantulaContext context;
	private RecoverService recoverService;
	public TarantulaApplicationDeployer(final TarantulaContext context ){
		this.context = context;
	}

	public void start() throws Exception {
		this.context._registerNode();
		recoverService = this.context.tarantulaCluster().recoverService();
		String bucketId = this.context.bucketId();
		List<LobbyDescriptor> bList = query(PortableRegistry.OID,new LobbyQuery(bucketId),new String[]{bucketId});
		ArrayList<LobbyConfiguration> configurations = new ArrayList();
		bList.forEach((d)->{
			this.context.setLobby(d);//override the default one
			LobbyConfiguration lcx = new LobbyConfiguration();
			lcx.descriptor  = d;
			configurations.add(lcx);
		});
		Collections.sort(configurations,new LobbyComparator());
		for(LobbyConfiguration c:configurations){//may load from cluster or data store or local files
			c.configurations = query(PortableRegistry.OID,new ApplicationConfigurationQuery(c.descriptor.distributionKey()),new String[]{c.descriptor.distributionKey()});
			this.context.configureConfigurations(c);
			c.views = query(PortableRegistry.OID,new OnViewQuery(c.descriptor.distributionKey()),new String[]{c.descriptor.distributionKey()});
			this.context.configureViews(c);//deploy views
			c.applications = query(PortableRegistry.OID,new ApplicationQuery(c.descriptor.distributionKey()),new String[]{c.descriptor.distributionKey()});
			OnLobby _ob = this.context.configure(c);
			this.context.deploymentService().register(_ob);
		}
	}

	public void shutdown() throws Exception {
		
	}
	private <T extends Recoverable> List<T> query(int factoryId,RecoverableFactory<T> factory,String[] params) throws Exception{
		List<T> tlist = new ArrayList<>();
		CountDownLatch _lock = new CountDownLatch(1);
		String cid = this.context.deploymentService().distributionCallback().registerQueryCallback((k,v)->{
			System.out.println(new String(v));
			T t = factory.create();
			t.fromBinary(v);
			t.distributionKey(new String(k));
			tlist.add(t);
		},()->{
			_lock.countDown();
			System.out.println("end query");
		});
		recoverService.queryStart(cid,context.dataStoreMaster,factoryId,factory.registryId(),params);
		_lock.await();
		this.context.deploymentService().distributionCallback().removeQueryCallback(cid);
		return tlist;
	}
}
