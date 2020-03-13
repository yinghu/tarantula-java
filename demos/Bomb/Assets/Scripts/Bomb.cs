
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Bomb : MonoBehaviour
{
    
    void OnEnable(){
        BombRun brun = GetComponent<BombRun>();
        brun.OnSetupEvent += OnStart1;
        brun.OnSetupEvent += OnStart2;
    }
    void FixedUpdate(){
        transform.localScale = transform.localScale*(1+Time.fixedDeltaTime/100);  
    }
    private void OnStart1(){
        Debug.Log("starting 111");
    }
    private void OnStart2(){
        Debug.Log("starting 222");
    }
}
