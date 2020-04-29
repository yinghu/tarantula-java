package com.tarantula.platform.service.cluster;

import com.google.gson.GsonBuilder;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.Member;
import com.hazelcast.spi.*;
import com.tarantula.*;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.*;
import com.tarantula.platform.bootstrap.ServiceBootstrap;
import com.tarantula.platform.event.MapStoreRecoveryEvent;
import com.tarantula.platform.service.Batch;
import com.tarantula.platform.service.DataStoreProvider;
import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.service.deployment.*;
import com.tarantula.platform.service.deployment.ServiceConfigurationParser;
import com.tarantula.platform.util.ResponseSerializer;
import com.tarantula.platform.util.SystemUtil;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClusterDeployService implements ManagedService, RemoteService, MembershipAwareService {

    private static TarantulaLogger log = JDKLogger.getLogger(ClusterDeployService.class);

    private NodeEngine nodeEngine;
    private TarantulaContext tarantulaContext;
    private ConcurrentHashMap<String,BatchCache> _cache = new ConcurrentHashMap<>();
    private DeploymentServiceProvider deploymentServiceProvider;
    private int scope;
    private GsonBuilder builder;
    @Override
    public void init(NodeEngine nodeEngine, Properties properties) {
        this.nodeEngine = nodeEngine;
        this.scope = Integer.parseInt(properties.getProperty("tarantula-scope"));
        tarantulaContext = TarantulaContext.getInstance();
        this.deploymentServiceProvider = this.tarantulaContext.deploymentService();
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
        new ServiceBootstrap(tarantulaContext._integrationClusterStarted,tarantulaContext._deployServiceStarted,new DeployServiceBootstrap(this),"deploy-service",true).start();
    }
    public void setup(){
        if(scope==Distributable.INTEGRATION_SCOPE){
            this.tarantulaContext.integrationCluster().addEventListener(this.nodeEngine.getLocalMember().getUuid(),(e)->{
                log.warn(e.toString()+"["+this.scope+"]");
                recover(e.source(),e.source(),true);
                return false;
            });
        }
        else if(scope==Distributable.DATA_SCOPE){
            this.tarantulaContext.tarantulaCluster().addEventListener(this.nodeEngine.getLocalMember().getUuid(),(e)->{
                log.warn(e.toString()+"["+scope+"]");
                recover(e.source(),e.sessionId(),true);
                return false;
            });
        }
        log.info("Clustering deployment service started ["+nodeEngine.getConfig().getGroupConfig().getName()+"] on scope ["+this.scope+"]");
    }
    @Override
    public void reset() {

    }

    @Override
    public void shutdown(boolean b) {
        log.warn("deploy service stopped on scope ["+this.scope+"]");
    }

    @Override
    public DistributedObject createDistributedObject(String s) {
        return new DeployServiceProxy(s,nodeEngine,this);
    }

    @Override
    public void destroyDistributedObject(String s) {


    }
    private void _send(Event event){
        if(this.scope==Distributable.DATA_SCOPE){
            this.tarantulaContext.tarantulaCluster().publish(event);
        }
        else{
            this.tarantulaContext.integrationCluster().publish(event);
        }
    }
    private void _read(String fn, int c,int t, ReadableByteChannel vc,String destination,String registerId){
        try{
            ByteBuffer buffer = ByteBuffer.allocate(100000);
            int v = 0;
            while (true) {
                final int len = vc.read(buffer);
                if (len < 0) {
                    break;
                }
                byte[] pb = new byte[len];
                buffer.flip();
                buffer.get(pb);
                MapStoreRecoveryEvent m = new MapStoreRecoveryEvent(destination,fn,pb,registerId,c,t,v++);
                _send(m);
                Thread.sleep(10);
                buffer.clear();
            }
        }
        catch (Exception ex){

        }
    }
    public void recover(String destination,String registerId,boolean fullBackup){
        DataStoreProvider dsp = this.tarantulaContext.dataStoreProvider();
        String loc = this.scope==Distributable.DATA_SCOPE?this.tarantulaContext.tarantulaCluster().subscription():this.tarantulaContext.integrationCluster().subscription();
        if(destination.equals(loc)){
            log.info("There is no need to recover from first node");
            MapStoreRecoveryEvent mre = new MapStoreRecoveryEvent(destination,"",new byte[0],registerId,0,0,0);
            this._send(mre);
            return;
        }
        this.tarantulaContext.schedule(new OneTimeRunner(100,()->{
            if(fullBackup){
                int ct[] = {0};
                dsp.backup(this.scope,(fn,c,fc)->{
                    this._read(fn,ct[0]++,c,fc,destination,registerId);
                });
            }
            else{
                int ct[] = {0};
                dsp.recover(this.scope,(fn,c,fc)->{
                    this._read(fn,ct[0]++,c,fc,destination,registerId);
                });
            }
        }));
    }

    public Batch query(int registryId,String[] params){
        //log.warn("Query on->"+registryId+"/"+nodeEngine.getLocalMember().getAddress().toString());
        BatchCache batchCache = onQuery(registryId,params);
        Batch batch = new Batch();
        batch.batchId = batchCache.batchId;
        batch.count = 0;
        batch.size = batchCache.cache.size();
        if(batch.size>0){
            Recoverable r = batchCache.cache.get(0);
            batch.key = r.distributionKey();
            batch.payload = r.binary()?r.toByteArray():SystemUtil.toJson(r.toMap());
        }
        else{
            batch.payload = new byte[0];
        }
        return batch;
    }

    public Batch query(String batchId,int count){
        BatchCache batchCache = _cache.get(batchId);
        Batch batch = new Batch();
        batch.count = count;
        batch.size = batchCache.cache.size();
        Recoverable r = batchCache.cache.get(count);
        if((count+1)==batch.size){
            _cache.remove(batchId);//remove on last count
        }
        batch.key = r.distributionKey();
        batch.payload = r.binary()?r.toByteArray():SystemUtil.toJson(r.toMap());
        return batch;
    }
    private BatchCache onQuery(int registryId,String[] params){
        BatchCache batchCache = this.createQuery(registryId,params);
        _cache.put(batchCache.batchId,batchCache);
        return batchCache;
    }
    private BatchCache createQuery(int registryId,String[] params){
        BatchCache batchCache = null;
        DataStore dataStore = tarantulaContext.masterDataStore();
        if(registryId==PortableRegistry.SERVICE_CONFIGURATION_CID){
            List rlist = dataStore.list(new ServiceConfigurationQuery(params[0]));
            if(rlist.isEmpty()){
                ServiceConfigurationParser ssp = new ServiceConfigurationParser();
                ssp.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream("tarantula-platform-service-config.xml"));
                rlist.addAll(ssp.configurationMapping.values());
                ServiceConfigurationParser sp = new ServiceConfigurationParser();
                sp.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream(this.tarantulaContext.serviceConfiguration));
                rlist.addAll(sp.configurationMapping.values());
                rlist.forEach((o)->{
                    ServiceConfiguration r = (ServiceConfiguration)o;
                    r.owner(params[0]);
                    dataStore.create(r);
                    r.configurationMappings.forEach((k,v)->{
                        v.owner(r.distributionKey());
                        v.tag(r.tag);
                        dataStore.create(v);
                    });
                });
            }
            batchCache = new BatchCache(UUID.randomUUID().toString(),rlist);
        }
        else if(registryId==PortableRegistry.APPLICATION_CONFIGURATION_CID){
            List alist = dataStore.list(new ApplicationConfigurationQuery(params[0],params[1]));
            batchCache = new BatchCache(UUID.randomUUID().toString(),alist);
        }
        else if(registryId==PortableRegistry.LOBBY_CID){
            RecoverableFactory query = new LobbyQuery(params[0]);
            List blist = new ArrayList();
            dataStore.list(query,(b)->{
                if(!b.disabled()){
                    blist.add(b);
                    //log.info(b.toString());
                }
                return true;
            });
            if(blist.isEmpty()){
                //load from local config
                List<String> dxml = loadFromLocal();
                XMLParser xp = new XMLParser();
                dxml.forEach((xm)->{
                    try{xp.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream(xm));}catch (Exception ex){ex.printStackTrace();}
                });
                xp.configurations.forEach((c)->{
                    c.descriptor.label(query.label());
                    c.descriptor.onEdge(true);
                    c.descriptor.owner(query.distributionKey());
                    dataStore.create(c.descriptor);
                    blist.add(c.descriptor);
                    c.applications.forEach((a)->{
                        a.owner(c.descriptor.distributionKey());
                        a.label("LDA");
                        a.onEdge(true);
                        dataStore.create(a);
                    });
                    c.views.forEach((v)->{
                        v.owner(c.descriptor.distributionKey());
                        dataStore.create(v);
                    });
                    c.configurations.forEach((s)->{
                        s.owner(c.descriptor.distributionKey());
                        dataStore.create(s);
                        s.configurationMappings.forEach((k,cf)->{
                            cf.owner(s.distributionKey());
                            cf.tag(s.tag);
                            dataStore.create(cf);
                        });
                    });
                });
            }
            batchCache = new BatchCache(UUID.randomUUID().toString(),blist);
        }
        else if(registryId==PortableRegistry.INSTANCE_INDEX_CID){
            List ilist = new ArrayList();
            dataStore.list(new InstanceRegistryQuery(params[0]),(ix)->{
                if(!ix.disabled()){
                    ilist.add(ix);
                }
                return true;
            });
            if(ilist.isEmpty()){//create instance/house/statistics/listing per partition
                DeploymentDescriptor deploymentDescriptor = new DeploymentDescriptor();
                deploymentDescriptor.distributionKey(params[0]);
                dataStore.load(deploymentDescriptor);
                for(int i=0;i<deploymentDescriptor.instancesOnStartupPerPartition();i++){
                    for(int p = 0;p<tarantulaContext.platformRoutingNumber;p++){
                        InstanceIndex instanceRegistry = new InstanceIndex();
                        //instanceRegistry.bank(true);
                        instanceRegistry.capacity(deploymentDescriptor.capacity());
                        instanceRegistry.applicationId(deploymentDescriptor.distributionKey());
                        instanceRegistry.owner(deploymentDescriptor.distributionKey());
                        instanceRegistry.accessMode(Session.FAST_PLAY_MODE);
                        instanceRegistry.routingNumber(p);
                        instanceRegistry.bucket(dataStore.bucket());
                        instanceRegistry.oid(SystemUtil.oid());
                        instanceRegistry.tournamentEnabled(deploymentDescriptor.tournamentEnabled());
                        if(dataStore.create(instanceRegistry)){
                            ilist.add(instanceRegistry);
                        }
                    }
                }
            }
            batchCache = new BatchCache(UUID.randomUUID().toString(),ilist);
        }
        else if(registryId==PortableRegistry.ON_INSTANCE_CID){
            List olist = dataStore.list(new OnInstanceQuery(params[0]));
            if(olist.isEmpty()){//create on instances per instance
                InstanceIndex ir = new InstanceIndex();
                ir.distributionKey(params[0]);
                if(dataStore.load(ir)){
                    for(int i=0;i<ir.capacity();i++){//pre-launch on instance
                        OnInstance _a = new OnInstanceTrack();
                        _a.instanceId(ir.distributionKey());
                        _a.owner(ir.distributionKey());
                        _a.onEdge(true);
                        if(dataStore.create(_a)){
                            olist.add(_a);
                        }
                    }
                }
            }
            batchCache = new BatchCache(UUID.randomUUID().toString(),olist);
        }

        else if(registryId==PortableRegistry.DELTA_STAT_CID){
            List hList = new ArrayList();
            DeltaStatistics h = new DeltaStatistics();
            h.distributionKey(params[0]);
            dataStore.load(h);
            hList.add(h);
            batchCache = new BatchCache(UUID.randomUUID().toString(),hList);
        }
        else if(registryId==PortableRegistry.STATISTICS_ENTRY_CID){
            List vlist = dataStore.list(new StatisticsEntryQuery(params[0]));
            batchCache = new BatchCache(UUID.randomUUID().toString(),vlist);
        }
        else if(registryId==PortableRegistry.ON_VIEW_OID){
            List vlist = dataStore.list(new OnViewQuery(params[0]));
            batchCache = new BatchCache(UUID.randomUUID().toString(),vlist);
        }
        else if(registryId== PortableRegistry.APPLICATION_DESCRIPTOR_CID){
            List dlist = new ArrayList();
            dataStore.list(new ApplicationQuery(params[0]),(a)->{
                //log.warn(a.toString());
                if(!a.disabled()){
                    dlist.add(a);
                }
                return true;
            });
            batchCache = new BatchCache(UUID.randomUUID().toString(),dlist);
        }
        return batchCache;
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
            JarFile file = new JarFile("../lib/gec-platform-"+tarantulaContext.platformVersion+".jar");
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
    public String addLobby(Descriptor descriptor){
        DataStore ds = this.tarantulaContext.masterDataStore();
        LobbyQuery query = new LobbyQuery(ds.bucket());
        AtomicBoolean duplicated = new AtomicBoolean(false);
        ResponseHeader resp = new ResponseHeader("addLobby");
        ds.list(query,(d)->{
            if(descriptor.typeId().equals(d.typeId())){
                duplicated.set(true);
                return false;
            }
            return true;
        });
        if(duplicated.get()){
            resp.successful(false);
            resp.message(descriptor.typeId()+" already existed");
            return this.builder.create().toJson(resp);
        }
        descriptor.owner(ds.bucket());
        descriptor.label(query.label());
        descriptor.onEdge(true);
        descriptor.resetEnabled(true);
        ds.create(descriptor);
        if(descriptor.deployCode()<=0||descriptor.tag()==null){
            resp.successful(true);
            resp.message("lobby created with deployCode ->"+descriptor.deployCode());
            return this.builder.create().toJson(resp);
        }
        DeploymentDescriptor lobby = new DeploymentDescriptor();
        lobby.typeId(descriptor.typeId());
        lobby.subtypeId(descriptor.typeId()+"-lobby");
        lobby.type("application");
        lobby.category("lobby");
        lobby.tag(descriptor.tag());//will ignore if the lobby tag is not provided
        lobby.singleton(true);
        lobby.deployPriority(15);
        lobby.applicationClassName("com.tarantula.platform.playmode.GameLobbyApplication");
        lobby.name(descriptor.name());
        lobby.description(descriptor.description());
        lobby.configurationName(descriptor.configurationName());
        lobby.responseLabel(descriptor.responseLabel());
        lobby.label("LDA");
        lobby.owner(descriptor.distributionKey());
        lobby.onEdge(true);
        if(ds.create(lobby)){
            resp.message(lobby.distributionKey()+" created on ["+descriptor.typeId()+"]");
        }
        else{
            resp.successful(false);
            resp.message("failed to create lobby on ["+descriptor.typeId()+"]");
        }
        return this.builder.create().toJson(resp);
    }
    public String enableLobby(String typeId,boolean enabled){
        DataStore ds = this.tarantulaContext.masterDataStore();
        LobbyQuery query = new LobbyQuery(ds.bucket());
        ResponseHeader resp = new ResponseHeader("enableLobby");
        resp.successful(false);
        ds.list(query,(d)->{
            if(typeId.equals(d.typeId())){
                d.disabled(!enabled);
                resp.successful(true);
                resp.message(d.distributionKey()+" disabled ["+d.disabled()+"]");
                ds.update(d);
                return false;
            }
            return true;
        });
        return this.builder.create().toJson(resp);
    }
    public String enableApplication(String applicationId,boolean enabled){
        DataStore ds = this.tarantulaContext.masterDataStore();
        DeploymentDescriptor app = new DeploymentDescriptor();
        app.distributionKey(applicationId);
        ResponseHeader resp = new ResponseHeader("enableApplication");
        resp.successful(false);
        if(ds.load(app)){
            app.disabled(!enabled);
            ds.update(app);
            resp.message(app.distributionKey()+" disabled ["+app.disabled()+"]");
            resp.toMap().put("typeId",app.typeId());
            resp.toMap().put("disabled",app.disabled());
            resp.successful(true);
        }
        return this.builder.create().toJson(resp);
    }
    public String addApplication(Descriptor descriptor){
        DataStore ds = this.tarantulaContext.masterDataStore();
        LobbyQuery query = new LobbyQuery(ds.bucket());
        Descriptor[] lobby={null};
        ResponseHeader resp = new ResponseHeader("addApplication");
        ds.list(query,(d)->{
            if(descriptor.typeId().equals(d.typeId())){
                lobby[0] = d;
                return false;
            }
            return true;
        });
        if(lobby[0]==null){
            resp.successful(false);
            resp.message("failed to create application on"+descriptor.typeId()+"]");
            return this.builder.create().toJson(resp);
        }
        if(!descriptor.singleton()){
            descriptor.applicationClassName("com.tarantula.platform.playmode.DynamicModuleApplication");
        }
        else{
            descriptor.applicationClassName("com.tarantula.platform.playmode.SingletonModuleApplication");
        }
        descriptor.leaderBoardHeader(descriptor.typeId());
        descriptor.resetEnabled(true);
        descriptor.runtimeDuration(descriptor.runtimeDuration()*60*1000);
        descriptor.runtimeDurationOnInstance(descriptor.runtimeDurationOnInstance()*60*1000);
        descriptor.owner(lobby[0].distributionKey());
        descriptor.label("LDA");
        descriptor.onEdge(true);
        if(ds.create(descriptor)){
            resp.toMap().put("applicationId",descriptor.distributionKey());
            resp.message(descriptor.distributionKey()+" created on ["+descriptor.typeId()+"]");
        }
        else{
            resp.successful(false);
            resp.message("failed to create application on ["+descriptor.typeId()+"]");
        }

        return this.builder.create().toJson(resp);
    }
    public String addView(OnView view){
        ResponseHeader resp = new ResponseHeader("addView");
        DataStore ds = this.tarantulaContext.masterDataStore();
        log.warn("Add view->"+view.owner());
        LobbyQuery query = new LobbyQuery(ds.bucket());
        Descriptor[] lobby={null};
        ds.list(query,(d)->{
            if(view.owner().equals(d.typeId())){
                lobby[0] = d;
                return false;
            }
            return true;
        });
        if(lobby[0]==null){
            resp.successful(false);
            resp.message("["+view.owner()+"] not found");
            return this.builder.create().toJson(resp);
        }
        view.owner(lobby[0].distributionKey());
        if(ds.create(view)){
            resp.message(view.distributionKey());
        }
        else{
            resp.successful(false);
            resp.message("failed to create view ["+view.viewId()+"]");
        }
        return this.builder.create().toJson(resp);
    }
    public String resetModule(String lobbyId,Descriptor descriptor){
        ResponseHeader resp = new ResponseHeader("resetModule",descriptor.subtypeId(),false);
        DataStore dataStore = this.tarantulaContext.masterDataStore();
        dataStore.list(new ApplicationQuery(lobbyId),(a)->{
            if(a.subtypeId().equals(descriptor.subtypeId())){
                a.codebase(descriptor.codebase());
                a.moduleArtifact(descriptor.moduleArtifact());
                a.moduleVersion(descriptor.moduleVersion());
                dataStore.update(a);
                resp.successful(true);
            }
            return true;
        });
        return this.builder.create().toJson(resp);
    }
    @Override
    public void memberAdded(MembershipServiceEvent membershipServiceEvent) {
        if(this.scope==Distributable.DATA_SCOPE){
            Member lm = nodeEngine.getLocalMember();
            int sz = nodeEngine.getClusterService().getSize();
            int pt = 0;
            for(Member m : nodeEngine.getClusterService().getMembers()){
                if(lm.getUuid().equals(m.getUuid())){
                    break;
                }
                pt++;
            }
            log.warn("partition updating on member added->["+pt+"/"+sz+"]"+lm.getUuid());
            for(int i=0;i<this.tarantulaContext.platformRoutingNumber;i++){
                this.tarantulaContext.integrationCluster().onPartition(i,i%sz==pt);
            }
        }
        this.deploymentServiceProvider.clusterUpdated(this.scope,membershipServiceEvent.getMember().getUuid(),true);
    }

    @Override
    public void memberRemoved(MembershipServiceEvent membershipServiceEvent) {
        if(this.scope==Distributable.DATA_SCOPE){
            Member lm = nodeEngine.getLocalMember();
            int sz = nodeEngine.getClusterService().getSize();
            int pt = 0;
            for(Member m : nodeEngine.getClusterService().getMembers()){
                if(lm.getUuid().equals(m.getUuid())){
                    break;
                }
                pt++;
            }
            log.warn("partition updating on member removed->["+pt+"/"+sz+"]"+lm.getUuid());
            for(int i=0;i<this.tarantulaContext.platformRoutingNumber;i++){
                this.tarantulaContext.integrationCluster().onPartition(i,i%sz==pt);
            }
        }
        this.deploymentServiceProvider.clusterUpdated(this.scope,membershipServiceEvent.getMember().getUuid(),false);
    }

    @Override
    public void memberAttributeChanged(MemberAttributeServiceEvent memberAttributeServiceEvent) {
    }


    private static class BatchCache{
        public List<Recoverable> cache;
        public String batchId;
        public BatchCache(String batchId,List<Recoverable> cache){
            this.batchId = batchId;
            this.cache = cache;
        }
    }

}
