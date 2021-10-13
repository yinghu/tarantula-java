package com.icodesoftware.integration.udp;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.protocol.OutboundMessage;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Arrays;


public class RelayService implements Runnable{

    private static TarantulaLogger log = JDKLogger.getLogger(RelayService.class);


    private DatagramSocket datagramChannel;

    public void start() throws Exception{
        this.datagramChannel = new DatagramSocket(null);
        InetSocketAddress addr = new InetSocketAddress("10.0.0.192",11933);
        this.datagramChannel.bind(addr);
    }
    public void shutdown() throws Exception{
        log.warn("Relay service is going down");
        this.datagramChannel.close();
    }

    @Override
    public void run() {
        //String _app = application.substring(application.lastIndexOf(".")+1);
        log.warn("Relay service is ready");
        while (true){
            try{
                DatagramPacket buffer = new DatagramPacket(new byte[512],512);
                this.datagramChannel.receive(buffer);
                byte[] payload = Arrays.copyOf(buffer.getData(),buffer.getLength());
                //mQueue.offer(new PendingMessage(payload,buffer.getSocketAddress()));
            }catch (Exception ex){
                //ignore
                ex.printStackTrace();
                //try{Thread.sleep(50);}catch (Exception exx){}
            }
        }
    }
}
