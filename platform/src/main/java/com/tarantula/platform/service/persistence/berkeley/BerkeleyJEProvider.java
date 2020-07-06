package com.tarantula.platform.service.persistence.berkeley;

import com.sleepycat.je.*;
import com.sleepycat.je.util.DbBackup;
import com.sleepycat.je.util.LogVerificationReadableByteChannel;
import com.tarantula.*;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.event.MapStoreSyncEvent;
import com.tarantula.platform.event.MapStoreVotingEvent;
import com.tarantula.platform.service.ClusterProvider;
import com.tarantula.platform.service.DataStoreProvider;
import com.tarantula.platform.service.ServiceContext;
import com.tarantula.platform.service.cluster.OneTimeRunner;
import com.tarantula.platform.service.persistence.*;
import com.tarantula.platform.util.SystemUtil;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * Updated by yinghu lu on 6/28/2020.
 */
public class BerkeleyJEProvider implements DataStoreProvider,MapStoreListener,EventListener{

    private static JDKLogger log = JDKLogger.getLogger(BerkeleyJEProvider.class);

    private static String ENCODING = "UTF-8";

    private String dataPath;
    private String integrationPath;

    private String backupPath;
    private String integrationBackupPath;
    private String database;
    private boolean trimming;
    private int partitionNumber;

    private boolean dailyBackup;

    private Node node;

    private EventService dataScopePublisher;
    private EventService integrationScopePublisher;

    private ClusterProvider dataCluster;
    private ClusterProvider integrationCluster;
    private ServiceContext serviceContext;

    private ConcurrentHashMap<String,ReplicatedDataStore> dMap = new ConcurrentHashMap<>();

    private Environment environment;
    private Environment integrationEnvironment;

    private String replicationTopic;
    private String backupTopic;

    private ShardingProvider iShardingProvider;
    private ShardingProvider dShardingProvider;

    @Override
    public void configure(Map<String, String> properties) {
        this.database = properties.get("name");
        this.trimming = Boolean.parseBoolean(properties.get("truncated"));
        //dataPath, integrationPath, activePath, backupPath
        this.dataPath = properties.get("dir")+ FileSystems.getDefault().getSeparator()+properties.get("dataPath");
        this.integrationPath =properties.get("dir")+ FileSystems.getDefault().getSeparator()+properties.get("integrationPath");
        this.backupPath = properties.get("dir")+ FileSystems.getDefault().getSeparator()+properties.get("backupPath")+FileSystems.getDefault().getSeparator()+properties.get("dataPath");
        this.integrationBackupPath = properties.get("dir")+ FileSystems.getDefault().getSeparator()+properties.get("backupPath")+FileSystems.getDefault().getSeparator()+properties.get("integrationPath");
        this.dailyBackup = properties.get("dailyBackup")!=null?Boolean.parseBoolean(properties.get("dailyBackup")):false;
        this.partitionNumber = Integer.parseInt(properties.get("partitionNumber"));
        this.node = new Node(properties.get("bucket"),properties.get("node"));
        this.replicationTopic = "tarantula-replication-topic-"+this.database;
        this.backupTopic = "tarantula-backup-topic-"+this.database;
    }
    public void addShardingProvider(ShardingProvider shardingProvider){
        if(shardingProvider.scope()==Distributable.INTEGRATION_SCOPE){
            iShardingProvider = shardingProvider;
        }
        else if(shardingProvider.scope()==Distributable.DATA_SCOPE){
            dShardingProvider = shardingProvider;
        }
    }
    @Override
    public DataStore create(String name) {
        return this.dMap.computeIfAbsent(name,(k)->{
            Database db = this.createDatabase(name,Distributable.INTEGRATION_SCOPE);
            this.iShardingProvider.registerDataStore(name);
            return  new BerkeleyDataStore(this.node,db,this);
        });
    }
    private Database createDatabase(String name,int scope){
        try{
            //log.warn("Create database ["+name+"] on scope ["+scope+"]");
            DatabaseConfig dbConfig = new DatabaseConfig();
            dbConfig.setAllowCreate(true);
            dbConfig.setDeferredWrite(true);
            if(scope==Distributable.DATA_SCOPE){
                return this.environment.openDatabase(null,name,dbConfig);
            }
            else if(scope==Distributable.INTEGRATION_SCOPE){
                return this.integrationEnvironment.openDatabase(null,name,dbConfig);
            }
            else{
                throw new UnsupportedOperationException("scope ["+scope+"] not supported");
            }
        }catch (Exception ex){
            throw new RuntimeException(name,ex);
        }
    }
    @Override
    public DataStore create(String name,int partition){
        return this.dMap.computeIfAbsent(name,(k)->{
            Database[] shards = new Database[partition];
            for(int i=0;i<partition;i++){
                shards[i]=createDatabase(name+"_"+i,Distributable.DATA_SCOPE);
            }
            this.dShardingProvider.registerDataStore(name,partition);
            return new PartitionDataStore(partition,this.node.bucketName,this.node.nodeName,name,shards,this);
        });
    }

