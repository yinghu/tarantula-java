package com.tarantula;

import com.icodesoftware.Recoverable;

/**
 * an application cluster that includes a set of applications.
 * for instance a game including lobby, room, stats, leader board, etc.
 * */
public interface ApplicationCluster extends Recoverable {

    String type();
    String typeId();
    String creationDate();
    String ownerName();
    String description();

}
