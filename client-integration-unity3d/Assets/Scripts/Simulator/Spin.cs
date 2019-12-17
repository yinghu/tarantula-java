using System.Collections;
using System.Collections.Generic;
using System.Threading;
using UnityEngine;
using UnityEngine.SceneManagement;
using Tarantula.Networking;
public class Spin : MonoBehaviour
{
    public float speed = 3.0f;
   
    private bool _active;
    private bool _moving;
    private Integration INS; 
    private Vector3 started;
    private Vector3 target;
    
    void Start(){
        INS = Integration.Instance;
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
    
    
    private async void OnTriggerEnter(Collider hit){
        if(hit.gameObject.tag =="gig" && gameObject.tag == "robot"){
            Debug.Log("KILLING IT->" + hit.gameObject.name+"/"+hit.gameObject.tag);   
            Payload payload = new Payload();
            payload.headers = new Header[]{new Header("accessId","e"),new Header("accessKey",hit.gameObject.name)};
            await INS.OnQuest(payload);
        }
    }
    private void OnTriggerStay(Collider hit){
       //Debug.Log("Collisding with stay" + hit.gameObject.name); 
    }

    private void OnTriggerExit(Collider hit){
       //Debug.Log("Collisding with exit" + hit.gameObject.name);
    }
}
