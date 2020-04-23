using System.Collections;
using System.Collections.Generic;
using System;
using UnityEngine;
using BeardedManStudios.Forge.Networking.Generated;
using BeardedManStudios.Forge.Networking;
using BeardedManStudios.Forge.Networking.Unity;
public class BumpRun : MonoBump
{
    
    
	public void OnDamage(){
       if(networkObject.IsOwner){
            Debug.Log("Collision on owner");  
       }
       //networkObject.SendRpc(RPC_ON_QUEST, Receivers.Owner,position,oid); 
    } 
}

