package com.tarantula.platform.service;

import com.icodesoftware.DataStore;
import com.icodesoftware.OnAccess;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.store.Transaction;

import java.util.Map;
import java.util.UUID;

public class MockStoreProvider extends AuthObject{

    private DataStore dataStore;
    private String secureKey;
    public MockStoreProvider(String typeId,String key){
        super(typeId,"");
        this.secureKey = key;
    }

    @Override
    public String name(){
        return OnAccess.MOCK_STORE;
    }


    @Override
    public void setup(ServiceContext serviceContext){
        super.setup(serviceContext);
        String ds = typeId.replaceAll("-","_")+"_mock_store_transaction";
        dataStore = serviceContext.dataStore(ds,serviceContext.partitionNumber());
    }
    @Override
    public boolean validate(Map<String,Object> params){
        String tid = UUID.randomUUID().toString();
        params.put(OnAccess.STORE_TRANSACTION_ID,tid);
        params.put(OnAccess.STORE_PRODUCT_ID,params.get(OnAccess.STORE_RECEIPT));
        params.put(OnAccess.STORE_QUANTITY,1);
        Transaction transaction = new Transaction();
        transaction.index(tid);
        transaction.owner((String)params.get(OnAccess.SYSTEM_ID));
        dataStore.create(transaction);
        return true;
    }

}
