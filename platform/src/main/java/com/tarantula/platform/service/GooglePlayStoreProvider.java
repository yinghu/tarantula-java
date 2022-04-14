package com.tarantula.platform.service;

import com.icodesoftware.OnAccess;
import com.icodesoftware.service.ServiceContext;

import java.util.Map;

public class GooglePlayStoreProvider extends AuthObject{

    public GooglePlayStoreProvider(){
        super("googleStore","","","","","",new String[0]);
    }

    @Override
    public void setup(ServiceContext serviceContext){
        super.setup(serviceContext);
    }
    @Override
    public boolean validate(Map<String,Object> params){
        params.put(OnAccess.STORE_PRODUCT_ID,params.get(OnAccess.STORE_RECEIPT));
        this.metricsListener.onUpdated(Metrics.GOOGLE_STORE_COUNT,1);
        return true;
    }
}