    public List<String> list(){
        return this.environment.getDatabaseNames();
    }
    @Override
    public String name() {
        return this.database;//provider name and default data store name
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.dataCluster = serviceContext.clusterProvider(Distributable.DATA_SCOPE);
        this.dataScopePublisher = serviceContext.eventService(Distributable.DATA_SCOPE);
        this.integrationCluster = serviceContext.clusterProvider(Distributable.INTEGRATION_SCOPE);
        this.integrationScopePublisher = serviceContext.eventService(Distributable.INTEGRATION_SCOPE);
    }
    @Override
    public void waitForData() {
        this.dataCluster.subscribe(this.replicationTopic,this);
        this.integrationCluster.subscribe(this.backupTopic,this);
    }
    @Override
    public void start() throws Exception {
        Path _path = Paths.get(this.dataPath);
        if(!Files.exists(_path)){
            Files.createDirectories(_path);
        }
        Path _ipath = Paths.get(this.integrationPath);
        if(!Files.exists(_ipath)){
            Files.createDirectories(_ipath);
        }
        Path _pback = Paths.get(this.backupPath);
        if(!Files.exists(_pback)){
            Files.createDirectories(_pback);
        }
        Path _piback = Paths.get(this.integrationBackupPath);
        if(!Files.exists(_piback)){
            Files.createDirectories(_piback);
        }
        File f = new File(dataPath+FileSystems.getDefault().getSeparator()+"last.dat");
        if(!f.exists()){
            f.createNewFile();
            DataOutputStream fo = new DataOutputStream(new FileOutputStream(f));
            fo.writeLong(0);
            fo.writeLong(0);
            fo.writeUTF(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            fo.close();
        }
        File fi = new File(integrationPath+FileSystems.getDefault().getSeparator()+"last.dat");
        if(!fi.exists()){
            fi.createNewFile();
            DataOutputStream fo = new DataOutputStream(new FileOutputStream(fi));
            fo.writeLong(0);
            fo.writeLong(0);
            fo.writeUTF(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            fo.close();
        }
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true);
        envConfig.setSharedCache(true);
        this.environment = new Environment(new File(dataPath),envConfig);
        if(trimming){
            log.warn("Database ["+this.database+"] configured as trimming mode");
            for(int i=0;i<this.partitionNumber;i++){
                long ret = this.environment.truncateDatabase(null,this.database+"-"+i,true);
                log.warn("Records ["+ret+"] truncated from ["+this.database+"-"+i+"]");
            }
        }
        this.integrationEnvironment = new Environment(new File(integrationPath),envConfig);
        //log.info("Waiting for loading data on first member from data scope store");
        HashSet<String> ln = new HashSet<>();
        for(String dn : this.environment.getDatabaseNames()){
            ln.add(dn.split("_")[0]);
        }
        ln.forEach((n)->{
            DataStore ds = this.create(n,this.partitionNumber);
            ds.count();
        });
        //log.info("Waiting for loading data on first member from integration scope store");
        for(String dn : this.integrationEnvironment.getDatabaseNames()){
            DataStore ds = this.create(dn);
            ds.count();
        }
        this.create(this.database,this.partitionNumber);
        log.info("Tarantula data store started on bucket ["+this.node.bucket()+"]");
    }

    @Override
    public void shutdown() throws Exception {
        iShardingProvider.shutdown();
        dShardingProvider.shutdown();
        dMap.forEach((k,v)->{
            v.close();
        });
        this.environment.close();
        this.integrationEnvironment.close();
        log.info("Berkeley JE data store shut down on ["+node.toString()+"]");
    }
    private void truncateOnBackup(Database tds){
        DiskOrderedCursor cursor = tds.openCursor(null);
        DatabaseEntry pk = new DatabaseEntry();
        DatabaseEntry pv = new DatabaseEntry();
        int dc = 0;
        do{
            if(cursor.getNext(pk,pv,null)==OperationStatus.SUCCESS){
                if(tds.delete(null,pk)==OperationStatus.SUCCESS){
                    dc++;
                }
            }
            else{
                break;
            }
        }while (true);
        cursor.close();
        log.info("Total records deleted ["+dc+"] from action entries on ["+tds.getDatabaseName()+"]");
    }
    private void migrateFromDataScope(Event mve){
    }
    private void migrateFromIntegrationScope(Event mve){
    }
    @Override
    public byte[] onUpdating(Metadata metadata,String key,Map<String,Object> pending){
        String ds = metadata.source();
        int pt = metadata.partition();
        return SystemUtil.toJson(pending);
    }
    @Override
    public void onUpdated(Metadata metadata, byte[] key, byte[] value) {
        //log.warn("DATA STORE->"+metadata.source());
        if(metadata.scope()==Recoverable.DATA_SCOPE){
            //use data store prefix as the active database
            //this.dataScopePublisher.publish(new MapStoreSyncEvent(this.replicationTopic,this.node.nodeName,key,value,(RecoverableMetadata) metadata));
            if(metadata.distributable()){
                this.dataCluster.set(metadata,key,value);
            }
        }
        else if(metadata.scope()==Recoverable.INTEGRATION_SCOPE){
            //this.integrationScopePublisher.publish(new MapStoreSyncEvent(this.backupTopic,this.node.nodeName,key,value,(RecoverableMetadata)metadata));
            if(metadata.distributable()){
                this.integrationCluster.set(metadata,key,value);
            }
        }
    }
    public void onLoaded(Metadata metadata,byte[] key,byte[] value){
        if(metadata.scope()==Recoverable.DATA_SCOPE){
            if(metadata.distributable()){
                this.dataCluster.set(metadata,key,value);
            }
        }
        else if(metadata.scope()==Recoverable.INTEGRATION_SCOPE){
            if(metadata.distributable()){
                this.integrationCluster.set(metadata,key,value);
            }
        }
    }
    public void backup(int scope){
        if(scope==Distributable.DATA_SCOPE){
            this.environment.sync();
            this._backup(this.environment,dataPath,backupPath);
        }
        else if(scope==Distributable.INTEGRATION_SCOPE){
            this.integrationEnvironment.sync();
            this._backup(integrationEnvironment,integrationPath,integrationBackupPath);
        }
    }
    private void _backup(Environment bEnvironment,String dPath,String bPath){
        DbBackup backup =null;
        try{
            DataInputStream fi = new DataInputStream(new FileInputStream(dPath+FileSystems.getDefault().getSeparator()+"last.dat"));
            long last = fi.readLong();
            long lastDir = fi.readLong();
            String lastVersion = fi.readUTF();
            fi.close();
            Path tp = Paths.get(bPath+FileSystems.getDefault().getSeparator()+lastDir);
            if(!Files.exists(tp)){
                Files.createDirectories(tp);
            }
            LocalDateTime st = LocalDateTime.now();
            log.info("JE Berkeley Incremental last index file/dir ["+last+"/"+lastDir+"] at ["+lastVersion+"] from ["+dPath+"] to ["+bPath+"]");
            backup = last>0?new DbBackup(bEnvironment,last):new DbBackup(bEnvironment);
            bEnvironment.checkpoint(new CheckpointConfig().setForce(true));
            backup.startBackup();
            String[] bset = backup.getLogFilesInBackupSet();
            for(String b : bset){
                FileChannel fc = new FileOutputStream(tp.toFile().getPath()+FileSystems.getDefault().getSeparator()+b).getChannel();
                FileInputStream fis = new FileInputStream(Paths.get(dPath+FileSystems.getDefault().getSeparator()+b).toFile());
                LogVerificationReadableByteChannel vc = new LogVerificationReadableByteChannel(bEnvironment,fis.getChannel(),b);
                ByteBuffer buffer = ByteBuffer.allocate(4096);
                while (true) {
                    final int len = vc.read(buffer);
                    if (len < 0) {
                        break;
                    }
                    buffer.flip();
                    fc.write(buffer);
                    buffer.clear();
                }
                fc.close();
                vc.close();
                backup.removeFileProtection(b);
            }
            LocalDateTime ed = LocalDateTime.now();
            log.info("JE Berkeley finished incremental backup files/duration ["+bset.length+"/"+SystemUtil.durationUTCMilliseconds(st,ed)+"] at ["+ed.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)+"]");
            DataOutputStream fo = new DataOutputStream(new FileOutputStream(dPath+FileSystems.getDefault().getSeparator()+"last.dat"));
            fo.writeLong(backup.getLastFileInBackupSet());
            fo.writeLong(lastDir+1);
            fo.writeUTF(ed.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            fo.close();

        }catch (Exception ex){
            log.error("incremental backup clashed",ex);
        }
        finally {
            backup.endBackup();
        }
    }
    public void backup(int scope,OnBackup onBackup){
        if(scope==Distributable.DATA_SCOPE){
            this.environment.sync();
            this._backup(onBackup,dataPath,this.environment);
        }
        else if(scope==Distributable.INTEGRATION_SCOPE){
            this.integrationEnvironment.sync();
            this._backup(onBackup,integrationPath,this.integrationEnvironment);
        }
    }
    private void _backup(OnBackup onBackup,String dPath,Environment bEnvironment){
        DbBackup backup =null;
        try{
            LocalDateTime st = LocalDateTime.now();
            log.info("JE Berkeley full backup starting at ["+st.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)+"] from ["+dPath+"]");
            backup = new DbBackup(bEnvironment);
            bEnvironment.checkpoint(new CheckpointConfig().setForce(true));
            backup.startBackup();
            String[] bset = backup.getLogFilesInBackupSet();
            for(String b : bset){
                FileInputStream fis = new FileInputStream(Paths.get(dPath+FileSystems.getDefault().getSeparator()+b).toFile());
                LogVerificationReadableByteChannel vc = new LogVerificationReadableByteChannel(bEnvironment,fis.getChannel(),b);
                onBackup.on(b,bset.length,vc);
                vc.close();
                backup.removeFileProtection(b);
            }
            LocalDateTime ed = LocalDateTime.now();
            log.info("JE Berkeley full backup files/duration ["+bset.length+"/"+SystemUtil.durationUTCMilliseconds(st,ed)+"] finished at ["+ed.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)+"]");
        }catch (Exception ex){
            log.error("full backup clashed",ex);
            throw new RuntimeException("backup crashed",ex);
        }
        finally {
            backup.endBackup();
        }
    }
    public void recover(int scope,OnBackup backup){
        if(scope==Distributable.DATA_SCOPE){
            _recover(backup,dataPath,backupPath,this.environment);
        }
        else if(scope==Distributable.INTEGRATION_SCOPE){
            _recover(backup,integrationPath,integrationBackupPath,this.integrationEnvironment);
        }
    }
    private void _recover(OnBackup backup,String dPath,String bPath,Environment bEnvironment){
        try{
            DataInputStream fi = new DataInputStream(new FileInputStream(dPath+FileSystems.getDefault().getSeparator()+"last.dat"));
            long last = fi.readLong(); //last file
            long lastDir = fi.readLong()-1; //last dir
            String lastVersion = fi.readUTF();//last updated
            fi.close();
            LocalDateTime st = LocalDateTime.now();
            log.info("JE Berkeley is recovering from index["+last+"/"+lastDir+"] from ["+lastVersion+"] from ["+bPath+"]");
            ArrayList<File> rlist = new ArrayList<>();
            do {
                Path tp = Paths.get(bPath + FileSystems.getDefault().getSeparator() + lastDir);
                for(String fn : tp.toFile().list()){
                    rlist.add(Paths.get(tp.toFile().getPath()+FileSystems.getDefault().getSeparator()+fn).toFile());
                }
                lastDir--;
            }while (lastDir>=0);
            for(File r : rlist){
                FileInputStream fis = new FileInputStream(r);
                LogVerificationReadableByteChannel vc = new LogVerificationReadableByteChannel(bEnvironment,fis.getChannel(),r.getName());
                backup.on(r.getName(),rlist.size(),vc);
                vc.close();
            }
            LocalDateTime ed = LocalDateTime.now();
            log.info("JE Berkeley finished total recovered files /duration ["+rlist.size()+"/"+SystemUtil.durationUTCMilliseconds(st,ed)+"] at ["+ed.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)+"]");

        }catch (Exception ex){
            log.error("recover clashed",ex);
            throw new RuntimeException("recover crashed",ex);
        }
    }
    @Override
    public void atMidnight() {
        if(dailyBackup){
            backup(Distributable.DATA_SCOPE);//daily incremental backup
            backup(Distributable.INTEGRATION_SCOPE);
        }
    }
    @Override
    public boolean onEvent(Event event) {
        if(event instanceof MapStoreSyncEvent){
            MapStoreSyncEvent mse = (MapStoreSyncEvent)event;
            Metadata mt = mse.metadata;
            DataStore rds = mt.scope()==Distributable.DATA_SCOPE?this.create(mt.source(),this.serviceContext.partitionNumber()):this.create(mt.source());
            ((ReplicatedDataStore)rds).onReplication(mse);
        }
        else if(event instanceof MapStoreVotingEvent){
            //if(!event.trackId().equals(this.registerId)){
            if(event.stub()==Distributable.DATA_SCOPE){
                this.serviceContext.schedule(new OneTimeRunner(100,()->{
                    migrateFromDataScope(event);
                }));
            }
            else if(event.stub()==Distributable.INTEGRATION_SCOPE){
                this.serviceContext.schedule(new OneTimeRunner(100,()->{
                    migrateFromIntegrationScope(event);
                }));
            }
            //}
        }
        return false;
    }

    public static class BerkeleyDataStore extends ReplicatedDataStore {

        private ConcurrentHashMap<Integer,RecoverableListener> rMap = new ConcurrentHashMap<>();

        private final Database berkeleyStore;
        private final Node node;
        private final MapStoreListener mapStoreListener;
        private final String dataStore;

        private final Semaphore pass = new Semaphore(DataStoreProvider.CONCURRENCY_ACCESS_LIMIT);

        public BerkeleyDataStore(Node node,Database database,MapStoreListener mapStoreListener){
            this.node = node;
            this.berkeleyStore = database;
            this.dataStore = this.berkeleyStore.getDatabaseName();
            this.mapStoreListener = mapStoreListener;
        }
        @Override
        public String bucket() {
            return this.node.bucketName;
        }

        @Override
        public String node() {
            return node.nodeName;
        }

        @Override
        public String name(){
            return this.dataStore;
        }
        public long count(){
            return this.berkeleyStore.count();
        }
        @Override
        public <T extends Recoverable> boolean create(T t) {
            try {
                pass.acquire();
                String okey = t.key().asString();
                if (okey == null) {
                    //use bucket/oid as the key
                    t.bucket(this.node.bucketName);
                    t.oid(SystemUtil.oid());
                    okey = t.key().asString();
                }
                byte[] key = okey.getBytes(ENCODING);
                byte[] value = SystemUtil.toJson(t.toMap());
                boolean suc = set(t,key,value);
                if(suc&&t.onEdge()){//update edge
                    onEdge(t,key);
                }
                return suc;
            }catch (Exception ex){
                log.error("error on create",ex);
                return false;
            }
            finally {
                pass.release();
            }
        }

        @Override
        public <T extends Recoverable> int create(T[] tb) {
            int suc =0;
            for(T t : tb){
                if(this.create(t)){
                    suc++;
                }
            }
            return suc;
        }

        @Override
        public <T extends Recoverable> boolean update(T t) {
            try{
                pass.acquire();
                String akey = t.key().asString();
                if(akey==null){
                    return false;
                }
                byte[] key = akey.getBytes(ENCODING);
                return set(t,key,SystemUtil.toJson(t.toMap()));
            }catch (Exception ex){
                log.error("error on update",ex);
                return false;
            }
            finally {
                pass.release();
            }
        }

        @Override
        public <T extends Recoverable> boolean createIfAbsent(T t, boolean loading) {
            try{
                pass.acquire();
                String akey = t.key().asString();
                if(akey==null){
                    t.bucket(this.node.bucketName);
                    t.oid(SystemUtil.oid());
                    akey = t.key().asString();
                }
                byte[] key = akey.getBytes(ENCODING);
                byte[] v;
                if((v=_get(key))==null){
                    boolean suc = set(t,key,SystemUtil.toJson(t.toMap()));
                    if(suc&&t.onEdge()){
                        onEdge(t,key);
                    }
                    return suc;
                }
                else{
                    if(loading){
                        t.fromMap(SystemUtil.toMap(v));
                    }
                    return false;
                }
            }catch (Exception ex){
                log.error("error on createIfAbsent",ex);
                return false;
            }
            finally {
                pass.release();
            }
        }

        @Override
        public <T extends Recoverable> boolean load(T t) {
            try{
                String akey = t.key().asString();
                if(akey==null){
                    return false;
                }
                byte[] key = akey.getBytes(ENCODING);
                byte[] value;
                if((value=get(t,key))==null){
                    return false;
                }
                t.fromMap(SystemUtil.toMap(value));
                return true;
            }catch (Exception ex){
                log.error("error on load",ex);
                return false;
            }
        }
        public void batch(byte[] key,Overflow overflow){

        }
        public void traverse(Overflow overflow) {
            DiskOrderedCursor cursor = this.berkeleyStore.openCursor(null);
            DatabaseEntry pk = new DatabaseEntry();
            DatabaseEntry pv = new DatabaseEntry();
            do{
                if(cursor.getNext(pk,pv,null)==OperationStatus.SUCCESS){
                    if(!overflow.on(this.dataStore,0,pk.getData(),pv.getData())){
                        break;
                    }
                }
                else{
                    break;
                }
            }while (true);
            cursor.close();
        }
        public void put(byte[] key,byte[] value){
            this._put(key,value);
        }
        public void set(byte[] key,byte[] value){
            try {
                pass.acquire();
                if(_put(key,value)){
                    this.mapStoreListener.onUpdated(new RecoverableMetadata(this.dataStore,0,0,this.scope(),true,false,null),key,value);
                }
            }catch (Exception ex){
                log.error("error on set",ex);
            }
            finally {
                pass.release();
            }
        }
        public byte[] get(byte[] key){
            return _get(key);
        }
        @Override
        public <T extends Recoverable> List<T> list(RecoverableFactory<T> query) {
            List<T> alist = new ArrayList<>();
            this.list(query,(t)->{
                alist.add(t);
                return true;
            });
            return alist;
        }

        @Override
        public <T extends Recoverable> void list(RecoverableFactory<T> query, Stream<T> stream) {
            try {
                String akey = (query.distributionKey() + Recoverable.PATH_SEPARATOR + query.label());
                byte[] owner = akey.getBytes(ENCODING);
                byte[] edgeList = _get(owner);
                if(edgeList==null){
                    return;
                }
                IndexSet indexSet = new IndexSet();
                indexSet.fromMap(SystemUtil.toMap(edgeList));

                for(String b: indexSet.keySet){
                    T t = query.create();
                    byte[] v;
                    byte[] ka = b.getBytes();
                    if((v=get(t,ka))!=null){
                        t.fromMap(SystemUtil.toMap(v));
                        t.distributionKey(new String(ka,ENCODING));
                        if(!stream.on(t)){

                            break;
                        }
                    }
                }

            }catch (Exception ex){
                ex.printStackTrace();
            }
        }


        @Override
        public RecoverableListener registerRecoverableListener(RecoverableListener recoverableListener) {
            return rMap.computeIfAbsent(recoverableListener.registryId(),(rid)->recoverableListener);
        }

        @Override
        public void unregisterRecoverableListener(int factoryId) {
            rMap.remove(factoryId);
        }


        @Override
        public void onReplication(MapStoreSyncEvent me) {
            //receive replication event
            Metadata metadata = me.metadata;
            if(!me.source().equals(this.node.nodeName)){
                this._put(me.key,me.payload());
            }
            if(!metadata.onEdge()){//callback on local application register
                RecoverableListener rl = rMap.get(metadata.factoryId());
                if(rl!=null){
                    rl.onUpdated(metadata,me.key,me.payload());
                }
            }
        }
        public void close(){
            this.berkeleyStore.close();
        }
        public int scope(){
            return Distributable.INTEGRATION_SCOPE;
        }
        private <T extends Recoverable> void onEdge(T t,byte[] key) throws Exception{
            byte[] owner = (t.owner()+Recoverable.PATH_SEPARATOR+t.label()).getBytes(ENCODING);
            IndexSet indexSet = new IndexSet();
            indexSet.distributionKey(t.owner());
            indexSet.label(t.label());
            byte[] v;
            if((v=_get(owner))!=null){
                indexSet.fromMap(SystemUtil.toMap(v));
            }
            indexSet.keySet.add(new String(key));
            v = SystemUtil.toJson(indexSet.toMap());
            if(_put(owner,v)){
                this.mapStoreListener.onUpdated(new RecoverableMetadata(this.dataStore, t.getFactoryId(), t.getClassId(), t.scope(), true, false, null), owner, v);
            }
        }
        private <T extends Recoverable> boolean set(T t,byte[] key,byte[] value){
            if(this._put(key,value)){
                this.mapStoreListener.onUpdated(new RecoverableMetadata(this.dataStore,t.getFactoryId(),t.getClassId(),t.scope(),false,t.distributable(),t.index()),key,value);
                return true;
            }else{
                return false;
            }
        }
        private <T extends Recoverable>  byte[] get(T t,byte[] key){
            byte[] v = _get(key);
            if(v!=null){
                this.mapStoreListener.onLoaded(new RecoverableMetadata(this.dataStore,t.getFactoryId(),t.getClassId(),t.scope(),false,t.distributable(),t.index()),key,v);
            }
            return v;
        }
        private boolean _put(byte[] key,byte[] value){
            return berkeleyStore.put(null,new DatabaseEntry(key),new DatabaseEntry(value))==OperationStatus.SUCCESS;
        }
        private byte[] _get(byte[] key){
            DatabaseEntry ve = new DatabaseEntry();
            OperationStatus status = berkeleyStore.get(null,new DatabaseEntry(key),ve,null);
            if(status==OperationStatus.SUCCESS){
                return ve.getData();
            }
            else{
                return null;
            }
        }
    }
}
