package com.tarantula.platform.service.persistence.web;

import com.google.gson.JsonObject;
import com.icodesoftware.OnSession;
import com.icodesoftware.Recoverable;
import com.icodesoftware.Session;
import com.icodesoftware.service.BackupProvider;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.util.HttpCaller;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.OnSessionTrack;
import com.tarantula.platform.configuration.WebHookConfiguration;

import java.util.Map;

public class WebHookBackupProvider extends HttpCaller implements BackupProvider {


    private WebHookConfiguration webHookConfiguration;
    private String accessKey;

    public WebHookBackupProvider(){
    }

    public WebHookBackupProvider(WebHookConfiguration webHookConfiguration){
        this.webHookConfiguration = webHookConfiguration;
    }


    @Override
    public boolean enabled() {
        return false;
    }

    @Override
    public void enabled(boolean enabled) {

    }

    @Override
    public int scope() {
        return 0;
    }

    @Override
    public void registerDataStore(String name) {

    }

    @Override
    public void registerDataStore(String prefix, int partitions) {

    }

    @Override
    public void configure(Map<String, Object> properties) {
        this.host = (String) properties.get("host");
        this.accessKey = (String) properties.get("accessKey");
        try{super._init();}catch (Exception ex){ex.printStackTrace();}
    }

    @Override
    public <T extends Recoverable> void update(Metadata metadata, String key, T t) {
        try {
            String[] headers = new String[]{
                    Session.TARANTULA_TAG,"presence/lobby",
                    Session.TARANTULA_ACTION,"onUpdate",
                    Session.TARANTULA_ACCESS_KEY,accessKey,
                    Session.TARANTULA_NAME,key+"#"+metadata.toJson()
            };
            String resp = super.post("backup",t.toBinary(),headers);
            System.out.println(resp);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public <T extends Recoverable> void create(Metadata metadata, String key, T t) {

    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
