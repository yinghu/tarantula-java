using System.Collections;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Threading;
using UnityEngine;
using UnityEngine.SceneManagement;
using TMPro;
public class Play : MonoBehaviour{
    
    private Integration INS;
    private TextMeshProUGUI pending;
   
    async void Start(){
        GameObject gp = GameObject.Find("/UI/Waiting"); 
        pending = gp.GetComponent<TextMeshProUGUI>();    
        INS = Integration.Instance;
        if(!INS.online){
            await INS.OnIndex(this);
            INS.online = await INS.OnDevice(this); 
            pending.SetText("CLICK TO PLAY");
        }
        else{
            pending.SetText("Sorry try again!");
        }
    }
    
    void Update(){
             
    }
    public async void Join(){
        if(INS.online){
            pending.SetText("Please Waiting ...");
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
