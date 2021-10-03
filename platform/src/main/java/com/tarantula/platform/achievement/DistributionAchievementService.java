package com.tarantula.platform.achievement;

import com.icodesoftware.service.ServiceProvider;

public interface DistributionAchievementService extends ServiceProvider {
    String NAME = "DistributionAchievementService";
    boolean register(String serviceName,String category,String itemId);
}
