using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Threading;
using Tarantula.Networking;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using TMPro;
using UnityEngine.SceneManagement;
public class Integration : MonoBehaviour{
    
    public GameEngineCluster integration;
   
    private bool pendingClick;
    
    
    private Descriptor game;
     
    private GameObject login;
    private GameObject pve;
    private GameObject pvp;

    private TextMeshProUGUI timer;
    
    private ForgeMenu forgeMenu;
    
    async void Start(){
        forgeMenu = GetComponent<ForgeMenu>();
        login = GameObject.Find("/UI/LOGIN");
        pve = GameObject.Find("/UI/PVE");
        pvp = GameObject.Find("/UI/PVP");
        GameObject tm = GameObject.Find("/UI/Timer");
        timer = tm.GetComponent<TextMeshProUGUI>();
        integration.OnInboundMessage += _OnStart; 
        integration.OnException += (ex,msg,code)=>{
            Debug.Log(msg);
            Debug.Log(ex);
            Debug.Log(code);
        };
        pendingClick = false;    
        if(!integration.online){
            await integration.Index(this);
            await integration.Device(this); 
            Debug.Log("Online->"+integration.online);
        }
        
    }
    void Update (){
        login.SetActive(!integration.online);  
        pve.SetActive(integration.online);  
        pvp.SetActive(integration.online);
    }
    
    public async void OnLogin(){
        if(!integration.online){
            await integration.Index(this);
            await integration.Device(this); 
            Debug.Log("Online->"+integration.online);
        }
    }
    public async void OnPVE(){
        if(pendingClick){
            return;
        }
        pendingClick = true;
        bool joined = await OnJoin(this,"RobotQuestPVE");     
        if(joined){
            //go to game play
            integration.OnInboundMessage -= _OnStart;
            SceneManager.LoadScene(SceneManager.GetActiveScene().buildIndex+1);
        }
    }
    public async void OnPVP(){
        if(pendingClick){
            return;
        }
        pendingClick = true;
        bool joined = await OnJoin(this,"RobotQuestPVP");
        if(joined){
            //go to game play
        }
    }
    public async void OnLogout(){
        if(integration.online){
            await integration.Logout(this);
            pendingClick = false;
        }
        Debug.Log(integration.online);     
    }
     private async Task<bool> OnJoin(MonoBehaviour caller,string gname){
        bool suc = await integration.OnLobby(caller,"robot-quest");
        if(!suc){
            return suc;
        }
        List<Descriptor> glist = integration.gameList();
        foreach(Descriptor desc in glist){
            Debug.Log("category->"+desc.category);
            if(desc.name.Equals(gname)){
                game = desc;
                break;
            }
        }
        return await integration.OnPlay(caller,"robot-quest/live",game,(jo)=>{
            //JToken occ = jo.SelectToken("gameObject.occupation");
            //int seatIndex = (int)occ.SelectToken("seatIndex");
            //int state = (int)occ.SelectToken("state");
            //arenaZone = (string)jo.SelectToken("gameObject.arenaZone");
            integration.game = game;
            if(!integration.udpEnabled&&jo.ContainsKey("connection")){
                Connection conn = jo.SelectToken("connection").ToObject<Connection>();
                Debug.Log(conn.host+"///"+conn.port);
                //forgeMenu.Host(conn.host,(ushort)conn.port);
            }
            if(forgeMenu.asServer){
                forgeMenu.Host("10.0.0.234",15937);
            }
            else{
                forgeMenu.Connect("10.0.0.234",15937);
            }
        });
    }
    async void _OnStart(InboundMessage msg){
        Debug.Log(msg.payload);
        //Debug.Log(msg.instanceId);        
        //Debug.Log(msg.payload);
        if(msg.query!=null&&msg.query.Equals("onStart")){
            //gameStage.OnStart(msg.payload);
            //Debug.Log("START=>>>"+msg.payload);
            //JObject jo = JObject.Parse(msg.payload);
            //integration.arena = (string)jo.SelectToken("arena");
            //integration.robotList = (JArray)jo.SelectToken("robotList");
            //matched = true;
        }
        else if(msg.query!=null&&msg.query.Equals("onTimer")){
            //gameStage.OnTimer(msg.payload);
        }
        else if(msg.query!=null&&msg.query.Equals("onMove")){
            //gameStage.OnMove(msg.payload);
        }
        else if(msg.query!=null&&msg.query.Equals("onEnd")){
            //gameStage.OnEnd();
            await integration.Logout(this);
            pendingClick = false;
            
        }
        else if(msg.query!=null&&msg.query.Equals("onQuest")){
            //gameStage.OnQuest(msg.payload);
        }
        else if(msg.query!=null&&msg.query.Equals("onRemove")){
            //gameStage.OnRemove(msg.payload);
        }
        else{
            //gameStage.OnMessage(msg);
        }
    } 
}

