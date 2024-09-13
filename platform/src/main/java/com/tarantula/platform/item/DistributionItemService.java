package com.tarantula.platform.item;

import com.icodesoftware.service.ServiceProvider;

public interface DistributionItemService extends ServiceProvider {
    String NAME = "DistributionItemService";
    boolean onRegisterItem(String gameServiceName,String serviceName,String category,String itemId);
    boolean onReleaseItem(String gameServiceName,String serviceName,String category,String itemId);

    //boolean onRegisterItem(String gameServiceName,String serviceName,String category,String itemId);
    //boolean onReleaseItem(String gameServiceName,String serviceName,String category,String itemId);


}
