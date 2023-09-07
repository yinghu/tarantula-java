package com.tarantula.test;


import com.icodesoftware.DataStore;


import com.icodesoftware.service.DataStoreProvider;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.OidKey;

import com.icodesoftware.util.SnowflakeKey;
import com.tarantula.platform.LobbyDescriptor;
import com.tarantula.platform.service.deployment.ApplicationQuery;
import com.tarantula.platform.service.deployment.LobbyQuery;
import com.tarantula.platform.service.deployment.XMLParser;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;


public class ApplicationLoadTest {


    DataStoreProvider dataStoreProvider;
    ServiceContext serviceContext;
    @BeforeClass
    public void setUp() {
        DataStoreTestEvn.setUp();
        dataStoreProvider = DataStoreTestEvn.dataStoreProvider;
        serviceContext = DataStoreTestEvn.serviceContext;
    }


    @AfterTest
    public void tearDown() throws Exception{
       dataStoreProvider.shutdown();
    }


    @Test(groups = { "ApplicationLoad" })
    public void descriptorHook(){
        Exception exception = null;
        try {
            DataStore ds = dataStoreProvider.createDataStore("tarantula");
            XMLParser xmlParser = new XMLParser();
            xmlParser.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-presence.xml"));
            long ownerId = serviceContext.deploymentServiceProvider().distributionId();
            xmlParser.configurations.forEach(lb->{
                LobbyDescriptor lobby = lb.descriptor;
                lobby.ownerKey(new SnowflakeKey(ownerId));
                Assert.assertTrue(ds.create(lobby));
                LobbyDescriptor load = new LobbyDescriptor();
                load.distributionId(lobby.distributionId());
                Assert.assertTrue(ds.load(load));
                Assert.assertEquals(load.typeId(),lobby.typeId());
                Assert.assertEquals(load.type(),lobby.type());
                Assert.assertEquals(load.name(),lobby.name());
                Assert.assertEquals(load.category(),lobby.category());
                List<LobbyDescriptor> lbs = ds.list(new LobbyQuery(ownerId));
                LobbyDescriptor lbx = lbs.get(0);
                Assert.assertEquals(lbx.distributionId(),lobby.distributionId());
                lb.applications.forEach(a->{
                    a.ownerKey(new SnowflakeKey(lobby.distributionId()));
                    Assert.assertTrue(ds.create(a));
                });
                ds.list(new ApplicationQuery(lobby.distributionId())).forEach(a->{
                    Assert.assertTrue(a.distributionId()!=0);
                });
            });
        }catch (Exception ex){
            ex.printStackTrace();
            exception = ex;
        }
        Assert.assertNull(exception);
    }

}
