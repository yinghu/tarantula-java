package com.tarantula.platform.presence;

import com.tarantula.*;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.TarantulaApplicationHeader;
import com.tarantula.platform.util.PresenceContextSerializer;
import com.tarantula.platform.util.SystemUtil;

import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Updated by yinghu lu on 8/26/19
 */
public class ProfileApplication extends TarantulaApplicationHeader {

    private DataStore dataStore;
    private ConcurrentHashMap<String,ContentTransaction> _pendingProgress = new ConcurrentHashMap<>();

    private DataStore avatarDataStore;

    private int uploadChunkSize;

    @Override
    public void callback(Session session, byte[] payload) throws Exception {
        if(session.action().equals("onProfile")){
            OnAccess cmd = this.builder.create().fromJson(new String((payload)).trim(),OnAccess.class);
            Profile u = this._load(cmd.systemId());
            PresenceContext pcx  = new PresenceContext();
            pcx.command("onProfile");
            pcx.profile = u;
            pcx.successful(true);
            session.write(builder.create().toJson(pcx).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("onUpload")){
            OnAccess acc = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            ContentTransaction ct = new ContentTransaction(session.systemId(),"Avatar");
            ct.chunkSize = uploadChunkSize;
            ct.contentSize = Integer.parseInt(acc.header("size"));
            ct.batchSize = Math.floorDiv(ct.contentSize,uploadChunkSize)+((ct.contentSize%uploadChunkSize)>0?1:0);
            ct.contentType = SystemUtil.mimeTypeFromWebBase64(acc.header("type"));
            this.avatarDataStore.create(ct);
            ct._batched = ct.batchSize;
            this._pendingProgress.put(session.systemId(),ct);
            acc.header("batchSize",ct.chunkSize+"");
            acc.header("transactionId",session.systemId());
            session.write(builder.create().toJson(acc).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().startsWith("avatar")){
            this.context.log(session.action(),OnLog.WARN);
            ContentTransaction _bx = this._pendingProgress.get(session.systemId());
            ContentChunk cc = new ContentChunk(session.systemId(),session.action(),Base64.getDecoder().decode(payload));
            this.avatarDataStore.create(cc);
            if(_bx.onTransaction(cc.toByteArray().length)){
                this._pendingProgress.remove(_bx.distributionKey());
                Profile u = this._load(session.systemId());
                PresenceContext pcx  = new PresenceContext();
                pcx.code(1);
                pcx.command(session.action());
                pcx.profile = u;
                pcx.successful(true);
                session.write(builder.create().toJson(pcx).getBytes(),this.descriptor.responseLabel());
            }
            else{
                session.write(this.builder.create().toJson(new ResponseHeader(session.action(),0)).getBytes(),this.descriptor.responseLabel());
            }
        }
        else{
            OnAccess cmd = this.builder.create().fromJson(new String((payload)).trim(),OnAccess.class);
            Profile u = this._load(cmd.systemId());
            PresenceContext pcx  = new PresenceContext();
            pcx.command("onProfile");
            pcx.profile = u;
            pcx.successful(true);
            session.write(builder.create().toJson(pcx).getBytes(),this.descriptor.responseLabel());
        }
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        Configuration cfg = this.context.configuration("setup");
        this.uploadChunkSize = Integer.parseInt(cfg.property("uploadChunkSize"));
        this.builder.registerTypeAdapter(PresenceContext.class,new PresenceContextSerializer());
        this.dataStore = this.context.dataStore("profile");
        this.avatarDataStore = this.context.dataStore("avatar");
        this.context.log("Profile application started on tag ["+descriptor.tag()+"]",OnLog.INFO);
    }

    private Profile _load(String systemId){
        Profile _px = new ProfileTrack();
        _px.distributionKey(systemId);
        if(this.dataStore.load(_px)){
            return _px;
        }
        else{
           return null;
        }
    }

}
