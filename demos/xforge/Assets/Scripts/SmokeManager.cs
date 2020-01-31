using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using BeardedManStudios.Forge.Networking;
using BeardedManStudios.Forge.Networking.Unity;
using BeardedManStudios.Forge.Networking.Lobby;
using BeardedManStudios.SimpleJSON;

public class SmokeManager : MonoBehaviour{
    
    public bool runAsServer;
    
    public string host;
    public ushort port;
    
    //private NetworkManager mgr = null;
    private NetWorker netWorker;
    
    void Start(){
        Rpc.MainThreadRunner = MainThreadManager.Instance;
        if(runAsServer){
            RunAsServer();
        }
        else{
            RunAsClinet(); 
        }
    }

    
    void Update(){
        
    }
    void RunAsServer(){
        //use UDPServer
        UDPServer s = new UDPServer(3);
        s.Connect(host,port);
        NetworkManager.Instance.Initialize(s);
    }
    void RunAsClinet(){
        //use UDPClient
        UDPClient c = new UDPClient();
        c.Connect(host, port);
        NetworkManager.Instance.Initialize(c);
    }
}
