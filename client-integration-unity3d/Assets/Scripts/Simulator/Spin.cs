using System.Collections;
using System.Collections.Generic;
using System.Threading;
using UnityEngine;
using UnityEngine.SceneManagement;
using Tarantula.Networking;
public class Spin : MonoBehaviour
{
    public float speed = 3.0f;
   
    bool _active;
    bool _moving;
    
    private Vector3 started;
    private Vector3 target;
    
    void Start(){
        _active = true;
        started = transform.position;
        gameObject.GetComponent<Collider>().isTrigger = true;
        Rigidbody rib = gameObject.AddComponent<Rigidbody>();
        rib.collisionDetectionMode = CollisionDetectionMode.ContinuousSpeculative;
        rib.isKinematic = true;
    }

   
    void Update(){
        if(_active){
            transform.Rotate(speed/2,speed,speed/3);
        }
        if(_moving){
            transform.position = Vector3.MoveTowards(transform.position, target, speed * Time.deltaTime);        
        }
    }
    
    public void OnSpin(bool active){
        _active = active;
    }
    public void OnMove(Vector3 dest,float f){
        target = Camera.main.ScreenToWorldPoint(dest);
        target.z = 0;
        speed = f;
        _moving = true;
    }
    
    
    private void OnTriggerEnter(Collider hit){
        Debug.Log("Collisding with enter" + hit.gameObject.name);
        //speed =2*speed;
    }
    private void OnTriggerStay(Collider hit){
       //Debug.Log("Collisding with stay" + hit.gameObject.name); 
    }

    private void OnTriggerExit(Collider hit){
       Debug.Log("Collisding with exit" + hit.gameObject.name);
    }
}
