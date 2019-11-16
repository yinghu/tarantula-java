using System.Collections;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Threading;
using System.Net;
using System.Net.WebSockets;
using System.Text;
using System;
using UnityEngine;
using UnityEngine.Networking;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;


namespace Tarantula.Networking{
   
   public delegate void ExceptionHandler(Exception ex);
    
   public class GameEngineCluster{
      
      public event ExceptionHandler OnException;
      
      private GecHttpClient _ghc;
      private Presence presence; 
       
      public  GameEngineCluster(string host){
            _ghc = new GecHttpClient(host);                  
      }  
      
      public  async Task<bool> Index(MonoBehaviour caller){
        try{
            string jstr = await _ghc.GetJson(caller,"/user/index",new Header[]{new Header("Tarantula-tag","index/lobby")});
            Debug.Log(jstr);
            return ParseLogin(jstr);
        }catch(Exception ex){
            OnException?.Invoke(ex);
            return false;
        }
      }
       public  async Task<bool> Login(MonoBehaviour caller,User user){
        try{
            Header[] headers = new Header[]{
                new Header("Tarantula-tag","index/user"),
                new Header("Tarantula-magic-key",user.login),
                new Header("Tarantula-action","onLogin")
            };
            string json = JsonConvert.SerializeObject(user);
            string jstr = await _ghc.PostJson(caller,"/user/action",headers,json);
            return ParseLogin(jstr);            
        }catch(Exception ex){
            OnException?.Invoke(ex);
            return false;
        }
    }
    public  async Task<bool> Profile(MonoBehaviour caller){
        try{
            Header[] headers = new Header[]{
                new Header("Tarantula-tag","presence/profile"),
                new Header("Tarantula-token",presence.token),
            };
            string jstr = await _ghc.GetJson(caller,"/service/action",headers);
            Debug.Log(jstr);
            //ParseProfile(jstr);
            return true;
        }catch(Exception ex){
            OnException?.Invoke(ex);
            return false;
        }
    }
    public  async Task<bool> Device(MonoBehaviour caller,Device device){
        try{
            Header[] headers = new Header[]{
                new Header("Tarantula-tag","index/user"),
                new Header("Tarantula-magic-key",device.deviceId),
                new Header("Tarantula-action","onReset")
            };
            string json = JsonConvert.SerializeObject(device);
            string jstr = await _ghc.PostJson(caller,"/user/action",headers,json);
            return ParseLogin(jstr);
        }catch(Exception ex){
            OnException?.Invoke(ex);
            return false;
        }
    }
      private bool ParseLogin(string json){
            JObject jo = JObject.Parse(json);
            bool suc = (bool)jo.SelectToken("successful");
            if(!suc){
                return suc;
            }
            //JToken tk = jo.SelectToken("presence");
            //presence = tk.ToObject<Presence>();
            //connection = jo.SelectToken("connection").ToObject<Connection>();
            return true;
      }   
   } 
    
   public class GecWebSocket{
        private ClientWebSocket _websocket;
        private string _url;

        public GecWebSocket(Connection connection,Presence presence,string subprotocol){
            _url = connection.protocol+"://"+connection.host+":"+connection.port+"/"+connection.path+"?accessKey="+presence.ticket+"&stub="+presence.stub+"&systemId="+presence.login;
            _websocket = new ClientWebSocket();
            _websocket.Options.AddSubProtocol(subprotocol);
            _websocket.Options.SetRequestHeader("Origin",connection.host);
        }
        public async Task<bool> Connect(){
            await _websocket.ConnectAsync(new Uri(_url),new CancellationTokenSource(5000).Token);
            return _websocket.State == WebSocketState.Open;
        }
        public async Task<bool> Send(string json){
           ArraySegment<Byte> om = new ArraySegment<Byte>(Encoding.UTF8.GetBytes(json.ToString()));     
           await _websocket.SendAsync(om, WebSocketMessageType.Text, true, CancellationToken.None);
           return true;
        }
        public async Task<string> Receive(){
            ArraySegment<Byte> rbuff = new ArraySegment<Byte>(new byte[4096]);
            WebSocketReceiveResult wrs = await _websocket.ReceiveAsync(rbuff,CancellationToken.None);
            return Encoding.UTF8.GetString(rbuff.Array,0,wrs.Count);
        }
   } 
   public class GecHttpClient{
        
        
        private string GEC_HOST = "localhost:8090";
       
        public GecHttpClient(string host){
            GEC_HOST = host;      
        }
        
        public async Task<string> GetJson(MonoBehaviour caller,string path,Header[] headers){
            using(UnityWebRequest www = new UnityWebRequest(GEC_HOST+path,"GET")){
                www.downloadHandler = (DownloadHandler)new DownloadHandlerBuffer();
                www.certificateHandler = new KeyValidator(); 
                foreach(Header h in headers){
                    www.SetRequestHeader(h.name,h.value);    
                }
                www.SetRequestHeader("Accept","application/json");
                var tcs = new TaskCompletionSource<string>();
                caller.StartCoroutine(SendWebRequest(www,tcs));    
                return await tcs.Task;
            }
        }
        public async Task<string> PostJson(MonoBehaviour caller,string path,Header[] headers,string json){
            using(UnityWebRequest www = new UnityWebRequest(GEC_HOST+path,"POST")){
                byte[] payload = Encoding.UTF8.GetBytes(json.ToString());
                www.downloadHandler = (DownloadHandler)new DownloadHandlerBuffer();
                www.certificateHandler = new KeyValidator(); 
                www.uploadHandler = (UploadHandler)new UploadHandlerRaw(payload);
                foreach(Header h in headers){
                    www.SetRequestHeader(h.name,h.value);    
                }
                www.SetRequestHeader("Accept","application/json");
                www.SetRequestHeader("Content-type", "application/x-www-form-urlencoded");
                www.SetRequestHeader("Tarantula-payload-size",""+payload.Length);
                var tcs = new TaskCompletionSource<string>();
                caller.StartCoroutine(SendWebRequest(www,tcs));    
                return await tcs.Task;
            }
        }  
        private static IEnumerator SendWebRequest(UnityWebRequest request, TaskCompletionSource<string> tcs){
			yield return request.SendWebRequest();
			if(request.isNetworkError || request.isHttpError) {
                tcs.SetResult("{'successful':false,'message':'"+request.error+"'}"); 
            }
            else{
                tcs.SetResult(request.downloadHandler.text);        
            }
		}
    }
    public class KeyValidator : CertificateHandler{

        protected override bool ValidateCertificate(byte[] certificateData){
            //put key validation here
            return true;
        }
    }
    public class Header{
        public string name { get; set; }
        public string value { get; set; }
        public Header(string name,string value){
            this.name = name;
            this.value = value;
        }
    }
    public class Device{
        public string deviceId { get; set; }
    }
    public class User{
        public string login { get; set; }
        public string nickname { get; set; }
        public string emailAddress { get; set; }
        public string password { get; set; }
    }
    public class Connection{
        public string command { get; set; }
        public int code { get; set; }
        public int timestamp { get; set; }
        public int sequence { get; set; }
        public bool successful { get; set; }
        public string path { get; set; }
        public string protocol { get; set; }
        public string host { get; set; }
        public string type { get; set; }
        public string serverId { get; set; }
        public bool secured { get; set; }
        public int port { get; set; }
    }
    public class Presence{
        public bool successful { get; set; }
        public string systemId { get; set; }
        public int stub { get; set; }
        public string token { get; set; }
        public string ticket { get; set; }
        public string balance { get; set; }
        public string login { get; set; }
    }
}
