using System.Net;
using System.Net.WebSockets;
using System.Net.Http;
using System.Net.Http.Headers;
using System;
using System.Threading;
using System.Threading.Tasks;
using System.IO;
using System.Text;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

using UnityEngine;

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
        
        public  async Task<bool> Index(){
            try{
                string jstr = await _ghc.GetJson("user/index",new Header[]{new Header("Tarantula-tag","index/lobby")});
                ParseLobbyList(jstr);
                return true;
            }catch(Exception ex){
                OnException?.Invoke(ex);
                return false;
            }
        }
        public  async Task<bool> Register(User user){
            try{
                Header[] headers = new Header[]{
                    new Header("Tarantula-tag","index/user"),
                    new Header("Tarantula-magic-key",user.login),
                    new Header("Tarantula-action","onRegister")
                };
                string json = JsonConvert.SerializeObject(user);
                Debug.Log(json);
                string jstr = await _ghc.PostJson("user/action",headers,json);
                Debug.Log(jstr);
                //ParseJson(jstr);
                return true;
            }catch(Exception ex){
                OnException?.Invoke(ex);
                return false;
            }
        }
        public  async Task<bool> Login(User user){
            try{
                Header[] headers = new Header[]{
                    new Header("Tarantula-tag","index/user"),
                    new Header("Tarantula-magic-key",user.login),
                    new Header("Tarantula-action","onLogin")
                };
                string json = JsonConvert.SerializeObject(user);
                Debug.Log(json);
                string jstr = await _ghc.PostJson("user/action",headers,json);
                Debug.Log(jstr);
                ParseLogin(jstr);
                return true;
            }catch(Exception ex){
                OnException?.Invoke(ex);
                return false;
            }
        }
        public  async Task<bool> Device(Device device){
            try{
                Header[] headers = new Header[]{
                    new Header("Tarantula-tag","index/user"),
                    new Header("Tarantula-magic-key",device.deviceId),
                    new Header("Tarantula-action","onReset")
                };
                string json = JsonConvert.SerializeObject(device);
                Debug.Log(json);
                string jstr = await _ghc.PostJson("user/action",headers,json);
                Debug.Log(jstr);
                ParseLogin(jstr);
                return true;
            }catch(Exception ex){
                OnException?.Invoke(ex);
                return false;
            }
        }
        public  async Task<bool> Presence(){
            try{
                Header[] headers = new Header[]{
                    new Header("Tarantula-tag","presence/lobby"),
                    new Header("Tarantula-token",presence.token),
                    new Header("Tarantula-action","onPresence")
                };
                //string json = JsonConvert.SerializeObject(user);
                //Debug.Log(json);
                string jstr = await _ghc.PostJson("service/action",headers,"{}");
                Debug.Log(jstr);
                ParsePresence(jstr);
                bool suc = await _gwc.Connect();
                Debug.Log("websocket->"+suc);
                return true;
            }catch(Exception ex){
                OnException?.Invoke(ex);
                return false;
            }
        }
        public  async Task<bool> Profile(string systemId){
            try{
                Header[] headers = new Header[]{
                    new Header("Tarantula-tag","presence/profile"),
                    new Header("Tarantula-token",presence.token),
                    new Header("Tarantula-action","onProfile")
                };
                Payload p = new Payload();
                p.command = "onProfile";
                p.headers = new Header[]{new Header("systemId",presence.systemId),new Header("stub",presence.stub.ToString())};
                string json = JsonConvert.SerializeObject(p);
                Debug.Log(json);
                string jstr = await _ghc.PostJson("service/action",headers,json);
                Debug.Log(jstr);
                //ParseProfile(jstr);
                return true;
            }catch(Exception ex){
                OnException?.Invoke(ex);
                return false;
            }
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
                Descriptor desc = tk[i].SelectToken("descriptor").ToObject<Descriptor>();
                Debug.Log("Desc->"+desc.typeId+"/"+desc.name+"/"+desc.tag);
                JArray ta = (JArray)tk[i].SelectToken("applications");
                if(ta.Count>0){
                    for(int j=0;j<ta.Count;j++){
                        Descriptor app = ta[j].ToObject<Descriptor>();
                        Debug.Log("App->"+app.typeId+"/"+app.name+"/"+desc.tag);
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
        
        private HttpClient _hc;
        
        public GecHttpClient(string host){
            _hc = new HttpClient();
            _hc.BaseAddress = new Uri(host);
            _hc.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));
        }
        public async Task<string> GetJson(string path,Header[] headers){
            using(HttpRequestMessage req = new HttpRequestMessage(new HttpMethod("GET"),path)){
                foreach(Header h in headers){
                    req.Headers.Add(h.name,h.value);    
                }
                HttpResponseMessage resp = await _hc.SendAsync(req);
                resp.EnsureSuccessStatusCode();
                return await resp.Content.ReadAsStringAsync();
            }
        }
        public async Task<string> PostJson(string path,Header[] headers,string json){
            using(HttpRequestMessage req = new HttpRequestMessage(new HttpMethod("POST"),path)){
                req.Content = new StringContent(json,Encoding.UTF8,"application/x-www-form-urlencoded");
                foreach(Header h in headers){
                    req.Headers.Add(h.name,h.value);    
                }
                req.Headers.Add("Tarantula-payload-size",""+json.Length);  
                HttpResponseMessage resp = await _hc.SendAsync(req);
                resp.EnsureSuccessStatusCode();
                return await resp.Content.ReadAsStringAsync();
            }       
        } 
    }
}