using System.Collections;
using System.Collections.Generic;
using System.Threading;
using UnityEngine;
using UnityEngine.SceneManagement;
using Tarantula.Networking;
public class Spin : MonoBehaviour
{
    public float speed = 5.0f;
   
    bool _active;
    private Vector3 started;
    void Start(){
        _active = true;
        started = transform.position;
    }

   
    void Update(){
        if(_active){
            transform.Rotate(speed/2,speed,speed/3);
        }    
    }
    
    public void OnSpin(bool active){
        _active = active;
    }
    public void OnMove(Vector3 dest){
        Vector3 target = Camera.main.ScreenToWorldPoint(dest);
        target.z = 0;
        transform.position = Vector3.Lerp(started, target, speed);
        Debug.Log(target);
    }
}
