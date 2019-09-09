package com.tarantula.integration.udp;
import com.google.gson.JsonObject;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

/*
* Service Connector the single connection to the gec cluster to receive updates and publish those updates via udp package.
* The connection should not write data from udp to gec cluster
* */
public class ServiceConnector implements Runnable {
    private SocketChannel socketChannel;
    private String serverId;
    private ByteBuffer readBuffer;
    private ConcurrentLinkedDeque<OutboundMessage> outboundQueue;

    private PendingData pending;
    public ServiceConnector(){
        this.serverId = UUID.randomUUID().toString();
    }
    public void start() throws Exception{
        this.outboundQueue = new ConcurrentLinkedDeque<>();
        this.readBuffer = ByteBuffer.allocate(1024);
        this.pending = new PendingData();
        socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost",6393));
        onRegister();
    }
    @Override
    public void run() {
        while(true){
            try{
                readBuffer.clear();
                int rn = socketChannel.read(readBuffer);
                if(rn>0){
                    readBuffer.flip();
                    while(readBuffer.hasRemaining()){
                        char c = (char) readBuffer.get();
                        if(!pending.onData){
                            if(!pending.onLabel&&c != ','){
                                pending.clientId.append(c);
                            }
                            else if(!pending.onLabel&&c==','){
                                pending.onLabel = true;
                            }
                            else if(pending.onLabel&&c!='{'){
                                pending.label.append(c);
                                pending.data.append(c);
                            }
                            else{
                                pending.data.append(c);
                                pending.onData = true;
                            }
                        }
                        else{
                            if(c=='|'){
                                outboundQueue.offer(pending.reset());
                                System.out.println(outboundQueue.poll());
                            }
                            else{
                                pending.data.append(c);
                            }
                        }
                    }
                }
                else{
                    Thread.sleep(10);
                }
            }catch (Exception ex){
                //ignore
            }
        }
    }

    private void onRegister() throws Exception{
        JsonObject payload = new JsonObject();
        payload.addProperty("action","onConnect");
        payload.addProperty("clientId","push/streaming");
        payload.addProperty("path","/push/action");
        payload.addProperty("streaming",true);
        payload.addProperty("serverId",serverId);
        JsonObject jsonObject = new JsonObject();
        payload.add("data",jsonObject);
        jsonObject.addProperty("command","onConnect");
        jsonObject.addProperty("type","udp");
        jsonObject.addProperty("secured",false);
        jsonObject.addProperty("protocol","udp");
        jsonObject.addProperty("host","localhost");
        jsonObject.addProperty("port",9999);
        jsonObject.addProperty("path","tarantula");
        jsonObject.addProperty("serverId",serverId);
        System.out.println(payload.toString().length());
        ByteBuffer buffer = ByteBuffer.wrap((payload.toString()+"|").getBytes());
        socketChannel.write(buffer);
    }

}
