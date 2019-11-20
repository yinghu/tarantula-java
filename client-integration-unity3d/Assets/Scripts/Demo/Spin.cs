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
    void Start(){
        _active = false;
    }

   
    void Update(){
        if(_active){
            transform.Rotate(10,speed,10);
        }    
    }
    
    public void OnSpin(bool active){
        _active = active;
    }
}
