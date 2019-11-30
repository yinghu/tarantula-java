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
    void Start(){
        _active = false;
    }

   
    void Update(){
        if(_active){
            transform.Rotate(speed/2,speed,speed/3);
            //transform.Translate();
        }    
    }
    
    public void OnSpin(bool active){
        _active = active;
    }
}
