package com.tarantula.platform.service.deployment;

import java.io.File;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.DeployCode;
import com.icodesoftware.service.OnLobby;
import com.icodesoftware.service.Serviceable;
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
		this.context._registerNode();
		DataStore datastore = this.context.masterDataStore();
		String bucketId = this.context.bucketId();
		List<LobbyDescriptor> bList = datastore.list(new LobbyQuery(bucketId));
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
			c.applications = datastore.list(new ApplicationQuery(c.descriptor.distributionKey()));
			OnLobby _ob = this.context.configure(c);
			this.context.deploymentService().register(_ob);
		}
		IndexSet indexSet = new IndexSet();
		indexSet.distributionKey(this.context.bucketId());
		indexSet.label(Account.GameClusterLabel);
		if(datastore.load(indexSet)){
			indexSet.keySet().forEach((gc)->{
				deployGameCluster(gc);
			});
		}
		IndexSet moduleIndex = new IndexSet();
		moduleIndex.distributionKey(this.context.bucketId());
		moduleIndex.label(Account.ModuleLabel);
		if(datastore.load(moduleIndex)){
			moduleIndex.keySet().forEach((pc)->{
				deployModule(pc);
			});
		}
	}
	private void deployModule(String publishingId){
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
			GameCluster gameCluster = new GameCluster();
			gameCluster.distributionKey(gameClusterId);
			if(!this.context.masterDataStore().load(gameCluster)) return;
			if((boolean)gameCluster.property(GameCluster.DISABLED)){
				return;
			}
			this.context.setGameServiceProvider(gameCluster);
			this.context.setGameClusterOnLobby(gameCluster,this);
		}catch (Exception ex){
			throw new RuntimeException(ex);
		}
	}

	private List<LobbyDescriptor> deployFromLocal(String bucketId) throws Exception{
		logger.warn("Deploying application from local settings with bucketId ["+bucketId+"]");
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
