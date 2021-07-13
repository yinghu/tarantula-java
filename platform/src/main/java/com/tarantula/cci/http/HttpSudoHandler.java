package com.tarantula.cci.http;

import com.tarantula.cci.HttpDispatcher;
import com.tarantula.cci.RequestHandler;
import com.tarantula.platform.service.EndPoint;

public class HttpSudoHandler extends HttpDispatcher {


    public void resource(EndPoint.Resource resource){
        requestHandler = resource.requestHandler(path());
    }
    @Override
    public String path() {
        return RequestHandler.SUDO_PATH;
    }
}
