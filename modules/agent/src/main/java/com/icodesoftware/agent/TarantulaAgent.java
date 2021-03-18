package com.icodesoftware.agent;
import com.sun.tools.attach.VirtualMachine;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.jar.JarFile;

public class TarantulaAgent {

    public static void premain(String agentArgs, Instrumentation inst){

    }
    public static void agentmain(String agentArgs, Instrumentation inst) {
        System.out.println("agent main->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+agentArgs);
        try{
            String codeUrl = "jar:"+agentArgs+"!/";
            JarURLConnection jarURLConnection = (JarURLConnection) new URL(codeUrl).openConnection();
            JarFile _jar = jarURLConnection.getJarFile();
            //final JarFile jf = _jar;
            _jar.stream().forEach((c)->{
                //if(c.getName().endsWith(".class")){
                String _name = c.getName();
                //System.out.println("new->>>>"+_name+"//");
                if(_name.endsWith(".class")){
                    int  last = _name.lastIndexOf(".");
                    String cn = _name.substring(0,last).replaceAll("/","\\.");
                    try{
                        System.out.println("CNAME->"+cn);
                        Class<?> cls = Class.forName(cn);
                        System.out.println(cls.getClassLoader().toString());
                    }catch (ClassNotFoundException cex){
                        cex.printStackTrace();
                    }
                }
                //}
            });
            Arrays.stream(inst.getAllLoadedClasses()).forEach((a)->{
                if(a.getName().startsWith("com.perfectday.game")){
                    System.out.println(a.getName());
                    System.out.println(a.getClassLoader().toString());
                }
            });
        }catch (Exception exception){
            exception.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception{
        VirtualMachine vm = VirtualMachine.attach(args[0]);
        String cd = Paths.get(".").toAbsolutePath().normalize().toString();
        File agent = new File(cd+"/gec-agent-1.0.jar");
        vm.loadAgent(agent.getAbsolutePath(),args[1]);
        vm.detach();
    }
}
