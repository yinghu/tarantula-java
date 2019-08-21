package com.tarantula.platform.presence;

import com.tarantula.*;
import com.tarantula.platform.*;
import com.tarantula.platform.event.IndexEvent;
import com.tarantula.DeploymentServiceProvider;
import com.tarantula.platform.util.IndexContextSerializer;

import java.io.BufferedInputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Updated by yinghu on 3/2/2018.
 */
public class IndexApplication extends TarantulaApplicationHeader implements OnView.Listener,OnLobby.Listener {

    private ConcurrentHashMap<String,OnView> _viewList = new ConcurrentHashMap<>();
    private CopyOnWriteArraySet<String> _lobbyList = new CopyOnWriteArraySet<>();
    private ConcurrentHashMap<String,byte[]> _resourceList = new ConcurrentHashMap<>();
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
            session.write(builder.create().toJson(ic).getBytes(),this.descriptor.responseLabel());
        }
        else if(session.action().equals("resource")){
            IndexEvent ie = (IndexEvent)session;
            String res = ie.viewId.replace("resource","web");
            byte[] data = _resourceList.computeIfAbsent(res,(rk)->{
                BufferedInputStream in = new BufferedInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream(res));
                try{
                    byte[] ret = new byte[in.available()];
                    in.read(ret);
                    return ret;
                }catch (Exception ex){
                    this.context.log("Resource ["+res+"] not existed",OnLog.WARN);
                    return new byte[0];
                }
                finally {
                    try{
                        if(in!=null){
                            in.close();
                        }
                    }catch (Exception ex){}
                }
            });
            session.write(data,this.descriptor.label());
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
        this.context.log("Index application started on tag ["+this.descriptor.tag()+"]",OnLog.INFO);
    }
    @Override
    public void onView(OnView onView) {
        this.context.log("View Added-->>"+onView.viewId(),OnLog.WARN);
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
