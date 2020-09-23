package com.tarantula.cci.udp;


import com.tarantula.*;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by yinghu lu on 9/21/2020.
 */
public class UDPSessionService implements EventService{

    private DatagramChannel datagramChannel;
    private final Connection connection;
    private final ConcurrentLinkedDeque<ByteBuffer> pendingData;

    private Thread receiver;
    public UDPSessionService(Connection connection,ConcurrentLinkedDeque<ByteBuffer> pendingData){
        this.connection = connection;
        this.pendingData = pendingData;
    }

    @Override
    public void publish(Event out) {
        try{
            ByteBuffer buffer = ByteBuffer.wrap(out.payload());
            datagramChannel.write(buffer);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public String subscription() {
        return null;
    }

    @Override
    public void retry(String retryKey) {

    }

    @Override
    public void registerEventListener(String topic, EventListener callback) {

    }

    @Override
    public RoutingKey instanceRoutingKey(String applicationId, String instanceId) {
        return null;
    }

    @Override
    public RoutingKey routingKey(String magicKey, String tag) {
        return null;
    }

    @Override
    public RoutingKey routingKey(String magicKey, String tag, int routingNumber) {
        return null;
    }

    @Override
    public boolean onEvent(Event event) {
        return false;
    }

    @Override
    public void start() throws Exception {
        this.datagramChannel = DatagramChannel.open();
        this.datagramChannel.connect(new InetSocketAddress(connection.host(),connection.port()));
        this.receiver = new Thread(()->{
           run();
        });
        this.receiver.setName("tarantula-udp-"+connection.serverId());
        this.receiver.start();
    }

    @Override
    public void shutdown() throws Exception {
        this.datagramChannel.close();
        this.receiver.interrupt();
    }


    private void run() {
        try{
            while (true){
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                datagramChannel.receive(buffer);
                //dispatch
                pendingData.offer(buffer);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
