package com.tarantula.platform.service;

import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.MetricsListener;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.configuration.PlatformConfigurationServiceProvider;
import com.tarantula.platform.configuration.WebClient;
import com.tarantula.platform.configuration.WebHookCredentialConfiguration;


public class WebHookProvider extends AuthObject {

    private static JDKLogger log = JDKLogger.getLogger(WebHookProvider.class);
    private PlatformConfigurationServiceProvider configurationServiceProvider;


    public WebHookProvider(PlatformGameServiceProvider gameServiceProvider, MetricsListener metricsListener){
        super(gameServiceProvider.gameCluster().typeId(),"");
        this.configurationServiceProvider = gameServiceProvider.configurationServiceProvider();
        this.applicationMetricsListener = metricsListener;
    }

    @Override
    public String name() {
        return OnAccess.WEB_HOOK;
    }

    public boolean upload(String query, byte[] bytes) {
        WebHookCredentialConfiguration webHookCredentialConfiguration = configurationServiceProvider.credentialConfiguration(OnAccess.WEB_HOOK);
        if(webHookCredentialConfiguration==null){
            log.warn("No web hook configuration available for ["+typeId+"]");
            return false;
        }
        WebClient webClient = webHookCredentialConfiguration.webClient(query);
        if(webClient==null){
            log.warn("No web client configuration available for ["+query+"]");
            return false;
        }
        return webClient.post(serviceContext,bytes);
    }
}
