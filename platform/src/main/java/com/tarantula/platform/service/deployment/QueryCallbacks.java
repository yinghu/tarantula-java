package com.tarantula.platform.service.deployment;

import com.icodesoftware.service.RecoverService;


public class QueryCallbacks {
    public final RecoverService.QueryCallback queryCallback;
    public final RecoverService.QueryEndCallback queryEndCallback;

    public QueryCallbacks(RecoverService.QueryCallback queryCallback, RecoverService.QueryEndCallback queryEndCallback){
        this.queryCallback =queryCallback;
        this.queryEndCallback = queryEndCallback;
    }
}
