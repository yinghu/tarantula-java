package com.tarantula.cci.tcp;

import com.tarantula.Event;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Updated by yinghu lu on 12/15/19
 */
public class PendingRequest {

    private ArrayDeque<String> inboundQueue = new ArrayDeque<>(100);
    private ArrayDeque<ByteBuffer> outboundQueue = new ArrayDeque<>(100);
    private StringBuffer pendingBuffer;

    private SelectionKey key;
    private String serverId;
    public PendingRequest(SelectionKey key){
        this.key = key;
        this.pendingBuffer = new StringBuffer(1024);
    }
    public void serverId(String serverId){ this.serverId = serverId;}
    public String serverId(){return this.serverId;}
    public synchronized void writeBuffer(ByteBuffer outbound){
        outboundQueue.offer(outbound);
        this.key.interestOps(SelectionKey.OP_WRITE);
        this.key.selector().wakeup();
    }
    public synchronized void writeIO(SocketChannel socketChannel) throws IOException{
        ByteBuffer buffer;
        do{
            buffer = outboundQueue.poll();
            if(buffer!=null){
                socketChannel.write(buffer);
            }
        }while (buffer!=null);
        this.key.interestOps(SelectionKey.OP_READ);
        this.key.selector().wakeup();
    }

    public synchronized void readIO(SocketChannel socketChannel) throws IOException{
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int num;
        do {
            buffer.clear();
            num = socketChannel.read(buffer);
            if(num>0){
                for(int i=0;i<num;i++){
                    char c = (char) buffer.get(i);
                    if(c == '|'){
                        inboundQueue.offerLast(pendingBuffer.toString());
                        pendingBuffer.setLength(0);
                    }
                    else{
                        pendingBuffer.append(c);
                    }
                }
            }else{
                throw new IOException("Channel closed");
            }
        }while (num==1024);
    }
    public synchronized List<String> readBuffer(){
        ArrayList<String> plist = new ArrayList<>();
        String pending;
        do{
            pending = inboundQueue.pollFirst();
            if(pending!=null){
                plist.add(pending);
            }
        }while (pending!=null);
        this.key.selector().wakeup();
        return plist;
    }
    public ByteBuffer toByteBuffer(Event responsiveEvent){
        byte[] cid = responsiveEvent.clientId().getBytes();
        byte[] lbl = responsiveEvent.label().getBytes();
        byte[] resp = responsiveEvent.payload();
        int bytes = resp.length+cid.length+lbl.length+2;
        ByteBuffer buffer = ByteBuffer.allocate(bytes);
        buffer.put(cid);
        buffer.put((byte)',');
        buffer.put(lbl);
        buffer.put(resp);
        buffer.put((byte)'|');
        buffer.flip();
        return buffer;
    }
    public void close(){
        try{
            this.key.cancel();
            this.key.channel().close();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
