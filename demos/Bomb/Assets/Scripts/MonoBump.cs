using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using BeardedManStudios.Forge.Networking.Generated;
using BeardedManStudios.Forge.Networking;
using BeardedManStudios.Forge.Networking.Unity;

public class MonoBump : BumpBehavior{
    
    public delegate void OnRPCEvent(RpcArgs args);
    
    protected override void NetworkStart(){
		base.NetworkStart();
        networkObject.onDestroy += _OnDestroy;
        //Debug.Log("network started=>MonoBump");
    }
    void Update(){
        if (networkObject == null){
			return;
        }
		if(!networkObject.IsOwner){
			transform.position = networkObject.position;
			transform.rotation = networkObject.rotation;
			return;
		}
        networkObject.position = transform.position;
		networkObject.rotation = transform.rotation;    
    }
    void _OnDestroy(NetWorker sender){
        //Debug.Log("network Killing->"+sender);    
    }
    void OnDestroy(){
        //Debug.Log("Killing->"+gameObject.name);    
    }
    public override void OnMove(RpcArgs args){}
    public override void OnLive(RpcArgs args){}
    public override void OnDamage(RpcArgs args){}
    public override void OnQuest(RpcArgs args){}
    public override void OnRemove(RpcArgs args){}
}
