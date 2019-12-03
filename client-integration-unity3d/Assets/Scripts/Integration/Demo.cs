using System.Collections;
using System.Collections.Generic;
using System.Threading;
using UnityEngine;
using UnityEngine.SceneManagement;
using Tarantula.Networking;
using TMPro;
public class Demo : MonoBehaviour{
    
    private Integration INS;
    void Start(){
        INS = Integration.Instance;
    }
    
    void Update()
    {
        
    }
    
    public async void Play(){
        bool suc = await INS.OnDevice(this); 
        if(suc){
           SceneManager.LoadScene("Simulator");     
        }
    }
    public async void Logout(){
        bool suc = await INS.OnLogout(this); 
        if(suc){
           //SceneManager.LoadScene("Simulator");     
        }
    }
    
    
}
