package com.tarantula.platform.service.deployment;
import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.service.OnLobby;
import com.icodesoftware.service.RecoverService;
import com.icodesoftware.service.Serviceable;
import com.tarantula.platform.*;
import com.tarantula.platform.service.Application;
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
		if(bList.isEmpty()){
			bList = deployFromLocal(bucketId);
		}
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
			T t = factory.create();
			t.fromBinary(v);
			t.distributionKey(new String(k));
			tlist.add(t);
		},()->{
			_lock.countDown();
		});
		recoverService.queryStart(cid,context.dataStoreMaster,factoryId,factory.registryId(),params);
		_lock.await();
		this.context.deploymentService().distributionCallback().removeQueryCallback(cid);
		return tlist;
	}
	private List<LobbyDescriptor> deployFromLocal(String bucketId){
		RecoverableFactory query = new LobbyQuery(bucketId);
		DataStore dataStore = this.context.masterDataStore();
		List<String> dxml = loadFromLocal();
		XMLParser xp = new XMLParser();
		dxml.forEach((xm)->{
			try{xp.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream(xm));}catch (Exception ex){ex.printStackTrace();}
		});
		ArrayList<LobbyDescriptor> blist = new ArrayList<>();
		xp.configurations.forEach((c)->{
			c.descriptor.label(query.label());
			c.descriptor.onEdge(true);
			c.descriptor.owner(query.distributionKey());
			dataStore.create(c.descriptor);
			dataStore.create(new LobbyTypeIdIndex(bucketId,c.descriptor.typeId(),c.descriptor.distributionKey(),""));
			blist.add(c.descriptor);
			c.applications.forEach((a)->{
				a.owner(c.descriptor.distributionKey());
				a.label(Application.LABEL);
				a.onEdge(true);
				dataStore.create(a);
			});
			c.views.forEach((v)->{
				v.owner(c.descriptor.distributionKey());
				dataStore.create(v);
			});
			c.configurations.forEach((cf)->{
				cf.owner(c.descriptor.distributionKey());
				dataStore.create(cf);
			});
		});
		return blist;
	}
	private List<String> loadFromLocal(){
		List<String> dlist = this.systemDeploy();
		File f = new File("../deploy");
		if(f.exists()){
			for(String s : f.list()){
				if(s.endsWith(".xml")){
					dlist.add(s);
				}
			}
		}
		return dlist;
	}
	private List<String> systemDeploy(){
		ArrayList<String> arrayList = new ArrayList<>();
		try{
			JarFile file = new JarFile("../lib/gec-platform-"+context.platformVersion+".jar");
			Enumeration e = file.entries();
			while (e.hasMoreElements()) {
				JarEntry je = (JarEntry) e.nextElement();
				String name = je.getName();
				if(name.startsWith("application")&&name.endsWith(".xml")){
					arrayList.add(name);
				}
			}
		}catch (Exception ex){
			ex.printStackTrace();
		}
		return arrayList;
	}
}
