using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using BeardedManStudios.Forge.Networking.Generated;
using BeardedManStudios.Forge.Networking;
using BeardedManStudios.Forge.Networking.Unity;
public class BulletRun : MonoSmoke{
    private Rigidbody rigidBody;
    
    void Start(){
        rigidBody = GetComponent<Rigidbody>();     
    }
    
    public void OnRun(Vector3 target){
        Debug.Log("running");
        //rigidBody.velocity = transform.TransformDirection(target);
        
    }
    void FixedUpdate(){
        rigidBody.AddForce(Vector3.forward * Time.fixedDeltaTime * 100f);
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