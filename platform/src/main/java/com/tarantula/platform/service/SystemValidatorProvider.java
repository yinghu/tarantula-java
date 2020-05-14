package com.tarantula.platform.service;

import com.tarantula.*;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.AccessControl;
import com.tarantula.platform.OnSessionTrack;
import com.tarantula.platform.PresenceIndex;
import com.tarantula.platform.SystemValidator;
import com.tarantula.platform.presence.User;
import com.tarantula.platform.util.SystemUtil;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SystemValidatorProvider implements TokenValidatorProvider {

    private static TarantulaLogger log = JDKLogger.getLogger(SystemValidatorProvider.class);

    private SystemValidator systemValidator;
    private int timeoutInMinutes;
    private int timeoutInSeconds;

    private ServiceContext serviceContext;
    private ConcurrentHashMap<String,Presence> pMap;
    private HashMap<String,Access.Role> rMap;
    private HashMap<String,AuthVendor> aMap;
    private DataStore pdataStore;
    private DataStore udataStore;

    private MessageDigest _messageDigest;

    public MessageDigest messageDigest(){
        try{
            return (MessageDigest)this._messageDigest.clone();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public TokenValidator tokenValidator(){
        return systemValidator.tokenValidator();
    }
    public Presence presence(String systemId){
        return pMap.computeIfAbsent(systemId,(k)->{
            PresenceIndex px = new PresenceIndex();
            px.distributionKey(systemId);
            pdataStore.load(px);
            px.dataStore(pdataStore);
            px.registerEventService(this.serviceContext.eventService(Distributable.INTEGRATION_SCOPE));
            return px;
        });
    }
    public void offSession(String systemId){
        Presence presence = pMap.remove(systemId);
        if(presence!=null){
            presence.disabled(true);
            presence.update();
        }
    }
    public void timeout(int minutes,int seconds){
        this.timeoutInMinutes = minutes;
        this.timeoutInSeconds = seconds;
    }
    public boolean validateAccessKey(String accessKey){
        return true;
    }
    public String ticket(String key,int stub,int duration){
        return SystemUtil.ticket(messageDigest(),key,stub,duration);
    }
    public boolean validateTicket(String key,int stub,String ticket){
        return SystemUtil.validTicket(messageDigest(),key,stub,ticket);
    }
    public List<ApplicationCluster> list(String systemId){
        return new ArrayList<>();
    }
    public List<Access.Role> list(){
        ArrayList<Access.Role> rlist = new ArrayList<>();
        rMap.forEach((k,v)->rlist.add(v));
        return rlist;
    }
    public Access.Role role(String systemId){
        if(systemId==null){
            return rMap.get("player");
        }
        Access acc = new User();
        acc.distributionKey(systemId);
        if(udataStore.load(acc)){
            return rMap.get(acc.role());
        }
        else{
            return rMap.get("player");
        }
    }
    @Override
    public String name() {
        return TokenValidatorProvider.NAME;
    }
    public AuthVendor authVendor(String name){
        return aMap.get(name);
    }
    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.pdataStore =  this.serviceContext.dataStore("presence",this.serviceContext.partitionNumber());
        this.udataStore =  this.serviceContext.dataStore("user",this.serviceContext.partitionNumber());
        AuthVendor google = this.serviceContext.authVendor("google");
        if(google!=null){
            aMap.put("google",(google));
        }
    }

    @Override
    public void waitForData() {
        log.info("System validator provider started");
    }

    @Override
    public void start() throws Exception {
        this.pMap = new ConcurrentHashMap<>();
        this.rMap = new HashMap<>();
        this.aMap = new HashMap<>();
        Access.Role root = new AccessControl("root",Access.ROOT_ACCESS_CONTROL);
        Access.Role owner = new AccessControl("owner",Access.OWNER_ACCESS_CONTROL);
        Access.Role admin = new AccessControl("admin",Access.ADMIN_ACCESS_CONTROL);
        Access.Role player = new AccessControl("player",Access.PLAYER_ACCESS_CONTROL);
        rMap.put(root.name(),root);
        rMap.put(owner.name(),owner);
        rMap.put(admin.name(),admin);
        rMap.put(player.name(),player);
        _messageDigest = MessageDigest.getInstance(MDA);
        this.systemValidator = new SystemValidator();
        this.systemValidator.systemValidatorProvider(this);
        this.systemValidator.timeout(this.timeoutInMinutes,this.timeoutInSeconds);
    }
    @Override
    public void shutdown() throws Exception {
        //this.systemValidator.shutdown();
    }
}
