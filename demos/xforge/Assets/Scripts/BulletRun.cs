using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using BeardedManStudios.Forge.Networking.Generated;
using BeardedManStudios.Forge.Networking;
using BeardedManStudios.Forge.Networking.Unity;
public class BulletRun : MonoSmoke{
   
    public void OnRun(){
        Debug.Log("running");
        Rigidbody rigidBody = GetComponent<Rigidbody>();
        rigidBody.velocity = transform.TransformDirection(new Vector3(0, 0,20f));
    }
   
    void OnCollisionEnter(Collision collision){
        if(collision.gameObject.tag=="bullet"&&gameObject.tag=="bump"&&networkObject.IsOwner){
            networkObject.Destroy(); 
        } 
    }
    void OnCollisionStay(Collision collision){
        
    }
    void OnCollisionExit(Collision collision){
        
    }
}