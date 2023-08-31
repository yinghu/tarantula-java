package com.tarantula.test;

import com.icodesoftware.AccessIndex;
import com.icodesoftware.DataStore;
import com.icodesoftware.service.AccessIndexService;

import com.icodesoftware.service.DataStoreProvider;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.OidKey;
import com.tarantula.platform.AccessIndexTrack;
import com.tarantula.platform.LobbyDescriptor;
import com.tarantula.platform.service.deployment.ApplicationQuery;
import com.tarantula.platform.service.deployment.LobbyQuery;
import com.tarantula.platform.service.deployment.XMLParser;
import com.tarantula.platform.util.SystemUtil;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;


public class LMDBDataStoreMigrationTest {


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

    @Test(groups = { "LMDBMigration" })
    public void accessIndexHook() {
        DataStore ds = dataStoreProvider.createAccessIndexDataStore(AccessIndexService.NAME+"1");
        AccessIndex accessIndex = new AccessIndexTrack("test","BDS", SystemUtil.oid(),1);
        //accessIndex.id(dataStoreProvider.nextId(ds.name()));

        Assert.assertTrue(ds.createIfAbsent(accessIndex,false));

        AccessIndex not_created = new AccessIndexTrack("test");
        Assert.assertFalse(ds.createIfAbsent(not_created,true));
        Assert.assertEquals(not_created.referenceId(),accessIndex.referenceId());
        //Assert.assertEquals(not_created.id(),accessIndex.id());
        Assert.assertEquals(not_created.owner(),accessIndex.owner());
        Assert.assertEquals(not_created.bucket(),accessIndex.bucket());
        Assert.assertEquals(not_created.oid(),accessIndex.oid());

        AccessIndex load = new AccessIndexTrack("test");
        Assert.assertTrue(ds.load(load));

        Assert.assertEquals(load.referenceId(),accessIndex.referenceId());
        //Assert.assertEquals(load.id(),accessIndex.id());
        Assert.assertEquals(load.owner(),accessIndex.owner());
        Assert.assertEquals(load.bucket(),accessIndex.bucket());
        Assert.assertEquals(load.oid(),accessIndex.oid());

        Assert.assertTrue(ds.delete(accessIndex));
        Assert.assertFalse(ds.load(accessIndex));

    }
    @Test(groups = { "LMDBMigration" })
    public void descriptorHook(){
        Exception exception = null;
        try {
            DataStore ds = dataStoreProvider.createDataStore("tarantula");
            XMLParser xmlParser = new XMLParser();
            xmlParser.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-presence.xml"));
            String ownerId = "T100";
            xmlParser.configurations.forEach(lb->{
                LobbyDescriptor lobby = lb.descriptor;
                lobby.ownerKey(new OidKey(ownerId));
                Assert.assertTrue(ds.create(lobby));
                LobbyDescriptor load = new LobbyDescriptor();
                load.oid(lobby.oid());
                Assert.assertTrue(ds.load(load));
                Assert.assertEquals(load.typeId(),lobby.typeId());
                Assert.assertEquals(load.type(),lobby.type());
                Assert.assertEquals(load.name(),lobby.name());
                Assert.assertEquals(load.category(),lobby.category());
                List<LobbyDescriptor> lbs = ds.list(new LobbyQuery(ownerId));
                LobbyDescriptor lbx = lbs.get(0);
                Assert.assertEquals(lbx.oid(),lobby.oid());
                lb.applications.forEach(a->{
                    a.ownerKey(new OidKey(lobby.oid()));
                    Assert.assertTrue(ds.create(a));
                });
                ds.list(new ApplicationQuery(lobby.oid())).forEach(a->{
                    Assert.assertTrue(a.oid()!=null);
                });
            });
        }catch (Exception ex){
            ex.printStackTrace();
            exception = ex;
        }
        Assert.assertNull(exception);
    }

}
