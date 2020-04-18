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
    
    public string typeId;
    private bool pendingClick;
    
    private Descriptor game;
     
    private GameObject login;
    private GameObject pve;
    private GameObject pvp;
    
    private TextMeshProUGUI timer;
    private TextMeshProUGUI message;
    private ForgeMenu forgeMenu;
    private bool inGame;
    private bool connected;
    private ushort port =15937;
    async void Start(){
        integration.room = new Room();
        forgeMenu = GetComponent<ForgeMenu>();
        if(integration.dedicated){
            Rpc.MainThreadRunner = MainThreadManager.Instance; 
            string ip = System.IO.File.ReadAllText(@"C:\mnt\ip.txt");
            forgeMenu.Host(ip,port);
            Debug.Log("Running headless mode with type id->"+typeId);
            Connection conn = new Connection();
            conn.host= ip;
            conn.port = port;
            conn.type = typeId;
            integration.room.connection = conn;
            await integration.Dedicated(this,conn);
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
        connected = false;
        if(!integration.online){
            await integration.Index(this);
            await integration.Device(this); 
            Debug.Log("Online->"+integration.online);
        }
    }
    public void OnGo(){
        if(connected){
            return;
        }
        _Forge();
    }
    private void _Forge(){
        connected = true;
        integration.OnInboundMessage -= _OnStart;
        Rpc.MainThreadRunner = MainThreadManager.Instance; 
        Connection xconn = integration.room.connection;
        if(xconn.offline){
            forgeMenu.Host(xconn.host,(ushort)xconn.port);
        }
        else{
            forgeMenu.Connect(xconn.host,(ushort)xconn.port);
        }
        StartCoroutine(WaitAndLoad());
    }
    private IEnumerator WaitAndLoad(){
        yield return new WaitForSeconds(2);
        SceneManager.LoadSceneAsync(SceneManager.GetActiveScene().buildIndex+1); 
    }
    void Update (){
        if(integration.dedicated){
            return;
        }
        login.SetActive(!integration.online);  
        pve.SetActive(!pendingClick&&integration.online);  
        //pvp.SetActive(!pendingClick&&integration.online);
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
        bool joined = await integration.OnPlay(this);     
        if(!joined){
            message.SetText(integration.message);
        }
    }
    public async void OnPVP(){
        //if(pendingClick){
            //return;
        //}
        pendingClick = true;
        bool suc = await integration.OnLeave(this);
        Debug.Log("leave->"+suc);
        //bool joined = await OnJoin(this,"RobotQuestPVP");
        //if(!joined){
            //message.SetText(integration.message);
        //}
    }
    public async void OnLogout(){
        if(integration.online){
            if(inGame){
                await integration.OnLeave(this);
            }
            await integration.Logout(this);
            pendingClick = false;
            inGame = false;
            connected = false;
            timer.SetText("00:00");
            message.SetText("Play Again");
        }
        Debug.Log(integration.online);     
    }
    void _OnStart(InboundMessage msg){
        if(msg.query!=null&&msg.query.Equals("onStart")){
            Debug.Log("START=>>>"+msg.payload);
            JObject jo = JObject.Parse(msg.payload);
            integration.room.arena = (string)jo.SelectToken("arena");
            if(jo.ContainsKey("connection")){
                Connection conn = jo.SelectToken("connection").ToObject<Connection>();
                integration.room.connection = conn;
            }
            else{
                Connection conn = new Connection();
                conn.offline = true;
                conn.host = "127.0.0.1";
                conn.port = port;
                integration.room.connection = conn;
            }
            OnGo();
        }
        else if(msg.query!=null&&msg.query.Equals("onTimer")){
            Debug.Log("START=>>>"+msg.payload);
            JObject jo = JObject.Parse(msg.payload);
            int m = (int)jo.SelectToken("m");
            int s = (int)jo.SelectToken("s");
            timer.SetText(m+":"+s);
        }
        else{
            Debug.Log("end=>>>"+msg.payload);
            Debug.Log("end=>>>"+msg.query);
        }
    } 
}

