package com.tarantula.platform;


import com.tarantula.Configurable;
import com.tarantula.Configuration;
import com.tarantula.Property;
import com.tarantula.platform.service.cluster.PortableRegistry;
import com.tarantula.platform.util.SystemUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by yinghu on 8/4/2020
 */
public class ConfigurableObject extends RecoverableObject implements Configurable{

    protected CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();

    public void registerListener(Listener listener){
        this.listeners.add(listener);
    }
    public void update(Configurable updated){
       this.listeners.forEach((c)->{
           c.onUpdated(updated);
       });
    }

}
