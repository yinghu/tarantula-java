using System.Collections;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Threading;
using UnityEngine;
using UnityEngine.SceneManagement;
using TMPro;
using Tarantula.Networking;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
public class Play : MonoBehaviour{
    
    
    private Integration INS;
    private TextMeshProUGUI pending;
    private bool matched; 
    private bool joined;
    
    void Awake(){
        Integration.OnMessage += _OnStart; 
    }
    async void Start(){
        GameObject gp = GameObject.Find("/UI/Waiting"); 
        pending = gp.GetComponent<TextMeshProUGUI>();    
        INS = Integration.Instance;
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
            SceneManager.LoadScene("Simulator");
        }             
    }
     void OnDestroy(){
        Integration.OnMessage -= _OnStart;
        Debug.Log("removed START handler");
    }
    public async void Exit(){
        await INS.OnExit(this);
        pending.SetText("Exited Session,Please Open Again");
    }
    public async void JoinPVP(){
        if(INS.online){
            pending.SetText("Please Waiting ...");
            if(joined){
                await INS.OnLeave(this);
                joined = false;
                pending.SetText("PLAY AGAIN");
                return;
            }
            joined = await INS.OnJoin(this,"RobotQuestPVP");
            if(joined){
                pending.SetText("PENDING ON ["+INS.arenaZone+"]");
            }
            else{
                pending.SetText("OPPS SOMETHING WRONG");
            }
        }
        else{
            pending.SetText("Invalid Session! Please Open Again");
        }
    }
    public async void JoinPVE(){
        if(INS.online){
            pending.SetText("Please Waiting ...");
            if(joined){
                await INS.OnLeave(this);
                joined = false;
                pending.SetText("PLAY AGAIN");
                return;
            }
            joined = await INS.OnJoin(this,"RobotQuestPVE");
            if(joined){
                pending.SetText("PENDING ON ["+INS.arenaZone+"]");
            }
            else{
                pending.SetText("OPPS SOMETHING WRONG");
            }
        }
        else{
            pending.SetText("Invalid Session! Please Open Again");
        }
    }
    void _OnStart(InboundMessage msg){
        if(msg.query!=null&&msg.query.Equals("onStart")){
            Debug.Log("START=>>>"+msg.payload);
            JObject jo = JObject.Parse(msg.payload);
            INS.arena = (string)jo.SelectToken("arena");
            matched = true;
        }            
    }
    
}
