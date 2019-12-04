using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;

public class Simulator : MonoBehaviour
{
    private Integration INS;
    public Spin spin;
    void Start()
    {
        INS = Integration.Instance;
        Integration.OnMessage +=(msg)=>{
            if(msg.instanceId!=null&&msg.instanceId.Equals(INS.game.gameId)){
                //Debug.Log(msg.payload);
            }
        };
    }

    // Update is called once per frame
    void Update()
    {
        if (Input.GetMouseButtonDown(0)) {
             Vector3 target = Input.mousePosition;
             Debug.Log(target);
             GameObject go = GameObject.Find("/View/Spin1");
             if(go!=null){
                go.name = "popo";
                Spin spin = go.GetComponent<Spin>();
                spin.OnSpin(false);
                Debug.Log(go.name);
             }
             spin.OnMove(target);
        }   
    }
    
    public async void Back(){
        bool suc = await INS.OnLeave(this); 
        if(suc){
            SceneManager.LoadScene("Integration");
        }     
    }
}
