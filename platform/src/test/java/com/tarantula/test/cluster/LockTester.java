package com.tarantula.test.cluster;


import com.tarantula.platform.util.RingBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
public class LockTester {

    public static void main(String[] args) throws Exception{
        System.out.println(String.format("%1$08x",111));
        RingBuffer<String> sb = new RingBuffer<>(new String[3]);
        for(int i=0;i<10;i++){
            if(!sb.push("i"+i)){
                sb.reset((t,limit)->{
                    String[] rs = new String[t.length*2];
                    int r=0;
                    for(int x=0;x<limit;x++){
                        //System.out.println(t[x]);
                        //if(!t[x].equals("i4")){
                            rs[r++]=t[x];
                        //}
                    }
                    rs[limit]="iop";
                    return rs;
                });
            }
            //System.out.println(sb.pull());
        }
        sb.reset((ca,limit)->{
            String[] cn =  new String[]{"2","3"};
            return cn;
        });
        for(int i=0;i<10;i++){
            System.out.println(sb.pop());
        }
        Path src = Paths.get("/mnt/tds/tarantula/data");
        for(String s : src.toFile().list()){
            System.out.println(s);
            //Files.move(Paths.get("/mnt/tds/tarantula/data/"+s),Paths.get("/mnt/tds/backup/tarantula/data/"+s));
        }

        Path dest = Paths.get("/mnt/tds/backup/tarantula/data");
        for(String s : dest.toFile().list()){
            Files.delete(Paths.get("/mnt/tds/backup/tarantula/data/"+s));
        }
        //Files.copy(src,dest);
    }
}
