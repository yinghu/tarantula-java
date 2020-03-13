using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using BeardedManStudios.Forge.Networking.Generated;
using BeardedManStudios.Forge.Networking;
using BeardedManStudios.Forge.Networking.Unity;

public class MonoBomb : BombBehavior{
    
    public delegate void OnRPCEvent(RpcArgs args);
    public delegate void OnSetup();
    public event OnSetup OnSetupEvent;
    
    
    private int zone;
    private string playerId;
    protected override void NetworkStart(){
		base.NetworkStart();
        networkObject.onDestroy += _OnDestroy;
        Debug.Log("network started bomb");
        OnSetupEvent?.Invoke();
    }
    void Update(){
        if (networkObject == null){
			return;
        }
		if(!networkObject.IsOwner){
			transform.position = networkObject.position;
			transform.rotation = networkObject.rotation;
            transform.localScale = networkObject.scale;
			return;
		}
        networkObject.position = transform.position;
		networkObject.rotation = transform.rotation;  
        networkObject.scale = transform.localScale;
    }
    void _OnDestroy(NetWorker sender){
        Debug.Log("network Killing from ->"+sender);    
    }
    void OnDestroy(){
        Debug.Log("Killing->"+zone+"///"+playerId);    
    }
    public void Setup(int zone,string playerId){
        if(networkObject.IsOwner){
            networkObject.SendRpc(RPC_ON_START, Receivers.All,zone,playerId); 
        }
    }
    public override void OnExplode(RpcArgs args){}
    
    public override void OnStart(RpcArgs args){
        zone = args.GetNext<int>();
        playerId = args.GetNext<string>();
    }
}
