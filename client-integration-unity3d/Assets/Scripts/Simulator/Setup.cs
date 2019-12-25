using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Setup : MonoBehaviour{
    
    private Rigidbody _rig;
    
    void Start() {
        _rig = gameObject.GetComponent<Rigidbody>();   
    }

    void Update()
    {
        
    }
    public void _Setup(){
        Debug.Log("setup ...");
        gameObject.name = "gp1000";
    }
    private  void OnTriggerEnter(Collider hit){
        if(gameObject.tag == "gig" && hit.gameObject.tag == "robot"){
            Debug.Log(hit.gameObject.name+" eating the gig...");
            Destroy(gameObject);
        }
        else if(gameObject.tag == "gig" && hit.gameObject.tag == "fws"){
            _rig.useGravity = false;
            _rig.isKinematic = true;
            //_rig.detectCollisions = false;
            Debug.Log(gameObject.name+" lading the gig...");
        }
        else if(gameObject.tag == "robot" && hit.gameObject.tag == "fws"){
            //_rig.useGravity = true;
            if(!gameObject.GetComponent<CharacterController>().enabled){
                _rig.isKinematic = true;
                gameObject.GetComponent<CharacterController>().enabled = true;
                //_rig.detectCollisions = true;
                Debug.Log(gameObject.name+" lading the robot...");
            }
        }
    }
}
