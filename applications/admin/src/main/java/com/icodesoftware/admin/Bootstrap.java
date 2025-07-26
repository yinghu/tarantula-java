package com.icodesoftware.admin;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Bootstrap {
    static {
        System.setProperty("java.util.logging.manager","com.icodesoftware.logging.TarantulaLogManager");
    }
    private static final TarantulaLogger log = JDKLogger.getLogger(Bootstrap.class);
    public static void main(String[] args) throws Exception{
        log.warn("Start tarantula admin service");
        InetSocketAddress ip = new InetSocketAddress(8090);
        HttpServer server = HttpServer.create(ip,100);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.createContext("/admin",new AdminServiceProvider());
        server.start();
    }
}
