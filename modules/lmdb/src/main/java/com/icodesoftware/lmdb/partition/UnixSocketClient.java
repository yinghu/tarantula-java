package com.icodesoftware.lmdb.partition;

import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

public class UnixSocketClient {

    public static void main(String[] arg) throws Exception{
        CountDownLatch countDownLatch = new CountDownLatch(5);
        for(int i=0;i<10;i++){
            String message = "hello :"+i;
            new Thread(()->{
                try{
                    new UnixSocketClient().send(message);
                    countDownLatch.countDown();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }).start();
        }
        countDownLatch.await();
    }

    public void send(String message){
        try {
            Path socketFile = Path.of(System.getProperty("user.home")).resolve("server.socket");
            UnixDomainSocketAddress address = UnixDomainSocketAddress.of(socketFile);
            ByteBuffer buffer = ByteBuffer.allocate(100);
            SocketChannel socketChannel = SocketChannel.open(StandardProtocolFamily.UNIX);
            socketChannel.connect(address);
            buffer.put(message.getBytes());
            buffer.flip();
            socketChannel.write(buffer);
            buffer.rewind();
            int num = socketChannel.read(buffer);
            if (num > 0) {
                buffer.flip();
                System.out.println(new String(buffer.array()));
            }

            socketChannel.close();
        }catch (Exception ex){

        }
    }



}
