using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using BeardedManStudios.Forge.Networking.Generated;
using BeardedManStudios.Forge.Networking;
using BeardedManStudios.Forge.Networking.Unity;
public class BumpRun : MonoBump
{
    
    public event OnRPCEvent OnMoveRPC; 
    public event OnRPCEvent OnLiveRPC; 
    public event OnRPCEvent OnDamageRPC;
    public event OnRPCEvent OnQuestRPC;
    public event OnRPCEvent OnRemoveRPC;
    
    public void OnRun(Vector3 dest,int index){
        if(networkObject==null){
            Debug.Log("no network object");
            return;
        }
        networkObject.SendRpc(RPC_ON_MOVE, Receivers.Owner,dest,index);
    }
	public void OnQuest(Vector3 position,string oid){
       networkObject.SendRpc(RPC_ON_QUEST, Receivers.Owner,position,oid); 
    }
    public void OnRemove(string oid){
       networkObject.SendRpc(RPC_ON_REMOVE, Receivers.Owner,oid); 
    }
    //RPC Callbacks 
    public override void OnMove(RpcArgs args){
        MainThreadManager.Run(() =>
		{
			OnMoveRPC?.Invoke(args);  
		});
    }
    public override void OnLive(RpcArgs args){
        MainThreadManager.Run(() =>
		{
            OnLiveRPC?.Invoke(args);    
        });   
    }
    public override void OnDamage(RpcArgs args){
        MainThreadManager.Run(() =>
		{
            OnDamageRPC?.Invoke(args);    
        });   
    }
    public override void OnQuest(RpcArgs args){
        MainThreadManager.Run(() =>
		{
            OnQuestRPC?.Invoke(args);   
        });   
    }
    public override void OnRemove(RpcArgs args){
        MainThreadManager.Run(() =>
		{
            OnRemoveRPC?.Invoke(args);   
        });   
    }
}

