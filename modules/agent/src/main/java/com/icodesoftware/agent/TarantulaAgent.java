package com.icodesoftware.agent;
import com.sun.tools.attach.VirtualMachine;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.service.ModuleClassLoader;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.nio.file.Paths;

public class TarantulaAgent {

    public static void premain(String agentArgs, Instrumentation inst){

    }
    public static void agentmain(String agentArgs, Instrumentation inst) {
        TarantulaContext tarantulaContext = TarantulaContext.getInstance();
        ModuleClassLoader classLoader = tarantulaContext.moduleClassLoader(agentArgs);
        classLoader.reset(inst);
    }

    public static void main(String[] args) throws Exception{
        VirtualMachine vm = VirtualMachine.attach(args[0]);
        String cd = Paths.get(".").toAbsolutePath().normalize().toString();
        File agent = new File(cd+"/gec-agent-1.0.jar");
        vm.loadAgent(agent.getAbsolutePath(),args[1]);
        vm.detach();
    }
}
