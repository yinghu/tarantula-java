using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using BeardedManStudios.Forge.Networking.Generated;
using BeardedManStudios.Forge.Networking;
using BeardedManStudios.Forge.Networking.Unity;

public class MonoBump : BumpBehavior{
    
    void Start(){
        
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
    void OnDestroy(){
        Debug.Log("Killing->"+gameObject.name);    
    }
    public override void OnMove(RpcArgs args){}
    //public override void MoveDown(RpcArgs args){}
    //public override void Move(RpcArgs args){}
    //public override void Validate(RpcArgs args){}
}
