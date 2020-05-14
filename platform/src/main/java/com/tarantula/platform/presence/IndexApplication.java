package com.tarantula.platform.presence;

import com.tarantula.*;
import com.tarantula.platform.*;
import com.tarantula.platform.event.IndexEvent;
import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.service.OnLobby;
import com.tarantula.platform.service.TokenValidatorProvider;
import com.tarantula.platform.util.PresenceContextSerializer;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Updated by yinghu on 8/24/19
 */
public class IndexApplication extends TarantulaApplicationHeader implements OnView.Listener, OnLobby.Listener {

    private ConcurrentHashMap<String,OnView> _viewList = new ConcurrentHashMap<>();
    private CopyOnWriteArraySet<String> _lobbyList = new CopyOnWriteArraySet<>();
    private List<Access.Role> roleList;
    private TokenValidatorProvider tokenValidatorProvider;
    @Override
    public void callback(Session session, byte[] payload) throws Exception {
        if(session.action().equals("view")){
            IndexEvent ie = (IndexEvent)session;
            PresenceContext ic = new PresenceContext("view");
            OnView view = this._viewList.get(ie.trackId());
            ic.view = view;
            session.write(builder.create().toJson(ic).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("index")){
            PresenceContext ic = new PresenceContext("index");
            ic.googleClientId = this.tokenValidatorProvider.authVendor("google").clientId();
            ic.stripeClientId = this.tokenValidatorProvider.authVendor("stripe").clientId();
            String typeId = session.trackId();
            OnView view = this._viewList.get("index");
            ic.lobbyList = this.context.index();
            _lobbyList.forEach((n)->{
                if(typeId!=null&&typeId.equals(n)){
                    ic.lobbyList.add(this.context.lobby(n));
                }
                else if(typeId==null){
                    ic.lobbyList.add(this.context.lobby(n));
                }
            });
            ic.view = view;
            ic.roleList = roleList;
            session.write(builder.create().toJson(ic).getBytes(),this.descriptor.responseLabel());
        }
        else{
            this.context.log(session.action(),OnLog.WARN);
            throw new RuntimeException("operation not supported");
        }
    }
    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        builder.registerTypeAdapter(PresenceContext.class,new PresenceContextSerializer());
        DeploymentServiceProvider deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        deploymentServiceProvider.registerOnViewListener(this);
        deploymentServiceProvider.registerOnLobbyListener(this);
        this.context.configuration().forEach((Configuration c)->{
            OnView v = new OnViewTrack();
            v.viewId(c.type());
            v.flag(c.property("flag"));
            v.contentBaseUrl(c.property("contentBaseUrl"));
            v.icon(c.property("icon"));
            v.category(c.property("category"));
            v.moduleFile(c.property("moduleFile"));
            v.moduleName(c.property("moduleName"));
            v.moduleResourceFile(c.property("moduleResourceFile"));
            _viewList.put(v.viewId(),v);
        });
        this.tokenValidatorProvider = this.context.serviceProvider(TokenValidatorProvider.NAME);
        this.roleList = this.tokenValidatorProvider.list();
        this.context.log("Index application started on tag ["+this.descriptor.tag()+"]",OnLog.INFO);
    }
    @Override
    public void onView(OnView onView) {
        //this.context.log("View Update-->>"+onView.toString(),OnLog.WARN);
        if(!onView.disabled()){
            this._viewList.put(onView.viewId(),onView);
        }
        else{
            this._lobbyList.remove(onView.viewId());
        }
    }

    @Override
    public void onLobby(OnLobby onLobby) {
        //context.log("Lobby Updated--->>"+onLobby.toString(),OnLog.WARN);
        if(!onLobby.closed()){
            this._lobbyList.add(onLobby.typeId());
        }
        else{
            this._lobbyList.remove(onLobby.typeId());
        }
    }
}
