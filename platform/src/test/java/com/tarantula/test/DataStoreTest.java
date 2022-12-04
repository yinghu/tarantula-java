package com.tarantula.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.bootstrap.TarantulaMain;
import com.tarantula.platform.service.AccessKey;
import com.tarantula.platform.service.AccessKeyQuery;
import com.tarantula.platform.service.DataStoreProvider;
import com.tarantula.platform.service.persistence.DataStoreConfigurationJsonParser;
import com.tarantula.platform.service.persistence.RevisionObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.List;

public class DataStoreTest {

    static {
        System.setProperty("java.util.logging.manager","com.icodesoftware.logging.TarantulaLogManager");
    }
    private static final TarantulaLogger log = JDKLogger.getLogger(TarantulaMain.class);


    DataStoreProvider dataStoreProvider;
    ServiceContext serviceContext;
    @BeforeClass
    public void setUp() {
        serviceContext = new TestServiceContext();
        DataStoreConfigurationJsonParser parser = new DataStoreConfigurationJsonParser("test-tarantula-platform-data-store-config.json",serviceContext,dataStoreProvider->{
            try{
                this.dataStoreProvider = dataStoreProvider;
                this.dataStoreProvider.start();
                this.dataStoreProvider.setup(serviceContext);
            }catch (Exception exx){
                exx.printStackTrace();
                throw new RuntimeException(exx);
            }
        });
        try {
            parser.start();
        }catch (Exception ex){
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    @Test(groups = { "DataStore" })
    public void smokeTest() {
        DataStore dataStore = dataStoreProvider.create("test",serviceContext.node().partitionNumber());
        AccessKey accessKey = new AccessKey();
        accessKey.typeId("test");
        accessKey.owner(this.serviceContext.node().bucketId());
        accessKey.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        accessKey.disabled(false);
        Assert.assertTrue(dataStore.create(accessKey));
        Assert.assertFalse(dataStore.create(accessKey));
        AccessKey load = new AccessKey();
        load.distributionKey(accessKey.distributionKey());
        Assert.assertTrue(dataStore.load(load));
        Assert.assertTrue(load.typeId().equals(accessKey.typeId()));
        AccessKey loadIf = new AccessKey();
        loadIf.distributionKey(load.distributionKey());
        Assert.assertFalse(dataStore.createIfAbsent(loadIf,true));
        Assert.assertTrue(load.typeId().equals(loadIf.typeId()));
        AccessKeyQuery query = new AccessKeyQuery(this.serviceContext.node().bucketId());
        List<AccessKey> keys = dataStore.list(query);
        Assert.assertTrue(keys.size()==1);
        Assert.assertTrue(accessKey.typeId().equals(keys.get(0).typeId()));
        AccessKey updating = keys.get(0);
        updating.disabled(true);
        Assert.assertTrue(dataStore.update(updating));
        AccessKey updated = new AccessKey();
        updated.distributionKey(updating.distributionKey());
        Assert.assertTrue(dataStore.load(updated));
        Assert.assertTrue(updated.disabled());
        Assert.assertTrue(updated.revision()==updating.revision());
        Assert.assertTrue(updated.revision()>loadIf.revision());
        byte[] raw = dataStore.load(accessKey.key().asString().getBytes());
        Assert.assertTrue(raw!=null);
        RevisionObject ro = RevisionObject.fromBinary(raw);
        Assert.assertTrue(ro.revision==updating.revision());
        Assert.assertTrue(ro.local);
        AccessKey fromRaw = new AccessKey();
        fromRaw.fromBinary(ro.data);
        Assert.assertTrue(fromRaw.typeId().equals(accessKey.typeId()));
    }

}
