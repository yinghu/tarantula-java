package com.tarantula.test;


import com.icodesoftware.DataStore;



import com.icodesoftware.util.SnowflakeKey;
import com.tarantula.platform.DeploymentDescriptor;
import com.tarantula.platform.LobbyDescriptor;
import com.tarantula.platform.service.deployment.ApplicationQuery;
import com.tarantula.platform.service.deployment.LobbyQuery;
import com.tarantula.platform.service.deployment.XMLParser;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;


public class DeploymentDescriptorTest extends DataStoreHook{


    @Test(groups = { "DeploymentDescriptor" })
    public void descriptorHook(){
        Exception exception = null;
        try {
            DataStore ds = dataStoreProvider.createDataStore("test_tarantula");
            long bucketId = serviceContext.deploymentServiceProvider().distributionId();
            List<LobbyDescriptor> lobbies = ds.list(new LobbyQuery(bucketId));
            Assert.assertEquals(lobbies.size(),0);
            XMLParser xmlParser = new XMLParser();
            xmlParser.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-presence.xml"));
            xmlParser.configurations.forEach(lb->{
                LobbyDescriptor lobby = lb.descriptor;
                lobby.ownerKey(new SnowflakeKey(bucketId));
                Assert.assertTrue(ds.create(lobby));
                lb.applications.forEach(a->{
                    a.ownerKey(new SnowflakeKey(lobby.distributionId()));
                    Assert.assertTrue(ds.create(a));
                });
            });
            lobbies = ds.list(new LobbyQuery(bucketId));
            Assert.assertEquals(lobbies.size(),1);
            List<DeploymentDescriptor> apps = ds.list(new ApplicationQuery(lobbies.get(0).distributionId()));
            Assert.assertEquals(apps.size(),2);
        }catch (Exception ex){
            ex.printStackTrace();
            exception = ex;
        }
        Assert.assertNull(exception);
    }

}
