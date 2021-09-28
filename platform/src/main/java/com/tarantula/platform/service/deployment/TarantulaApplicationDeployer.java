package com.tarantula.platform.service.deployment;

import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.icodesoftware.*;
import com.icodesoftware.service.DeployCode;
import com.icodesoftware.service.OnLobby;
import com.icodesoftware.service.RecoverService;
import com.icodesoftware.service.Serviceable;
import com.tarantula.platform.*;
import com.tarantula.platform.service.ApplicationProvider;
import com.tarantula.platform.service.cluster.PortableRegistry;

public class TarantulaApplicationDeployer implements Serviceable, Configurable.Listener<OnLobby> {

	private final TarantulaContext context;
	public TarantulaApplicationDeployer(final TarantulaContext context ){
		this.context = context;
	}
	public void shutdown() throws Exception {

	}
	public void start() throws Exception {
		this.context._registerNode();
		RecoverService recoverService = this.context.tarantulaCluster().recoverService();
		String bucketId = this.context.bucketId();
		List<LobbyDescriptor> bList = query(recoverService,PortableRegistry.OID,new LobbyQuery(bucketId),new String[]{bucketId});
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
			c.views = this.context.loadViewList(c.descriptor.typeId());
			this.context.configureViews(c);//deploy views
			c.applications = query(recoverService,PortableRegistry.OID,new ApplicationQuery(c.descriptor.distributionKey()),new String[]{c.descriptor.distributionKey()});
			OnLobby _ob = this.context.configure(c);
			this.context.deploymentService().register(_ob);
		}
		byte[] gameClusterIndexData = this.context.integrationCluster().recoverService().loadGameClusterIndex();
		IndexSet indexSet = new IndexSet();
		indexSet.distributionKey(this.context.bucketId());
		indexSet.label(Account.GameClusterLabel);
		if(gameClusterIndexData!=null&&gameClusterIndexData.length>0){
			indexSet.fromBinary(gameClusterIndexData);
			this.context.masterDataStore().update(indexSet);
		}
		if(this.context.masterDataStore().load(indexSet)){
			indexSet.keySet().forEach((gc)->{
				deployGameCluster(gc);
			});
		}
		byte[] moduleIndexData = this.context.integrationCluster().recoverService().loadModuleIndex();
		IndexSet moduleIndex = new IndexSet();
		moduleIndex.distributionKey(this.context.bucketId());
		moduleIndex.label(Account.ModuleLabel);
		if(moduleIndexData!=null&&moduleIndexData.length>0){
			moduleIndex.fromBinary(moduleIndexData);
			this.context.masterDataStore().update(moduleIndex);
		}
		if(this.context.masterDataStore().load(moduleIndex)){
			moduleIndex.keySet().forEach((pc)->{
				deployModule(pc);
			});
		}
	}
	private void deployModule(String publishingId){
		try {
			RecoverService recoverService = this.context.integrationCluster().recoverService();
			String memberId = recoverService.findDataNode(this.context.dataStoreMaster,publishingId.getBytes());
			List<LobbyDescriptor> blist = this.context.queryFromIntegrationNode(memberId,PortableRegistry.OID, new LobbyQuery(publishingId), new String[]{publishingId},false);
			blist.forEach((lb)->{
				this.context.setOnLobby(memberId,lb,this);
			});
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}
	private void deployGameCluster(String gameClusterId){
		try {
			RecoverService recoverService = this.context.integrationCluster().recoverService();
			String memberId = recoverService.findDataNode(context.dataStoreMaster,gameClusterId.getBytes());
			if(memberId==null){
				return;
			}
			byte[] ret = recoverService.load(memberId,context.dataStoreMaster,gameClusterId.getBytes());
			GameCluster gameCluster = new GameCluster();
			gameCluster.distributionKey(gameClusterId);
			gameCluster.fromBinary(ret);
			if((boolean)gameCluster.property(GameCluster.DISABLED)){
				return;
			}
			this.context.setGameClusterOnLobby(memberId,gameCluster,this);
		}catch (Exception ex){
			throw new RuntimeException(ex);
		}
	}
	private <T extends Recoverable> List<T> query(RecoverService recoverService,int factoryId,RecoverableFactory<T> factory,String[] params) throws Exception{
		List<T> tlist = new ArrayList<>();
		CountDownLatch _lock = new CountDownLatch(1);
		String cid = this.context.deploymentService().distributionCallback().registerQueryCallback((k,v)->{
			T t = factory.create();
			t.fromBinary(v);
			t.distributionKey(new String(k));
			if(!t.disabled()){
				tlist.add(t);
			}
		},()-> _lock.countDown());
		recoverService.queryStart(null,cid,context.dataStoreMaster,factoryId,factory.registryId(),params);
		_lock.await();
		this.context.deploymentService().distributionCallback().removeQueryCallback(cid);
		return tlist;
	}
	private List<LobbyDescriptor> deployFromLocal(String bucketId) throws Exception{
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
				a.label(ApplicationProvider.LABEL);
				a.onEdge(true);
				if(c.descriptor.deployCode()== DeployCode.SYSTEM_MODULE||c.descriptor.deployCode()== DeployCode.APPLICATION_MODULE||c.descriptor.deployCode()== DeployCode.USER_MODULE){
					a.applicationClassName(this.context.singleModuleApplication);
				}
				dataStore.create(a);
			});
		});
		return blist;
	}
	private List<String> loadFromLocal() throws Exception{
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
	private List<String> systemDeploy() throws Exception{
		ArrayList<String> arrayList = new ArrayList<>();
		JarFile file = new JarFile("../lib/gec-platform-"+context.platformVersion+".jar");
		Enumeration e = file.entries();
		while (e.hasMoreElements()) {
			JarEntry je = (JarEntry) e.nextElement();
			String name = je.getName();
			if(name.startsWith("application")&&name.endsWith(".xml")){
				arrayList.add(name);
			}
		}
		return arrayList;
	}
	@Override
	public  void onUpdated(OnLobby onLobby){
		this.context.deploymentService().register(onLobby);
	}
}
