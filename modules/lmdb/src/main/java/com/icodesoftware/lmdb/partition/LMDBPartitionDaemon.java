package com.icodesoftware.lmdb.partition;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;

public class LMDBPartitionDaemon {

    private static boolean running = true;
    private static ServerSocketChannel serverSocketChannel;
    private static TarantulaLogger logger = JDKLogger.getLogger(LMDBPartitionDaemon.class);

    private static LMDBPartitionProvider lmdbPartitionProvider;

    public static void main(String[] args) throws Exception{
        lmdbPartitionProvider = LMDBPartitionProvider.create(false);
        LMDBPartitionDaemon.start();
        Thread shutdown = new Thread(()->{
            try{
                LMDBPartitionDaemon.shutdown();
            }catch(Exception ex){
                ex.printStackTrace();
            }
        },"lmdb-partition-daemon-shutdown-hook");
        Runtime.getRuntime().addShutdownHook(shutdown);
    }

    public static void start() throws Exception{
        Path socketFile = Path.of(System.getProperty("user.home")).resolve("server.socket");
        Files.deleteIfExists(socketFile);
        UnixDomainSocketAddress address = UnixDomainSocketAddress.of(socketFile);
        serverSocketChannel = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
        serverSocketChannel.bind(address);
        ByteBuffer buffer = ByteBuffer.allocate(100);
        new Thread(()->{
            try{
                logger.warn("Unit daemon is listening on ["+socketFile+"]");
                while (running){
                    buffer.clear();
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    int num = socketChannel.read(buffer);
                    if(num>0){
                        buffer.flip();
                        logger.warn(new String(buffer.array()));
                        buffer.rewind();
                        socketChannel.write(buffer);
                    }
                    Thread.sleep(100);
                }
                logger.warn("Unix socket daemon is closed");
            }catch (Exception ex){
                logger.error("Error on Socket",ex);
            }
        },"lmdb-partition-daemon").start();
    }

    public static void shutdown() throws Exception{
        logger.warn("Unix socket daemon is going to shutdown");
        running = false;
        serverSocketChannel.close();
    }

    private void save(String dbiName,ByteBuffer key,ByteBuffer value){
        LMDBPartition partition = lmdbPartitionProvider.partition(key);
        try{
            if(partition.put(dbiName,key,value)){
                lmdbPartitionProvider.onPut(partition,key.rewind());
            }
        }catch (Throwable throwable){

        }
    }


}
