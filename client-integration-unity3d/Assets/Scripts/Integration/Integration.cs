using System.Collections;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Threading;
using System;
using UnityEngine;
using UnityEngine.SceneManagement;
using Tarantula.Networking;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

[CreateAssetMenu(fileName = "Integration", menuName = "Scripts/Integration", order = 1)]
public class Integration : ScriptableObject{
    
    public string GEC_HOST;
    public static string deviceId;
    
    private static GameEngineCluster gec;
    private static Integration instance;
    
    private static string _HOST;
    public Descriptor game{get;set;}
    public bool online{get=>gec.online;}
    
    public static event InboundMessageHandler OnMessage;
    
    [RuntimeInitializeOnLoadMethod]
    private static void _Init(){
        Debug.Log("Initializing Integration on ["+SystemInfo.deviceUniqueIdentifier+"]");
        instance = Resources.Load<Integration>("Integration");
        gec = new GameEngineCluster(_HOST);
         gec.OnException += (ex,code)=>{
             Debug.Log(ex.Message+"<<CODE>>"+code);
         };
         gec.OnWebSocket += _OnWebSocketMessage;
         gec.OnUDPSocket += _OnUDPSocketMessage;
         gec.OnInboundMessage += (msg)=>{
            OnMessage?.Invoke(msg);
         };
    }
	public static Integration Instance{
		get{return instance;}
	}
    void OnEnable(){
        _HOST = GEC_HOST;
        deviceId = SystemInfo.deviceUniqueIdentifier;
        Debug.Log("GEC OPEN->"+_HOST);
    }
    async void OnDestroy(){
        Debug.Log("GEC CLOSE->"+_HOST);
        await gec.Close();
    }
    void Awake(){
    }
    static async void _OnWebSocketMessage(){
        Debug.Log("Listen on WEB SOCKET");
        await gec.OnWebSocketMessage();
    }
    static async void _OnUDPSocketMessage(){
        Debug.Log("Listen on UDP");
        await gec.OnUDPSocketMessage();
    }
   
    
    //async local wrappers    
    public async Task<bool> OnMove(Payload payload){
        OutboundMessage<Payload> om = new OutboundMessage<Payload>();
        om.label = "rq";
        om.query = payload.command;
        om.instanceId = game.gameId;
        om.payload = payload;
        bool suc = await gec.SendOnUDP(om);
        if(!suc){
            suc = await gec.SendOnInstance(game.applicationId,game.instanceId,payload);
        }
        return suc;
    }
    public async Task<bool> OnJoin(MonoBehaviour caller,Action<JObject> jo,string gname){
        bool suc = await gec.OnLobby(caller,"robotquest");
        if(!suc){
            return suc;
        }
        List<Descriptor> glist = gec.gameList();
        foreach(Descriptor desc in glist){
            //Debug.Log(desc.name+"<><><>"+gname);
            if(desc.name.Equals(gname)){
                game = desc;
                break;
            }    
        }
        //Debug.Log("PLAYING ON ["+game.name+"]");
        return await gec.OnPlay(caller,"robotquest-service/live",game,jo);
    }
    public async Task<bool> OnLeave(MonoBehaviour caller){
        Payload payload = new Payload();
        payload.command = "onLeave";
        return await  gec.OnInstance(caller,game,payload,(ps)=>{
            gec.CloseUDP();
        });
    }
    public async Task<bool> OnIndex(MonoBehaviour caller){
        return await gec.Index(caller);
    }
    public async Task<bool> OnExit(MonoBehaviour caller){
        return await gec.Logout(caller);
    }
    public async Task<bool> OnDevice(MonoBehaviour caller){
        Device device = new Device();
        device.deviceId = deviceId;
        return await gec.Device(caller,device);
    }
}
