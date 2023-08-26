package com.tarantula.test;

import com.icodesoftware.AccessIndex;
import com.icodesoftware.DataStore;
import com.icodesoftware.lmdb.LMDBDataStoreProvider;
import com.icodesoftware.service.AccessIndexService;

import com.icodesoftware.service.DataStoreProvider;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.LongTypeKey;
import com.tarantula.platform.AccessIndexTrack;
import com.tarantula.platform.LobbyDescriptor;
import com.tarantula.platform.service.deployment.ApplicationQuery;
import com.tarantula.platform.service.deployment.LobbyQuery;
import com.tarantula.platform.service.deployment.XMLParser;
import com.tarantula.platform.statistics.StatisticsEntry;
import com.tarantula.platform.statistics.StatisticsEntryQuery;
import com.tarantula.platform.util.SystemUtil;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
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
        accessIndex.id(dataStoreProvider.nextId(ds.name()));

        Assert.assertTrue(ds.createIfAbsent(accessIndex,false));

        AccessIndex not_created = new AccessIndexTrack("test");
        Assert.assertFalse(ds.createIfAbsent(not_created,true));
        Assert.assertEquals(not_created.referenceId(),accessIndex.referenceId());
        Assert.assertEquals(not_created.id(),accessIndex.id());
        Assert.assertEquals(not_created.owner(),accessIndex.owner());
        Assert.assertEquals(not_created.bucket(),accessIndex.bucket());
        Assert.assertEquals(not_created.oid(),accessIndex.oid());

        AccessIndex load = new AccessIndexTrack("test");
        Assert.assertTrue(ds.load(load));

        Assert.assertEquals(load.referenceId(),accessIndex.referenceId());
        Assert.assertEquals(load.id(),accessIndex.id());
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
            DataStore ds = dataStoreProvider.createAccessIndexDataStore(AccessIndexService.NAME+"3");
            XMLParser xmlParser = new XMLParser();
            xmlParser.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-presence.xml"));
            long ownerId = 100;
            xmlParser.configurations.forEach(lb->{
                LobbyDescriptor lobby = lb.descriptor;
                lobby.ownerKey(new LongTypeKey(ownerId));
                Assert.assertTrue(ds.create(lobby));
                LobbyDescriptor load = new LobbyDescriptor();
                load.id(lobby.id());
                Assert.assertTrue(ds.load(load));
                Assert.assertEquals(load.typeId(),lobby.typeId());
                Assert.assertEquals(load.type(),lobby.type());
                Assert.assertEquals(load.name(),lobby.name());
                Assert.assertEquals(load.category(),lobby.category());
                List<LobbyDescriptor> lbs = ds.list(new LobbyQuery(ownerId));
                LobbyDescriptor lbx = lbs.get(0);
                Assert.assertEquals(lbx.id(),lobby.id());
                lb.applications.forEach(a->{
                    a.ownerKey(new LongTypeKey(lobby.id()));
                    Assert.assertTrue(ds.create(a));
                });
                ds.list(new ApplicationQuery(lobby.id())).forEach(a->{
                    Assert.assertTrue(a.id()>0);
                });
            });
        }catch (Exception ex){
            ex.printStackTrace();
            exception = ex;
        }
        Assert.assertNull(exception);
    }
    @Test(groups = { "LMDBMigration" })
    public void statisticsHook(){
        DataStore ds = dataStoreProvider.create("statistics",1);
        LongTypeKey owner = new LongTypeKey(1000);
        StatisticsEntry kills = new StatisticsEntry();
        kills.ownerKey(owner);
        kills.name("kills");
        Assert.assertTrue(ds.create(kills));
        StatisticsEntry wins = new StatisticsEntry();
        wins.ownerKey(owner);
        wins.name("kills");
        Assert.assertTrue(ds.create(wins));
        int[] ct ={0};
        ds.list(new StatisticsEntryQuery(owner.id())).forEach(e->{
            Assert.assertTrue(ds.load(e));
            ct[0]++;
        });
        Assert.assertEquals(ct[0],2);
    }

}
