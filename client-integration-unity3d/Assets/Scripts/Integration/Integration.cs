using System.Collections;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Threading;
using UnityEngine;
using UnityEngine.SceneManagement;
using Tarantula.Networking;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
[CreateAssetMenu(fileName = "Integration", menuName = "Scripts/Integration", order = 1)]
public class Integration : ScriptableObject{
    
    public string GEC_HOST;
    public string deviceId;
    
    private GameEngineCluster gec;
    private static Integration instance;
    
	public static Integration Instance
	{
		get
		{
			if (instance != null){ 
                return instance;
            }
            return Resources.Load<Integration>("Integration");
		}
	}
    void OnEnable(){
        Debug.Log("enabled gec");
    }
    async void OnDisable(){
        Debug.Log("disabled gec");
        await gec.Close();
    }
    void Awake(){
         Debug.Log("starting gec");
         gec = new GameEngineCluster(GEC_HOST);
         gec.OnException += (ex)=>{
             Debug.Log(ex);
         };
         gec.OnWebSocket += _OnWebSocketMessage;
         gec.OnUDPSocket += _OnUDPSocketMessage;
         gec.OnInboundMessage += (msg)=>{
             if(msg.label!=null){
                Debug.Log(msg.label);
                Debug.Log(msg.instanceId);
                Debug.Log(msg.query);
                Debug.Log(msg.payload);
             }
             else{
                 Debug.Log(msg.payload);
             }
         };
    }
    async void _OnWebSocketMessage(bool suc){
        Debug.Log("web socket->"+suc);
        await gec.OnWebSocketMessage();
    }
    async void _OnUDPSocketMessage(bool suc){
        Debug.Log("udp socket->"+suc);
        await gec.OnUDPSocketMessage();
    }
   
    void OnDestroy(){
       Debug.Log("gec closed");
       gec.Close();           
    }
   
    //async local wrappers    
    public async Task<bool> OnDevice(MonoBehaviour caller, Device device){
        return await gec.Device(caller,device);
    }
    public async Task<bool> OnLogin(MonoBehaviour caller, User user){
        return await gec.Login(caller,user);
    }
    public async Task<bool> OnLogout(MonoBehaviour caller){
        return await gec.Logout(caller);
    }
    public async Task<bool> OnRegister(MonoBehaviour caller, User user){
        return await gec.Register(caller,user);
    }
    public async Task<bool> OnProfile(MonoBehaviour caller){
        return await gec.Profile(caller);
    }
    public async Task<bool> OnLevel(MonoBehaviour caller){
        return await gec.Level(caller);
    }
}
