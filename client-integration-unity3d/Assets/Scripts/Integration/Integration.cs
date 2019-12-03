using System.Collections;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Threading;
using UnityEngine;
using Tarantula.Networking;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

public class Integration : MonoBehaviour{
    
    public string GEC_HOST;
    public string deviceId;
    
    private GameEngineCluster gec;
    private static Integration instance;

	public static Integration Instance{
		get
		{
			if (instance == null)
			{
				instance = FindObjectOfType<Integration>();
				if (instance == null)
				{
					GameObject obj = new GameObject
					{
						name = typeof(Integration).Name
					};
					instance = obj.AddComponent<Integration>();
				}
			}

			return instance;
		}
	}
    
    void Awake(){
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
         DontDestroyOnLoad(this.gameObject);
    }
    async void _OnWebSocketMessage(bool suc){
        Debug.Log("web socket->"+suc);
        await gec.OnWebSocketMessage();
    }
    async void _OnUDPSocketMessage(bool suc){
        Debug.Log("udp socket->"+suc);
        await gec.OnUDPSocketMessage();
    }
    async void Start(){
        Device dev = new Device();
        dev.deviceId = deviceId;
        await OnDevice(dev);    
    }
    async void OnDestroy(){
       await gec.Close();           
    }
    public async void Logout(){
        await OnLogout();
    }
    public async void Play(){
        await OnProfile();
        Debug.Log(gec.profile.nickname);
    }
    void Update(){
        //if (Input.GetMouseButtonDown(0)){
            //Vector3 mouse = Input.mousePosition;
            //spin.OnMove(mouse);
        //}
    }
    //async local wrappers 
    public async Task<bool> OnDevice(Device device){
        return await gec.Device(this,device);
    }
    public async Task<bool> OnLogin(User user){
        return await gec.Login(this,user);
    }
    public async Task<bool> OnLogout(){
        return await gec.Logout(this);
    }
    public async Task<bool> OnRegister(User user){
        return await gec.Register(this,user);
    }
    public async Task<bool> OnProfile(){
        return await gec.Profile(this);
    }
    public async Task<bool> OnLevel(){
        return await gec.Level(this);
    }

}
