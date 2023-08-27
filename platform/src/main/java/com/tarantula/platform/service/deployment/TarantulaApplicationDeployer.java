package com.tarantula.platform.service.deployment;

import java.io.File;
import java.util.*;

import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.DeployCode;
import com.icodesoftware.service.OnLobby;
import com.icodesoftware.service.Serviceable;
import com.icodesoftware.util.LongTypeKey;
import com.tarantula.platform.*;
import com.tarantula.platform.service.ApplicationProvider;

public class TarantulaApplicationDeployer implements Serviceable, Configurable.Listener<OnLobby> {

	private final TarantulaContext context;
	private TarantulaLogger logger = JDKLogger.getLogger(TarantulaApplicationDeployer.class);
	public TarantulaApplicationDeployer(final TarantulaContext context ){
		this.context = context;
	}

	public void shutdown() throws Exception {

	}
	public void start() throws Exception {
		this.context._syncNodeData();
		DataStore datastore = this.context.masterDataStore();
		long bucketId = this.context.node().bucketId();
		BucketIndex bucketIndex = new BucketIndex(bucketId);
		datastore.createIfAbsent(bucketIndex,true);
		List<LobbyDescriptor> bList = bucketIndex.lobbyCount>0?datastore.list(new LobbyQuery(bucketId)):new ArrayList<>();
		if(bList.isEmpty()){
			bList = deployFromLocal(bucketIndex);
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
			c.applications = datastore.list(new ApplicationQuery(c.descriptor.id()));
			OnLobby _ob = this.context.configure(c);
			this.context.deploymentService().register(_ob);
		}
		/**
		long deploymentId = this.context.node().deploymentId();
		IndexSet indexSet = new IndexSet();
		indexSet.id(deploymentId);
		indexSet.label(Account.GameClusterLabel);
		if(datastore.load(indexSet)){
			indexSet.keySet().forEach((gc)->{
				deployGameCluster(gc);
			});
		}
		IndexSet moduleIndex = new IndexSet();
		moduleIndex.id(deploymentId);
		moduleIndex.label(Account.ModuleLabel);
		if(datastore.load(moduleIndex)){
			moduleIndex.keySet().forEach((pc)->{
				//deployModule(pc);
			});
		}**/
	}
	private void deployModule(long publishingId){
		try {
			List<LobbyDescriptor> blist = this.context.masterDataStore().list(new LobbyQuery(publishingId));
			blist.forEach((lb)->{
				this.context.setOnLobby(lb,this);
			});
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}
	private void deployGameCluster(String gameClusterId){
		try {
			GameCluster gameCluster = this.context.loadGameCluster(gameClusterId);//new GameCluster();
			//gameCluster.distributionKey(gameClusterId);
			//if(!this.context.masterDataStore().load(gameCluster)) return;
			if(gameCluster==null || gameCluster.disabled()){
				return;
			}
			this.context.setGameServiceProvider(gameCluster);
			this.context.setGameClusterOnLobby(gameCluster,this);
		}catch (Exception ex){
			throw new RuntimeException(ex);
		}
	}

	private List<LobbyDescriptor> deployFromLocal(BucketIndex bucketIndex) throws Exception{
		logger.warn("Deploying application from local settings with bucketId ["+bucketIndex.id()+"]");
		DataStore dataStore = this.context.masterDataStore();
		List<String> dxml = loadFromLocal();
		XMLParser xp = new XMLParser();
		dxml.forEach((xm)->{
			try{
				xp.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream(xm));
			}catch (Exception ex){
				logger.warn("XML parsing error",ex);
				throw new RuntimeException(ex);//stop
			}
		});
		ArrayList<LobbyDescriptor> blist = new ArrayList<>();
		xp.configurations.forEach((c)->{
			c.descriptor.onEdge(true);
			c.descriptor.ownerKey(new LongTypeKey(bucketIndex.id()));
			dataStore.create(c.descriptor);
			dataStore.createIfAbsent(new LobbyTypeIdIndex(bucketIndex.id(),c.descriptor.typeId(),c.descriptor.id(),0),false);
			blist.add(c.descriptor);
			c.applications.forEach((a)->{
				a.ownerKey(c.descriptor.key());
				a.onEdge(true);
				if(c.descriptor.deployCode()== DeployCode.SYSTEM_MODULE||c.descriptor.deployCode()== DeployCode.APPLICATION_MODULE||c.descriptor.deployCode()== DeployCode.USER_MODULE){
					a.applicationClassName(this.context.singleModuleApplication);
				}
				dataStore.create(a);
			});
		});
		return blist;
	}
	private List<String> loadFromLocal(){
		List<String> dlist = new ArrayList<>();
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

	@Override
	public  void onUpdated(OnLobby onLobby){
		this.context.deploymentService().register(onLobby);
	}
}
