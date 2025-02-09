package com.icodesoftware.protocol;

import com.icodesoftware.service.EndPoint;
import com.icodesoftware.util.VirtualThreadExecutor;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;

public class SocketEndpointService implements EndPoint {

    public static final int PORT = 5000;
    public static final int BACK_LOG = 100;
    public static final int PERMITS = 8;
    protected int port = PORT;
    protected String address;
    protected int backlog = BACK_LOG;
    protected ServerSocket serverSocket;
    protected boolean started;
    protected Executor executor;
    protected int permits = PERMITS;
    @Override
    public void address(String address) {
        this.address = address;
    }

    @Override
    public void port(int port) {
        this.port = port;
    }

    @Override
    public int port() {
        return port;
    }
    public void backlog(int backlog){
        this.backlog = backlog;
    }


    @Override
    public String name() {
        return EndPoint.TCP_ENDPOINT;
    }

    @Override
    public void start() throws Exception {
        if(started) return;
        executor = VirtualThreadExecutor.create(permits);
        serverSocket = address ==null? new ServerSocket(port,backlog) : new ServerSocket(port,backlog, InetAddress.getByName(address));
        started = true;
        while (started){
            try{
                Socket socket = serverSocket.accept();
                executor.execute(()->onSocket(socket));
            }catch (Exception ex){
                onException(ex);
            }
        }
    }

    @Override
    public void shutdown() throws Exception {
        if(!started) return;
        serverSocket.close();
    }

    protected void onSocket(Socket socket){

    }

    protected void onException(Exception ex){

    }
}
