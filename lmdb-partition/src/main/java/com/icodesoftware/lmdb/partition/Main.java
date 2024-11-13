package com.icodesoftware.bootstrap;


public class Main {


    public static void main(String[] args) throws Exception{
        Thread thread = new Thread(()->{
            try{
                Thread.sleep(5000);
                ProcessBuilder processBuilder = new ProcessBuilder(command());
                processBuilder.inheritIO();
                processBuilder.start();
            }
            catch (Exception ex){

            }
        });
        thread.start();
        thread.join();
    }
    private static String[] command(){
        return new String[]{"cmd.exe","/c","partition.bat"};
        //return new String[]{"/bin/sh","-c", "tarantula.sh"};
    }
}
