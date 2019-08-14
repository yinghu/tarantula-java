package com.tarantula.platform.presence;

import com.tarantula.*;
import com.tarantula.platform.*;
import com.tarantula.platform.event.IndexEvent;
import com.tarantula.DeploymentServiceProvider;
import com.tarantula.platform.util.IndexContextSerializer;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Updated by yinghu on 3/2/2018.
 */
public class IndexApplication extends TarantulaApplicationHeader implements OnView.Listener,OnLobby.Listener {

    private ConcurrentHashMap<String,OnView> _viewList = new ConcurrentHashMap<>();
    private CopyOnWriteArraySet<String> _lobbyList = new CopyOnWriteArraySet<>();
    @Override
    public void callback(Session session, byte[] payload) throws Exception {
        if(session.action().equals("view")){
            IndexEvent ie = (IndexEvent)session;
            IndexContext ic = new IndexContext();
            OnView view = this._viewList.get(ie.viewId);
            ic.view = view;
            session.write(builder.create().toJson(ic).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("index")){
            IndexContext ic = new IndexContext();
            OnView view = this._viewList.get("index");
            ic.lobbyList = this.context.index();
            _lobbyList.forEach((n)->{
                ic.lobbyList.add(this.context.lobby(n));
            });
            ic.view = view;
            //ic.oAuthVendorList = new ArrayList<>();
            //ic.oAuthVendorList.add(this.context.validator().vendor("google"));
            //ic.oAuthVendorList.add(this.context.validator().vendor("stripe"));
            session.write(builder.create().toJson(ic).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("resource")){
            IndexEvent ie = (IndexEvent)session;
            String res = ie.viewId.replace("resource","web");
            BufferedInputStream in = new BufferedInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream(res));
            byte[] ret = new byte[in.available()];
            in.read(ret);
            session.write(ret,0,"text/javascript",this.descriptor.responseLabel(),true);
            in.close();
        }
        else{
            this.context.log(session.action(),OnLog.WARN);
            throw new RuntimeException("operation not supported");
        }
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        super.setup(context);
        builder.registerTypeAdapter(IndexContext.class,new IndexContextSerializer());
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
        //this.context.log(this.context.validator().vendor("google").clientId(),OnLog.WARN);
        this.context.log("Index application started",OnLog.INFO);
    }
    /**
    @Override
    public boolean onEvent(Event event) {
        if(event.action().equals("index")){
            IndexContext ic = new IndexContext();
            OnView view = this._viewList.get("index");
            ic.lobbyList = this.context.index();
            _lobbyList.forEach((n)->{
                context.log(n,OnLog.WARN);
                ic.lobbyList.add(this.context.lobby(n));
            });
            ic.view = view;
            event.write(builder.create().toJson(ic).getBytes(),this.descriptor.responseLabel());
        }
        else if(event.action().equals("view")){
            IndexEvent ie = (IndexEvent)event;
            IndexContext ic = new IndexContext();
            OnView view = this._viewList.get(ie.viewId);
            ic.view = view;
            event.write(builder.create().toJson(ic).getBytes(),this.descriptor.responseLabel());
        }
        else{
            this.context.log(event.toString(),OnLog.WARN);
        }
        return true;
    }
    **/
    @Override
    public void onView(OnView onView) {
        //this.context.log("VIEW ADDED-->>"+onView.viewId(),OnLog.WARN);
        this._viewList.put(onView.viewId(),onView);
    }

    @Override
    public void onLobby(OnLobby onLobby) {
        context.log("Lobby Updated--->>"+onLobby.toString(),OnLog.WARN);
        if(!onLobby.closed()){
            this._lobbyList.add(onLobby.typeId());
        }
        else{
            this._lobbyList.remove(onLobby.typeId());
        }
    }
}
