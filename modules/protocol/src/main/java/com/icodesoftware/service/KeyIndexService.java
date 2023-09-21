package com.icodesoftware.service;

import com.icodesoftware.Event;
import com.icodesoftware.Recoverable;

public interface KeyIndexService extends ServiceProvider{

    String NAME = "KeyIndexService";
    String STORE_NAME = "kdx_";
    //KeyIndex lookup(String source,String key);
    KeyIndex lookup(String source, Recoverable.Key key);

    void onReplicated(Event event);
    boolean createIfAbsent(KeyIndex keyIndex);

    boolean update(KeyIndex keyIndex);

    ClusterProvider.Node nextNode();

    ClusterProvider.Node[] nextNodeList(int expected);

    ClusterProvider.Node[] nodeList(KeyIndex keyIndex);

    ClusterProvider.Node[] nodeList(KeyIndex keyIndex,int expected);

    ClusterProvider.Node[] recoveringNodeList(String source,String key);

}
