using System.Collections;
using System.Collections.Generic;
using System;
using UnityEngine;
using BeardedManStudios.Forge.Networking.Generated;
using BeardedManStudios.Forge.Networking;
using BeardedManStudios.Forge.Networking.Unity;
public class BumpRun : MonoBump
{
    
    //public event OnRPCEvent OnLiveRPC; 
    //public event OnRPCEvent OnDamageRPC;
    //public event OnRPCEvent OnQuestRPC;
    //public event OnRPCEvent OnRemoveRPC;
    
    Dictionary<byte,Action<RpcArgs>> _rpc = new Dictionary<byte,Action<RpcArgs>>();
    
    public void OnRun(byte cid,params object[] args){
        if(networkObject==null){
            return;
        }
        networkObject.SendRpc(cid,Receivers.Owner,args);
    }
	public void OnQuest(Vector3 position,string oid){
       networkObject.SendRpc(RPC_ON_QUEST, Receivers.Owner,position,oid); 
    }
    public void OnRemove(string oid){
       networkObject.SendRpc(RPC_ON_REMOVE, Receivers.Owner,oid); 
    }
    public void RegisterRpcCallback(byte mid,Action<RpcArgs> callback){
        _rpc.Add(mid,callback);
    }
    //RPC Callbacks 
    public override void OnMove(RpcArgs args){
        MainThreadManager.Run(() =>
		{
			_rpc[RPC_ON_MOVE].Invoke(args);
		});
    }
    public override void OnLive(RpcArgs args){
        MainThreadManager.Run(() =>
		{
            //OnLiveRPC?.Invoke(args);    
        });   
    }
    public override void OnDamage(RpcArgs args){
        MainThreadManager.Run(() =>
		{
            //OnDamageRPC?.Invoke(args);    
        });   
    }
    public override void OnQuest(RpcArgs args){
        MainThreadManager.Run(() =>
		{
            Debug.Log("ReadIndex->"+args.ReadIndex);
            //OnQuestRPC?.Invoke(args);   
        });   
    }
    public override void OnRemove(RpcArgs args){
        MainThreadManager.Run(() =>
		{
            //OnRemoveRPC?.Invoke(args);   
        });   
    }
}

