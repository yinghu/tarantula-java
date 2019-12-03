using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;

public class ObjectCreator : MonoBehaviour
{
    private Integration INS;
    void Start()
    {
        INS = Integration.Instance;
    }

    // Update is called once per frame
    void Update()
    {
        
    }
    
    public async void Back(){
        bool suc = await INS.OnLogout(this); 
        if(suc){
            SceneManager.LoadScene("Integration");
        }     
    }
}
