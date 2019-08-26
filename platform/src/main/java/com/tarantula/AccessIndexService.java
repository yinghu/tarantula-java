package com.tarantula;


/**
 * Updated by yinghu lu on 8/26/19
 */
public interface AccessIndexService extends ServiceProvider{

    String NAME = "AccessIndexService";

    AccessIndex set(String accessKey,String systemId);

    AccessIndex get(String accessKey);

}
