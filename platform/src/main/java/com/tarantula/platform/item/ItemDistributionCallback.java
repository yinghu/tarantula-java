package com.tarantula.platform.item;

public interface ItemDistributionCallback {

    boolean onItemRegistered(String category,String itemId);
    boolean onItemReleased(String category,String itemId);

    boolean onItemRegistered(int publishId);
    boolean onItemReleased(int publishId);

}
