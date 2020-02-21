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
using BeardedManStudios.Forge.Networking.Generated;
using BeardedManStudios.Forge.Networking;
using BeardedManStudios.Forge.Networking.Unity;
public class Integration : MonoBehaviour{
    
    public GameEngineCluster integration;
    public bool headless;
    private bool pendingClick;
    private bool matched;
    
    private Descriptor game;
     
    private GameObject login;
    private GameObject pve;
    private GameObject pvp;

    private TextMeshProUGUI timer;
    private TextMeshProUGUI message;
    private ForgeMenu forgeMenu;
    private bool inGame;
    private bool connected;
    async void Start(){
        forgeMenu = GetComponent<ForgeMenu>();
        if(headless){
            Rpc.MainThreadRunner = MainThreadManager.Instance; 
            forgeMenu.Host("10.0.0.234",15937);
            Debug.Log("Running headless mode");
            SceneManager.LoadScene(SceneManager.GetActiveScene().buildIndex+1);
            return;
        }
        login = GameObject.Find("/UI/LOGIN");
        pve = GameObject.Find("/UI/PVE");
        pvp = GameObject.Find("/UI/PVP");
        GameObject tm = GameObject.Find("/UI/Timer");
        timer = tm.GetComponent<TextMeshProUGUI>();
        GameObject tms = GameObject.Find("/UI/Message");
        message = tms.GetComponent<TextMeshProUGUI>();
        integration.OnInboundMessage += _OnStart; 
        integration.OnException += (ex,msg,code)=>{
            Debug.Log(msg);
            Debug.Log(ex);
            Debug.Log(code);
        };
        pendingClick = false;
        matched = false;
        connected = false;
        if(!integration.online){
            await integration.Index(this);
            await integration.Device(this); 
            Debug.Log("Online->"+integration.online);
        }
        
    }
    void _Forge(){
        if(connected){
            return;
        }
        connected = true;
        Rpc.MainThreadRunner = MainThreadManager.Instance; 
        Connection xconn = integration.room.connection;
        if(forgeMenu.asServer){
            forgeMenu.Host(xconn.host,(ushort)xconn.port);
        }
        else{
            forgeMenu.Connect(xconn.host,(ushort)xconn.port);
        }
        //SceneManager.LoadSceneAsync(SceneManager.GetActiveScene().buildIndex+1);
    }
    void Update (){
        if(headless){
            return;
        }
        login.SetActive(!integration.online);  
        pve.SetActive(!pendingClick&&integration.online);  
        pvp.SetActive(!pendingClick&&integration.online);
        if(matched){        
            Debug.Log("Going to arena->"+integration.room.arena);
            integration.OnInboundMessage -= _OnStart;
            SceneManager.LoadScene(SceneManager.GetActiveScene().buildIndex+1);
        }
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
        if(!joined){
            message.SetText(integration.message);
            //go to game play
            //integration.OnInboundMessage -= _OnStart;
            //SceneManager.LoadScene(SceneManager.GetActiveScene().buildIndex+1);
        }
    }
    public async void OnPVP(){
        if(pendingClick){
            return;
        }
        pendingClick = true;
        bool joined = await OnJoin(this,"RobotQuestPVP");
        if(!joined){
            message.SetText(integration.message);
        }
    }
    public async void OnLogout(){
        if(integration.online){
            if(inGame){
                Payload payload = new Payload();
                payload.command = "onLeave";
                //integration.OnInboundMessage -= _OnStart;
                await  integration.OnInstance(this,integration.game,payload,(ps)=>{
                    Debug.Log(ps);
                    if(connected){
                        NetworkManager.Instance.Disconnect();
                    }
                    integration.CloseUDP();
                });
            }
            await integration.Logout(this);
            pendingClick = false;
            matched = false;
            inGame = false;
            connected = false;
            timer.SetText("00:00");
            message.SetText("Play Again");
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
            Occupation occ = jo.SelectToken("gameObject.occupation").ToObject<Occupation>();
            string arenaZone = (string)jo.SelectToken("gameObject.arenaZone");
            Room room = new Room((int)jo.SelectToken("gameObject.capacity"),arenaZone);
            room.connection = new Connection();
            room.occupations[occ.seatIndex]=occ;
            room.state = occ.state;
            room.seatIndex = occ.seatIndex;
            room.totalJoined = occ.totalJoined;
            integration.game = game;
            if(!integration.udpEnabled&&jo.ContainsKey("connection")){
                Connection conn = jo.SelectToken("connection").ToObject<Connection>();
                Debug.Log(conn.host+"///"+conn.port);
                room.connection = conn;
            }
            else{
                room.connection.host = "10.0.0.234";
                room.connection.port = 15937;
            }
            integration.room = room;
            _Forge();
            message.SetText("Players["+room.totalJoined+"/"+room.capacity+"]");
            inGame = true;
        });
    }
    void _OnStart(InboundMessage msg){
        if(msg.query!=null&&msg.query.Equals("onStart")){
            //gameStage.OnStart(msg.payload);
            Debug.Log("START=>>>"+msg.payload);
            JObject jo = JObject.Parse(msg.payload);
            integration.room.arena = (string)jo.SelectToken("arena");
            //integration.robotList = (JArray)jo.SelectToken("robotList");
            //_Forge();
            matched = true;
        }
        else if(msg.query!=null&&msg.query.Equals("onTimer")){
            JObject jo = JObject.Parse(msg.payload);
            int m = (int)jo.SelectToken("m");
            int s = (int)jo.SelectToken("s");
            timer.SetText(m+":"+s);
        }
    } 
}

