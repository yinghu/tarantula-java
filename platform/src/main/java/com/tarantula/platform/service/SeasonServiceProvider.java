package com.tarantula.platform.service;


import com.icodesoftware.service.MetricsListener;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.configuration.PlatformConfigurationServiceProvider;


public class SeasonServiceProvider extends AuthObject {


    private PlatformConfigurationServiceProvider configurationServiceProvider;


    public SeasonServiceProvider(PlatformGameServiceProvider gameServiceProvider, MetricsListener metricsListener){
        super(gameServiceProvider.gameCluster().typeId(),"");
        this.configurationServiceProvider = gameServiceProvider.configurationServiceProvider();
        this.applicationMetricsListener = metricsListener;
    }

    @Override
    public String name() {
        return "season";
    }

    @Override
    public byte[] download(String query) {
        return new byte[0];
    }
}
