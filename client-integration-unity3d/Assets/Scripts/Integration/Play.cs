using System.Collections;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Threading;
using UnityEngine;
using UnityEngine.SceneManagement;

public class Play : MonoBehaviour{
    
    private Integration INS;
    
   
    async void Start(){
        INS = Integration.Instance;
        if(!INS.online){
            await INS.OnIndex(this);
            INS.online = await INS.OnDevice(this); 
            Debug.Log("JOINED->"+INS.online);
        }
    }
    
    void Update(){
             
    }
    public async void Join(){
        if(INS.online){
            bool suc = await INS.OnJoin(this,(jo)=>{
                Debug.Log(">>"+(string)jo.SelectToken("index"));
            });
            if(suc){
                Debug.Log(INS.game.gameId);
                SceneManager.LoadScene("Simulator");
            }     
        }
    }
    
}
