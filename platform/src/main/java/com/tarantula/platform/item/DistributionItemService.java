package com.tarantula.platform.item;

import com.icodesoftware.service.ServiceProvider;

public interface DistributionItemService extends ServiceProvider {
    String NAME = "DistributionItemService";
    boolean register(String gameServiceName,String serviceName,String category,String itemId);
}
