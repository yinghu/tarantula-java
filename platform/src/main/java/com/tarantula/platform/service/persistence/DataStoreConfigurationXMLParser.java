package com.tarantula.platform.service.persistence;

import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.Serviceable;
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

public class DataStoreConfigurationXMLParser extends DefaultHandler implements Serviceable {


    String currentLoad;
    HashMap<String,String> properties = new HashMap();
    String currentProperty;
    String value;

    private String dataBucketGroup;
    private String dataBucketNode;

    private String dataStoreProviderConfiguration;
    private String dataDir;

    private int partitionNumber;
    private int accessIndexPartitionNumber;
    private String dataStoreDailyBackup;


    private TarantulaContext tarantulaContext;
    public DataStoreConfigurationXMLParser(String dconfig,TarantulaContext tx){
        this.dataStoreProviderConfiguration = dconfig;
        this.dataBucketGroup = tx.dataBucketGroup;
        this.dataBucketNode = tx.dataBucketNode;
        this.partitionNumber = tx.partitionNumber();
        this.accessIndexPartitionNumber = tx.accessIndexRoutingNumber;
        this.dataDir = tx.dataStoreDir;
        this.dataStoreDailyBackup = tx.dataStoreDailyBackup?"true":"false";
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
            this.tarantulaContext.deploymentDataStoreProvider.configure(properties);
            properties.clear();
        }
        else if(qname.equals("property")){
            properties.put(currentProperty, value);
        }
        else if(qname.equals("data-store-service")){
            _start(this.tarantulaContext.deploymentDataStoreProvider);
        }

    }
    @Override
    public void startElement(String uri, String lname, String qname, Attributes attributes) throws SAXException {
        if(qname.equals("data-source")){
            String name = (attributes.getValue("name"));
            if(!name.equals(DeploymentServiceProvider.DEPLOY_DATA_STORE)) throw new RuntimeException("master data store name must be->"+DeploymentServiceProvider.DEPLOY_DATA_STORE);
            String provider = attributes.getValue("provider");
            String trimming = attributes.getValue("truncated");
            String backupEnabled = attributes.getValue("backup-enabled");
            this.tarantulaContext.deploymentDataStoreProvider = this.dataStoreProvider(provider.trim());
            properties.put("name",name.trim());
            properties.put("truncated",trimming);
            properties.put("backupEnabled",backupEnabled.trim());
            properties.put("bucket",this.dataBucketGroup);
            properties.put("node",this.dataBucketNode);
            properties.put("partitionNumber",this.partitionNumber+"");
            properties.put("dir",this.dataDir);
            properties.put("poolSetting",this.tarantulaContext.dataReplicationThreadPoolSetting);
            properties.put("dailyBackup",dataStoreDailyBackup);
            this.currentLoad = name.trim();
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

    void _start(DataStoreProvider ds){
        try{
            ds.start();
            ds.setup(this.tarantulaContext);
            if(ds.name().equals(DeploymentServiceProvider.NAME)) this.tarantulaContext.deploymentDataStoreProvider = ds;
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
