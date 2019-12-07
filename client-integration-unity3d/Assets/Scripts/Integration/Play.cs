using System.Collections;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Threading;
using UnityEngine;
using UnityEngine.SceneManagement;
using TMPro;
using Tarantula.Networking;
public class Play : MonoBehaviour{
    
    private Integration INS;
    private TextMeshProUGUI pending;
    private bool matched; 
    private bool joined;
    async void Start(){
        GameObject gp = GameObject.Find("/UI/Waiting"); 
        pending = gp.GetComponent<TextMeshProUGUI>();    
        INS = Integration.Instance;
        Integration.OnMessage += _OnStart;
        if(!INS.online){
            await INS.OnIndex(this);
            await INS.OnDevice(this); 
            pending.SetText("CLICK TO PLAY ["+Integration.deviceId+"]");
        }
        else{
            pending.SetText("PLAY AGAIN");
        }
    }
    
    void Update(){
        if(matched){
            Integration.OnMessage -= _OnStart;  
            SceneManager.LoadScene("Simulator");
        }             
    }
    public async void Exit(){
        await INS.OnExit(this);
        pending.SetText("Exited Session,Please Open Again");
    }
    public async void Join(){
        if(INS.online){
            pending.SetText("Please Waiting ...");
            if(joined){
                await INS.OnLeave(this);
                joined = false;
                return;
            }
            joined = await INS.OnJoin(this,(jo)=>{
                pending.SetText("Pending on ["+(string)jo.SelectToken("index")+"]");
            });   
        }
        else{
            pending.SetText("Invalid Session! Please Open Again");
        }
    }
    void _OnStart(InboundMessage msg){
        if(msg.query!=null&&msg.query.Equals("onStart")){
            Debug.Log(msg.payload);
            matched = true;
        }            
    }
    
}
