using System.Net;
using System.Net.WebSockets;
using System.Net.Http;
using System.Net.Http.Headers;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using System.IO;
using System.Text;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

using UnityEngine;
using UnityEngine.Networking;
using GameEngineCluster.Model;
namespace GameEngineCluster{
    
    public delegate void GecHandler(Exception ex);
   
    public class NetworkingManager{
        
        private GecHttpClient _ghc;
        private GecWebSocket _gwc;
        private Presence presence;
        private Connection connection;
        
        public event GecHandler OnException;
        
        public NetworkingManager(string host){
            _ghc = new GecHttpClient(host);
        }
        
        private void ParsePresence(string json){
            JObject jo = JObject.Parse(json);
            bool suc = (bool)jo.SelectToken("successful");
            if(!suc){
                return;
            }
            JToken tk = jo.SelectToken("connection");
            connection = tk.ToObject<Connection>();
            _gwc = new GecWebSocket(connection,presence,"tarantula-service"); 
        }
        private void ParseLogin(string json){
            JObject jo = JObject.Parse(json);
            bool suc = (bool)jo.SelectToken("successful");
            if(!suc){
                return;
            }
            JToken tk = jo.SelectToken("presence");
            presence = tk.ToObject<Presence>();
            Debug.Log(presence.systemId);
            Debug.Log(presence.token);
            Debug.Log(presence.login);
            /**
            for(int i=0;i<tk.Count;i++){
                Descriptor desc = tk[i].SelectToken("descriptor").ToObject<Descriptor>();
                Debug.Log("Desc->"+desc.typeId+"/"+desc.name+"/"+desc.tag);
                JArray ta = (JArray)tk[i].SelectToken("applications");
                if(ta.Count>0){
                    for(int j=0;j<ta.Count;j++){
                        Descriptor app = ta[j].ToObject<Descriptor>();
                        Debug.Log("App->"+app.typeId+"/"+app.name+"/"+desc.tag);
                    }
                }
            } **/   
        }
        private void ParseLobbyList(string json){
            JObject jo = JObject.Parse(json);
            bool suc = (bool)jo.SelectToken("successful");
            if(!suc){
                return;
            }
            JArray tk = (JArray)jo.SelectToken("lobbyList");
            for(int i=0;i<tk.Count;i++){
                //Descriptor desc = tk[i].SelectToken("descriptor").ToObject<Descriptor>();
                //Debug.Log("Desc->"+desc.typeId+"/"+desc.name+"/"+desc.tag);
                JArray ta = (JArray)tk[i].SelectToken("applications");
                if(ta.Count>0){
                    for(int j=0;j<ta.Count;j++){
                        //Descriptor app = ta[j].ToObject<Descriptor>();
                        //Debug.Log("App->"+app.typeId+"/"+app.name+"/"+desc.tag);
                    }
                }
            }    
        }
    }
    
    public class GecWebSocket{
        private ClientWebSocket _websocket;
        private string _url;
        
        public GecWebSocket(Connection connection,Presence presence,string subprotocol){
            _url = connection.protocol+"://"+connection.host+":"+connection.port+"/"+connection.path+"?accessKey="+presence.ticket+"&stub="+presence.stub+"&systemId="+presence.login;
            Debug.Log(_url);
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
                tcs.SetResult("{}"); 
            }
            else{
                tcs.SetResult(request.downloadHandler.text);        
            }
		}
    }
}