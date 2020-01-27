using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using BeardedManStudios.Forge.Networking.Generated;
using BeardedManStudios.Forge.Networking;
using BeardedManStudios.Forge.Networking.Unity;
public class BumpRun : SmokeBehavior
{
    // Start is called before the first frame update
    void Start(){
        //Debug.Log("nid->"+networkObject.NetworkId);//
        //Debug.Log("uid->"+networkObject.UniqueIdentity);//type id
    }
    private void Update()
	{
		// If unity's Update() runs, before the object is
		// instantiated in the network, then simply don't
		// continue, otherwise a bug/error will happen.
		// 
		// Unity's Update() running, before this object is instantiated
		// on the network is **very** rare, but better be safe 100%
		if (networkObject == null)
			return;
		
		// If we are not the owner of this network object then we should
		// move this cube to the position/rotation dictated by the owner
		if (!networkObject.IsOwner)
		{
			transform.position = networkObject.position;
			transform.rotation = networkObject.rotation;
			return;
		}
        networkObject.position = transform.position;
		networkObject.rotation = transform.rotation;
		// Let the owner move the cube around with the arrow keys
		// Note: Forge Networking takes care of only sending the delta, so there
		// is no need for you to do that manually
	}
    public override void MoveUp(RpcArgs args){}
    public override void MoveDown(RpcArgs args){}
    public override void Move(RpcArgs args){}
    void OnCollisionEnter(Collision collision){
        if(collision.gameObject.tag=="robot"&&gameObject.tag == "bump"&&networkObject.IsOwner){
            //networkObject.Destroy(); 
        } 
    }
    void OnCollisionStay(Collision collision){
        
    }
    void OnCollisionExit(Collision collision){
        
    }
}
