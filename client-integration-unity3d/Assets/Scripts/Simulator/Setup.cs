using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Setup : MonoBehaviour{
    
    private Rigidbody _rig;
    
    void Start() {
    
    }

    void Update(){
        
    }
    public void _Setup(){
        Debug.Log("Setup ["+gameObject.name+"]");
        _rig = gameObject.GetComponent<Rigidbody>();
    }
    private  void OnTriggerEnter(Collider hit){
        if(gameObject.tag == "gig" && hit.gameObject.tag == "robot"){
            Debug.Log(hit.gameObject.name+" eating the gig...");
            Destroy(gameObject);
        }
        else if(gameObject.tag == "gig" && hit.gameObject.tag == "fws"){
            _rig.isKinematic = true;
            Debug.Log(gameObject.name+" lading the gig...");
        }
        else if(gameObject.tag == "robot" && hit.gameObject.tag == "fws"){
            if(!gameObject.GetComponent<CharacterController>().enabled){
                _rig.isKinematic = true;
                gameObject.GetComponent<CharacterController>().enabled = true;
                Debug.Log(gameObject.name+" lading the robot...");
            }
        }
    }
}
