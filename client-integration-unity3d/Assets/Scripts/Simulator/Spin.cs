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
}
