package com.tarantula.platform.service.persistence;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableRegistry;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.DataStoreSummary;
import com.icodesoftware.service.KeyIndex;
import com.icodesoftware.service.KeyIndexService;
import com.icodesoftware.util.BinaryKey;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.service.cluster.recover.DistributionDataViewer;

import java.util.Base64;

public class DataStoreViewer implements DataStoreSummary {

    private DataStore dataStore;
    private TarantulaContext tarantulaContext;
    public DataStoreViewer(TarantulaContext tarantulaContext,DataStore dataStore){
        this.tarantulaContext = tarantulaContext;
        this.dataStore = dataStore;
    }
    @Override
    public String name() {
        return dataStore.name();
    }



    @Override
    public long totalRecords() {
        return dataStore.count();
    }


    public void list(DataStoreSummary.View view){
        dataStore.backup().forEach((k,v)-> {
            Recoverable.DataHeader h = v.readHeader();
            RecoverableRegistry registry = tarantulaContext.recoverableRegistry(h.factoryId());
            Recoverable r = registry.create(h.classId());
            r.readKey(k);
            r.read(v);
            return view.on(tarantulaContext.node(),h,r);
        });
    }

    public void load(byte[] key, DataStoreSummary.View view){
        BinaryKey akey = new BinaryKey(Base64.getDecoder().decode(key));
        if(!dataStore.name().startsWith(KeyIndexService.STORE_NAME)){
            KeyIndex keyIndex = tarantulaContext.keyIndexService.lookup(dataStore.name(),akey);
        }
        System.out.println("DB : "+dataStore.name());
        this.dataStore.backup().get(akey,(k,v)->{
            Recoverable.DataHeader h = v.readHeader();
            RecoverableRegistry registry = tarantulaContext.recoverableRegistry(h.factoryId());
            Recoverable recoverable = registry.create(h.classId());
            recoverable.read(v);
            recoverable.readKey(k);
            view.on(null,h,recoverable);
            return true;
        });
    }
}
