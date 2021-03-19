package com.tarantula.platform.service.deployment;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.service.ModuleClassLoader;

import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.JarFile;

public class DynamicModuleClassLoader extends ModuleClassLoader {

    private TarantulaLogger log = JDKLogger.getLogger(DynamicModuleClassLoader.class);

    private HashMap<String,Class> _cached = new HashMap<>();
    private String codeUrl;
    private boolean loaded;

    private boolean resetEnabled;

    CopyOnWriteArrayList<PlatformDeploymentServiceProvider.ModuleProxy> proxies = new CopyOnWriteArrayList();
    public DynamicModuleClassLoader(Descriptor descriptor){
        toJarUrl(descriptor);
    }
    synchronized void _load(){
        JarFile _jar=null;
        try{
            JarURLConnection jarURLConnection = (JarURLConnection) new URL(codeUrl).openConnection();
            _jar = jarURLConnection.getJarFile();
            final  JarFile jf = _jar;
            _jar.stream().forEach((c)->{
                if(c.getName().endsWith(".class")){
                    try{
                        String jn = c.getName();
                        int  last = jn.lastIndexOf(".");
                        String cn = jn.substring(0,last).replaceAll("/","\\.");
                        InputStream in = jf.getInputStream(c);
                        byte[] cdata = new byte[in.available()];
                        in.read(cdata);
                        if(!_cached.containsKey(cn)){//skip preloaded static classes
                            Class result = defineClass(cn, cdata, 0, cdata.length);
                            super.resolveClass(result);
                            _cached.put(cn,result);
                        }
                    }catch (Exception ex){
                        log.error("error",ex);
                        throw new RuntimeException(ex);
                    }
                }
            });
        }catch (Exception ex){
            ex.printStackTrace();
        }
        finally {
            if(_jar!=null){
                try{_jar.close();}catch (Exception ex){}
            }
            loaded = true;
        }
    }

    synchronized void _clear(){
        proxies.clear();
        _cached.clear();
    }
    @Override
    public synchronized Class findClass(String name) throws ClassNotFoundException{
        return loadClass(name,true);
    }

    @Override
    public synchronized Class loadClass(String name) throws ClassNotFoundException {
        return loadClass(name,false);
    }
    @Override
    public synchronized Class loadClass(String name,boolean resolveIt) throws ClassNotFoundException {
        //1. get from local cache
        //2. get from system class if it is
        //3. get from .class file
        //4. cache it locally
        Class result;
        result = _cached.get(name);
        if(result!=null){
            return result;
        }
        try{
            return super.findSystemClass(name);
        }catch (ClassNotFoundException cex){
            if(name.startsWith("java.")){//filter illegal named classes such as java.
                throw cex;
            }
        }
        byte[] cdata = loadClassFromCodeBase(name);
        result = defineClass(name, cdata, 0, cdata.length);
        if(resolveIt){
            super.resolveClass(result);
        }
        _cached.put(name,result);
        return result;
    }
    public synchronized Module newModule(String name){
        try{
            return (Module)loadClass(name,true).getConstructor().newInstance();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public synchronized void loadResource(String name,Module.OnResource onResource){
        try{
            JarURLConnection jarURLConnection = (JarURLConnection) new URL(codeUrl).openConnection();
            JarFile jar = null;
            try{
                jar = jarURLConnection.getJarFile();
                InputStream in = jar.getInputStream(jar.getJarEntry(name));
                onResource.on(in);
                in.close();
            }catch (Exception iex){
                throw new RuntimeException(name+" not existed",iex);
            }
            finally {
                if(jar!=null){
                    jar.close();
                }
            }
        }catch (Exception ex){
            throw new RuntimeException("module not existed",ex);
        }
    }
    private byte[] loadClassFromCodeBase(String name) throws ClassNotFoundException{
        try{
            JarURLConnection jarURLConnection = (JarURLConnection) new URL(codeUrl).openConnection();
            JarFile jar =null;
            try{
                jar = jarURLConnection.getJarFile();
                String cn = name.replaceAll("\\.","/")+".class";
                InputStream in = jar.getInputStream(jar.getJarEntry(cn));
                byte[] cdata = new byte[in.available()];
                in.read(cdata);
                in.close();
                return cdata;
            }catch (Exception iex){
                throw new ClassNotFoundException(name,iex);
            }
            finally {
                if(loaded&&jar!=null){jar.close();}
            }
        }
        catch (Exception ex){
            throw new ClassNotFoundException(name,ex);
        }
    }

    public synchronized void reset(boolean resetEnabled){
        this.resetEnabled = resetEnabled;
    }
    @Override
    public synchronized void reset(Instrumentation instrumentation) {
        if(this.resetEnabled){
            //profiling the class loader classes
            log.warn("profiling class loader ->"+toString());
            Arrays.stream(instrumentation.getAllLoadedClasses()).forEach((c)->{
                if(c.getName().startsWith("com.perfectday.game")){
                    log.warn(c.getName()+">>>>"+c.getClassLoader().toString());
                }
            });
        }
        this.resetEnabled=(false);
    }
    private void toJarUrl(Descriptor descriptor){
        this.codeUrl = "jar:"+descriptor.codebase()+"/"+descriptor.moduleArtifact()+"-"+descriptor.moduleVersion()+".jar!/";
    }
}
