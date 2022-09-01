package com.tarantula.platform.item;

public interface ClusterConfigurationCallback {

    boolean onItemRegistered(String category,String itemId);
    boolean onItemReleased(String category,String itemId);
}
