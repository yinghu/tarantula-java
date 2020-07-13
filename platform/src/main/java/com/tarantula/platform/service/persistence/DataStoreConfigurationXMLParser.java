package com.tarantula.platform.service.persistence;

import com.tarantula.platform.service.ServiceProvider;
import com.tarantula.platform.service.Serviceable;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.service.DataStoreProvider;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Updated by yinghu on 7/5/2020
 */
public class DataStoreConfigurationXMLParser extends DefaultHandler implements Serviceable {


    public final ConcurrentHashMap<String,ServiceProvider> _loaded;

    String currentLoad;
    HashMap<String,String> properties = new HashMap();
    String currentProperty;
    String value;

    private String dataBucketGroup;
    private String dataBucketNode;

    private String dataStoreProviderConfiguration;
    private String dataDir;
    //private String dataRecoveryDir;

    private int partitionNumber;
    private int accessIndexPartitionNumber;
    private String dataStoreDailyBackup;

    private ShardingProvider shardingProvider;
    private Shard shard;
    private TarantulaContext tarantulaContext;
    public DataStoreConfigurationXMLParser(String dconfig,TarantulaContext tx, ConcurrentHashMap<String,ServiceProvider> _providers){
        this.dataStoreProviderConfiguration = dconfig;
        this.dataBucketGroup = tx.dataBucketGroup;
        this.dataBucketNode = tx.dataBucketNode;
        this.partitionNumber = tx.partitionNumber();
        this.accessIndexPartitionNumber = tx.accessIndexRoutingNumber;
        this.dataDir = tx.dataStoreDir;
        //this.dataRecoveryDir = tx.dataStoreRecoveryDir;
        this.dataStoreDailyBackup = tx.dataStoreDailyBackup?"true":"false";
        this._loaded = _providers;
        this.tarantulaContext = tx;
    }
    private void parse(InputStream xml) throws Exception{
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser p = factory.newSAXParser();
        p.parse(xml,this);
    }

    @Override
    public void endElement(String uri, String lname, String qname) throws SAXException {
        if(qname.equals("data-source")){
            DataStoreProvider ds = (DataStoreProvider)_loaded.get(currentLoad);
            ds.configure(properties);
            //_start(ds);
            properties.clear();
        }
        else if(qname.equals("sharding-provider")){
            DataStoreProvider dsp = (DataStoreProvider) _loaded.get("tarantula");
            _start(this.shardingProvider);
            dsp.addShardingProvider(this.shardingProvider);
            properties.clear();
        }
        else if(qname.equals("shard")){
            this._start(properties,this.shard);
            this.shardingProvider.addShard(this.shard);
            this.properties.clear();
        }
        else if(qname.equals("property")){
            properties.put(currentProperty, value);
        }
        else if(qname.equals("data-store-service")){
            DataStoreProvider ds = (DataStoreProvider)_loaded.get(currentLoad);
            _start(ds);
        }

    }
    @Override
    public void startElement(String uri, String lname, String qname, Attributes attributes) throws SAXException {
        if(qname.equals("data-source")){
            String name = (attributes.getValue("name"));
            String provider = (attributes.getValue("provider"));
            String trimming = (attributes.getValue("truncated"));
            this._loaded.put(name.trim(),this.dataStoreProvider(provider.trim()));
            properties.put("name",name.trim());
            properties.put("truncated",trimming);
            properties.put("bucket",this.dataBucketGroup);
            properties.put("node",this.dataBucketNode);
            properties.put("partitionNumber",this.partitionNumber+"");
            properties.put("dir",this.dataDir);
            properties.put("poolSetting",this.tarantulaContext.dataReplicationThreadPoolSetting);
            properties.put("dailyBackup",dataStoreDailyBackup);
            this.currentLoad = name.trim();
        }
        else if(qname.equals("sharding-provider")){
            this.shardingProvider = this.shardingProvider(attributes.getValue("provider"));
            HashMap<String,String> _cfg = new HashMap<>();
            _cfg.put("name",attributes.getValue("name"));
            _cfg.put("scope",attributes.getValue("scope"));
            _cfg.put("p2",this.partitionNumber+"");
            _cfg.put("p1",this.accessIndexPartitionNumber+"");
            _cfg.put("node",tarantulaContext.dataBucketNode);
            _cfg.put("shards",attributes.getValue("shards"));
            _cfg.put("enabled",attributes.getValue("enabled"));
            this.shardingProvider.configure(_cfg);
        }
        else if(qname.equals("shard")){
            this.shard = new Shard(Integer.parseInt(attributes.getValue("sharding-number")));
        }
        else if(qname.equals("property")){
            currentProperty = attributes.getValue("name").trim();
        }
    }
    @Override
    public void characters(char[] ch, int start, int length)throws SAXException {
        value = new String(ch,start,length);
    }
    DataStoreProvider dataStoreProvider(String provider){
        try {
            return (DataStoreProvider)Class.forName(provider).getConstructor().newInstance();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    ShardingProvider shardingProvider(String provider){
        try {
            return (ShardingProvider)Class.forName(provider).getConstructor().newInstance();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    void _start(DataStoreProvider ds){
        try{
            ds.start();
            ds.setup(this.tarantulaContext);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    void _start(ShardingProvider ds){
        try{
            ds.start();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    void _start(HashMap<String,String> config,Shard shard){
        try{
            shard.configuration(config);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public void start() throws Exception {
        try{
            File f = new File("/etc/tarantula/"+this.dataStoreProviderConfiguration);
            InputStream in = new FileInputStream(f);
            this.parse(in);
        }catch (Exception ex){
            this.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream(this.dataStoreProviderConfiguration));
        }
    }


    public void shutdown() throws Exception {

    }
}
