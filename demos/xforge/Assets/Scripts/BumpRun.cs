using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using BeardedManStudios.Forge.Networking.Generated;
using BeardedManStudios.Forge.Networking;
using BeardedManStudios.Forge.Networking.Unity;
public class BumpRun : MonoSmoke{
    
    void OnCollisionEnter(Collision collision){
        if(collision.gameObject.tag=="robot"&&gameObject.tag == "bump"&&networkObject.IsOwner){
            networkObject.Destroy(); 
        } 
    }
    void OnCollisionStay(Collision collision){
        
    }
    void OnCollisionExit(Collision collision){
        
    }
}
