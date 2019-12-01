package com.tarantula.platform.service.deployment;

import com.tarantula.Descriptor;
import com.tarantula.Module;

import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.JarFile;

public class DynamicModuleClassLoader extends ClassLoader{

    private HashMap<String,Class> _cached = new HashMap<>();
    private String codeUrl;
    private boolean loaded;
    CopyOnWriteArrayList<PlatformDeploymentServiceProvider.ModuleProxy> proxies = new CopyOnWriteArrayList();
    public DynamicModuleClassLoader(Descriptor descriptor){
        this.codeUrl = "jar:"+descriptor.codebase()+"/"+descriptor.moduleArtifact()+"-"+descriptor.moduleVersion()+".jar!/";
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
                        //ex.printStackTrace();
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
            throw new RuntimeException(name,ex);
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


}
