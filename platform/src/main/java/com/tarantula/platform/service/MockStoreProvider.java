package com.tarantula.platform.service;

import com.icodesoftware.DataStore;
import com.icodesoftware.OnAccess;
import com.icodesoftware.service.ServiceContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MockStoreProvider extends AuthObject{

    private Map<String, MockStoreProvider.KeyDataStore> serviceKeys;
    public MockStoreProvider(Map<String,String> bundleKeys){
        super("mockStore","","","","","",new String[0]);
        serviceKeys = new HashMap<>();
        bundleKeys.forEach((k,v)->{
            serviceKeys.put(k,new MockStoreProvider.KeyDataStore(v));
        });
    }
    @Override
    public void setup(ServiceContext serviceContext){
        super.setup(serviceContext);
        serviceKeys.forEach((k,v)->{
            String ds = k.replaceAll("-","_")+"_mock_store_transaction";
            v.dataStore = serviceContext.dataStore(ds,serviceContext.partitionNumber());
        });
    }
    @Override
    public boolean validate(Map<String,Object> params){
        String tid = UUID.randomUUID().toString();
        params.put(OnAccess.STORE_TRANSACTION_ID,tid);
        params.put(OnAccess.STORE_PRODUCT_ID,params.get(OnAccess.STORE_RECEIPT));
        params.put(OnAccess.STORE_QUANTITY,1);
        return true;
    }

    private class KeyDataStore{
        public String key;
        public DataStore dataStore;
        public KeyDataStore(String key){
            this.key = key;
        }
    }
}
